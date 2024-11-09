package io.github.acoboh.query.filter.mongodb.spel;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
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

	/**
	 * Default constructor
	 *
	 */
	public SpelResolverContextBasic() {
	}

	@Override
	public Mono<Object> evaluate(String securityExpression, MultiValueMap<String, Object> contextValues) {
		return null;
	}
}
