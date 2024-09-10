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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;

import io.github.acoboh.query.filter.mongodb.domain.PostBlogDocumentFilterDef;
import io.github.acoboh.query.filter.mongodb.dto.PostBlogSimplifiedDTO;
import io.github.acoboh.query.filter.mongodb.model.CommentModel;
import io.github.acoboh.query.filter.mongodb.model.PostBlogDocument;
import io.github.acoboh.query.filter.mongodb.repositories.PostBlogDocumentRepository;
import io.github.acoboh.query.filter.mongodb.spring.SpringIntegrationTestBase;

@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostBlogProjectionTests {

	private static final PostBlogDocument DOC_1 = new PostBlogDocument();
	private static final PostBlogDocument DOC_2 = new PostBlogDocument();

	private static final PostBlogSimplifiedDTO DOC1_DTO = new PostBlogSimplifiedDTO();
	private static final PostBlogSimplifiedDTO DOC2_DTO = new PostBlogSimplifiedDTO();

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

		DOC1_DTO.setId(DOC_1.getId());
		DOC1_DTO.setTitle(DOC_1.getTitle());
		DOC1_DTO.setContent(DOC_1.getContent());

		DOC2_DTO.setId(DOC_2.getId());
		DOC2_DTO.setTitle(DOC_2.getTitle());
		DOC2_DTO.setContent(DOC_2.getContent());

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
	@DisplayName("1. Query by ID and projection")
	@Order(1)
	void queryById() {

		var qf = qfProcessor.newQueryFilter("id=eq:1", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var list = qf.executeAggregateAndProject(PostBlogSimplifiedDTO.class);
		assertThat(list).isNotNull().hasSize(1).containsExactly(DOC1_DTO);

		qf = qfProcessor.newQueryFilter("id=eq:2", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = qf.executeAggregateAndProject(PostBlogSimplifiedDTO.class);
		assertThat(list).isNotNull().hasSize(1).containsExactly(DOC2_DTO);

	}

	@Test
	@DisplayName("2. Sort by ID and projection")
	@Order(2)
	void testSortProjectBase() {

		var qf = qfProcessor.newQueryFilter("sort=+id", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var list = qf.executeAggregateAndProject(PostBlogSimplifiedDTO.class);
		assertThat(list).isNotNull().hasSize(2).containsExactly(DOC1_DTO, DOC2_DTO);

		qf = qfProcessor.newQueryFilter("sort=-id", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = qf.executeAggregateAndProject(PostBlogSimplifiedDTO.class);
		assertThat(list).isNotNull().hasSize(2).containsExactly(DOC2_DTO, DOC1_DTO);

	}

	@Test
	@DisplayName("3. Sort by date and projection")
	@Order(3)
	void testSortByDate() {

		var qf = qfProcessor.newQueryFilter("sort=+date", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var list = qf.executeAggregateAndProject(PostBlogSimplifiedDTO.class);
		assertThat(list).isNotNull().hasSize(2).containsExactly(DOC1_DTO, DOC2_DTO);

		qf = qfProcessor.newQueryFilter("sort=-date", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = qf.executeAggregateAndProject(PostBlogSimplifiedDTO.class);
		assertThat(list).isNotNull().hasSize(2).containsExactly(DOC2_DTO, DOC1_DTO);

	}

	@Test
	@DisplayName("END. Cleanup")
	@Order(Ordered.LOWEST_PRECEDENCE)
	void cleanup() {
		repository.deleteAll();
		assertThat(repository.findAll()).isEmpty();
	}
}
