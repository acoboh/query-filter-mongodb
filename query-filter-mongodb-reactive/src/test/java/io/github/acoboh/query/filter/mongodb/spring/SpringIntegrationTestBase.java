package io.github.acoboh.query.filter.mongodb.spring;

import java.util.TimeZone;

import io.github.acoboh.query.filter.mongodb.SpELBeanExample;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.data.mongodb.core.index.TextIndexDefinition.TextIndexDefinitionBuilder;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

import com.mongodb.reactivestreams.client.MongoClients;

import io.github.acoboh.query.filter.mongodb.annotations.EnableQueryFilter;
import io.github.acoboh.query.filter.mongodb.config.QueryFilterAutoconfigure;
import io.github.acoboh.query.filter.mongodb.domain.BasicDocumentFilterDef;
import io.github.acoboh.query.filter.mongodb.model.TextDocument;
import io.github.acoboh.query.filter.mongodb.repositories.PostBlogDocumentRepository;

public class SpringIntegrationTestBase {

	@Configuration(proxyBeanMethods = false)
	@EnableReactiveMongoRepositories(basePackageClasses = PostBlogDocumentRepository.class)
	@EnableTransactionManagement
	@Import(QueryFilterAutoconfigure.class)
	@EnableQueryFilter(basePackageClasses = BasicDocumentFilterDef.class)
	public static class Config {

		@Container
		public static final MongoDBContainer mongoContainer = new MongoDBContainer("mongo:7.0");

		static {
			TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
			mongoContainer.start();
		}

		@Bean
		ReactiveMongoTemplate reactiveMongoTemplate() throws Exception {

			var mongoTemplate = new ReactiveMongoTemplate(MongoClients.create(mongoContainer.getReplicaSetUrl()),
					"test");
			initIndexes(mongoTemplate);
			return mongoTemplate;
		}

		@Bean
		static PropertySourcesPlaceholderConfigurer propertySources() {
			return new PropertySourcesPlaceholderConfigurer();
		}

		void initIndexes(ReactiveMongoTemplate mongoTemplate) {
			var indexOps = mongoTemplate.indexOps(TextDocument.class);

			TextIndexDefinition textIndex = new TextIndexDefinitionBuilder().onField("name").onField("surname").build();

			indexOps.ensureIndex(textIndex).block();
		}

		@Bean("customBean")
		public SpELBeanExample spELBeanExample() {
			return () -> "Hello World";
		}

	}

}
