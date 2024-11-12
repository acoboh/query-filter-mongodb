package io.github.acoboh.query.filter.mongodb.openapi.config;

import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import io.github.acoboh.query.filter.mongodb.annotations.QFParam;
import io.github.acoboh.query.filter.mongodb.processor.QFProcessor;
import io.swagger.v3.oas.models.OpenAPI;

@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
class OpenApiCustomizerReactive extends OpenApiCustomiserAbstract {

	private static final Logger LOGGER = LoggerFactory.getLogger(OpenApiCustomizerReactive.class);

	OpenApiCustomizerReactive(ApplicationContext applicationContext) {
		super(applicationContext);
	}

	@Override
	public void customise(OpenAPI openApi) {
		RequestMappingHandlerMapping mappingHandler = applicationContext.getBean("requestMappingHandlerMapping",
				RequestMappingHandlerMapping.class);

		for (var requestMapping : mappingHandler.getHandlerMethods().entrySet()) {
			LOGGER.debug("Checking path {}", requestMapping.getKey());
			processParameter(openApi, requestMapping);
		}
	}

	private void processParameter(OpenAPI openAPI, Entry<RequestMappingInfo, HandlerMethod> requestMapping) {

		for (var param : requestMapping.getValue().getMethodParameters()) {
			var qfParamAnnotation = param.getParameterAnnotation(QFParam.class);
			if (qfParamAnnotation == null) {
				continue;
			}

			QFProcessor<?, ?> processor = getQfProcessor(param, qfParamAnnotation);
			if (processor == null) {
				continue;
			}

			Set<String> requestMappingPatterns = requestMapping.getKey().getPatternsCondition().getPatterns().stream()
					.map(PathPattern::getPatternString).collect(Collectors.toSet());
			if (requestMappingPatterns.isEmpty()) {
				// If no patterns are found, try to get direct paths
				requestMappingPatterns = requestMapping.getKey().getDirectPaths();
			}

			RequestMethod method = requestMapping.getKey().getMethodsCondition().getMethods().iterator().next();
			String paramName = getParamName(param);

			processPath(openAPI, method, paramName, qfParamAnnotation, processor, requestMappingPatterns);

		}

	}
}
