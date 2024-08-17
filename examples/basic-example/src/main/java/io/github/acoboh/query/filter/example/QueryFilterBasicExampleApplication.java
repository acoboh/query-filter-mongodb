package io.github.acoboh.query.filter.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;

import io.github.acoboh.query.filter.example.filterdef.BasicDocumentFilterDef;
import io.github.acoboh.query.filter.mongodb.annotations.EnableQueryFilter;

@SpringBootApplication
@EnableQueryFilter(basePackageClasses = BasicDocumentFilterDef.class)
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
public class QueryFilterBasicExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(QueryFilterBasicExampleApplication.class, args);
	}

}
