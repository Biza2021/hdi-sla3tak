package com.hdisla3tak.app.config;

import com.hdisla3tak.app.repository.AppUserRepository;
import com.hdisla3tak.app.repository.CustomerRepository;
import com.hdisla3tak.app.repository.RepairItemHistoryRepository;
import com.hdisla3tak.app.repository.RepairItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
@Order(2)
public class PhaseFourTenantHardeningRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PhaseFourTenantHardeningRunner.class);

    private final AppUserRepository appUserRepository;
    private final CustomerRepository customerRepository;
    private final RepairItemRepository repairItemRepository;
    private final RepairItemHistoryRepository repairItemHistoryRepository;
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public PhaseFourTenantHardeningRunner(AppUserRepository appUserRepository,
                                          CustomerRepository customerRepository,
                                          RepairItemRepository repairItemRepository,
                                          RepairItemHistoryRepository repairItemHistoryRepository,
                                          JdbcTemplate jdbcTemplate,
                                          DataSource dataSource) {
        this.appUserRepository = appUserRepository;
        this.customerRepository = customerRepository;
        this.repairItemRepository = repairItemRepository;
        this.repairItemHistoryRepository = repairItemHistoryRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        verifyNoNullTenantRows();

        hardenNotNull("app_users", "shop_id");
        hardenNotNull("customers", "shop_id");
        hardenNotNull("repair_items", "shop_id");
        hardenNotNull("repair_item_history", "shop_id");

        replaceSingleColumnUniqueWithComposite("app_users", "username", "ux_app_users_shop_username", "shop_id, username");
        replaceSingleColumnUniqueWithComposite("customers", "phone_number", "ux_customers_shop_phone", "shop_id, phone_number");
        replaceSingleColumnUniqueWithComposite("repair_items", "pickup_code", "ux_repair_items_shop_pickup_code", "shop_id, pickup_code");
    }

    private void verifyNoNullTenantRows() {
        long usersWithoutShop = appUserRepository.countByShopIsNull();
        long customersWithoutShop = customerRepository.countByShopIsNull();
        long repairItemsWithoutShop = repairItemRepository.countByShopIsNull();
        long historyEntriesWithoutShop = repairItemHistoryRepository.countByShopIsNull();

        if (usersWithoutShop > 0 || customersWithoutShop > 0 || repairItemsWithoutShop > 0 || historyEntriesWithoutShop > 0) {
            throw new IllegalStateException(
                "Tenant hardening aborted because null shop ownership still exists: users=%d, customers=%d, repairItems=%d, historyEntries=%d"
                    .formatted(usersWithoutShop, customersWithoutShop, repairItemsWithoutShop, historyEntriesWithoutShop)
            );
        }
    }

    private void hardenNotNull(String tableName, String columnName) {
        jdbcTemplate.execute("alter table " + tableName + " alter column " + columnName + " set not null");
        log.info("Tenant hardening: enforced NOT NULL on {}.{}.", tableName, columnName);
    }

    private void replaceSingleColumnUniqueWithComposite(String tableName,
                                                        String singleColumnName,
                                                        String compositeIndexName,
                                                        String compositeColumns) throws Exception {
        for (String indexName : findSingleColumnUniqueIndexes(tableName, singleColumnName)) {
            if (compositeIndexName.equalsIgnoreCase(indexName)) {
                continue;
            }
            dropUniqueArtifact(tableName, indexName);
        }

        jdbcTemplate.execute("create unique index if not exists " + compositeIndexName + " on " + tableName + "(" + compositeColumns + ")");
        log.info("Tenant hardening: ensured composite unique index {} on {}({}).", compositeIndexName, tableName, compositeColumns);
    }

    private Set<String> findSingleColumnUniqueIndexes(String tableName, String singleColumnName) throws Exception {
        Map<String, Set<String>> uniqueIndexes = new LinkedHashMap<>();
        try (Connection connection = dataSource.getConnection();
             ResultSet resultSet = connection.getMetaData().getIndexInfo(connection.getCatalog(), connection.getSchema(), tableName, true, false)) {
            while (resultSet.next()) {
                short type = resultSet.getShort("TYPE");
                if (type == DatabaseMetaData.tableIndexStatistic) {
                    continue;
                }
                String indexName = resultSet.getString("INDEX_NAME");
                String columnName = resultSet.getString("COLUMN_NAME");
                if (indexName == null || columnName == null) {
                    continue;
                }
                uniqueIndexes.computeIfAbsent(indexName, ignored -> new LinkedHashSet<>())
                    .add(columnName.toLowerCase(Locale.ROOT));
            }
        }

        Set<String> matchingIndexes = new LinkedHashSet<>();
        for (Map.Entry<String, Set<String>> entry : uniqueIndexes.entrySet()) {
            if (entry.getValue().size() == 1 && entry.getValue().contains(singleColumnName.toLowerCase(Locale.ROOT))) {
                matchingIndexes.add(entry.getKey());
            }
        }
        return matchingIndexes;
    }

    private void dropUniqueArtifact(String tableName, String artifactName) {
        executeIgnoringFailure("alter table " + tableName + " drop constraint if exists " + artifactName);
        executeIgnoringFailure("drop index if exists " + artifactName);
        log.info("Tenant hardening: removed legacy unique artifact {} from {}.", artifactName, tableName);
    }

    private void executeIgnoringFailure(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception ex) {
            log.debug("Tenant hardening ignored SQL failure for [{}]: {}", sql, ex.getMessage());
        }
    }
}
