package io.github.acoboh.query.filter.mongodb.domain;

import io.github.acoboh.query.filter.mongodb.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.mongodb.annotations.QFElement;
import io.github.acoboh.query.filter.mongodb.model.PostBlogDocument;
import io.github.acoboh.query.filter.mongodb.operations.QFOperationEnum;

@QFDefinitionClass(PostBlogDocument.class)
public class PostBlogSpelFilterDef {

	@QFElement("likes")
	private int likes;

	@QFElement(value = "comments.likes", isSpPELExpression = true, defaultValues = "#likes * 100", defaultOperation = QFOperationEnum.GREATER_THAN, order = 0)
	private int commentLikes;

}
