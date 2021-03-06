package com.amelco.artemis.queue;

import com.amelco.artemis.ArtemisConfiguration;
import com.amelco.artemis.model.MessageDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ArtemisConfiguration.class})
public class QueuesTest {

    @Autowired
    private QueueConsumer queueConsumer;

    @Autowired
    private QueueProducer queueProducer;

    @Test
    public void validateMessageIsConsumedAfterBeenSend() throws InterruptedException {

        final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        final int numberThreads = 10;
        final int numberMessages = 500;

        for (int x = 0; x < numberThreads; x++) {
            executor.submit(() ->
            {
                for (int i = 0; i < numberMessages; i++) {
                    try {
                        queueProducer.send(new MessageDto("test", new Date()));
                    } catch (final Exception e) {
                        System.out.printf("ERROR: " + e.getMessage());
                    }
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);

        assertThat(queueConsumer.getReceivedMessagesCount()).isEqualTo((numberThreads * numberMessages) -1 );
    }
}