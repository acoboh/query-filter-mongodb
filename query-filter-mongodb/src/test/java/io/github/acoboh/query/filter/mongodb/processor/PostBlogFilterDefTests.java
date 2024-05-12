package io.github.acoboh.query.filter.mongodb.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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

import io.github.acoboh.query.filter.mongodb.domain.PostBlogDocumentFilterDef;
import io.github.acoboh.query.filter.mongodb.model.CommentModel;
import io.github.acoboh.query.filter.mongodb.model.PostBlogDocument;
import io.github.acoboh.query.filter.mongodb.repositories.PostBlogDocumentRepository;
import io.github.acoboh.query.filter.mongodb.spring.SpringIntegrationTestBase;

@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostBlogFilterDefTests {

	private static final PostBlogDocument DOC_1 = new PostBlogDocument();
	private static final PostBlogDocument DOC_2 = new PostBlogDocument();

	static {
		DOC_1.setId("1");
		DOC_1.setTitle("Title 1 - Doc 1 example");
		DOC_1.setContent("Content 1");
		DOC_1.setDate(LocalDateTime.of(2023, 1, 1, 0, 0, 0));
		DOC_1.setTags(new String[] { "tag1", "tag2", "common" });

		List<CommentModel> comments1 = new ArrayList<>();

		CommentModel comment1 = new CommentModel();
		comment1.setStars(5);
		comment1.setComment("Comment 1");
		comment1.setDate(LocalDateTime.of(2023, 1, 1, 1, 0, 0));

		CommentModel comment2 = new CommentModel();
		comment2.setStars(4);
		comment2.setComment("Comment 2");
		comment2.setDate(LocalDateTime.of(2023, 1, 1, 2, 0, 0));

		comments1.add(comment1);
		comments1.add(comment2);

		DOC_1.setComments(comments1);

		DOC_2.setId("2");
		DOC_2.setTitle("Title 2 - Other example 2");
		DOC_2.setContent("Content 2");
		DOC_2.setDate(LocalDateTime.of(2024, 1, 1, 0, 0, 0));
		DOC_2.setTags(new String[] { "tag3", "tag4", "common" });

		List<CommentModel> comments2 = new ArrayList<>();

		CommentModel comment3 = new CommentModel();
		comment3.setStars(3);
		comment3.setComment("Comment 3");
		comment3.setDate(LocalDateTime.of(2024, 1, 1, 1, 0, 0));

		CommentModel comment4 = new CommentModel();
		comment4.setStars(2);
		comment4.setComment("Comment 4");
		comment4.setDate(LocalDateTime.of(2024, 1, 1, 2, 0, 0));

		comments2.add(comment3);
		comments2.add(comment4);

		DOC_2.setComments(comments2);

	}

	@Autowired
	private QFProcessor<PostBlogDocumentFilterDef, PostBlogDocument> qfProcessor;

	@Autowired
	private PostBlogDocumentRepository repository;

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
	}

	@Test
	@DisplayName("2. Query by title")
	@Order(2)
	void queryByTitle() {

		var qf = qfProcessor.newQueryFilter("title=starts:Title 1 -", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var query = qf.toCriteriaQuery();
		var doc = query.getQueryObject();

		assertThat(doc).hasSize(1);

		var firstEntry = doc.entrySet().iterator().next();
		assertThat(firstEntry.getKey()).isEqualTo("title");
		assertThat(firstEntry.getValue()).isInstanceOf(Pattern.class);
		assertThat(((Pattern) firstEntry.getValue()).pattern()).isEqualTo("^Title 1 -");

		var list = qf.executeFindQuery();
		assertThat(list).hasSize(1).containsExactly(DOC_1);

		qf = qfProcessor.newQueryFilter("title[ends]=example 2", QFParamType.LHS_BRACKETS);
		assertThat(qf).isNotNull();

		list = qf.executeFindQuery();
		assertThat(list).hasSize(1).containsExactly(DOC_2);

	}

	@Test
	@DisplayName("3. Test by comments stars")
	@Order(3)
	void testByCommentsStars() {

		var qf = qfProcessor.newQueryFilter("commentsStars=gt:3", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var list = qf.executeFindQuery();
		assertThat(list).hasSize(1).containsExactly(DOC_1);

		qf = qfProcessor.newQueryFilter("commentsStars[lt]=3", QFParamType.LHS_BRACKETS);
		assertThat(qf).isNotNull();

		list = qf.executeFindQuery();
		assertThat(list).hasSize(1).containsExactly(DOC_2);

	}

	@Test
	@DisplayName("4. Query by tags")
	@Order(4)
	void queryByTags() {

		var qf = qfProcessor.newQueryFilter("tags=in:tag1,tag2", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var list = qf.executeFindQuery();
		assertThat(list).hasSize(1).containsExactly(DOC_1);

		qf = qfProcessor.newQueryFilter("tags[nin]=tag1", QFParamType.LHS_BRACKETS);
		assertThat(qf).isNotNull();

		list = qf.executeFindQuery();
		assertThat(list).hasSize(1).containsExactly(DOC_2);

		qf = qfProcessor.newQueryFilter("tags=eq:common", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = qf.executeFindQuery();
		assertThat(list).hasSize(2).containsExactlyInAnyOrder(DOC_1, DOC_2);

	}

	@Test
	@DisplayName("5. Query by title and stars")
	@Order(5)
	void queryByTitleAndStars() {

		var qf = qfProcessor.newQueryFilter("title=like:exa&commentsStars=gte:1", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var list = qf.executeFindQuery();
		assertThat(list).hasSize(2).containsExactlyInAnyOrder(DOC_1, DOC_2);

		qf = qfProcessor.newQueryFilter("title=like:exa&commentsStars=gte:4", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = qf.executeFindQuery();
		assertThat(list).hasSize(1).containsExactly(DOC_1);

	}

	@Test
	@DisplayName("END. Cleanup")
	@Order(Ordered.LOWEST_PRECEDENCE)
	void cleanup() {
		repository.deleteAll();
		assertThat(repository.findAll()).isEmpty();
	}

}
