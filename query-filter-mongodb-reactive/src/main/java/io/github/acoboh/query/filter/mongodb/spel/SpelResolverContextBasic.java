package io.github.acoboh.query.filter.mongodb.spel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
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
@ConditionalOnMissingClass("org.springframework.security.access.expression.SecurityExpressionHandler")
class SpelResolverContextBasic implements SpelResolverInterface {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpelResolverContextBasic.class);

	private final SpelExpressionParser parser = new SpelExpressionParser();

	private final ApplicationContext appContext;

	/**
	 * Default constructor
	 *
	 */
	public SpelResolverContextBasic(ApplicationContext appContext) {
		this.appContext = appContext;
	}

	@Override
	public Mono<Object> evaluate(String securityExpression, MultiValueMap<String, Object> contextValues) {
		return Mono.deferContextual(deferContext -> {

			ServerWebExchange found = null;
			try {
				found = deferContext.get(ServerWebExchange.class);
			} catch (Exception e) {
				LOGGER.trace("No exchange found in context");
			}
			final ServerWebExchange exchange = found;

			return Mono.fromCallable(() -> {
				StandardEvaluationContext evaluationContext = new StandardEvaluationContext();

				SpelUtils.fillContextWithMap(evaluationContext, contextValues);

				if (exchange != null) {
					SpelUtils.fillContextWithRequestValues(evaluationContext, exchange);
				}

				evaluationContext.setBeanResolver(new BeanFactoryResolver(appContext));
				return parser.parseExpression(securityExpression).getValue(evaluationContext);
			});
		});

	}
}
