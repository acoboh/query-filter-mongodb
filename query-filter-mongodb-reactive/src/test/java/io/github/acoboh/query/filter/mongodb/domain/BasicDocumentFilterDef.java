package io.github.acoboh.query.filter.mongodb.domain;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import io.github.acoboh.query.filter.mongodb.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.mongodb.annotations.QFElement;
import io.github.acoboh.query.filter.mongodb.annotations.QFSortable;
import io.github.acoboh.query.filter.mongodb.model.BasicDocument;

@QFDefinitionClass(BasicDocument.class)
public class BasicDocumentFilterDef {

	@QFElement("id")
	private String id;

	@QFElement("name")
	private String name;

	@QFElement("dateTime")
	private LocalDateTime dateTime;

	@QFSortable("timestamp")
	private Timestamp timestamp;

	@QFElement("customName")
	private String customName;

	@QFElement("baseName")
	private String baseName;
}
