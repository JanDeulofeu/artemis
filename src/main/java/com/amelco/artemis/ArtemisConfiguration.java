package com.amelco.artemis;


import org.apache.activemq.artemis.api.core.DiscoveryGroupConfiguration;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.UDPBroadcastEndpointFactory;
import org.apache.activemq.artemis.api.core.client.loadbalance.RoundRobinConnectionLoadBalancingPolicy;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.JMSFactoryType;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.jms.Session;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan("com.amelco.artemis")
public class ArtemisConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(ArtemisConfiguration.class);

    @Value("${artemis.broker.url}")
    private String brokerUrl;

    @Value("${artemis.broker.port}")
    private Integer brokerPort;

    @Value("${artemis.broker.usr}")
    private String brokerUser;

    @Value("${artemis.broker.pwd}")
    private String brokerPwd;

    @Value("${artemis.queue.destination}")
    private String destination;

    private final Integer POOL_SIZE;
    private final Integer POOL_MAX_SIZE;


    public ArtemisConfiguration() {
        this.POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
        this.POOL_MAX_SIZE = this.POOL_SIZE * 4;
    }

    @Bean(name = "threadPoolExecutor", initMethod = "initialize", destroyMethod = "destroy")
    public ThreadPoolTaskExecutor threadPoolExecutor() {
        final ThreadPoolTaskExecutor thp = new ThreadPoolTaskExecutor();
        thp.setMaxPoolSize(this.POOL_MAX_SIZE);
        thp.setCorePoolSize(this.POOL_SIZE);
        thp.setQueueCapacity(this.POOL_SIZE);
        thp.setWaitForTasksToCompleteOnShutdown(true);
        thp.setAwaitTerminationSeconds(10);
        thp.setThreadNamePrefix("Artemis-" + this.destination + "-consumer");
        thp.setThreadGroupName("Artemis-ThreadPool-");
        return thp;
    }

    @Bean(name = "pooledConnectionFactory", initMethod = "start", destroyMethod = "stop")
    public PooledConnectionFactory pooledConnectionFactory() {

        final PooledConnectionFactory pool = new PooledConnectionFactory();
        pool.setCreateConnectionOnStartup(true);
        pool.setReconnectOnException(true);
        pool.setConnectionFactory(connectionFactory());
        pool.setExpiryTimeout(1500);
        return pool;
    }

    @Bean(name = "jmsTemplate")
    public JmsTemplate jmsTemplate() {
        final JmsTemplate jmsTemplate = new JmsTemplate();

        if (isTopic()) {
            jmsTemplate.setPubSubDomain(true);
        }
        jmsTemplate.setDestinationResolver(new DynamicDestinationResolver());

        jmsTemplate.setExplicitQosEnabled(true);
        jmsTemplate.setTimeToLive(javax.jms.Message.DEFAULT_TIME_TO_LIVE);
        jmsTemplate.setDeliveryMode(javax.jms.DeliveryMode.PERSISTENT);
        jmsTemplate.setPriority(javax.jms.Message.DEFAULT_PRIORITY);

        jmsTemplate.setConnectionFactory(pooledConnectionFactory());
        return jmsTemplate;
    }


    @Bean(name = "jmsListenerContainer")
    public DefaultJmsListenerContainerFactory defaultJmsListenerContainerFactory() {
        final DefaultJmsListenerContainerFactory msgLisContainer = new DefaultJmsListenerContainerFactory();

        if (isTopic()) {
            msgLisContainer.setPubSubDomain(true);
        }
        msgLisContainer.setDestinationResolver(new DynamicDestinationResolver());
        msgLisContainer.setAutoStartup(true);

        msgLisContainer.setConnectionFactory(connectionFactory());
        msgLisContainer.setTaskExecutor(threadPoolExecutor());
        msgLisContainer.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        msgLisContainer.setErrorHandler(throwable -> LOG.error("jmsListenerContainer", throwable, "Error in queue [{}]", this.destination));
        return msgLisContainer;
    }


    private ActiveMQConnectionFactory connectionFactory() {

        final ActiveMQConnectionFactory cf = configureMultipleIps();
        cf.setConnectionLoadBalancingPolicyClassName(RoundRobinConnectionLoadBalancingPolicy.class.getName());

        cf.setAutoGroup(true);
        cf.setCacheDestinations(true);

        cf.setCallFailoverTimeout(5000);
        cf.setFailoverOnInitialConnection(false);
        cf.setInitialConnectAttempts(15);

        cf.setUser(brokerUser);
        cf.setPassword(brokerPwd);

        cf.setMaxRetryInterval(5000);
        cf.setClientFailureCheckPeriod(500);
        cf.setCallTimeout(5000);
        cf.setConnectionTTL(5000);

        cf.setThreadPoolMaxSize(this.POOL_MAX_SIZE);
        cf.setScheduledThreadPoolMaxSize(this.POOL_MAX_SIZE);

        return cf;
    }

    private boolean isTopic() {
        return destination.startsWith("topic.");
    }

    private ActiveMQConnectionFactory configureMultipleIps() {
        final ActiveMQConnectionFactory cf;
        try {
            cf = ActiveMQJMSClient.createConnectionFactory("(tcp://iombenampss01:61617,tcp://iombenampss02:61617,tcp://iombenampss03:61617)?reconnectAttempts=15", "artemis");
            cf.setConnectionLoadBalancingPolicyClassName(RoundRobinConnectionLoadBalancingPolicy.class.getName());
        } catch (Exception e) {
            throw new RuntimeException("Error connecting to Artemis using Broadcast", e);
        }
        return cf;
    }


    private ActiveMQConnectionFactory configurePublicIP() {

        final Map<String, Object> connectionParams = new HashMap<>();

        connectionParams.put(TransportConstants.HOST_PROP_NAME, brokerUrl);
        connectionParams.put(TransportConstants.PORT_PROP_NAME, brokerPort);

        final TransportConfiguration transportConfiguration =
                new TransportConfiguration(
                        NettyConnectorFactory.class.getName(),
                        connectionParams);

        return ActiveMQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, transportConfiguration);
    }

    private ActiveMQConnectionFactory configureUDPConnection() {

        final UDPBroadcastEndpointFactory udpBroadcastEndpointFactory = new UDPBroadcastEndpointFactory();
        udpBroadcastEndpointFactory.setGroupAddress(brokerUrl);
        udpBroadcastEndpointFactory.setGroupPort(brokerPort);

        final DiscoveryGroupConfiguration discoveryGroupConfiguration = new DiscoveryGroupConfiguration();
        discoveryGroupConfiguration.setBroadcastEndpointFactory(udpBroadcastEndpointFactory);

        return ActiveMQJMSClient.createConnectionFactoryWithoutHA(discoveryGroupConfiguration, JMSFactoryType.CF);
    }
}
