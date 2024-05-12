package io.github.acoboh.query.filter.mongodb.domain;

import io.github.acoboh.query.filter.mongodb.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.mongodb.annotations.QFText;
import io.github.acoboh.query.filter.mongodb.model.TextDocument;

@QFDefinitionClass(TextDocument.class)
public class TextFilterDef {

	@QFText
	private String search;

}
