package io.github.acoboh.query.filter.mongodb.model;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
public class BasicDocument {

	@Id
	private String id;

	private String name;

	private LocalDateTime dateTime;

	private Date timestamp;

	@Field("mappingName")
	private String customName;

	@Field
	private String baseName;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getCustomName() {
		return customName;
	}

	public void setCustomName(String customName) {
		this.customName = customName;
	}

	public String getBaseName() {
		return baseName;
	}

	public void setBaseName(String baseName) {
		this.baseName = baseName;
	}

	@Override
	public int hashCode() {
		return Objects.hash(dateTime, id, name, timestamp);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		BasicDocument that = (BasicDocument) o;
		return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(dateTime, that.dateTime)
				&& Objects.equals(timestamp.toInstant(), that.timestamp.toInstant())
				&& Objects.equals(customName, that.customName) && Objects.equals(baseName, that.baseName);
	}

	@Override
	public String toString() {
		return "BasicDocument{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", dateTime=" + dateTime
				+ ", timestamp=" + timestamp + ", customName='" + customName + '\'' + ", baseName='" + baseName + '\''
				+ '}';
	}
}
