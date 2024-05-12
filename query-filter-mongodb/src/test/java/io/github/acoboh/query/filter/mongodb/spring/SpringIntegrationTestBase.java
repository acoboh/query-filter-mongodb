package io.github.acoboh.query.filter.mongodb.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.data.mongodb.core.index.TextIndexDefinition.TextIndexDefinitionBuilder;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

import com.mongodb.client.MongoClients;

import io.github.acoboh.query.filter.mongodb.annotations.EnableQueryFilter;
import io.github.acoboh.query.filter.mongodb.config.QueryFilterAutoconfigure;
import io.github.acoboh.query.filter.mongodb.domain.BasicDocumentFilterDef;
import io.github.acoboh.query.filter.mongodb.model.TextDocument;
import io.github.acoboh.query.filter.mongodb.repositories.PostBlogDocumentRepository;

public class SpringIntegrationTestBase {

	@Configuration
	@EnableMongoRepositories(basePackageClasses = PostBlogDocumentRepository.class)
	@EnableTransactionManagement
	@Import(QueryFilterAutoconfigure.class)
	@EnableQueryFilter(basePackageClasses = BasicDocumentFilterDef.class)
	public static class Config {

		@Container
		public static final MongoDBContainer mongoContainer = new MongoDBContainer("mongo:7.0");

		static {
			mongoContainer.start();
		}

		@Bean
		MongoTemplate mongoTemplate() throws Exception {
			var mongoTemplate = new MongoTemplate(MongoClients.create(mongoContainer.getReplicaSetUrl()), "test");
			initIndexes(mongoTemplate);
			return mongoTemplate;
		}

		@Bean
		static PropertySourcesPlaceholderConfigurer propertySources() {
			return new PropertySourcesPlaceholderConfigurer();
		}

		void initIndexes(MongoTemplate mongoTemplate) {
			IndexOperations indexOps = mongoTemplate.indexOps(TextDocument.class);

			TextIndexDefinition textIndex = new TextIndexDefinitionBuilder().onField("name").onField("surname").build();

			indexOps.ensureIndex(textIndex);
		}

	}

}
