package io.github.acoboh.query.filter.mongodb.processor.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.acoboh.query.filter.mongodb.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.mongodb.model.BasicDocument;
import io.github.acoboh.query.filter.mongodb.model.CommentModel;
import io.github.acoboh.query.filter.mongodb.model.PostBlogDocument;
import io.github.acoboh.query.filter.mongodb.model.PostBlogType;
import io.github.acoboh.query.filter.mongodb.processor.QFPath.QFElementDefType;

class FieldClassProcessorTests {

	@DisplayName("Test basic data all properties")
	@Test
	void testAllPropertiesBasicData() throws QueryFilterDefinitionException {

		var processorId = new FieldClassProcessor(BasicDocument.class, "id");

		var pathId = processorId.getPaths();

		assertThat(pathId).hasSize(1);

		var firstPathId = pathId.get(0);

		assertThat(firstPathId.getFieldClass()).isEqualTo(String.class);
		assertThat(firstPathId.getPath()).isEqualTo("id");
		assertThat(firstPathId.getType()).isEqualTo(QFElementDefType.PROPERTY);

		var processor = new FieldClassProcessor(BasicDocument.class, "name");

		assertThat(processor).isNotNull();

		var path = processor.getPaths();

		assertThat(path).hasSize(1);

		var firstPath = path.get(0);

		assertThat(firstPath.getFieldClass()).isEqualTo(String.class);
		assertThat(firstPath.getPath()).isEqualTo("name");
		assertThat(firstPath.getType()).isEqualTo(QFElementDefType.PROPERTY);
		assertThat(firstPath.getMappingName()).isEqualTo("name");

		// Test date time property is LocalDateTime class
		var processorDateTime = new FieldClassProcessor(BasicDocument.class, "dateTime");

		assertThat(processorDateTime).isNotNull();

		var pathDateTime = processorDateTime.getPaths();

		assertThat(pathDateTime).hasSize(1);

		var firstPathDateTime = pathDateTime.get(0);

		assertThat(firstPathDateTime.getFieldClass()).isEqualTo(LocalDateTime.class);
		assertThat(firstPathDateTime.getPath()).isEqualTo("dateTime");
		assertThat(firstPathDateTime.getType()).isEqualTo(QFElementDefType.PROPERTY);
		assertThat(firstPathDateTime.getMappingName()).isEqualTo("dateTime");

		// Test timestamp property is Timestamp class
		var processorTimestamp = new FieldClassProcessor(BasicDocument.class, "timestamp");

		assertThat(processorTimestamp).isNotNull();

		var pathTimestamp = processorTimestamp.getPaths();

		assertThat(pathTimestamp).hasSize(1);

		var firstPathTimestamp = pathTimestamp.get(0);

		assertThat(firstPathTimestamp.getFieldClass()).isEqualTo(java.util.Date.class);
		assertThat(firstPathTimestamp.getPath()).isEqualTo("timestamp");
		assertThat(firstPathTimestamp.getType()).isEqualTo(QFElementDefType.PROPERTY);
		assertThat(firstPathTimestamp.getMappingName()).isEqualTo("timestamp");

		// Test with field mapping
		var processorFieldMapping = new FieldClassProcessor(BasicDocument.class, "customName");

		assertThat(processorFieldMapping).isNotNull();

		var pathFieldMapping = processorFieldMapping.getPaths();

		assertThat(pathFieldMapping).hasSize(1);

		var firstPathFieldMapping = pathFieldMapping.get(0);

		assertThat(firstPathFieldMapping.getFieldClass()).isEqualTo(String.class);
		assertThat(firstPathFieldMapping.getPath()).isEqualTo("customName");
		assertThat(firstPathFieldMapping.getType()).isEqualTo(QFElementDefType.PROPERTY);
		assertThat(firstPathFieldMapping.getMappingName()).isEqualTo("mappingName");

		// Test with field mapping empty
		var processorFieldMappingEmpty = new FieldClassProcessor(BasicDocument.class, "baseName");

		assertThat(processorFieldMappingEmpty).isNotNull();

		var pathFieldMappingEmpty = processorFieldMappingEmpty.getPaths();

		assertThat(pathFieldMappingEmpty).hasSize(1);

		var firstPathFieldMappingEmpty = pathFieldMappingEmpty.get(0);

		assertThat(firstPathFieldMappingEmpty.getFieldClass()).isEqualTo(String.class);
		assertThat(firstPathFieldMappingEmpty.getPath()).isEqualTo("baseName");
		assertThat(firstPathFieldMappingEmpty.getType()).isEqualTo(QFElementDefType.PROPERTY);
		assertThat(firstPathFieldMappingEmpty.getMappingName()).isEqualTo("baseName");

	}

