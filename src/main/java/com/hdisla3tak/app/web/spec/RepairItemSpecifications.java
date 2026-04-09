package com.hdisla3tak.app.web.spec;

import com.hdisla3tak.app.domain.Customer;
import com.hdisla3tak.app.domain.RepairItem;
import com.hdisla3tak.app.domain.Shop;
import com.hdisla3tak.app.domain.enums.ItemCategory;
import com.hdisla3tak.app.domain.enums.RepairStatus;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class RepairItemSpecifications {

    private RepairItemSpecifications() {}

    public static Specification<RepairItem> matchesSearch(String q) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(q)) {
                return null;
            }
            String like = "%" + q.trim().toLowerCase() + "%";
            Join<RepairItem, Customer> customerJoin = root.join("customer");
            return cb.or(
                cb.like(cb.lower(root.get("title")), like),
                cb.like(cb.lower(root.get("pickupCode")), like),
                cb.like(cb.lower(customerJoin.get("fullName")), like),
                cb.like(cb.lower(customerJoin.get("phoneNumber")), like)
            );
        };
    }

    public static Specification<RepairItem> belongsToShop(Long shopId) {
        return (root, query, cb) -> shopId == null ? null : cb.equal(root.get("shop").get("id"), shopId);
    }

    public static Specification<RepairItem> hasStatus(RepairStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<RepairItem> hasDelivered(String delivered) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(delivered)) {
                return null;
            }
            if ("yes".equalsIgnoreCase(delivered) || "true".equalsIgnoreCase(delivered)) {
                return cb.isNotNull(root.get("deliveredAt"));
            }
            if ("no".equalsIgnoreCase(delivered) || "false".equalsIgnoreCase(delivered)) {
                return cb.isNull(root.get("deliveredAt"));
            }
            return null;
        };
    }

    public static Specification<RepairItem> hasCategory(String category) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(category)) {
                return null;
            }
            try {
                return cb.equal(root.get("category"), ItemCategory.valueOf(category));
            } catch (IllegalArgumentException ex) {
                return null;
            }
        };
    }
}
