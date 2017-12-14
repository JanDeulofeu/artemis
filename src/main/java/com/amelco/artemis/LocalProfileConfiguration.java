package com.amelco.artemis;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Profile("local")
@PropertySource(value = "classpath:configuration/artemis-local.properties")
@Configuration
public class LocalProfileConfiguration {
}
