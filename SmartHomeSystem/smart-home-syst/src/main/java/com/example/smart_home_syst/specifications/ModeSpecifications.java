package com.example.smart_home_syst.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.example.smart_home_syst.enumerator.ModeType;
import com.example.smart_home_syst.model.Mode;

public class ModeSpecifications {
    private static Specification<Mode> titleLike(String title) {
        return (root, query, criterialBuilder) -> {
            if (title==null || title.trim().isEmpty()) {
                return null;
            }
            return criterialBuilder.like(criterialBuilder.lower(root.get("title")), 
            "%"+title.trim().toLowerCase()+"%");
        };
    }

    private static Specification<Mode> definiteType(ModeType type) {
        return (root, query, criterialBuilder) -> {
            if (type==null || type.name().trim().isEmpty()) {
                return null;
            }
            return criterialBuilder.like(criterialBuilder.lower(root.get("type")), 
            "%"+type.name().trim().toLowerCase()+"%");
        };
    }

    public static Specification<Mode> filter (String title, ModeType type) {
        return Specification.allOf(titleLike(title), definiteType(type));
    }
}
