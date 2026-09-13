package com.cloudbank.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AwsMskIamConfigurationTest {

    @Test
    void awsProfileConfiguresMskIam() throws Exception {
        Properties p = load("application-aws.properties");

        assertEquals(
                "${CLOUDBANK_KAFKA_BOOTSTRAP_SERVERS}",
                p.getProperty("spring.kafka.bootstrap-servers")
        );

        assertEquals(
                "${CLOUDBANK_KAFKA_SECURITY_PROTOCOL:SASL_SSL}",
                p.getProperty(
                        "spring.kafka.properties.security.protocol"
                )
        );

        assertEquals(
                "${CLOUDBANK_KAFKA_SASL_MECHANISM:AWS_MSK_IAM}",
                p.getProperty(
                        "spring.kafka.properties.sasl.mechanism"
                )
        );

        assertEquals(
                "${CLOUDBANK_KAFKA_SASL_JAAS_CONFIG:software.amazon.msk.auth.iam.IAMLoginModule required;}",
                p.getProperty(
                        "spring.kafka.properties.sasl.jaas.config"
                )
        );

        assertEquals(
                "${CLOUDBANK_KAFKA_SASL_CALLBACK_HANDLER_CLASS:software.amazon.msk.auth.iam.IAMClientCallbackHandler}",
                p.getProperty(
                        "spring.kafka.properties.sasl.client.callback.handler.class"
                )
        );

        assertDoesNotThrow(
                () -> Class.forName(
                        "software.amazon.msk.auth.iam."
                        + "IAMClientCallbackHandler"
                )
        );
    }

    private static Properties load(String resource)
            throws Exception {
        Properties properties = new Properties();

        try (InputStream in =
                     new ClassPathResource(resource)
                             .getInputStream()) {
            properties.load(in);
        }

        return properties;
    }
}
