package io.github.acoboh.query.filter.mongodb.repositories;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

import io.github.acoboh.query.filter.mongodb.model.BasicDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface BasicDocumentRepository extends ReactiveMongoRepository<BasicDocument, String> {

}
