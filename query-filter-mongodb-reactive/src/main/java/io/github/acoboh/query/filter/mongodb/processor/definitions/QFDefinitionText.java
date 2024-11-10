package io.github.acoboh.query.filter.mongodb.processor.definitions;

import java.lang.reflect.Field;

import com.mongodb.client.model.TextSearchOptions;

import io.github.acoboh.query.filter.mongodb.annotations.QFBlockParsing;
import io.github.acoboh.query.filter.mongodb.annotations.QFText;

/**
 * Definitions of QFText annotations
 */
public class QFDefinitionText extends QFAbstractDefinition {

	private final QFText qfTextAnnotation;
	private final TextSearchOptions textSearchOptions;

	QFDefinitionText(Field filterField, Class<?> filterClass, Class<?> entityClass, QFBlockParsing blockParsing,
			QFText qfTextAnnotation) {
		super(filterField, filterClass, entityClass, blockParsing);

		this.qfTextAnnotation = qfTextAnnotation;

		if (!qfTextAnnotation.name().isBlank()) {
			this.filterName = qfTextAnnotation.name();
		}

		String language = null;
		if (!qfTextAnnotation.language().isBlank()) {
			language = qfTextAnnotation.language();
		}

		textSearchOptions = new TextSearchOptions().caseSensitive(qfTextAnnotation.caseSensitive())
				.diacriticSensitive(qfTextAnnotation.diacriticSensitive()).language(language);

	}

	/**
	 * Get original annotation
	 * 
	 * @return original annotation
	 */
	public QFText getQfTextAnnotation() {
		return qfTextAnnotation;
	}

	/**
	 * Get the text search options
	 * 
	 * @return text search options
	 */
	public TextSearchOptions getTextSearchOptions() {
		return textSearchOptions;
	}

}