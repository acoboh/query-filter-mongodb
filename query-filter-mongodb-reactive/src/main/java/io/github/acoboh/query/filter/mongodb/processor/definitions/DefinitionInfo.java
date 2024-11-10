package io.github.acoboh.query.filter.mongodb.processor.definitions;

import java.util.List;

import io.github.acoboh.query.filter.mongodb.processor.QFPath;

record DefinitionInfo(List<List<QFPath>> paths, List<Class<?>> finalClasses, List<String> mappingNames) {

}
