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

//	private static final String SECURITY_EXPRESSION_CLASS = "org.springframework.security.access.expression.SecurityExpressionHandler";

//	@Bean
//	@ConditionalOnMissingClass(SECURITY_EXPRESSION_CLASS)
//	SpelResolverContextBasic spelResolverContextBasic(HttpServletRequest request, HttpServletResponse response) {
//		return new SpelResolverContextBasic(request, response);
//	}

//	@Bean
//	@ConditionalOnClass(name = SECURITY_EXPRESSION_CLASS)
//	SecuritySpelResolverContext securitySpelResolverContext(
//			List<SecurityExpressionHandler<?>> securityExpressionHandlers, HttpServletRequest request,
//			HttpServletResponse response, ApplicationContext context) {
//		return new SecuritySpelResolverContext(securityExpressionHandlers, request, response, context);
//	}

}
