package io.github.acoboh.query.filter.mongodb.spel;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ SecuritySpelResolverContext.class, SpelResolverContextBasic.class })
public class SpelConfig {
}
