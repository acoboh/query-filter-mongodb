package io.github.acoboh.query.filter.mongodb.spel;

import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.PropertyValue;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.ServletRequestParameterPropertyValues;
import org.springframework.web.servlet.View;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * SPEL Context base to resolve SpEL expressions
 *
 * @author Adrián Cobo
 * 
 */
public abstract class SpelResolverContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpelResolverContext.class);

	/**
	 * Evaluate any expression
	 *
	 * @param securityExpression expression to evaluate
	 * @param contextValues      actual context values
	 * @param request            request
	 * @param response           response
	 * @return object evaluated
	 */
	public Object evaluate(String securityExpression, MultiValueMap<String, Object> contextValues,
			HttpServletRequest request, HttpServletResponse response, boolean nullOnError) {

		ExpressionParser expressionParser = getExpressionParser();

		Expression expression = expressionParser.parseExpression(securityExpression);

		EvaluationContext context = getEvaluationContext(request, response);

		if (request != null) {
			fillContextWithRequestValues(context, request);
		}

		fillContextWithMap(context, contextValues);

		try {
			return expression.getValue(context);
		} catch (EvaluationException e) {
			if (nullOnError) {
				return null;
			} else {
				LOGGER.error("Error evaluating expression: {}", securityExpression, e);
				throw e;
			}
		}

	}

	/**
	 * Get expression parser to resolve de SpEL expression
	 * 
	 * @return the expression parser to use
	 */
	public abstract ExpressionParser getExpressionParser();

	/**
	 * Get the evaluation context of the expression
	 * 
	 * @param request  request
	 * @param response response
	 * 
	 * 
	 * @return evaluation context to use
	 */
	public abstract EvaluationContext getEvaluationContext(HttpServletRequest request, HttpServletResponse response);

	private void fillContextWithRequestValues(EvaluationContext context, HttpServletRequest request) {

		Object pathObject = request.getAttribute(View.PATH_VARIABLES);
		if (pathObject instanceof Map<?, ?> map) {
			context.setVariable("_pathVariables", map);
		}

		var properties = new ServletRequestParameterPropertyValues(request);
		Map<String, Object> requestParams = properties.getPropertyValueList().stream()
				.collect(Collectors.toMap(PropertyValue::getName, PropertyValue::getValue));

		context.setVariable("_parameters", requestParams);
	}

	private void fillContextWithMap(EvaluationContext context, MultiValueMap<String, Object> contextValues) {
		contextValues.forEach((k, v) -> {
			if (v.size() > 1) {
				context.setVariable(k, v);
			} else if (v.size() == 1) {
				context.setVariable(k, v.get(0));
			}
		});
	}
}
