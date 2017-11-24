package com.amelco.artemis.queue;


import com.amelco.artemis.model.MessageDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class QueueConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(QueueConsumer.class);

    @Value("${artemis.queue.destination}\"")
    private String destination;

    private AtomicInteger atomicInteger = new AtomicInteger();

    public int getReceivedMessagesCount() {
        return atomicInteger.get();
    }

    @JmsListener(destination = "${artemis.queue.destination}", containerFactory = "jmsListenerContainer" , concurrency = "5-50")
    public void consume(final MessageDto message) {
        LOG.debug("Thread [{}] - Message Number [{}] Consumed message [{}] from Destination [{}]", Thread.currentThread().getId(), atomicInteger.incrementAndGet(), message, destination);
    }
}
