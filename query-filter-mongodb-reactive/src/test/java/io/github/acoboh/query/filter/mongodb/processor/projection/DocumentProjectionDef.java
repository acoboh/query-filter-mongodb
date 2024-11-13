package io.github.acoboh.query.filter.mongodb.processor.projection;

public class DocumentProjectionDef {

	private String id;

	private String name;

	private Inner1 inner1;

	private TestENUM testENUM;

	public static class Inner1 {

		private String id;

		private String name1;

		private Inner2 inner2;

		public static class Inner2 {

			private String id;

			private String name2;

		}

	}

	public enum TestENUM {
		ENUM1, ENUM2
	}

}
