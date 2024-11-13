package io.github.acoboh.query.filter.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import io.github.acoboh.query.filter.example.filterdef.BasicDocumentFilterDef;
import io.github.acoboh.query.filter.example.repositories.BasicDocumentRepository;
import io.github.acoboh.query.filter.mongodb.annotations.EnableQueryFilter;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableQueryFilter(basePackageClasses = BasicDocumentFilterDef.class)
@EnableReactiveMongoRepositories(basePackageClasses = BasicDocumentRepository.class)
@EnableSpringDataWebSupport
public class QueryFilterBasicExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(QueryFilterBasicExampleApplication.class, args);
	}

}