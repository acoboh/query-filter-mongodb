package io.github.acoboh.query.filter.mongodb.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.data.domain.PageRequest;
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
class PostBlogSortTests {

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
	@DisplayName("1. Sort by ID")
	@Order(1)
	void queryById() {

		var qf = qfProcessor.newQueryFilter("sort=+id", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var list = qf.executeFindQuery();
		assertThat(list).hasSize(2).containsExactly(DOC_1, DOC_2);

		qf = qfProcessor.newQueryFilter("sort=-id", QFParamType.LHS_BRACKETS);
		assertThat(qf).isNotNull();

		list = qf.executeFindQuery();
		assertThat(list).hasSize(2).containsExactly(DOC_2, DOC_1);
	}

	@Test
	@DisplayName("2. Sort by Starts")
	@Order(2)
	void sortByNested() {

		var qf = qfProcessor.newQueryFilter("sort=+commentsStars", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var list = qf.executeFindQuery();
		assertThat(list).hasSize(2).containsExactly(DOC_2, DOC_1);

		qf = qfProcessor.newQueryFilter("sort=-commentsStars", QFParamType.LHS_BRACKETS);
		assertThat(qf).isNotNull();

		list = qf.executeFindQuery();
		assertThat(list).hasSize(2).containsExactly(DOC_1, DOC_2);
	}

	@Test
	@DisplayName("3. Sort by ID on aggreggates")
	@Order(3)
	void sortByAggregates() {
		var qf = qfProcessor.newQueryFilter("sort=+id", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var list = qf.executeAggregateAndProject(PostBlogDocument.class);
		assertThat(list).hasSize(2).containsExactly(DOC_1, DOC_2);

		qf = qfProcessor.newQueryFilter("sort=-id", QFParamType.LHS_BRACKETS);
		assertThat(qf).isNotNull();

		list = qf.executeAggregateAndProject(PostBlogDocument.class);
		assertThat(list).hasSize(2).containsExactly(DOC_2, DOC_1);
	}

	@Test
	@DisplayName("4. Sort by star comments on aggreggates")
	@Order(4)
	void sortByStartsAggregates() {
		var qf = qfProcessor.newQueryFilter("sort=+commentsStars", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var list = qf.executeAggregateAndProject(PostBlogDocument.class);
		assertThat(list).hasSize(2).containsExactly(DOC_2, DOC_1);

		qf = qfProcessor.newQueryFilter("sort=-commentsStars", QFParamType.LHS_BRACKETS);
		assertThat(qf).isNotNull();

		list = qf.executeAggregateAndProject(PostBlogDocument.class);
		assertThat(list).hasSize(2).containsExactly(DOC_1, DOC_2);
	}

	@Test
	@DisplayName("5. Sort by IDon aggreggates pageable")
	@Order(5)
	void sortByAggregatePageable() {
		var qf = qfProcessor.newQueryFilter("sort=+id", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var list = qf.executeAggregateAndProject(PageRequest.of(0, 10), PostBlogDocument.class);
		assertThat(list).hasSize(2).containsExactly(DOC_1, DOC_2);

		qf = qfProcessor.newQueryFilter("sort=-id", QFParamType.LHS_BRACKETS);
		assertThat(qf).isNotNull();

		list = qf.executeAggregateAndProject(PageRequest.of(0, 10), PostBlogDocument.class);
		assertThat(list).hasSize(2).containsExactly(DOC_2, DOC_1);
	}

	@Test
	@DisplayName("6. Sort by star comments on aggreggates pageable")
	@Order(6)
	void sortByStartsAggregatePageable() {
		var qf = qfProcessor.newQueryFilter("sort=+commentsStars", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var list = qf.executeAggregateAndProject(PageRequest.of(0, 10), PostBlogDocument.class);
		assertThat(list).hasSize(2).containsExactly(DOC_2, DOC_1);

		qf = qfProcessor.newQueryFilter("sort=-commentsStars", QFParamType.LHS_BRACKETS);
		assertThat(qf).isNotNull();

		list = qf.executeAggregateAndProject(PageRequest.of(0, 10), PostBlogDocument.class);
		assertThat(list).hasSize(2).containsExactly(DOC_1, DOC_2);
	}

	@Test
	@DisplayName("END. Cleanup")
	@Order(Ordered.LOWEST_PRECEDENCE)
	void cleanup() {
		repository.deleteAll();
		assertThat(repository.findAll()).isEmpty();
	}

}
