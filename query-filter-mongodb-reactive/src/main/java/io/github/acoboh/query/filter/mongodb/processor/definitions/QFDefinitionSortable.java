package io.github.acoboh.query.filter.mongodb.processor.definitions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.github.acoboh.query.filter.mongodb.annotations.QFBlockParsing;
import io.github.acoboh.query.filter.mongodb.annotations.QFSortable;
import io.github.acoboh.query.filter.mongodb.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.mongodb.processor.QFPath;

/**
 * Definition of QFSortable annotations
 */
public class QFDefinitionSortable extends QFAbstractDefinition implements IDefinitionSortable {

	private final List<List<QFPath>> paths = new ArrayList<>(1);
	private final String pathMappingName;

	QFDefinitionSortable(Field filterfield, Class<?> filterClass, Class<?> entityClass, QFBlockParsing blockParsing,
			QFSortable sortableAnnotation) throws QueryFilterDefinitionException {
		super(filterfield, filterClass, entityClass, blockParsing);

		var processor = new FieldClassProcessor(filterClass, sortableAnnotation.value());
		paths.add(processor.getPaths());
		pathMappingName = paths.get(0).stream().map(QFPath::getMappingName).collect(Collectors.joining("."));
	}

	@Override
	public List<List<QFPath>> getPaths() {
		return paths;
	}

	@Override
	public boolean isSortable() {
		return true;
	}

	@Override
	public String getFirstPathMappingName() {
		return pathMappingName;
	}

}
