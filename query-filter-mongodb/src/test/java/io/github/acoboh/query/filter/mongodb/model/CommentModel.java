package io.github.acoboh.query.filter.mongodb.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class CommentModel {

	private int stars;

	private int likes;

	private String comment;

	private LocalDateTime date;

	public CommentModel() {
		// Empty default constructor
	}

	public int getStars() {
		return stars;
	}

	public void setStars(int stars) {
		this.stars = stars;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public int getLikes() {
		return likes;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}

	@Override
	public int hashCode() {
		return Objects.hash(comment, date, likes, stars);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommentModel other = (CommentModel) obj;
		return Objects.equals(comment, other.comment) && Objects.equals(date, other.date) && likes == other.likes
				&& stars == other.stars;
	}

}
