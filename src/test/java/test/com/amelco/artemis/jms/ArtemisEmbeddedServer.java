package test.com.amelco.artemis.jms;

import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.ConnectionFactoryConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSConfiguration;
import org.apache.activemq.artemis.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSConfigurationImpl;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;

@Profile("local")
@Configuration
public class ArtemisEmbeddedServer {

    @Value("${artemis.broker.url}")
    private String brokerUrl;

    @Value("${artemis.broker.port}")
    private Integer brokerPort;


    @Bean(name = "embeddedJMS", initMethod = "start", destroyMethod = "stop")
    public EmbeddedJMS artemisEmbedded() throws Exception {

        final ConfigurationImpl configuration = new ConfigurationImpl().setPersistenceEnabled(false)
                .setJournalDirectory("target/data/journal")
                .setSecurityEnabled(false)
                .addAcceptorConfiguration("tcp", String.format("tcp://%s:%s", brokerUrl, brokerPort))
                .addConnectorConfiguration("connector", String.format("tcp://%s:%s", brokerUrl, brokerPort));

        final ConnectionFactoryConfiguration cfConfig = new ConnectionFactoryConfigurationImpl()
                .setName("cf")
                .setConnectorNames(Arrays.asList("connector"))
                .setBindings("cf");

        final JMSConfiguration jmsConfig = new JMSConfigurationImpl();

        jmsConfig.getConnectionFactoryConfigurations().add(cfConfig);

        return new EmbeddedJMS()
                .setConfiguration(configuration)
                .setJmsConfiguration(jmsConfig);
    }
}
