package io.github.acoboh.query.filter.mongodb.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import io.github.acoboh.query.filter.mongodb.model.PostBlogDocument;

public interface PostBlogDocumentRepository extends MongoRepository<PostBlogDocument, String> {

}
