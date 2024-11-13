package io.github.acoboh.query.filter.mongodb.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

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

import io.github.acoboh.query.filter.mongodb.domain.PostBlogSpelBeanFilterDef;
import io.github.acoboh.query.filter.mongodb.domain.PostBlogSpelFilterDef;
import io.github.acoboh.query.filter.mongodb.domain.PostBlogSpelFilterNotIgnoreDef;
import io.github.acoboh.query.filter.mongodb.model.CommentModel;
import io.github.acoboh.query.filter.mongodb.model.PostBlogDocument;
import io.github.acoboh.query.filter.mongodb.repositories.PostBlogDocumentRepository;
import io.github.acoboh.query.filter.mongodb.spring.SpringIntegrationTestBase;
import reactor.core.publisher.SignalType;
import reactor.test.StepVerifier;
import reactor.test.util.TestLogger;
import reactor.util.Logger;
import reactor.util.Loggers;

@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SpelExpDefTests {

	private static final Logger LOGGER = Loggers.getLogger(SpelExpDefTests.class);

	private static final PostBlogDocument DOC_1 = new PostBlogDocument();
	private static final PostBlogDocument DOC_2 = new PostBlogDocument();

	static {
		DOC_1.setId("1");
		DOC_1.setTitle("Title 1 - Doc 1 example");
		DOC_1.setContent("Content 1 Hello World! ");
		DOC_1.setDate(LocalDateTime.of(2023, 1, 1, 0, 0, 0));
		DOC_1.setTags(new String[] { "tag1", "tag2", "common" });
		DOC_1.setLikes(4);

		List<CommentModel> comments1 = new ArrayList<>();

		CommentModel comment1 = new CommentModel();
		comment1.setStars(5);
		comment1.setComment("Comment 1");
		comment1.setDate(LocalDateTime.of(2023, 1, 1, 1, 0, 0));
		comment1.setLikes(100);

		CommentModel comment2 = new CommentModel();
		comment2.setStars(4);
		comment2.setComment("Comment 2");
		comment2.setDate(LocalDateTime.of(2023, 1, 1, 2, 0, 0));
		comment2.setLikes(200);

		comments1.add(comment1);
		comments1.add(comment2);

		DOC_1.setComments(comments1);

		DOC_2.setId("2");
		DOC_2.setTitle("Title 2 - Other example 2");
		DOC_2.setContent("Content 2 with some text");
		DOC_2.setDate(LocalDateTime.of(2024, 1, 1, 0, 0, 0));
		DOC_2.setTags(new String[] { "tag3", "tag4", "common" });
		DOC_2.setLikes(10);

		List<CommentModel> comments2 = new ArrayList<>();

		CommentModel comment3 = new CommentModel();
		comment3.setStars(3);
		comment3.setComment("Comment 3");
		comment3.setDate(LocalDateTime.of(2024, 1, 1, 1, 0, 0));
		comment3.setLikes(300);

		CommentModel comment4 = new CommentModel();
		comment4.setStars(2);
		comment4.setComment("Comment 4");
		comment4.setDate(LocalDateTime.of(2024, 1, 1, 2, 0, 0));
		comment4.setLikes(400);

		comments2.add(comment3);
		comments2.add(comment4);

		DOC_2.setComments(comments2);
	}

	@Autowired
	private QFProcessor<PostBlogSpelFilterDef, PostBlogDocument> qfProcessor;

	@Autowired
	private QFProcessor<PostBlogSpelBeanFilterDef, PostBlogDocument> qfBeanProcessor;

	@Autowired
	private QFProcessor<PostBlogSpelFilterNotIgnoreDef, PostBlogDocument> qfBeanNotIgnoreProcessor;

	@Autowired
	private PostBlogDocumentRepository repository;

	@Test
	@DisplayName("0. Setup")
	@Order(0)
	void setup() {
		assertThat(qfProcessor).isNotNull();
		assertThat(repository).isNotNull();

		StepVerifier.create(repository.findAll()).expectNextCount(0).verifyComplete();

		repository.save(DOC_1).block();
		repository.save(DOC_2).block();

		StepVerifier.create(repository.findAll()).expectNext(DOC_1, DOC_2).verifyComplete();
	}

	@Test
	@DisplayName("1. Test default SpEL expression")
	@Order(1)
	void testDefaultSpELExpression() {

		var qf = qfProcessor.newQueryFilter("likes=gte:1", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var blogList = qf.executeFindQuery();
		StepVerifier.create(blogList).expectNext(DOC_1, DOC_2).verifyComplete();

	}

	@Test
	@DisplayName("2. Test Filter SpEL expressions")
	@Order(2)
	void testFilteringSpELExpressions() {

		var qf = qfProcessor.newQueryFilter("likes=gte:5", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var blogList = qf.executeFindQuery();
		StepVerifier.create(blogList).expectNextCount(0).verifyComplete();

	}

	@Test
	@DisplayName("3. Test default SpEL expression with bean")
	@Order(3)
	void testDefaultSpELExpressionWithBean() {

		var qf = qfBeanProcessor.newQueryFilter("", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var blogList = qf.executeFindQuery();
		StepVerifier.create(blogList).expectNext(DOC_1).verifyComplete();

	}

	@Test
	@DisplayName("4. Test SpEL is blank")
	@Order(4)
	void testSpELIsBlank() {

		var qf = qfProcessor.newQueryFilter("", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var blogList = qf.executeFindQuery();
		StepVerifier.create(blogList).expectNext(DOC_1, DOC_2).verifyComplete();

	}

	@Test
	@DisplayName("5. Test SpEL is not ignore")
	@Order(5)
	void testSpELIsNotIgnore() {

		var qf = qfBeanNotIgnoreProcessor.newQueryFilter("", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var blogList = qf.executeFindQuery().log(LOGGER, Level.FINEST, true, SignalType.values());
		StepVerifier.create(blogList).expectError().verify();

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
