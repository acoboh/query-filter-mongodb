package io.github.acoboh.query.filter.mongodb.openapi.config;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.github.acoboh.query.filter.mongodb.annotations.QFParam;
import io.github.acoboh.query.filter.mongodb.operations.QFOperationEnum;
import io.github.acoboh.query.filter.mongodb.operations.QFOperationTextEnum;
import io.github.acoboh.query.filter.mongodb.processor.QFProcessor;
import io.github.acoboh.query.filter.mongodb.processor.definitions.IDefinitionSortable;
import io.github.acoboh.query.filter.mongodb.processor.definitions.QFAbstractDefinition;
import io.github.acoboh.query.filter.mongodb.processor.definitions.QFDefinitionElement;
import io.github.acoboh.query.filter.mongodb.processor.definitions.QFDefinitionText;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;

public abstract class OpenApiCustomiserAbstract implements OpenApiCustomizer {

	private static final Logger LOGGER = LoggerFactory.getLogger(OpenApiCustomiserAbstract.class);

	protected final ApplicationContext applicationContext;

	OpenApiCustomiserAbstract(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	protected void processPath(OpenAPI openApi, RequestMethod requestMethod, String paramName,
			QFParam qfParamAnnotation, QFProcessor<?, ?> processor, Set<String> requestMappingPatterns) {
		for (String path : requestMappingPatterns) { // For multiple mapping on same method

			Optional<PathItem> optPath = openApi.getPaths().entrySet().stream().filter(e -> e.getKey().equals(path))
					.map(Map.Entry::getValue).findFirst();

			if (optPath.isEmpty()) {
				LOGGER.error("Error processing {} path", path);
				continue;
			}

			Operation op = getOperation(optPath.get(), requestMethod);

			Optional<io.swagger.v3.oas.models.parameters.Parameter> optParam = op.getParameters().stream()
					.filter(e -> e.getName().equals(paramName)).findFirst();

			if (optParam.isEmpty()) {
				LOGGER.error("Error getting parameter filter on path {}", path);
				continue;
			}

			String actualDesc = optParam.get().getDescription();

			LOGGER.debug("Override description {}", actualDesc);

			optParam.get().setDescription(createDescription(qfParamAnnotation, processor));

			// Force string schema on swagger
			Schema<String> schema = new Schema<>();
			schema.type("string");
			optParam.get().setSchema(schema);
		}
	}

	protected String createDescription(QFParam annotation, QFProcessor<?, ?> processor) {

		StringBuilder builder = new StringBuilder("Filter is <b><i>").append(annotation.type().getBeatifulName());

		builder.append("</i></b>. Available fields: \n");

		Collection<QFAbstractDefinition> defValues = processor.getDefinitionMap().values();
		List<QFAbstractDefinition> defValuesOrdered = new ArrayList<>(defValues);
		defValuesOrdered.sort(Comparator.comparing(QFAbstractDefinition::getFilterName));

		for (var def : defValuesOrdered) {
			if (def.isConstructorBlocked()) {
				continue;
			}

			builder.append("<p><b>").append(def.getFilterName()).append("</b>:");

			if (def instanceof QFDefinitionElement defElement) {
				Set<QFOperationEnum> qfOperations = QFOperationEnum
						.getOperationsOfClass(defElement.getFirstFinalClass());

				if (!qfOperations.isEmpty()) {
					builder.append(" Operations: [<i>");
					String operationsAvailable = qfOperations.stream().map(QFOperationEnum::getValue)
							.collect(Collectors.joining(","));
					builder.append(operationsAvailable).append("</i>]");
				}

			}

			if (def instanceof QFDefinitionText) {
				String operations = Stream.of(QFOperationTextEnum.values()).map(QFOperationTextEnum::getValue)
						.collect(Collectors.joining(","));

				builder.append(" <i>(Text)</i> Operations:[<i>").append(operations).append("</i>]");
			}

			if (def instanceof IDefinitionSortable idef && idef.isSortable()) {
				builder.append(" <i>(Sortable)</i>");
			}

		}

		return builder.toString();

	}

	protected Operation getOperation(PathItem item, RequestMethod method) {

		return switch (method) {

		case DELETE -> item.getDelete();
		case HEAD -> item.getHead();
		case OPTIONS -> item.getOptions();
		case PATCH -> item.getPatch();
		case POST -> item.getPost();
		case PUT -> item.getPut();
		case TRACE -> item.getTrace();
		case GET -> item.getGet();
		default -> throw new IllegalArgumentException("Method not supported" + method);
		};

	}

	protected String getParamName(MethodParameter param) {
		String paramName = param.getParameterName();
		LOGGER.trace("Param name from MethodParam {}", paramName);
		if (paramName == null) {
			paramName = param.getParameter().getName();
			LOGGER.trace("Param name from parameter {}", paramName);
		}
		RequestParam requestParamAnnotation = param.getParameterAnnotation(RequestParam.class);
		if (requestParamAnnotation != null && !requestParamAnnotation.name().isEmpty()) {
			paramName = requestParamAnnotation.name();
			LOGGER.trace("Param name from RequestParam annotation {}", paramName);
		}
		return paramName;
	}

	protected QFProcessor<?, ?> getQfProcessor(MethodParameter param, QFParam qfParamAnnotation) {
		var filterType = (ParameterizedType) param.getGenericParameterType();
		Class<?> classType = (Class<?>) filterType.getActualTypeArguments()[0];

		var resolvableBeanType = ResolvableType.forClassWithGenerics(QFProcessor.class, qfParamAnnotation.value(),
				classType);
		String[] names = applicationContext.getBeanNamesForType(resolvableBeanType);
		if (names.length > 1) {
			LOGGER.warn("Multiple beans found for type {}", resolvableBeanType);
		} else if (names.length == 0) {
			LOGGER.error("No bean found for type {}", resolvableBeanType);
			return null;
		}

		QFProcessor<?, ?> processor = applicationContext.getBean(names[0], QFProcessor.class);
		return processor;
	}

}
