package io.github.acoboh.query.filter.example.services;

import org.springframework.data.domain.Page;

import io.github.acoboh.query.filter.example.documents.BasicDocument;
import io.github.acoboh.query.filter.example.domain.BasicDocumentDTO;
import io.github.acoboh.query.filter.mongodb.processor.QueryFilter;
import reactor.core.publisher.Mono;

public interface BasicDocumentService {

	Mono<Page<BasicDocumentDTO>> getPosts(QueryFilter<BasicDocument> filter, int page, int size);

	Mono<String> createPost(BasicDocumentDTO post);

	Mono<BasicDocumentDTO> getPost(String uuid);

	Mono<Void> updatePost(String uuid, BasicDocumentDTO post);

	Mono<Void> deletePost(String uuid);
}
