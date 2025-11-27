package com.example.smart_home_syst.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.example.smart_home_syst.enumerator.DeviceType;
import com.example.smart_home_syst.model.Device;

public class DeviceSpecifications {
    private static Specification<Device> titleLike(String title) {
        return (root, query, criterialBuilder) -> {
            if (title==null || title.trim().isEmpty()) {
                return null;
            }
            return criterialBuilder.like(criterialBuilder.lower(root.get("title")), 
            "%"+title.trim().toLowerCase()+"%");
        };
    }

    private static Specification<Device> powerGreater (Double min_power) {
        return (root, query, criterialBuilder) -> {
            if (min_power == null) {
                return null;
            }
            return criterialBuilder.greaterThanOrEqualTo(root.get("power"), min_power);
        };
    }
    private static Specification<Device> priceLower (Double max_power) {
        return (root, query, criterialBuilder) -> {
            if (max_power == null) {
                return null;
            }
            return criterialBuilder.lessThanOrEqualTo(root.get("power"), max_power);
        };
    }

    private static Specification<Device> sameActivity (Boolean activity) {
        return (root, query, criterialBuilder) -> {
            if (activity == null) {
                return null;
            }
            return criterialBuilder.equal(root.get("active"), activity);
        };
    }
    
    private static Specification<Device> definiteType(DeviceType type) {
        return (root, query, criterialBuilder) -> {
            if (type == null) {
                return null;
            }
            return criterialBuilder.equal(root.get("type"), type);
        };
    }

    public static Specification<Device> filter (String title, Double min_power, Double max_power, Boolean activity, DeviceType type) {
        return Specification.allOf(titleLike(title), powerGreater(min_power), priceLower(max_power), sameActivity(activity), definiteType(type));
    }
}
