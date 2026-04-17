package id.ac.ui.cs.advprog.mysawitpayment.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XenditPropertiesTest {

    @Test
    void shouldSetAndGetPropertiesCorrectly() {
        XenditProperties properties = new XenditProperties();

        properties.setSecretKey("secret-key");
        properties.setBaseUrl("https://api.xendit.co");
        properties.setWebhookToken("webhook-token");

        assertThat(properties.getSecretKey()).isEqualTo("secret-key");
        assertThat(properties.getBaseUrl()).isEqualTo("https://api.xendit.co");
        assertThat(properties.getWebhookToken()).isEqualTo("webhook-token");
    }
}