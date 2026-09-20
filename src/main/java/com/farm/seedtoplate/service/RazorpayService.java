package com.farm.seedtoplate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.farm.seedtoplate.config.RazorpayProperties;
import com.farm.seedtoplate.domain.PaymentStatus;
import com.farm.seedtoplate.exception.ApiException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RazorpayService {

    private final RazorpayProperties razorpayProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public RazorpayService(RazorpayProperties razorpayProperties, ObjectMapper objectMapper) {
        this.razorpayProperties = razorpayProperties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public RazorpayLinkResponse createPaymentLink(UUID paymentIntentId, UUID reservationId, BigDecimal amount, Instant expiresAt, String callbackUrl) {
        ensureConfigured();
        try {
            JsonNode payload = objectMapper.createObjectNode()
                .put("amount", amount.movePointRight(2).intValueExact())
                .put("currency", "INR")
                .put("accept_partial", false)
                .put("description", "Seed & Plate reservation " + reservationId)
                .put("reference_id", paymentIntentId.toString())
                .put("expire_by", expiresAt == null ? Instant.now().plusSeconds(48 * 60 * 60).getEpochSecond() : expiresAt.getEpochSecond())
                .put("callback_url", callbackUrl)
                .put("callback_method", "get");

            JsonNode response = execute(
                HttpRequest.newBuilder()
                    .uri(URI.create(normalizeBaseUrl() + "/payment_links"))
                    .header("Authorization", basicAuth())
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build()
            );

            return new RazorpayLinkResponse(
                text(response, "id"),
                text(response, "short_url"),
                mapPaymentStatus(text(response, "status")),
                text(response, "payment_id")
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Could not create the Razorpay payment link right now");
        } catch (IOException e) {
            throw new ApiException("Could not create the Razorpay payment link right now");
        }
    }

    public RazorpayLinkResponse fetchPaymentLink(String linkId) {
        ensureConfigured();
        try {
            JsonNode response = execute(
                HttpRequest.newBuilder()
                    .uri(URI.create(normalizeBaseUrl() + "/payment_links/" + linkId))
                    .header("Authorization", basicAuth())
                    .GET()
                    .build()
            );

            return new RazorpayLinkResponse(
                text(response, "id"),
                text(response, "short_url"),
                mapPaymentStatus(text(response, "status")),
                text(response, "payment_id")
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Could not verify the Razorpay payment state right now");
        } catch (IOException e) {
            throw new ApiException("Could not verify the Razorpay payment state right now");
        }
    }

    public boolean verifyWebhookSignature(String body, String signature) {
        ensureConfigured();
        if (!StringUtils.hasText(signature)) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(razorpayProperties.getWebhookSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
            byte[] actual = hexToBytes(signature);
            return java.security.MessageDigest.isEqual(expected, actual);
        } catch (Exception e) {
            return false;
        }
    }

    public RazorpayWebhookEvent parseWebhook(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode paymentLink = root.path("payload").path("payment_link").path("entity");
            JsonNode payment = root.path("payload").path("payment").path("entity");
            return new RazorpayWebhookEvent(
                text(root, "event"),
                text(paymentLink, "id"),
                text(payment, "id"),
                mapPaymentStatus(text(paymentLink, "status"))
            );
        } catch (IOException e) {
            throw new ApiException("Webhook payload could not be parsed");
        }
    }

    public boolean isEnabled() {
        return razorpayProperties.isEnabled();
    }

    private JsonNode execute(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String body = response.body() == null ? "" : response.body();
            String detail = body.isBlank() ? "" : ": " + body.substring(0, Math.min(body.length(), 500));
            throw new ApiException("Razorpay request failed with status " + response.statusCode() + detail);
        }
        return objectMapper.readTree(response.body());
    }

    private void ensureConfigured() {
        if (!razorpayProperties.isEnabled()
            || !StringUtils.hasText(razorpayProperties.getKeyId())
            || !StringUtils.hasText(razorpayProperties.getKeySecret())) {
            throw new ApiException("Payment gateway is not configured yet");
        }
    }

    private String basicAuth() {
        String raw = razorpayProperties.getKeyId() + ":" + razorpayProperties.getKeySecret();
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private String normalizeBaseUrl() {
        return razorpayProperties.getApiBaseUrl().replaceAll("/$", "");
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private static PaymentStatus mapPaymentStatus(String status) {
        if (status == null) return PaymentStatus.PENDING;
        return switch (status) {
            case "paid", "captured" -> PaymentStatus.SUCCESS;
            case "cancelled", "expired", "failed" -> PaymentStatus.FAILED;
            case "created", "issued", "partially_paid" -> PaymentStatus.INITIATED;
            default -> PaymentStatus.PENDING;
        };
    }

    private static byte[] hexToBytes(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            return new byte[0];
        }
        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            result[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return result;
    }

    public record RazorpayLinkResponse(String linkId, String shortUrl, PaymentStatus status, String paymentId) {}

    public record RazorpayWebhookEvent(String eventType, String linkId, String paymentId, PaymentStatus status) {}
}
