package com.amelco.artemis.queue;

import com.amelco.artemis.model.MessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class QueueProducer {

    private static final Logger LOG = LoggerFactory.getLogger(QueueProducer.class);

    @Value("${artemis.queue.destination}")
    private String destination;

    @Autowired
    private JmsTemplate jmsTemplate;


    public void send(final MessageDto message) {
        LOG.debug("Sending message [{}] to Destination [{}]", message, destination);
        jmsTemplate.convertAndSend(destination, message);
    }
}