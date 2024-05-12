package io.github.acoboh.query.filter.mongodb.model;

import java.util.Objects;

import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class TextDocument {

	@TextIndexed
	private String name;

	@TextIndexed
	private String surname;

	public TextDocument() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, surname);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TextDocument other = (TextDocument) obj;
		return Objects.equals(name, other.name) && Objects.equals(surname, other.surname);
	}

}
