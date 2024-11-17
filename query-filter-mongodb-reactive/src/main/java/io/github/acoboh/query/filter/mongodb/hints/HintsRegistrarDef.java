package io.github.acoboh.query.filter.mongodb.hints;

import java.util.Set;

import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.annotation.AnnotatedElementUtils;

import io.github.acoboh.query.filter.mongodb.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.mongodb.annotations.QFProjection;

/**
 * Class that allows support for Spring Native projects
 * 
 * @see <a href="https://github.com/acoboh/query-filter-mongodb/wiki">Official
 *      Wiki</a>
 */
@Configuration
@ImportRuntimeHints(HintsRegistrarDef.QFRuntimeHintsRegistrar.class)
public class HintsRegistrarDef {

	HintsRegistrarDef() {
		// Empty default constructor
	}

	static class QFRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

		private static final Logger LOGGER = LoggerFactory.getLogger(QFRuntimeHintsRegistrar.class);

		private static final MemberCategory[] memberCategories = { MemberCategory.PUBLIC_FIELDS,
				MemberCategory.DECLARED_FIELDS, MemberCategory.PUBLIC_CLASSES, MemberCategory.DECLARED_CLASSES };

		@Override
		public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

			var rh = hints.reflection();

			Reflections reflect = new Reflections(
					new ConfigurationBuilder().setUrls(ClasspathHelper.forJavaClassPath()));

			Set<Class<?>> annotatedClasses = reflect.getTypesAnnotatedWith(QFDefinitionClass.class);

			LOGGER.info("Found {} classes annotated with QFDefinitionClass", annotatedClasses.size());
			for (Class<?> annotatedClass : annotatedClasses) {
				LOGGER.info("Processing class {}", annotatedClass.getName());
				rh.registerType(annotatedClass, memberCategories);

				Set<QFProjection> qfProjection = AnnotatedElementUtils.findMergedRepeatableAnnotations(annotatedClass,
						QFProjection.class);

				for (QFProjection projection : qfProjection) {
					LOGGER.info("Processing projection {}", projection.value());
					rh.registerType(projection.value(), memberCategories);
				}

			}

			hints.resources().registerPattern("queryfilter-messages/messages_*.properties");

		}

	}

}
