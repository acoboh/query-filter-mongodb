package io.github.acoboh.query.filter.mongodb.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import io.github.acoboh.query.filter.mongodb.model.BasicDocument;

public interface BasicDocumentRepository extends ReactiveMongoRepository<BasicDocument, String> {

}
