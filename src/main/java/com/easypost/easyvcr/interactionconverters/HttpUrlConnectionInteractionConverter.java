package com.easypost.easyvcr.interactionconverters;

import com.easypost.easyvcr.Censors;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableRequestBody;
import com.easypost.easyvcr.requestelements.HttpInteraction;
import com.easypost.easyvcr.requestelements.Request;
import com.easypost.easyvcr.requestelements.Response;
import com.easypost.easyvcr.requestelements.Status;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.easypost.easyvcr.internalutilities.Tools.readFromInputStream;

/**
 * The interaction converter to convert Http(s)UrlConnection requests/responses to/from EasyVCR requests/responses.
 */
public final class HttpUrlConnectionInteractionConverter extends BaseInteractionConverter {
    /**
     * Convert a HttpURLConnection request to an EasyVCR request.
     *
     * @param connection  The HttpURLConnection request.
     * @param requestBody The request body.
     * @param censors     The censors to apply to the request.
     * @return The EasyVCR request.
     */
    public Request createRecordedRequest(HttpURLConnection connection, RecordableRequestBody requestBody,
                                         Censors censors) {
        try {
            // collect elements from the connection
            String uriString = connection.getURL().toString();
            connection.disconnect();
            Map<String, List<String>> headers = connection.getRequestProperties();
            String body = requestBody.getData();
            String method = connection.getRequestMethod();

            // apply censors
            uriString = censors.applyQueryParametersCensors(uriString);
            headers = censors.applyHeadersCensors(headers);
            body = censors.applyBodyParametersCensors(body);


            // create the request
            Request request = new Request();
            request.setMethod(method);
            request.setUri(new URI(uriString));
            request.setHeaders(headers);
            request.setBody(body);

            return request;
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Convert a HttpURLConnection response to a ResponseAndTime object.
     *
     * @param connection The HttpURLConnection response.
     * @param censors    The censors to apply to the response.
     * @return The ResponseAndTime object.
     */
    public ResponseAndTime createRecordedResponse(HttpURLConnection connection, Censors censors) {
        try {
            // quickly time how long it takes to get the initial response
            Instant start = Instant.now();
            int responseCode = connection.getResponseCode();
            Instant end = Instant.now();
            long milliseconds = Duration.between(start, end).toMillis();

            // collect elements from the connection
            String message = connection.getResponseMessage();
            String uriString = connection.getURL().toString();
            Map<String, List<String>> headers = connection.getHeaderFields();
            String body = null;
            String errors = null;
            try {
                body = readFromInputStream(connection.getInputStream());
                errors = readFromInputStream(connection.getErrorStream());
            } catch (NullPointerException | IOException ignored) {  // nothing in body if bad status code from server
            }

            // apply censors
            uriString = censors.applyQueryParametersCensors(uriString);
            headers = censors.applyHeadersCensors(headers);
            // we don't censor the response body, only the request body

            // create the response
            Response response = new Response();
            response.setStatus(new Status(responseCode, message));
            response.setUri(new URI(uriString));
            response.setHeaders(headers);
            if (body != null) {
                response.setBody(body);
            }
            if (errors != null) {
                response.setErrors(errors);
            }

            return new ResponseAndTime(response, milliseconds);
        } catch (URISyntaxException | IOException ignored) {
            return null;
        }
    }

    /**
     * Convert a Http(s)URLConnection to an EasyVCR HttpInteraction.
     *
     * @param connection  The Http(s)URLConnection.
     * @param requestBody The request body.
     * @param censors     The censors to apply to the interaction.
     * @return The EasyVCR HttpInteraction.
     */
    public HttpInteraction createInteraction(HttpURLConnection connection, RecordableRequestBody requestBody,
                                             Censors censors) {
        Request request = createRecordedRequest(connection, requestBody, censors);
        ResponseAndTime responseAndTime = createRecordedResponse(connection, censors);
        connection.disconnect();
        return createInteraction(request, responseAndTime.response, responseAndTime.time);
    }
}
