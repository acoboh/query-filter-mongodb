package io.github.acoboh.query.filter.mongodb.domain;

import io.github.acoboh.query.filter.mongodb.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.mongodb.annotations.QFElement;
import io.github.acoboh.query.filter.mongodb.model.PostBlogDocument;
import io.github.acoboh.query.filter.mongodb.operations.QFOperationEnum;

@QFDefinitionClass(PostBlogDocument.class)
public class PostBlogSpelBeanFilterDef {

	@QFElement(value = "content", isSpPELExpression = true, defaultValues = "@customBean.getBeanData()", defaultOperation = QFOperationEnum.LIKE)
	private String content;

}
