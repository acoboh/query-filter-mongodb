package io.github.acoboh.query.filter.mongodb.hints;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(ReactiveHintsRegistrar.ReactiveHintsRuntimeRegistrar.class)
public class ReactiveHintsRegistrar {

	static class ReactiveHintsRuntimeRegistrar implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
			var rh = hints.reflection();

			// TODO Add hints for reactive classes
//			rh.registerType(jakarta.servlet.http.HttpServletRequest.class, MemberCategory.values());
//			hints.proxies().registerJdkProxy(jakarta.servlet.http.HttpServletRequest.class);
		}
	}
}
