package io.github.acoboh.query.filter.mongodb.processor;

import java.util.regex.Pattern;

/**
 * Enumeration for all parsing standards
 *
 * @author Adrián Cobo
 * 
 */
public enum QFParamType {

	/**
	 * RHS Colon standard
	 * <p>
	 * Example:
	 * <p>
	 * <code>
	 * authorName=eq:Adrian
	 * </code>
	 *
	 */
	RHS_COLON("(sort=([^&]+))|(([^&=]+)=(?:([a-zA-Z]+):)?((?:[^&]|&[^a-zA-Z0-9])*[^&]*))", // Pattern Regex
			"RHS Colon"),

	/**
	 * LHS Brackets standard
	 * <p>
	 * Example:
	 * <p>
	 * <code>
	 * authorName[eq]=Adrian
	 * </code>
	 *
	 */
	LHS_BRACKETS("(sort=([^&]+))|(([^&=]+)\\[([a-zA-Z]+)\\]=((?:[^&]|&[^a-zA-Z0-9])*[^&]*))", // Pattern Regex
			"LHS Brackets"); // Name

	private final Pattern pattern;

	private final String beatifulName;

	QFParamType(String regexPattern, String beatifulName) {

		this.pattern = Pattern.compile(regexPattern);
		this.beatifulName = beatifulName;
	}

	/**
	 * Get pattern for parsing
	 *
	 * @return pattern
	 */
	public Pattern getPattern() {
		return pattern;
	}

	/**
	 * Beautiful name for documentation
	 *
	 * @return beautiful name
	 */
	public String getBeatifulName() {
		return beatifulName;
	}

}
