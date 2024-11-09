package io.github.acoboh.query.filter.mongodb.spel;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.expression.EvaluationContext;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.server.ServerWebExchange;

public class SpelUtils {

	private SpelUtils() {
		// Utility class
	}

	public static void fillContextWithRequestValues(EvaluationContext context, ServerWebExchange exchange) {
		Object pathObject = exchange.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		if (pathObject instanceof Map<?, ?> map) {
			context.setVariable("_pathVariables", map);
		}

		var mlMap = exchange.getRequest().getQueryParams();

		Map<String, Object> queryMap = mlMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
			if (e.getValue().size() > 1) {
				return e.getValue().toArray(new String[0]);
			} else if (e.getValue().size() == 1) {
				return e.getValue().get(0);
			}
			throw new IllegalStateException("Query parameter value is empty");
		}));

		context.setVariable("_parameters", queryMap);
	}

	public static void fillContextWithMap(EvaluationContext context, MultiValueMap<String, Object> contextValues) {
		contextValues.forEach((k, v) -> {
			if (v.size() > 1) {
				context.setVariable(k, v);
			} else if (v.size() == 1) {
				context.setVariable(k, v.get(0));
			}
		});
	}

}
