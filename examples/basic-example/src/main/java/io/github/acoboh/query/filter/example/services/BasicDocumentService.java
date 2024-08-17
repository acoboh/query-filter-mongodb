package io.github.acoboh.query.filter.example.services;

import org.springframework.data.domain.Page;

import io.github.acoboh.query.filter.example.documents.BasicDocument;
import io.github.acoboh.query.filter.example.domain.BasicDocumentDTO;
import io.github.acoboh.query.filter.mongodb.processor.QueryFilter;

public interface BasicDocumentService {

	public Page<BasicDocumentDTO> getPosts(QueryFilter<BasicDocument> filter, int page, int size);

	public String createPost(BasicDocumentDTO post);

	public BasicDocumentDTO getPost(String uuid);

	public void updatePost(String uuid, BasicDocumentDTO post);

	public void deletePost(String uuid);
}
