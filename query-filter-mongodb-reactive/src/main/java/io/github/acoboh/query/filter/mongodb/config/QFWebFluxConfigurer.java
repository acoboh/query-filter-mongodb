package io.github.acoboh.query.filter.mongodb.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import io.github.acoboh.query.filter.mongodb.converters.QFCustomConverter;
import io.github.acoboh.query.filter.mongodb.processor.QFProcessor;

/**
 * Class to enable custom converters for Spring Boot Controllers via
 * {@linkplain WebFluxConfigurer} converters
 *
 * @author Adrián Cobo
 * 
 */
@Configuration
@EnableWebFlux
public class QFWebFluxConfigurer implements WebFluxConfigurer {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFWebFluxConfigurer.class);

	private final List<QFProcessor<?, ?>> processors;

	QFWebFluxConfigurer(List<QFProcessor<?, ?>> processors) {
		this.processors = processors;
	}

	/** {@inheritDoc} */
	@Override
	public void addFormatters(FormatterRegistry registry) {
		LOGGER.info("Using QueryFilterWebMvcConfigurer. Registering custom formatters");
		registry.addConverter(new QFCustomConverter(processors));
	}

}
