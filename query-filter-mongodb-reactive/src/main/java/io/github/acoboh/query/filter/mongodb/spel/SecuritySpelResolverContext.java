package io.github.acoboh.query.filter.mongodb.spel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * SPEL Context resolver bean
 *
 * @author Adrián Cobo
 * 
 */
@Component
@Scope("request")
@ConditionalOnClass(name = "org.springframework.security.access.expression.SecurityExpressionHandler")
class SecuritySpelResolverContext implements SpelResolverInterface {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecuritySpelResolverContext.class);

	private final SpelExpressionParser parser = new SpelExpressionParser();

	private final ApplicationContext appContext;

	/**
	 * Default constructor
	 *
	 * @param context Application context
	 */
	public SecuritySpelResolverContext(ApplicationContext context) {
		this.appContext = context;

	}

	@Override
	public Mono<Object> evaluate(String securityExpression, MultiValueMap<String, Object> contextValues) {
		return Mono.deferContextual(deferContext -> {
			// Get exchange from context
			ServerWebExchange found = null;
			try {
				found = deferContext.get(ServerWebExchange.class);
			} catch (Exception e) {
				LOGGER.trace("No exchange found in context");
			}
			final ServerWebExchange exchange = found;

			return ReactiveSecurityContextHolder.getContext().defaultIfEmpty(new SecurityContext() {
				@Override
				public Authentication getAuthentication() {
					return null;
				}

				@Override
				public void setAuthentication(Authentication authentication) {

				}
			}).map(ctx -> {
				StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
				if (ctx.getAuthentication() != null) {
					evaluationContext.setRootObject(ctx.getAuthentication());
				}

				// Fill with custom context values
				SpelUtils.fillContextWithMap(evaluationContext, contextValues);

				// Fill with path variables and query parameters
				if (exchange != null) {
					SpelUtils.fillContextWithRequestValues(evaluationContext, exchange);
				}

				evaluationContext.setBeanResolver(new BeanFactoryResolver(appContext));
				return parser.parseExpression(securityExpression).getValue(evaluationContext);
			});
		});

	}
}
