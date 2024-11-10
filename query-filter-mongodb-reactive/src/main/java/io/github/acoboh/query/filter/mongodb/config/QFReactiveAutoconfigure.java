package io.github.acoboh.query.filter.mongodb.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({ QFBeanFactoryPostProcessor.class })
public class QFReactiveAutoconfigure {
}
