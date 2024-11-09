package io.github.acoboh.query.filter.mongodb.advisor;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebExchange;

import io.github.acoboh.query.filter.mongodb.exceptions.QueryFilterException;
import io.github.acoboh.query.filter.mongodb.exceptions.language.ExceptionLanguageResolver;
import io.github.acoboh.query.filter.mongodb.properties.AdvisorProperties;
import io.github.acoboh.query.filter.mongodb.properties.QueryFilterProperties;
import reactor.core.publisher.Mono;

/**
 * Class with {@linkplain ControllerAdvice} annotation for multi-language
 * exception support
 *
 * @author Adrián Cobo
 * 
 */
@ControllerAdvice
@ConditionalOnProperty(name = "query-filter.advisor.enabled", havingValue = "true", matchIfMissing = true)
public class QFExceptionAdvisor {

	private final ResourceBundleMessageSource messageSource;

	/**
	 * Default constructor
	 *
	 * @param properties query filter properties
	 */
	protected QFExceptionAdvisor(QueryFilterProperties properties) {
		AdvisorProperties props = properties.getAdvisor();

		messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename(props.getMessageSourceBaseName());
		messageSource.setDefaultEncoding(props.getMessageSourceDefaultEncoding());
		messageSource.setUseCodeAsDefaultMessage(props.isMessageSourceUseCodeAsDefaultMessage());
	}

	/**
	 * Handler for all {@linkplain QueryFilterException} exceptions
	 *
	 * @param ex      exception throw
	 * @param request original {@linkplain ServerWebExchange} request
	 * @return new response
	 */
	@ExceptionHandler(QueryFilterException.class)
	public Mono<ResponseEntity<Object>> handleEngineExceptions(QueryFilterException ex, ServerWebExchange request) {
		return handleAdvisorMessageResolver(ex, ex, request);
	}

	private Mono<ResponseEntity<Object>> handleAdvisorMessageResolver(ExceptionLanguageResolver resolver, Exception ex,
			ServerWebExchange request) {
		String message = message(resolver.getMessageCode(), resolver.getArguments());
		return defaultErrorMessage(ex, request, resolver.getHttpStatus(), message, false);
	}

	private Mono<ResponseEntity<Object>> defaultErrorMessage(Exception e, ServerWebExchange request, HttpStatus status,
			String message, boolean extend) {

		Map<String, Object> map = new LinkedHashMap<>(6);
		map.put("timestamp", new Date());
		map.put("status", status.value());
		map.put("error", status.getReasonPhrase());
		map.put("exception", e.getClass());
		map.put("message", message);
		map.put("path", request.getRequest().getPath().value());

		if (extend && e.getCause() != null) {
			map.put("causeClass", e.getCause().getClass());
			map.put("causeMessage", e.getCause().getMessage());
		}

		return Mono.just(ResponseEntity.status(status).body(map));
	}

	private String message(String code, Object... args) {
		return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
	}

}
