package id.ac.ui.cs.advprog.mysawitpayment.client;

import id.ac.ui.cs.advprog.mysawitpayment.dto.result.CreateInvoiceResult;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.XenditInvoiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "payment.gateway", havingValue = "xendit")
public class XenditPaymentGatewayClient implements PaymentGatewayClient {

    private final XenditProperties xenditProperties;
    private final RestClient restClient = RestClient.create();

    @Override
    public CreateInvoiceResult createTopupInvoice(UUID transactionId, UUID adminId, BigDecimal amountIdr) {
        Map<String, Object> body = new HashMap<>();
        body.put("external_id", transactionId.toString());
        body.put("amount", amountIdr.intValueExact());
        body.put("description", "Top-up SawitDollar for admin " + adminId);
        body.put("currency", "IDR");

        XenditInvoiceResponse response = restClient.post()
                .uri(xenditProperties.getBaseUrl() + "/v2/invoices")
                .header(HttpHeaders.AUTHORIZATION, buildBasicAuthHeader(xenditProperties.getSecretKey()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(XenditInvoiceResponse.class);

        return CreateInvoiceResult.builder()
                .gatewayReferenceId(response.getId())
                .paymentUrl(response.getInvoiceUrl())
                .expiresAt(response.getExpiryDate())
                .status(response.getStatus())
                .build();
    }

    private String buildBasicAuthHeader(String secretKey) {
        String auth = secretKey + ":";
        String encoded = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}