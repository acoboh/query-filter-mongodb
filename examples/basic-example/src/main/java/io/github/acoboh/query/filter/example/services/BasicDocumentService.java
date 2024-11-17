package io.github.acoboh.query.filter.example.services;

import org.springframework.data.domain.Page;

import io.github.acoboh.query.filter.example.documents.BasicDocument;
import io.github.acoboh.query.filter.example.domain.BasicDocumentDTO;
import io.github.acoboh.query.filter.mongodb.processor.QueryFilter;

public interface BasicDocumentService {

	Page<BasicDocumentDTO> getPosts(QueryFilter<BasicDocument> filter, int page, int size);

	String createPost(BasicDocumentDTO post);

	BasicDocumentDTO getPost(String uuid);

	void updatePost(String uuid, BasicDocumentDTO post);

	void deletePost(String uuid);
}
