package com.mcd.plantation.service.impl;

import java.util.UUID;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.mcd.plantation.dto.request.SubmitDocument;
import com.mcd.plantation.dto.response.HttpClientResponse;
import com.mcd.plantation.dto.response.PdfGenerationResponse;
import com.mcd.plantation.dto.response.ReturnParam;
import com.mcd.plantation.exception.ServiceException;
import com.mcd.plantation.pojo.ErrorCode;
import com.mcd.plantation.util.Base64Utils;
import com.mcd.plantation.util.CheckUtil;
import com.mcd.plantation.util.GenerateUtil;
import com.mcd.plantation.util.JsonUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PdfService {

    @Value("${swagam.pdf-generation-url}")
    private String pdfUrl;

    @Value("${swagam.api-base-url}")
    private String swagamBaseUrl;

    private final RestTemplate restTemplate;

    /**
     * Generate PDF from the PDF generation service.
     */
    public ReturnParam generate(Object payload, String pdfCode) {

        try {

            if (pdfUrl == null || pdfUrl.isBlank()) {
                return new ReturnParam(false, "PDF generation URL is empty");
            }

            PdfRequestWrapper pdfRequestWrapper = new PdfRequestWrapper(payload, pdfCode);

            String jsonBody = JsonUtil.convertObjectToJson(pdfRequestWrapper);

            System.out.println("PDF request body: " + jsonBody);

            String response = postJson(pdfUrl, jsonBody, null);

            PdfGenerationResponse parsed = JsonUtil.convertJsonToObject(response, PdfGenerationResponse.class);

            return new ReturnParam(true, "SUCCESS", parsed.getData());

        } catch (Exception e) {

            return new ReturnParam(false, "PDF generation error: " + e.getMessage());
        }
    }

    /**
     * Generic POST JSON method using RestTemplate.
     */
    private String postJson(String clientUrl, String jsonData, java.util.Map<String, String> headers)
            throws Exception {

        if (clientUrl == null || clientUrl.isBlank()) {
            throw new Exception("CLIENT URL SERVICE URL EMPTY");
        }

        if (jsonData == null) {
            throw new Exception("REQUEST JSON IS EMPTY");
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        if (headers != null) {
            headers.forEach(httpHeaders::set);
        }

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonData, httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(clientUrl, HttpMethod.POST, requestEntity,
                String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new Exception("Failed : HTTP error code : " + response.getStatusCode().value());
        }

        return response.getBody();
    }

    private record PdfRequestWrapper(
            Object data,
            String uniqueCode) {
    }

    /**
     * Generate PDF and store it in document repository.
     */
    public String generateAndStore(Object payload, String pdfCode, String authToken) {

        ReturnParam pdfGenerated = generate(payload, pdfCode);

        if (!pdfGenerated.isStatus()) {

            throw new ServiceException("PDF generation failed", new Throwable("PDF generation failed"),
                    ErrorCode.SERVICE_LAYER_ERROR.changeMessage("PDF generation failed for pdfCode " + pdfCode));
        }

        ReturnParam isStored = storeAndReturnId(pdfGenerated.getValue(), authToken);

        if (!isStored.isStatus()) {

            throw new ServiceException("Document storage failed: " + isStored.getMessage(),
                    new Throwable("Document storage failed: " + isStored.getMessage()),
                    ErrorCode.SERVICE_LAYER_ERROR.changeMessage("Document storage failed: " + isStored.getMessage()));
        }

        return isStored.getValue();
    }

    /**
     * Decode PDF Base64 and upload it to document repository.
     */
    public ReturnParam storeAndReturnId(String pdfBase64, String authToken) {

        try {

            if (CheckUtil.isNullOrEmpty(pdfBase64)) {
                return new ReturnParam(false, "PDF Base64 is empty");
            }

            byte[] bytes = Base64Utils.decode(pdfBase64);

            String filename = "G-" + GenerateUtil.getDateTimeRandomNum(8, "-") + ".pdf";

            String mimeType = getContentType(bytes, filename);

            String payload = buildDocSubmit(filename, bytes, mimeType);

            HttpClientResponse response = call(swagamBaseUrl, payload, authToken);

            if (!response.isStatus()) {

                return new ReturnParam(false, "Doc repo error: " + response.getMessage());
            }

            String docId = getDocID(response.getBody());

            if (CheckUtil.isNullOrEmpty(docId)) {

                return new ReturnParam(false, "Doc repo returned empty document ID");
            }

            return new ReturnParam(true, "success", docId);

        } catch (Exception e) {

            return new ReturnParam(false, "Upload exception: " + e.getMessage());
        }
    }

    /**
     * Detect MIME type of document.
     */
    private String getContentType(byte[] fileBytes, String fileNameExt) {

        try {

            Tika tika = new Tika();

            return tika.detect(fileBytes, fileNameExt);

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    /**
     * Call document repository addDocument API.
     */
    public HttpClientResponse call(String apiUri, String inputJson, String token) {

        try {

            if (CheckUtil.isNullOrEmpty(apiUri) || CheckUtil.isNullOrEmpty(inputJson)) {

                return new HttpClientResponse(false, "API URL AND INPUT JSON IS REQUIRED", "Doc REPO API");
            }

            if (!isValidURL(apiUri)) {

                return new HttpClientResponse(false, "API URL IS NOT VALID", "Doc REPO API");
            }

            if (!JsonUtil.isValidJson(inputJson)) {

                return new HttpClientResponse(false, "INPUT JSON DATA IS NOT VALID", "Doc REPO API");
            }

            String url = apiUri.endsWith("/") ? apiUri + "docrepo/addDocument" : apiUri + "/docrepo/addDocument";

            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.set("X-SESS-NONCE", UUID.randomUUID().toString());

            if (!CheckUtil.isNullOrEmpty(token)) {

                headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            }

            HttpEntity<String> requestEntity = new HttpEntity<>(inputJson, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            HttpClientResponse result;

            if (response.getStatusCode().is2xxSuccessful()) {

                result = new HttpClientResponse(true, "Request Send To Client", url);

            } else {

                result = new HttpClientResponse(false,
                        "Request Sending Failed With Response Code " + response.getStatusCode().value(), url);
            }

            result.setStatusCode(response.getStatusCode().value());

            result.setBody(response.getBody());

            result.setClientInfo("Doc REPO API");

            return result;

        } catch (Exception e) {

            return new HttpClientResponse(false, "Exception While Connecting To Client", e, apiUri);
        }
    }

    /**
     * Build document repository request JSON.
     */
    private String buildDocSubmit(String name, byte[] docBytes, String contentType) {

        SubmitDocument submitDocument = new SubmitDocument(name,
                Base64Utils.encodeToString(docBytes), contentType);

        return JsonUtil.convertObjectToJson(submitDocument);
    }

    /**
     * Extract status from JSON response.
     */
    public static boolean getStatus(String respJson) {

        if (JsonUtil.isValidJson(respJson)) {

            return Boolean.parseBoolean(JsonUtil.getValueByKey(respJson, "status"));
        }

        return false;
    }

    /**
     * Extract message from JSON response.
     */
    public static String getMessage(String respJson) {

        if (JsonUtil.isValidJson(respJson)) {

            return JsonUtil.getValueByKey(respJson, "message");
        }

        return null;
    }

    /**
     * Extract document ID from JSON response.
     */
    public static String getDocID(String respJson) {

        if (JsonUtil.isValidJson(respJson)) {

            return JsonUtil.getValueByKey(respJson, "docId");
        }

        return null;
    }

    /**
     * Validate URL.
     */
    public static boolean isValidURL(String url) {

        try {

            new java.net.URL(url).toURI();

            return true;

        } catch (Exception e) {

            return false;
        }
    }
}