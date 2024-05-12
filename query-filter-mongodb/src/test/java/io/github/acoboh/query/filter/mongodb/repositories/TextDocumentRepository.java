package io.github.acoboh.query.filter.mongodb.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import io.github.acoboh.query.filter.mongodb.model.TextDocument;

public interface TextDocumentRepository extends MongoRepository<TextDocument, String> {

}
