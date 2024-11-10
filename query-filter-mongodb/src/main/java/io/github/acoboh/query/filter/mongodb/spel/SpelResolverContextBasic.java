package io.github.acoboh.query.filter.mongodb.spel;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * SPEL Context resolver bean
 *
 * @author Adrián Cobo
 * 
 */
@Component
@ConditionalOnMissingClass("org.springframework.security.access.expression.SecurityExpressionHandler")
class SpelResolverContextBasic extends SpelResolverContext {

	private final ApplicationContext applicationContext;

	public SpelResolverContextBasic(ApplicationContext appContext) {
		applicationContext = appContext;
	}

	@Override
	public ExpressionParser getExpressionParser() {
		return new SpelExpressionParser();
	}

	@Override
	public EvaluationContext getEvaluationContext(HttpServletRequest request, HttpServletResponse response) {
		var toRet = new StandardEvaluationContext();
		toRet.setBeanResolver(new BeanFactoryResolver(applicationContext));
		return toRet;
	}

}
