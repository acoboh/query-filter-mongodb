package io.github.acoboh.query.filter.example.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import io.github.acoboh.query.filter.example.documents.BasicDocument;
import io.github.acoboh.query.filter.example.domain.BasicDocumentDTO;

@Mapper(componentModel = "spring")
public interface BasicDocumentMapper {

	// To DTO methods
	BasicDocumentDTO toDTO(BasicDocument document);

	// To Model methods
	BasicDocument toModel(BasicDocumentDTO dto);

	// Update model methods
	BasicDocument updateModel(@MappingTarget BasicDocument post, BasicDocumentDTO dto);

}
