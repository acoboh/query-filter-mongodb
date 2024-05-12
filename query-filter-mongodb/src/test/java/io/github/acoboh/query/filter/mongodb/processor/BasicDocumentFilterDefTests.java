package io.github.acoboh.query.filter.mongodb.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.bson.Document;
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

import io.github.acoboh.query.filter.mongodb.domain.BasicDocumentFilterDef;
import io.github.acoboh.query.filter.mongodb.model.BasicDocument;
import io.github.acoboh.query.filter.mongodb.repositories.BasicDocumentRepository;
import io.github.acoboh.query.filter.mongodb.spring.SpringIntegrationTestBase;

@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BasicDocumentFilterDefTests {

	private static final BasicDocument DOC_1 = new BasicDocument();
	private static final BasicDocument DOC_2 = new BasicDocument();

	static {
		DOC_1.setId("1");
		DOC_1.setName("Name 1");
		DOC_1.setDateTime(LocalDateTime.of(2023, 1, 1, 0, 0, 0));
		DOC_1.setTimestamp(Timestamp.valueOf(DOC_1.getDateTime()));
		DOC_1.setCustomName("CNAME1");
		DOC_1.setBaseName("BNAME1");

		DOC_2.setId("2");
		DOC_2.setName("Name 2");
		DOC_2.setDateTime(LocalDateTime.of(2024, 1, 1, 0, 0, 0));
		DOC_2.setTimestamp(Timestamp.valueOf(DOC_2.getDateTime()));
		DOC_2.setCustomName("CNAME2");
		DOC_2.setBaseName("BNAME2");
	}

	@Autowired
	private QFProcessor<BasicDocumentFilterDef, BasicDocument> qfProcessor;

	@Autowired
	private BasicDocumentRepository repository;

	@Test
	@DisplayName("0. Setup")
	@Order(0)
	void setup() {
		assertThat(qfProcessor).isNotNull();
		assertThat(repository).isNotNull();

		assertThat(repository.findAll()).isEmpty();

		repository.save(DOC_1);
		repository.save(DOC_2);

		assertThat(repository.findAll()).hasSize(2).containsExactlyInAnyOrder(DOC_1, DOC_2);
	}

	@Test
	@DisplayName("1. Query by ID")
	@Order(1)
	void queryById() {

		var qf = qfProcessor.newQueryFilter("id=eq:1", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var query = qf.toCriteriaQuery();
		var doc = query.getQueryObject();

		Document expectedDoc = new Document("_id", "1");
		assertThat(doc).isEqualTo(expectedDoc);

		var list = qf.executeFindQuery();
		assertThat(list).hasSize(1).containsExactly(DOC_1);

		qf = qfProcessor.newQueryFilter("id[eq]=2", QFParamType.LHS_BRACKETS);
		assertThat(qf).isNotNull();

		list = qf.executeFindQuery();
		assertThat(list).hasSize(1).containsExactly(DOC_2);

		qf = qfProcessor.newQueryFilter("id=ne:1", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = qf.executeFindQuery();
		assertThat(list).hasSize(1).containsExactly(DOC_2);

	}

	@Test
	@DisplayName("2. Query by datetime")
	@Order(2)
	void testByDateTime() {

		var qf = qfProcessor.newQueryFilter("dateTime=gte:2024-01-01T00:00:00Z", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var list = qf.executeFindQuery();
		assertThat(list).hasSize(1).containsExactly(DOC_2);

		qf = qfProcessor.newQueryFilter("dateTime[lte]=2023-01-01T00:00:00Z", QFParamType.LHS_BRACKETS);
		assertThat(qf).isNotNull();

		list = qf.executeFindQuery();
		assertThat(list).hasSize(1).containsExactly(DOC_1);

	}

	@Test
	@DisplayName("3. Query sort by timestamp")
	@Order(3)
	void sortByTimestamp() {
		var qf = qfProcessor.newQueryFilter("sort=+timestamp", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var list = qf.executeFindQuery();
		assertThat(list).hasSize(2).containsExactly(DOC_1, DOC_2);

		qf = qfProcessor.newQueryFilter("sort=-timestamp", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = qf.executeFindQuery();
		assertThat(list).hasSize(2).containsExactly(DOC_2, DOC_1);
	}

	@Test
	@DisplayName("4. Query by custom name field")
	@Order(4)
	void testByCustomNameField() {
		var qf = qfProcessor.newQueryFilter("customName=eq:CNAME1", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var query = qf.toCriteriaQuery();
		var doc = query.getQueryObject();

		Document expectedDoc = new Document("mappingName", "CNAME1");
		assertThat(doc).isEqualTo(expectedDoc);

		var list = qf.executeFindQuery();
		assertThat(list).hasSize(1).containsExactly(DOC_1);

		qf = qfProcessor.newQueryFilter("customName[ne]=CNAME1", QFParamType.LHS_BRACKETS);
		assertThat(qf).isNotNull();

		list = qf.executeFindQuery();
		assertThat(list).hasSize(1).containsExactly(DOC_2);

	}

	@Test
	@DisplayName("5. Query by base name field")
	@Order(5)
	void testByBaseNameField() {
		var qf = qfProcessor.newQueryFilter("baseName=eq:BNAME1", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var list = qf.executeFindQuery();
		assertThat(list).hasSize(1).containsExactly(DOC_1);

		qf = qfProcessor.newQueryFilter("baseName[ne]=BNAME1", QFParamType.LHS_BRACKETS);
		assertThat(qf).isNotNull();

		list = qf.executeFindQuery();
		assertThat(list).hasSize(1).containsExactly(DOC_2);

	}

	@Test
	@DisplayName("END. Cleanup")
	@Order(Ordered.LOWEST_PRECEDENCE)
	void cleanup() {
		repository.deleteAll();
		assertThat(repository.findAll()).isEmpty();
	}

}
