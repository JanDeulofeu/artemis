package com.amelco.artemis;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Profile("remote")
@PropertySource(value = "classpath:configuration/artemis-remote.properties")
@Configuration
public class RemoteProfileConfiguration {
}
