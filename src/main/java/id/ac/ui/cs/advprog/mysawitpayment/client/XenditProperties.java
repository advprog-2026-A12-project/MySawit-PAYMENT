package id.ac.ui.cs.advprog.mysawitpayment.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "xendit")
public class XenditProperties {

    private String secretKey;

    private String baseUrl;

    private String webhookToken;
}