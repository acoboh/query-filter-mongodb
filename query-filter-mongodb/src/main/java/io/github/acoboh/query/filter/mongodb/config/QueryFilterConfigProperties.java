package io.github.acoboh.query.filter.mongodb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for QueryFilter
 */
@Configuration
@ConfigurationProperties(prefix = "query.filter.post-processor")
public class QueryFilterConfigProperties {

	private boolean enabled = true;

	/**
	 * Check if the QueryFilter QFBeanFactoryPostProcessor is enabled
	 * 
	 * @return true if enabled, false otherwise
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Set the QueryFilter QFBeanFactoryPostProcessor enabled
	 * 
	 * @param enabled true to enable, false to disable
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
