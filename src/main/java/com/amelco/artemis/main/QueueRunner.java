package com.amelco.artemis.main;

import com.amelco.artemis.ArtemisConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QueueRunner {

    public static void main(final String[] args)
    {
        SpringApplication.run(ArtemisConfiguration.class, args);
    }

}
