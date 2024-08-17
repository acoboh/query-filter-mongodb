package io.github.acoboh.query.filter.example.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import io.github.acoboh.query.filter.example.documents.BasicDocument;

public interface BasicDocumentRepository extends MongoRepository<BasicDocument, String> {

}
