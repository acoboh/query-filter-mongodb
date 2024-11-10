[![License](https://img.shields.io/github/license/acoboh/query-filter-mongodb.svg)](https://raw.githubusercontent.com/acoboh/query-filter-mongodb/main/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.acoboh/query-filter-mongodb.svg)](https://central.sonatype.com/artifact/io.github.acoboh/query-filter-mongodb)
[![javadoc](https://javadoc.io/badge2/io.github.acoboh/query-filter-mongodb/javadoc.svg)](https://javadoc.io/doc/io.github.acoboh/query-filter-mongodb)
[![CodeQL](https://github.com/acoboh/query-filter-mongodb/actions/workflows/codeql.yml/badge.svg)](https://github.com/acoboh/query-filter-mongodb/actions/workflows/codeql.yml)
[![Maven Publish](https://github.com/acoboh/query-filter-mongodb/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/acoboh/query-filter-mongodb/actions/workflows/maven-publish.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=acoboh_query-filter-mongodb&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=acoboh_query-filter-mongodb)

## Introduction

The QueryFilterMongoDB Library adds the possibility of creating custom filters with RHS Colon and LHS Brackets with
Spring Data MongoDB easily. This library is useful for allowing the user to obtain data according to their requirements
in an easy way for the programmer. With just a few small configuration classes, users will have the ability to create
filters with infinite possibilities. Compatible with MVC and WebFlux

Example:

```
author=eq:Acobo&project=starts:Query Filter&versions=gt:2
```

Will be traduced automatically to a MongoDB

```
{
  "author": "Acobo",
  "project": { "$regularExpression" : { "pattern" : "^Query Filter", "options" : "i"}}
  "versions": { $gt : 2 }
}
```

## Features

* Easy installation on Spring Boot 3.X
* Fully compatible with Spring MVC with Spring Data MongoDB and Spring WebFlux and Spring Data MongoDB Reactive
* Create filter specifications based on Entity Models
* Search with *Text Search*
* Convert RHS Colon or LHS Brackets filter into a MongoDB Query.
* Manually add fields and block operations on each new filter field.
* Create custom predicates for each query filter.
* Expose filter documentation on endpoints.
* Create extended OpenAPI documentation with QueryFilter specification.

## Installation

You can install the library by adding the following dependency to your project's `pom.xml` file:

```xml

<dependencies>
    <!-- Spring MVC -->
    <dependency>
        <groupId>io.github.acoboh</groupId>
        <artifactId>query-filter-mongodb</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </dependency>

    <!-- Or Reactive version -->
    <dependency>
        <groupId>io.github.acoboh</groupId>
        <artifactId>query-filter-mongodb-reactive</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

## Getting Started

First of all, you need an entity model.

```java

@Document
public class PostBlog {

    public enum PostType {
        VIDEO, TEXT
    }

    @Id
    private Long id;

    private String author;

    private String text;

    private double avgNote;

    private int likes;

    private LocalDateTime createDate;

    private Timestamp lastTimestamp;

    private boolean published;

    private PostType postType;

}
```

Once you import the library, creating new filters becomes remarkably easy. To create your first filter, you only need to
specify it within a new class.

```java

@QFDefinitionClass(PostBlog.class) // Select the entity related model
public class PostFilterDef {

    @QFElement("author")
    private String author;

    @QFElement("likes")
    private int likes;

    @QFElement("avgNote")
    private double avgNote;

    @QFDate
    @QFElement("createDate")
    private LocalDateTime createDate;

    @QFSortable("lastTimestamp")
    private Timestamp lastTimestamp;

    @QFElement("postType")
    private String postType;

    @QFElement("published")
    @QFBlockParsing
    private boolean published;

}
```

With the class annotation @QFDefinitionClass, you specify the entity model on which you want to apply the filters.
Additionally, you have other annotations to indicate each of the available fields for filtering:

- `@QFElement`: Specifies the field name on which filtering operations can be performed. The field name indicates the
  text to be used on the RHS or LHS of the filter. _(The name used on RHS or LHS can be overridden with the annotation
  properties.)_
- `@QFDate`: Specifies that the selected field is a date. You can select the format of the text to be parsed. _(The
  default format is **yyyy-MM-dd'T'HH:mm:ss'Z'** and the timezone is **UTC**)_
- `@QFSortable`: Specifies that the field is only sortable and cannot be filtered. This is useful when you only want to
  enable sorting by a field but do not want it to be filterable. _(If you already used the `QFElement` annotation, the
  field will be sortable by default, and you do not need to use this annotation)_
- `@QFBlockParsing`: Specifies that this field is blocked during the stage of parsing from the *String* filter to the
  *QueryFilter* object. If the field is present in the *String*, an exception will be thrown. This is useful when you
  need to ensure that some fields cannot be filtered by a user but need to be filtered manually in the code. _(For
  example, usernames, roles, and other sensitive data.)_

Once you have created that class, there are only two more steps.

The first step is to enable the **Query Filter** bean processors. You can do that with the following annotation on the
main class:

```java
@EnableQueryFilter(basePackageClasses = PostFilterDef.class)
```

> **_NOTE_**: The `basePackageClasses` and `basePackages` are not required by default

Now you can use the filter on the controller easily:

```java

@RestController
@RequestMapping("/posts")
public class PostRestController {

    // MVC Version
    @GetMapping
    public List<PostBlog> getPosts(
            @RequestParam(required = false, defaultValue = "") @QFParam(PostFilterDef.class) QueryFilter<PostBlog> filter) {
        return filter.executeFindQuery();
    }

    public Flux<PostBlog> getPostFlux(
            @RequestParam(required = false, defaultValue = "") @QFParam(PostFilterDef.class) QueryFilter<PostBlog> filter) {
        return filter.executeFindQuery();
    }
}
```

With the `@QFParam` annotation, you can select the filter to be used. By using the `QueryFilter<PostBlog>` object,
you can automatically create the final query filter object. Once you have the `filter` object, you have the flexibility
to perform operations directly with it or use it directly on the repository.

The `@QFParam` annotation allows you to define a parameter in your controller method, which will be used to receive the
filter provided by the client. The Query Filter library will handle the conversion of the client's filter into the
`QueryFilter<PostBlog>` object, which can then be used for querying your data.

You can also perform paginated queries with the following methods:

```java

@RestController
@RequestMapping("/posts")
public class PostRestController {

    // Spring MVC
    @GetMapping
    public Page<PostBlog> getPosts(
            @RequestParam(required = false, defaultValue = "") @QFParam(PostFilterDef.class) QueryFilter<PostBlog> filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return filter.executeFindQuery(PageRequest.of(page, size));
    }

    // Spring Webflux
    @GetMapping
    public Mono<Page<PostBlog>> getPosts(
            @RequestParam(required = false, defaultValue = "") @QFParam(PostFilterDef.class) QueryFilter<PostBlog> filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return filter.executeFindQuery(PageRequest.of(page, size));
    }
}
```

## OpenAPI Documentation

If you use Swagger-UI with OpenAPI 3 documentation, you can easily expose an automatic generated documentation of the
filter.

![Image from OpenAPI example](/doc/resources/swagger-example-posts.png)

You need to import the following library:

```xml

<dependency>
    <groupId>io.github.acoboh</groupId>
    <artifactId>query-filter-mongodb-openapi</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Now automatically, all the controllers will be analyzed by the library and will override the OpenAPI documentation.

## How to write *String Filters*

Once you have your service with the **Query Filter** enabled, you can start using **RHS Colon** and **LHS Brackets**
standards to filter data effectively.

Following the OpenAPI documentation, you have several options to filter on each field.

### Allowed operations

- **eq**: Equals
- **ne**: Not equals
- **gt**: Greater than
- **gte**: Greater or equal than
- **lt**: Less than
- **lte**: Less or equal than
- **like**: Like _(for string operations)_
- **starts**: Starts with _(for string operations)_
- **ends**: Ends with _(for string opertions)_
- **in**: IN (in operator)
- **nin**: Not IN (not it operator)
- **null**: Is null (is null or is not null. The value must be `false` or `true`)

### RHS Colon

The syntax of this standard is the following one:

```log
<field>=<operation>:<value>
```

An example could be:

```log
author=eq:acobo
```

The filter will produce an MongoDB query like:

```mongodb
{ 'author': 'acobo' }
```

You can use other operations. Examples:

- `avgNote=gte:5`
- `postType=ne:VIDEO`

> **_TIP_**: With RHS_COLON you can filter with `author=acobo` and will do the `eq` operation automatically

### LHS Brackets

The syntax of this standard is the following one:

```log
<field>[<operation>]=<value>
```

An example could be:

```log
author[eq]=acobo
```

The filter will produce an SQL query like:

```mongodb
{ 'author': 'acobo' }
```

You can use other operations. Examples:

- `avgNote[gte]=5`
- `postType[ne]=VIDEO`

### Sort results

If you want to sort, you can do it with the following syntax:

```log
sort=<direction><field>
```

The direction can be:

- **+**: For ascending direction
- **-**: For descending direction

An example could be:

```log
sort=+author
```

### Concatenate multiple filters

If you want to concatenate multiple filters, you can easily do it with the `&` operator.

And example with **RHS Colon** could be:

```log
author=eq:acobo&avgNote=gte:5&sort=-avgNote
```

The same example with **LHS Brakets**:

```log
author[eq]=acobo&avgNote[gte]=5&sort=-avgNote
```

You can concatenate multiple sort operations. If you do that, the order is important

```log
sort=-avgNote,+likes
```

If you change the order:

```log
sort=+likes,-avgNote
```

## SpEL Expressions

You can create SpEL Expressions on the `@QFElement` using other fields or beans.

```java
@QFElement(value = "author", expression = "@authenticated.getUsername()")
```

```java

@Bean("authenticated")
public CustomBean customBean() {
    return new CustomAuthBean() {
        @Override
        public String getUsername() {
            return "acobo"; // Implement custom logic
        }
    };
}
```

Automatically, the `author` field will be filled with the authenticated user returned by the `authenticated` bean.

## MORE DOCUMENTATION

<!-- To see full documentation, check the [Wiki section](https://github.com/acoboh/query-filter-mongodb/wiki) -->
More documentation coming soon
