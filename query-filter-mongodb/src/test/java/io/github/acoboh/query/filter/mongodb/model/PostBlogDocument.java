package io.github.acoboh.query.filter.mongodb.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.data.annotation.Id;

public class PostBlogDocument {

	@Id
	private String id;

	private String title;

	private String content;

	private PostBlogType type;

	private LocalDateTime date;

	private List<CommentModel> comments;

	private CommentModel authorComment;

	private String[] tags;

	private int likes;

	public PostBlogDocument() {
		// Empty default constructor
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public PostBlogType getType() {
		return type;
	}

	public void setType(PostBlogType type) {
		this.type = type;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public List<CommentModel> getComments() {
		return comments;
	}

	public void setComments(List<CommentModel> comments) {
		this.comments = comments;
	}

	public CommentModel getAuthorComment() {
		return authorComment;
	}

	public void setAuthorComment(CommentModel authorComment) {
		this.authorComment = authorComment;
	}

	public String[] getTags() {
		return tags;
	}

	public void setTags(String[] tags) {
		this.tags = tags;
	}

	public int getLikes() {
		return likes;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(tags);
		result = prime * result + Objects.hash(authorComment, comments, content, date, id, likes, title, type);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PostBlogDocument other = (PostBlogDocument) obj;
		return Objects.equals(authorComment, other.authorComment) && Objects.equals(comments, other.comments)
				&& Objects.equals(content, other.content) && Objects.equals(date, other.date)
				&& Objects.equals(id, other.id) && likes == other.likes && Arrays.equals(tags, other.tags)
				&& Objects.equals(title, other.title) && type == other.type;
	}

}
