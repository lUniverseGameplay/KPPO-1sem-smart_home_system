package com.example.smart_home_syst.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.example.smart_home_syst.model.Room;

public class RoomSpecifications {
    private static Specification<Room> titleLike(String title) {
        return (root, query, criterialBuilder) -> {
            if (title==null || title.trim().isEmpty()) {
                return null;
            }
            return criterialBuilder.like(criterialBuilder.lower(root.get("title")), 
            "%"+title.trim().toLowerCase()+"%");
        };
    }

    private static Specification<Room> locationLike(String location) {
        return (root, query, criterialBuilder) -> {
            if (location==null || location.trim().isEmpty()) {
                return null;
            }
            return criterialBuilder.like(criterialBuilder.lower(root.get("location")), 
            "%"+location.trim().toLowerCase()+"%");
        };
    }

    private static Specification<Room> capacityGreater (Integer min_capacity) {
        return (root, query, criterialBuilder) -> {
            if (min_capacity == null) {
                return null;
            }
            return criterialBuilder.greaterThanOrEqualTo(root.get("capacity"), min_capacity);
        };
    }
    private static Specification<Room> capacityLower (Integer max_capacity) {
        return (root, query, criterialBuilder) -> {
            if (max_capacity == null) {
                return null;
            }
            return criterialBuilder.lessThanOrEqualTo(root.get("capacity"), max_capacity);
        };
    }

    public static Specification<Room> filter (String title, String location, Integer max_capacity, Integer min_capacity) {
        return Specification.allOf(titleLike(title), locationLike(location), capacityGreater(min_capacity), capacityLower(max_capacity));
    }
}