	@DisplayName("Test data with nested properties")
	@Test
	void testNestedProperties() throws QueryFilterDefinitionException {
		var processor = new FieldClassProcessor(PostBlogDocument.class, "comments.stars");

		assertThat(processor).isNotNull();

		var path = processor.getPaths();

		assertThat(path).hasSize(2);

		var firstPath = path.get(0);

		assertThat(firstPath.getFieldClass()).isEqualTo(CommentModel.class);
		assertThat(firstPath.getPath()).isEqualTo("comments");
		assertThat(firstPath.getType()).isEqualTo(QFElementDefType.LIST);
		assertThat(firstPath.getMappingName()).isEqualTo("comments");

		var secondPath = path.get(1);

		assertThat(secondPath.getFieldClass()).isEqualTo(int.class);
		assertThat(secondPath.getPath()).isEqualTo("stars");
		assertThat(secondPath.getType()).isEqualTo(QFElementDefType.PROPERTY);
		assertThat(secondPath.getMappingName()).isEqualTo("stars");

		// Test with nested comment string property
		var processorComment = new FieldClassProcessor(PostBlogDocument.class, "comments.comment");

		assertThat(processorComment).isNotNull();

		var pathComment = processorComment.getPaths();

		assertThat(pathComment).hasSize(2);

		var firstPathComment = pathComment.get(0);

		assertThat(firstPathComment.getFieldClass()).isEqualTo(CommentModel.class);
		assertThat(firstPathComment.getPath()).isEqualTo("comments");
		assertThat(firstPathComment.getType()).isEqualTo(QFElementDefType.LIST);
		assertThat(firstPathComment.getMappingName()).isEqualTo("comments");

		var secondPathComment = pathComment.get(1);

		assertThat(secondPathComment.getFieldClass()).isEqualTo(String.class);
		assertThat(secondPathComment.getPath()).isEqualTo("comment");
		assertThat(secondPathComment.getType()).isEqualTo(QFElementDefType.PROPERTY);
		assertThat(secondPathComment.getMappingName()).isEqualTo("comment");

	}

	@DisplayName("Test array data")
	@Test
	void testArrayData() throws QueryFilterDefinitionException {
		var processor = new FieldClassProcessor(PostBlogDocument.class, "tags");

		assertThat(processor).isNotNull();

		var path = processor.getPaths();

		assertThat(path).hasSize(1);

		var firstPath = path.get(0);

		assertThat(firstPath.getFieldClass()).isEqualTo(String.class);
		assertThat(firstPath.getPath()).isEqualTo("tags");
		assertThat(firstPath.getType()).isEqualTo(QFElementDefType.LIST);
		assertThat(firstPath.getMappingName()).isEqualTo("tags");
	}

	@DisplayName("Test nested simple data")
	@Test
	void testNestedSimpleData() throws QueryFilterDefinitionException {

		var processor = new FieldClassProcessor(PostBlogDocument.class, "authorComment.comment");

		assertThat(processor).isNotNull();

		var path = processor.getPaths();

		assertThat(path).hasSize(2);

		var firstPath = path.get(0);

		assertThat(firstPath.getFieldClass()).isEqualTo(CommentModel.class);
		assertThat(firstPath.getPath()).isEqualTo("authorComment");
		assertThat(firstPath.getType()).isEqualTo(QFElementDefType.PROPERTY);
		assertThat(firstPath.getMappingName()).isEqualTo("authorComment");

		var secondPath = path.get(1);

		assertThat(secondPath.getFieldClass()).isEqualTo(String.class);
		assertThat(secondPath.getPath()).isEqualTo("comment");
		assertThat(secondPath.getType()).isEqualTo(QFElementDefType.PROPERTY);
		assertThat(secondPath.getMappingName()).isEqualTo("comment");

	}

	@DisplayName("Test enum simple data")
	@Test
	void testEnumData() throws QueryFilterDefinitionException {
		var processor = new FieldClassProcessor(PostBlogDocument.class, "type");

		assertThat(processor).isNotNull();

		var path = processor.getPaths();

		assertThat(path).hasSize(1);

		var firstPath = path.get(0);

		assertThat(firstPath.getFieldClass()).isEqualTo(PostBlogType.class);
		assertThat(firstPath.getPath()).isEqualTo("type");
		assertThat(firstPath.getType()).isEqualTo(QFElementDefType.ENUM);
		assertThat(firstPath.getMappingName()).isEqualTo("type");
	}

}
