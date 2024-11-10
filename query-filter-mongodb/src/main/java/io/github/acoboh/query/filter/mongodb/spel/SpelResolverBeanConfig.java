package io.github.acoboh.query.filter.mongodb.spel;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration class to instantiate SpelResolverContext beans
 * 
 * @author Adrián Cobo
 *
 */
@Configuration(proxyBeanMethods = false)
@Import({ SpelResolverContextBasic.class, SecuritySpelResolverContext.class })
public class SpelResolverBeanConfig {

}
