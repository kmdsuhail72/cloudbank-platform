package com.cloudbank.notification.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AwsRuntimeConfigurationTest {

    @Test
    void awsProfileConfiguresMskAndSes()
            throws Exception {
        Properties p = load("application-aws.properties");

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
                "${CLOUDBANK_EMAIL_SMTP_PORT:587}",
                p.getProperty("spring.mail.port")
        );

        assertEquals(
                "${CLOUDBANK_EMAIL_SMTP_USERNAME}",
                p.getProperty("spring.mail.username")
        );

        assertEquals(
                "${CLOUDBANK_EMAIL_SMTP_PASSWORD}",
                p.getProperty("spring.mail.password")
        );

        assertEquals(
                "${CLOUDBANK_EMAIL_SMTP_AUTH:true}",
                p.getProperty(
                        "spring.mail.properties.mail.smtp.auth"
                )
        );

        assertEquals(
                "${CLOUDBANK_EMAIL_SMTP_STARTTLS:true}",
                p.getProperty(
                        "spring.mail.properties.mail.smtp."
                        + "starttls.enable"
                )
        );

        assertDoesNotThrow(
                () -> Class.forName(
                        "software.amazon.msk.auth.iam."
                        + "IAMClientCallbackHandler"
                )
        );
    }

    @Test
    void defaultProfileStillSupportsLocalMailpit()
            throws Exception {
        Properties p = load("application.properties");

        assertEquals(
                "${CLOUDBANK_EMAIL_SMTP_PORT:1025}",
                p.getProperty("spring.mail.port")
        );

        assertEquals(
                "${CLOUDBANK_EMAIL_SMTP_USERNAME:}",
                p.getProperty("spring.mail.username")
        );

        assertEquals(
                "${CLOUDBANK_EMAIL_SMTP_PASSWORD:}",
                p.getProperty("spring.mail.password")
        );

        assertEquals(
                "${CLOUDBANK_EMAIL_SMTP_AUTH:false}",
                p.getProperty(
                        "spring.mail.properties.mail.smtp.auth"
                )
        );

        assertEquals(
                "${CLOUDBANK_EMAIL_SMTP_STARTTLS:false}",
                p.getProperty(
                        "spring.mail.properties.mail.smtp."
                        + "starttls.enable"
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
