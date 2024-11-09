package io.github.acoboh.query.filter.mongodb.processor.definitions;

import java.util.List;

import io.github.acoboh.query.filter.mongodb.processor.QFPath;

/**
 * Sortable interface
 */
public interface IDefinitionSortable {

	/**
	 * Get the sort paths
	 * 
	 * @return sort paths
	 */
	List<List<QFPath>> getPaths();

	/**
	 * Get filter name
	 * 
	 * @return filter name
	 */
	String getFilterName();

	/**
	 * Get if the field is sortable
	 * 
	 * @return true if sortable, false otherwise
	 */
	boolean isSortable();

	/**
	 * Get first path mapping name
	 * 
	 * @return the first mapping name
	 */
	String getFirstPathMappingName();
}
