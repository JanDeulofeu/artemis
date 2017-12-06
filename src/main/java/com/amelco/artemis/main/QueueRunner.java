package com.amelco.artemis.main;

import com.amelco.artemis.AtsArtemisConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QueueRunner {

    public static void main(final String[] args)
    {
        SpringApplication.run(AtsArtemisConfiguration.class, args);
    }
}
