package com.hdisla3tak.app;

import com.hdisla3tak.app.domain.AppUser;
import com.hdisla3tak.app.domain.Customer;
import com.hdisla3tak.app.domain.RepairItem;
import com.hdisla3tak.app.domain.Shop;
import com.hdisla3tak.app.domain.enums.ItemCategory;
import com.hdisla3tak.app.domain.enums.RepairStatus;
import com.hdisla3tak.app.domain.enums.UserRole;
import com.hdisla3tak.app.repository.AppUserRepository;
import com.hdisla3tak.app.repository.CustomerRepository;
import com.hdisla3tak.app.repository.RepairItemRepository;
import com.hdisla3tak.app.repository.ShopRepository;
import com.hdisla3tak.app.security.ShopUserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:tenant-hardening;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.thymeleaf.cache=false"
})
@AutoConfigureMockMvc
@Transactional
class MultiTenantHardeningIntegrationTest {

    private static final Path uploadRoot = createUploadRoot();

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("app.storage.upload-dir", () -> uploadRoot.resolve("items").toString());
    }

    private static Path createUploadRoot() {
        try {
            return Files.createTempDirectory("hdi-sla3tak-test-uploads");
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RepairItemRepository repairItemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void onboardingCreatesShopAndOwnerAndRedirectsToShopLogin() throws Exception {
        mockMvc.perform(post("/shops/new")
                .with(csrf())
                .param("businessName", "Casablanca Repair")
                .param("shopSlug", "casablanca-repair")
                .param("fullName", "Owner One")
                .param("username", "owner")
                .param("password", "secret123")
                .param("confirmPassword", "secret123"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/casablanca-repair/login"));

        Shop shop = shopRepository.findBySlugIgnoreCase("casablanca-repair").orElseThrow();
        AppUser owner = appUserRepository.findByUsernameIgnoreCaseAndShop_Id("owner", shop.getId()).orElseThrow();

        assertThat(shop.getName()).isEqualTo("Casablanca Repair");
        assertThat(owner.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void loginIsScopedByShopSlug() throws Exception {
        Shop alpha = saveShop("Alpha Repairs", "alpha");
        Shop beta = saveShop("Beta Repairs", "beta");
        saveUser(alpha, "owner", "alpha-pass");
        saveUser(beta, "owner", "beta-pass");

        mockMvc.perform(post("/login")
                .with(csrf())
                .param("shopSlug", "alpha")
                .param("username", "owner")
                .param("password", "alpha-pass"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/alpha"));

        mockMvc.perform(post("/login")
                .with(csrf())
                .param("shopSlug", "alpha")
                .param("username", "owner")
                .param("password", "beta-pass"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/alpha/login?error=true"));
    }

    @Test
    void globalLoginFailsClosedWhenMultipleShopsExist() throws Exception {
        saveShop("Alpha Repairs", "alpha");
        saveShop("Beta Repairs", "beta");

        mockMvc.perform(get("/login"))
            .andExpect(status().isNotFound());
    }

    @Test
    void crossTenantCustomerAndImageAccessAreBlocked() throws Exception {
        Shop alpha = saveShop("Alpha Repairs", "alpha");
        Shop beta = saveShop("Beta Repairs", "beta");
        AppUser alphaAdmin = saveUser(alpha, "admin", "secret123");
        Customer betaCustomer = saveCustomer(beta, "Beta Customer", "0611111111");
        RepairItem betaItem = saveRepairItem(beta, betaCustomer, "TRACK-BETA", "token-beta-" + UUID.randomUUID(), "Camera");
        writeImage(beta, "private.jpg", betaItem);

        mockMvc.perform(get("/beta/customers/{id}", betaCustomer.getId())
                .with(user(new ShopUserPrincipal(alphaAdmin))))
            .andExpect(status().isNotFound());

        mockMvc.perform(get("/beta/items/{id}", betaItem.getId())
                .with(user(new ShopUserPrincipal(alphaAdmin))))
            .andExpect(status().isNotFound());

        mockMvc.perform(get("/beta/items/{id}/image", betaItem.getId())
                .with(user(new ShopUserPrincipal(alphaAdmin))))
            .andExpect(status().isNotFound());
    }

    @Test
    void publicTrackingAndImageStayBoundToOwningRecord() throws Exception {
        Shop beta = saveShop("Beta Repairs", "beta");
        Customer betaCustomer = saveCustomer(beta, "Beta Customer", "0622222222");
        RepairItem betaItem = saveRepairItem(beta, betaCustomer, "TRACK-BETA", "token-beta-" + UUID.randomUUID(), "Tablet");
        writeImage(beta, "public.jpg", betaItem);

        mockMvc.perform(get("/track/{token}", betaItem.getPublicTrackingToken()))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Beta Repairs")));

        mockMvc.perform(get("/track/{token}/image", betaItem.getPublicTrackingToken()))
            .andExpect(status().isOk())
            .andExpect(content().contentType("image/jpeg"));

        mockMvc.perform(get("/track/{token}/image", "missing-token"))
            .andExpect(status().isNotFound());
    }

    private Shop saveShop(String name, String slug) {
        Shop shop = new Shop();
        shop.setName(name);
        shop.setSlug(slug);
        return shopRepository.save(shop);
    }

    private AppUser saveUser(Shop shop, String username, String rawPassword) {
        AppUser user = new AppUser();
        user.setShop(shop);
        user.setFullName(username + " full");
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(UserRole.ADMIN);
        user.setActive(true);
        return appUserRepository.save(user);
    }

    private Customer saveCustomer(Shop shop, String fullName, String phoneNumber) {
        Customer customer = new Customer();
        customer.setShop(shop);
        customer.setFullName(fullName);
        customer.setPhoneNumber(phoneNumber);
        return customerRepository.save(customer);
    }

    private RepairItem saveRepairItem(Shop shop,
                                      Customer customer,
                                      String pickupCode,
                                      String trackingToken,
                                      String title) {
        RepairItem item = new RepairItem();
        item.setShop(shop);
        item.setCustomer(customer);
        item.setCategory(ItemCategory.PHONE);
        item.setTitle(title);
        item.setDateReceived(LocalDate.now());
        item.setStatus(RepairStatus.RECEIVED);
        item.setPickupCode(pickupCode);
        item.setPublicTrackingToken(trackingToken);
        return repairItemRepository.save(item);
    }

    private void writeImage(Shop shop, String filename, RepairItem item) throws IOException {
        Path tenantDirectory = uploadRoot.resolve("items").resolve(shop.getId().toString());
        Files.createDirectories(tenantDirectory);
        Path file = tenantDirectory.resolve(filename);
        Files.write(file, new byte[] {1, 2, 3, 4});
        item.setImagePath(shop.getId() + "/" + filename);
        repairItemRepository.save(item);
    }
}
