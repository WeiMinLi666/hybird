package org.wyman.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnCacheTypeCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String requiredCacheType = (String) metadata.getAnnotationAttributes(ConditionalOnCacheType.class.getName()).get("value");
        String activeCacheType = context.getEnvironment().getProperty("cache.type", "redis");

        return activeCacheType.equalsIgnoreCase(requiredCacheType);
    }
}