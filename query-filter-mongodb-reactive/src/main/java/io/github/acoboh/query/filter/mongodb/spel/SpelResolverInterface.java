package io.github.acoboh.query.filter.mongodb.spel;

import org.springframework.util.MultiValueMap;

import reactor.core.publisher.Mono;

public interface SpelResolverInterface {

	Mono<Object> evaluate(String securityExpression, MultiValueMap<String, Object> contextValues, boolean nullOnError);

}
