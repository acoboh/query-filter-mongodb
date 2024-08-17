package io.github.acoboh.query.filter.example.controllers;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.acoboh.query.filter.example.documents.BasicDocument;
import io.github.acoboh.query.filter.example.domain.BasicDocumentDTO;
import io.github.acoboh.query.filter.example.filterdef.BasicDocumentFilterDef;
import io.github.acoboh.query.filter.example.services.BasicDocumentService;
import io.github.acoboh.query.filter.mongodb.annotations.QFParam;
import io.github.acoboh.query.filter.mongodb.processor.QueryFilter;

@RestController
@RequestMapping("/posts")
public class BasicDocumentController {

	private final BasicDocumentService service;

	BasicDocumentController(BasicDocumentService service) {
		this.service = service;
	}

	@GetMapping
	public Page<BasicDocumentDTO> getPosts(
			@QFParam(BasicDocumentFilterDef.class) @RequestParam(required = false, defaultValue = "", name = "filter") QueryFilter<BasicDocument> filter,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		return service.getPosts(filter, page, size);
	}

	@PostMapping
	public String createPost(@RequestBody BasicDocumentDTO post) {
		return service.createPost(post);
	}

	@GetMapping("/{uuid}")
	public BasicDocumentDTO getPost(@PathVariable String uuid) {
		return service.getPost(uuid);
	}

	@PostMapping("/{uuid}")
	public void updatePost(@PathVariable String uuid, @RequestBody BasicDocumentDTO post) {
		service.updatePost(uuid, post);
	}

	@DeleteMapping("/{uuid}")
	public void deletePost(@PathVariable String uuid) {
		service.deletePost(uuid);
	}
}
