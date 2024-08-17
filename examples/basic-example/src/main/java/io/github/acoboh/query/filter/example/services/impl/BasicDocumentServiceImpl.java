package io.github.acoboh.query.filter.example.services.impl;

import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import io.github.acoboh.query.filter.example.documents.BasicDocument;
import io.github.acoboh.query.filter.example.domain.BasicDocumentDTO;
import io.github.acoboh.query.filter.example.exceptions.ResourceNotFoundException;
import io.github.acoboh.query.filter.example.mapper.BasicDocumentMapper;
import io.github.acoboh.query.filter.example.repositories.BasicDocumentRepository;
import io.github.acoboh.query.filter.example.services.BasicDocumentService;
import io.github.acoboh.query.filter.mongodb.processor.QueryFilter;

@Service
class BasicDocumentServiceImpl implements BasicDocumentService {

	private static final Logger log = LoggerFactory.getLogger(BasicDocumentServiceImpl.class);
	private static final BasicDocumentMapper mapper = Mappers.getMapper(BasicDocumentMapper.class);

	private final BasicDocumentRepository repository;

	BasicDocumentServiceImpl(BasicDocumentRepository repository) {
		this.repository = repository;
	}

	@Override
	public Page<BasicDocumentDTO> getPosts(QueryFilter<BasicDocument> filter, int page, int size) {
		log.debug("Getting posts page {} size {} filter {}", page, size, filter);
		return filter.executeFindQuery(PageRequest.of(page, size)).map(mapper::toDTO);
	}

	@Override
	public BasicDocumentDTO getPost(String uuid) {

		var doc = repository.findById(uuid)
				.orElseThrow(() -> new ResourceNotFoundException("Doc not found with id " + uuid));

		return mapper.toDTO(doc);
	}

	@Override
	public String createPost(BasicDocumentDTO post) {
		var doc = mapper.toModel(post);
		return repository.save(doc).getId();
	}

	@Override
	public void updatePost(String uuid, BasicDocumentDTO dto) {
		var doc = repository.findById(uuid)
				.orElseThrow(() -> new ResourceNotFoundException("Doc not found with id " + uuid));

		doc = mapper.updateModel(doc, dto);
		repository.save(doc);

	}

	@Override
	public void deletePost(String uuid) {
		var doc = repository.findById(uuid)
				.orElseThrow(() -> new ResourceNotFoundException("Doc not found with id " + uuid));

		repository.delete(doc);

	}

}
