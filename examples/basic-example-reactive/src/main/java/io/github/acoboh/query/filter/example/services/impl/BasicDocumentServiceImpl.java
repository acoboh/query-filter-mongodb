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
import reactor.core.publisher.Mono;

@Service
class BasicDocumentServiceImpl implements BasicDocumentService {

	private static final Logger log = LoggerFactory.getLogger(BasicDocumentServiceImpl.class);
	private static final BasicDocumentMapper mapper = Mappers.getMapper(BasicDocumentMapper.class);

	private final BasicDocumentRepository repository;

	BasicDocumentServiceImpl(BasicDocumentRepository repository) {
		this.repository = repository;
	}

	@Override
	public Mono<Page<BasicDocumentDTO>> getPosts(QueryFilter<BasicDocument> filter, int page, int size) {
		log.debug("Getting posts page {} size {} filter {}", page, size, filter);
		return filter.executeAggregateAndProject(PageRequest.of(page, size), BasicDocumentDTO.class);
	}

	@Override
	public Mono<BasicDocumentDTO> getPost(String uuid) {
		return repository.findById(uuid)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Doc not found with id " + uuid)))
				.map(mapper::toDTO);

	}

	@Override
	public Mono<String> createPost(BasicDocumentDTO post) {
		var doc = mapper.toModel(post);
		return repository.save(doc).map(BasicDocument::getId);
	}

	@Override
	public Mono<Void> updatePost(String uuid, BasicDocumentDTO dto) {
		return repository.findById(uuid)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Doc not found with id " + uuid)))
				.map(e -> mapper.updateModel(e, dto)).flatMap(repository::save).then();
	}

	@Override
	public Mono<Void> deletePost(String uuid) {
		return repository.findById(uuid)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Doc not found with id " + uuid)))
				.flatMap(repository::delete).then();
	}

}
