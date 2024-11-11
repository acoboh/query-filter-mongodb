package io.github.acoboh.query.filter.mongodb.openapi.config;

import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import io.github.acoboh.query.filter.mongodb.annotations.QFParam;
import io.github.acoboh.query.filter.mongodb.processor.QFProcessor;
import io.swagger.v3.oas.models.OpenAPI;

/**
 * Class used to customize the OpenAPI definition with filter elements
 *
 * @author Adrián Cobo
 * 
 */
@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class OpenApiCustomizerMvc extends OpenApiCustomiserAbstract {

	private static final Logger LOGGER = LoggerFactory.getLogger(OpenApiCustomizerMvc.class);

	/**
	 * Default constructor
	 *
	 * @param applicationContext appContext
	 */
	OpenApiCustomizerMvc(ApplicationContext applicationContext) {
		super(applicationContext);
	}

	/** {@inheritDoc} */
	@Override
	public void customise(OpenAPI openApi) {

		RequestMappingHandlerMapping mappingHandler = applicationContext.getBean("requestMappingHandlerMapping",
				RequestMappingHandlerMapping.class);

		for (var requestMapping : mappingHandler.getHandlerMethods().entrySet()) {
			LOGGER.debug("Checking path {}", requestMapping.getKey());

			processParameter(openApi, requestMapping);

		}
	}

	private void processParameter(OpenAPI openApi, Entry<RequestMappingInfo, HandlerMethod> requestMapping) {
		for (var param : requestMapping.getValue().getMethodParameters()) {
			var qfParamAnnotation = param.getParameterAnnotation(QFParam.class);
			if (qfParamAnnotation == null) {
				continue;
			}

			QFProcessor<?, ?> processor = getQfProcessor(param, qfParamAnnotation);
			if (processor == null) {
				continue;
			}

			Set<String> requestMappingPatterns;
			if (requestMapping.getKey().getPathPatternsCondition() != null) {
				requestMappingPatterns = requestMapping.getKey().getPathPatternsCondition().getPatternValues();
			} else { // Otherwise will be illegal state exception
				requestMappingPatterns = requestMapping.getKey().getPatternsCondition().getPatterns();
			}

			RequestMethod method = requestMapping.getKey().getMethodsCondition().getMethods().iterator().next();
			String paramName = getParamName(param);

			processPath(openApi, method, paramName, qfParamAnnotation, processor, requestMappingPatterns);

		}
	}

}
