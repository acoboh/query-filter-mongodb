package io.github.acoboh.query.filter.example.services;

import org.springframework.data.domain.Page;

import io.github.acoboh.query.filter.example.documents.BasicDocument;
import io.github.acoboh.query.filter.example.domain.BasicDocumentDTO;
import io.github.acoboh.query.filter.mongodb.processor.QueryFilter;
import reactor.core.publisher.Mono;

public interface BasicDocumentService {

	public Mono<Page<BasicDocumentDTO>> getPosts(QueryFilter<BasicDocument> filter, int page, int size);

	public Mono<String> createPost(BasicDocumentDTO post);

	public Mono<BasicDocumentDTO> getPost(String uuid);

	public Mono<Void> updatePost(String uuid, BasicDocumentDTO post);

	public Mono<Void> deletePost(String uuid);
}
