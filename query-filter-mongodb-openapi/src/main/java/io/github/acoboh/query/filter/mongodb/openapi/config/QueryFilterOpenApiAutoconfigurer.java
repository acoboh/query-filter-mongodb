package io.github.acoboh.query.filter.mongodb.openapi.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * @author Adrián Cobo
 *
 */
@AutoConfiguration
@Import({ OpenApiCustomizerMvc.class, OpenApiCustomizerReactive.class })
public class QueryFilterOpenApiAutoconfigurer {

}
