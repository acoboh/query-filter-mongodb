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
	public List<List<QFPath>> getPaths();

	/**
	 * Get filter name
	 * 
	 * @return filter name
	 */
	public String getFilterName();

	/**
	 * Get if the field is sortable
	 * 
	 * @return true if sortable, false otherwise
	 */
	public boolean isSortable();

	/**
	 * Get first path mapping name
	 * 
	 * @return the first mapping name
	 */
	public String getFirstPathMappingName();
}
