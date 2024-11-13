package io.github.acoboh.query.filter.mongodb.processor.projection;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.acoboh.query.filter.mongodb.exceptions.definition.QFTypeException;

class ProjectionDefinitionTests {

	@Test
	@DisplayName("Projection definition")
	void testProjectionDefinition() throws QFTypeException {

		var projectDefinition = new ProjectionDefinition(DocumentProjectionDef.class);

		var projectionOperation = projectDefinition.getProjectionOperation();
		assertThat(projectionOperation).isNotNull();

		// Check return type
		assertThat(projectDefinition.getReturnType()).isEqualTo(DocumentProjectionDef.class);

		// Check projection operation

		String[] expectedFields = new String[] { "_id", "name", "testENUM", "inner1._id", "inner1.name1",
				"inner1.inner2._id", "inner1.inner2.name2" };

		var itr = projectionOperation.getFields().iterator();
		int index = 0;

		while (itr.hasNext()) {
			// inner class ExposedField not available
			itr.next();
			index++;
		}

		assertThat(index).isEqualTo(expectedFields.length);

		for (String fieldName : expectedFields) {
			assertThat(projectionOperation.getFields().getField(fieldName)).isNotNull();
		}

	}

}
