package com.example.banking.interbank.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Properties;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/** The local profile example must give a value to every key that otherwise needs an environment variable. */
class LocalProfileExampleTest {

    private static final Pattern ENV_PLACEHOLDER = Pattern.compile("\\$\\{[A-Z][A-Z0-9_]*}");

    private static Properties load(String name) throws IOException {
        Properties properties = new Properties();
        try (var stream = new ClassPathResource(name).getInputStream()) {
            properties.load(stream);
        }
        return properties;
    }

    @Test
    void should_override_every_key_of_application_properties_that_needs_an_environment_variable() throws IOException {
        Properties main = load("application.properties");
        Properties example = load("application-local.properties.example");

        var missing = new TreeSet<String>();
        main.forEach((key, value) -> {
            if (ENV_PLACEHOLDER.matcher(value.toString()).find() && !example.containsKey(key)) {
                missing.add(key.toString());
            }
        });

        assertThat(missing).isEmpty();
    }

    @Test
    void should_not_reference_classes_or_keys_that_no_longer_exist() throws IOException {
        Properties example = load("application-local.properties.example");

        assertThat(example.values().stream().map(Object::toString))
                .noneMatch(value -> value.contains("com.example.banking.services"));
        assertThat(example.stringPropertyNames()).noneMatch(key -> key.startsWith("spring.redis."))
                .doesNotContain("spring.cloud.stream.bindings.output.destination");
    }
}
