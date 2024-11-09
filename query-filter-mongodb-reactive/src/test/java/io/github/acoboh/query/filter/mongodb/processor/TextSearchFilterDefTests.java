package io.github.acoboh.query.filter.mongodb.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Predicate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;

import io.github.acoboh.query.filter.mongodb.domain.TextFilterDef;
import io.github.acoboh.query.filter.mongodb.model.TextDocument;
import io.github.acoboh.query.filter.mongodb.repositories.TextDocumentRepository;
import io.github.acoboh.query.filter.mongodb.spring.SpringIntegrationTestBase;
import reactor.test.StepVerifier;

@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TextSearchFilterDefTests {

	private static TextDocument DOC_1 = new TextDocument();
	private static TextDocument DOC_2 = new TextDocument();
	private static TextDocument DOC_3 = new TextDocument();

	static {
		DOC_1.setName("John");
		DOC_1.setSurname("Can Doe");

		DOC_2.setName("Jane");
		DOC_2.setSurname("Doe");

		DOC_3.setName("John");
		DOC_3.setSurname("Smith");
	}

	@Autowired
	private QFProcessor<TextFilterDef, TextDocument> qfProcessor;

	@Autowired
	private TextDocumentRepository repository;

	@Test
	@DisplayName("0. Setup")
	@Order(0)
	void setup() {
		assertThat(qfProcessor).isNotNull();
		assertThat(repository).isNotNull();

		StepVerifier.create(repository.findAll()).expectNextCount(0).verifyComplete();

		repository.save(DOC_1).block();
		repository.save(DOC_2).block();
		repository.save(DOC_3).block();

		StepVerifier.create(repository.findAll()).expectNext(DOC_1, DOC_2, DOC_3).verifyComplete();
	}

	@Test
	@DisplayName("1. Text Search")
	@Order(1)
	void textSearch() {

		var qf = qfProcessor.newQueryFilter("search=eq:n Doe", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var listData = qf.executeFindQuery();
		// Expect on any order DOC_1, DOC_2
		Predicate<TextDocument> predicate = doc -> doc.equals(DOC_1) || doc.equals(DOC_2);
		StepVerifier.create(listData).expectNextMatches(predicate).expectNextMatches(predicate).verifyComplete();

		qf = qfProcessor.newQueryFilter("search=eq:John Smith", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		listData = qf.executeFindQuery();
		Predicate<TextDocument> predicate2 = doc -> doc.equals(DOC_1) || doc.equals(DOC_3);
		StepVerifier.create(listData).expectNextMatches(predicate2).expectNextMatches(predicate2).verifyComplete();

	}

	@Test
	@DisplayName("2. Text search complex")
	@Order(2)
	void textSearchComplex() {

		var qf = qfProcessor.newQueryFilter("search=eq:John -Smith", QFParamType.RHS_COLON);

		var listData = qf.executeFindQuery();
		StepVerifier.create(listData).expectNext(DOC_1).verifyComplete();

		qf = qfProcessor.newQueryFilter("search=eq:\"John Smith\"", QFParamType.RHS_COLON);

		listData = qf.executeFindQuery();
		StepVerifier.create(listData).expectNextCount(0).verifyComplete();

		qf = qfProcessor.newQueryFilter("search=eq:\"n Doe\"", QFParamType.RHS_COLON);

		listData = qf.executeFindQuery();
		StepVerifier.create(listData).expectNext(DOC_1).verifyComplete();

	}

	@Test
	@DisplayName("END. Cleanup")
	@Order(Ordered.LOWEST_PRECEDENCE)
	void cleanup() {
		StepVerifier.create(repository.deleteAll()).verifyComplete();
		repository.deleteAll();
		StepVerifier.create(repository.findAll()).expectNextCount(0).verifyComplete();
	}

}
