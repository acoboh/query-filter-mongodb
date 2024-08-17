package io.github.acoboh.query.filter.example.filterdef;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import io.github.acoboh.query.filter.example.documents.BasicDocument;
import io.github.acoboh.query.filter.mongodb.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.mongodb.annotations.QFElement;
import io.github.acoboh.query.filter.mongodb.annotations.QFSortable;
import io.github.acoboh.query.filter.mongodb.annotations.QFText;

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

	@QFText
	private String textIndex;
}
