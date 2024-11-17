package io.github.acoboh.query.filter.mongodb.config;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.Assert;

import io.github.acoboh.query.filter.mongodb.annotations.EnableQueryFilter;
import io.github.acoboh.query.filter.mongodb.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.mongodb.exceptions.QueryFilterException;
import io.github.acoboh.query.filter.mongodb.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.mongodb.processor.QFProcessor;

/**
 * Query filter bean factory post processor for QueryFilter custom beans
 *
 * @author Adrián Cobo
 * 
 */
@Configuration(proxyBeanMethods = false)
public class QFBeanFactoryPostProcessor
		implements ApplicationContextAware, BeanDefinitionRegistryPostProcessor, Ordered {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFBeanFactoryPostProcessor.class);

	private ApplicationContext applicationContext;
	private ApplicationContextAwareSupport applicationContextAwareSupport;

	/** {@inheritDoc} */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		Assert.notNull(applicationContext, "ApplicationContext cannot be null");
		this.applicationContext = applicationContext;

		this.applicationContextAwareSupport = applicationContext.getBean(ApplicationContextAwareSupport.class);
	}

	private static Set<Class<?>> getClassAnnotated(List<String> packages) {

		Assert.notNull(packages, "packages must not be null");

		Set<Class<?>> classSet = new HashSet<>();

		for (String pack : packages) {
			LOGGER.debug("Checking package with time annotated {}", pack);
			if (pack.startsWith("$")) {
				LOGGER.debug("Ignoring variable package, {}", pack);
				continue;
			}

			final String packRName = prefixPattern(pack);
			final String packBIName = prefixPattern("BOOT-INF.classes." + pack);

			LOGGER.trace("Package regex {} and {}", packRName, packBIName);

			Reflections reflect = new Reflections(new ConfigurationBuilder().forPackages(pack).filterInputsBy(p -> {
				boolean matches = p.matches(packRName) || p.matches(packBIName);
				LOGGER.trace("Pack {} matches {}", p, matches);
				return matches;
			}));

			Set<Class<?>> classFound = reflect.getTypesAnnotatedWith(QFDefinitionClass.class);

			LOGGER.info("Found {} classes on package {}", classFound.size(), pack);

			if (LOGGER.isDebugEnabled()) {
				classFound.forEach(e -> LOGGER.debug("Adding class {} with QueryFilterClass Annotation", e));
			}

			classSet.addAll(classFound);

		}

		return classSet;
	}

	private static String prefixPattern(String fqn) {
		if (!fqn.endsWith("."))
			fqn += ".";
		return fqn.replace(".", "\\.").replace("$", "\\$") + ".*";
	}

	private <T extends Annotation> List<String> getBeansWithAnnotation(Class<T> annotation, boolean repeatable,
			SupplierPackages<T> supplier) {

		List<String> packages = new ArrayList<>();

		applicationContext.getBeansWithAnnotation(annotation).forEach((name, instance) -> {

			if (!repeatable) {
				T scan = AnnotatedElementUtils.findMergedAnnotation(instance.getClass(), annotation);
				if (scan != null) {
					packages.addAll(supplier.getPackages(scan, instance));
				}
			} else {
				Set<T> scans = AnnotatedElementUtils.findMergedRepeatableAnnotations(instance.getClass(), annotation);

				for (T scan : scans) {
					packages.addAll(supplier.getPackages(scan, instance));
				}
			}

		});

		LOGGER.debug("Getting beans with annotation {}: {}", annotation, packages);

		return packages;

	}

	private QFProcessor<?, ?> registerQueryFilterClass(Class<?> cl, BeanDefinitionRegistry beanFactory)
			throws QueryFilterDefinitionException {

		String beanName = cl.getName() + "queryFilterBean";

		QFDefinitionClass annotationClass = cl.getAnnotation(QFDefinitionClass.class);
		if (annotationClass == null) {
			LOGGER.warn("The class {} missing annotation QueryFilterClass", cl);
			return null;
		}

		ResolvableType resolvableType = ResolvableType.forClassWithGenerics(QFProcessor.class, cl,
				annotationClass.value());
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setTargetType(resolvableType);
		beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
		beanDefinition.setAutowireCandidate(true);

		LOGGER.trace("Registering bean definition for AOT");
		ConstructorArgumentValues argValues = new ConstructorArgumentValues();
		argValues.addGenericArgumentValue(cl);
		argValues.addGenericArgumentValue(annotationClass.value());
		argValues.addGenericArgumentValue(new RuntimeBeanReference("applicationContextAwareSupport"));

		beanDefinition.setConstructorArgumentValues(argValues);
		beanDefinition.setBeanClass(QFProcessor.class);

		DefaultListableBeanFactory bf = (DefaultListableBeanFactory) beanFactory;

		try {
			bf.registerBeanDefinition(beanName, beanDefinition);
			QFProcessor<?, ?> ret = new QFProcessor<>(cl, annotationClass.value(), applicationContextAwareSupport);
			bf.registerSingleton(beanName, ret);
			return ret;
		} catch (QueryFilterException e) {
			LOGGER.error("Error registering bean query filter of class {}", cl);
			throw e;
		}

	}

	/** {@inheritDoc} */
	@Override
	public int getOrder() {
		return 0;
	}

	@FunctionalInterface
	interface SupplierPackages<T extends Annotation> {
		List<String> getPackages(T annotation, Object instance);
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		Assert.notNull(registry, "beanFactory cannot be null");

		LOGGER.info("Configure all query filter definition classes...");

		List<String> packagesToAnalyze = getBeansWithAnnotation(EnableQueryFilter.class, false, (scan, instance) -> {
			List<String> scanPackages = new ArrayList<>();
			scanPackages.addAll(Arrays.asList(scan.basePackages()));
			scanPackages.addAll(Stream.of(scan.basePackageClasses()).map(e -> e.getPackage().getName()).toList());
			return scanPackages;
		});

		if (packagesToAnalyze.isEmpty()) {
			LOGGER.debug("Trying get component scan beans to search for QueryFilter classes...");
			packagesToAnalyze = getBeansWithAnnotation(ComponentScan.class, true, (scan, instance) -> {
				List<String> scanPackages = new ArrayList<>();
				scanPackages.addAll(Arrays.asList(scan.basePackages()));
				scanPackages.addAll(Stream.of(scan.basePackageClasses()).map(e -> e.getPackage().getName()).toList());
				return scanPackages;

			});
		}

		if (packagesToAnalyze.isEmpty()) {
			LOGGER.debug("Trying get SpringBootApplication beans to search for QueryFilter classes...");
			packagesToAnalyze = getBeansWithAnnotation(SpringBootApplication.class, false,
					(scan, instance) -> List.of(instance.getClass().toString()));

		}

		Set<Class<?>> classSet = getClassAnnotated(packagesToAnalyze);

		for (Class<?> cl : classSet) {
			try {
				registerQueryFilterClass(cl, registry);
			} catch (QueryFilterDefinitionException e) {
				throw new BeanCreationException("Error creating bean query filter", e);
			}
		}

	}

}
