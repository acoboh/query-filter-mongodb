package io.github.acoboh.query.filter.mongodb.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

import io.github.acoboh.query.filter.mongodb.advisor.QFExceptionAdvisor;
import io.github.acoboh.query.filter.mongodb.hints.HintsRegistrarDef;
import io.github.acoboh.query.filter.mongodb.properties.QueryFilterProperties;
import io.github.acoboh.query.filter.mongodb.spel.SpelResolverBeanConfig;

/**
 * Auto-configution class for QueryFilter Library
 */
@AutoConfiguration
@Import({ QFExceptionAdvisor.class, SpelResolverBeanConfig.class, QFBeanFactoryPostProcessor.class,
		QFWebMvcConfigurer.class, QueryFilterProperties.class, HintsRegistrarDef.class,
		ApplicationContextAwareSupport.class })
public class QueryFilterAutoconfigure {

}
