package io.github.acoboh.query.filter.mongodb.domain;

import io.github.acoboh.query.filter.mongodb.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.mongodb.annotations.QFElement;
import io.github.acoboh.query.filter.mongodb.annotations.QFProjection;
import io.github.acoboh.query.filter.mongodb.dto.PostBlogSimplifiedDTO;
import io.github.acoboh.query.filter.mongodb.model.PostBlogDocument;

@QFDefinitionClass(PostBlogDocument.class)
@QFProjection(PostBlogSimplifiedDTO.class)
public class PostBlogDocumentFilterDef {

	@QFElement("id")
	private String id;

	@QFElement("title")
	private String title;

	@QFElement("content")
	private String content;

	@QFElement("type")
	private String type;

	@QFElement("date")
	private String date;

	@QFElement("comments.stars")
	private int commentsStars;

	@QFElement("tags")
	private String tags;

}
