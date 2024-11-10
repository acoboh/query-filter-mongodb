package io.github.acoboh.query.filter.mongodb.matcher;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.acoboh.query.filter.mongodb.operations.QFOperationEnum;
import io.github.acoboh.query.filter.mongodb.processor.QFParamType;

class MatcherTest {

	@Test
	@DisplayName("Test all operators RHS_COLON")
	void testAllOperators() {

		for (var op : QFOperationEnum.values()) {

			String filterLHS = "authorName[" + op.getValue() + "]=Adrian";
			String filterRHS = "authorName=" + op.getValue() + ":Adrian";
			String[] filters = { filterLHS, filterRHS };
			QFParamType[] types = { QFParamType.LHS_BRACKETS, QFParamType.RHS_COLON };

			for (int i = 0; i < filters.length; i++) {
				var matcher = types[i].getPattern().matcher(filters[i]);
				boolean found = false;
				while (matcher.find()) {
					found = true;

					// 1,2 false 3,4,6 true, 4,6 authorName, Adrian
					assertThat(matcher.group(1)).isNull();
					assertThat(matcher.group(2)).isNull();
					assertThat(matcher.group(3)).isNotNull().isEqualTo(filters[i]);
					assertThat(matcher.group(4)).isNotNull().isEqualTo("authorName");
					assertThat(matcher.group(5)).isNotNull().isEqualTo(op.getValue());
					assertThat(matcher.group(6)).isNotNull().isEqualTo("Adrian");

				}

				assertThat(found).isTrue();
			}

		}

		// Test shortcut EQ
		String filterRHS = "authorName=Adrian";
		var matcher = QFParamType.RHS_COLON.getPattern().matcher(filterRHS);
		boolean found = false;
		while (matcher.find()) {
			found = true;

			// 1,2 false 3,4,6 true, 4,6 authorName, Adrian
			assertThat(matcher.group(1)).isNull();
			assertThat(matcher.group(2)).isNull();
			assertThat(matcher.group(3)).isNotNull().isEqualTo(filterRHS);
			assertThat(matcher.group(4)).isNotNull().isEqualTo("authorName");
			assertThat(matcher.group(5)).isNull();
			assertThat(matcher.group(6)).isNotNull().isEqualTo("Adrian");

		}

		assertThat(found).isTrue();

	}

}
