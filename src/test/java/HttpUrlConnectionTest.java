import com.easypost.easyvcr.AdvancedSettings;
import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.CensorElement;
import com.easypost.easyvcr.Censors;
import com.easypost.easyvcr.ExpirationActions;
import com.easypost.easyvcr.HttpClientType;
import com.easypost.easyvcr.HttpClients;
import com.easypost.easyvcr.MatchRules;
import com.easypost.easyvcr.Mode;
import com.easypost.easyvcr.RecordingExpirationException;
import com.easypost.easyvcr.TimeFrame;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpsURLConnection;
import com.google.gson.JsonParseException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.easypost.easyvcr.internalutilities.Tools.readFromInputStream;

public class HttpUrlConnectionTest {

    private static FakeDataService.ExchangeRates GetExchangeRatesRequest(Cassette cassette, Mode mode) throws Exception {
        RecordableHttpsURLConnection connection = TestUtils.getSimpleHttpsURLConnection(cassette.name, mode, null);

        FakeDataService.HttpsUrlConnection fakeDataService = new FakeDataService.HttpsUrlConnection(connection);

        return fakeDataService.getExchangeRates();
    }

    @Test
    public void testPOSTRequest() throws Exception {
        AdvancedSettings advancedSettings = new AdvancedSettings();
        advancedSettings.matchRules = new MatchRules().byMethod().byBody().byFullUrl();
        RecordableHttpsURLConnection connection =
                TestUtils.getSimpleHttpsURLConnection("https://www.google.com", "test_post_request", Mode.Record, advancedSettings);
        String jsonInputString = "{'name': 'Upendra', 'job': 'Programmer'}";
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        OutputStream output = null;
        try {
            output = connection.getOutputStream();
            output.write(jsonInputString.getBytes(StandardCharsets.UTF_8));
        } finally {
            if (output != null) {
                output.close();
            }
        }
        connection.connect();
        String json = readFromInputStream(connection.getInputStream());
        Assert.assertNotNull(json);
    }

    @Test
    public void testNonJsonDataWithCensors() throws Exception {
        AdvancedSettings advancedSettings = new AdvancedSettings();

        List<String> bodyCensors = new ArrayList<>();
        bodyCensors.add("Date");
        advancedSettings.censors = new Censors("*****").censorBodyElementsByKeys(bodyCensors);

        advancedSettings.matchRules = new MatchRules().byMethod().byBody().byFullUrl();
        RecordableHttpsURLConnection connection =
                TestUtils.getSimpleHttpsURLConnection("https://www.google.com", "test_non_json", Mode.Record, advancedSettings);
        String jsonInputString = "{'name': 'Upendra', 'job': 'Programmer'}";
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        OutputStream output = null;
        try {
            output = connection.getOutputStream();
            output.write(jsonInputString.getBytes(StandardCharsets.UTF_8));
        } finally {
            if (output != null) {
                output.close();
            }
        }
        Assert.assertThrows(JsonParseException.class, () -> connection.connect());
    }

    @Test
    public void testClient() throws Exception {
        RecordableHttpsURLConnection connection =
                TestUtils.getSimpleHttpsURLConnection("https://www.google.com", "test_client", Mode.Bypass, null);

        Assert.assertNotNull(connection);
    }

    @Test
    public void testFakeDataServiceClient() throws Exception {
        RecordableHttpsURLConnection connection =
                TestUtils.getSimpleHttpsURLConnection("https://www.google.com", "test_client", Mode.Bypass, null);

        FakeDataService.HttpsUrlConnection fakeDataService = new FakeDataService.HttpsUrlConnection(connection);

        Assert.assertNotNull(fakeDataService);
    }

    @Test
    public void testErase() throws Exception {
        Cassette cassette = TestUtils.getCassette("test_erase");

        // record something to the cassette
        GetExchangeRatesRequest(cassette, Mode.Record);
        Assert.assertTrue(cassette.numInteractions() > 0);

        // erase the cassette
        cassette.erase();
        Assert.assertEquals(0, cassette.numInteractions());
    }

    @Test
    public void testEraseAndRecord() throws Exception {
        Cassette cassette = TestUtils.getCassette("test_erase_and_record");
        cassette.erase(); // Erase cassette before recording

        FakeDataService.ExchangeRates exchangeRates = GetExchangeRatesRequest(cassette, Mode.Record);

        Assert.assertNotNull(exchangeRates);
        Assert.assertTrue(cassette.numInteractions() > 0); // Make sure cassette is not empty
    }

    @Test
    public void testEraseAndPlayback() {
        Cassette cassette = TestUtils.getCassette("test_erase_and_record");
        cassette.erase(); // Erase cassette before recording

        // cassette is empty, so replaying should throw an exception
        Assert.assertThrows(Exception.class, () -> GetExchangeRatesRequest(cassette, Mode.Replay));
    }

    @Test
    public void testAutoMode() throws Exception {
        Cassette cassette = TestUtils.getCassette("test_auto_mode");
        cassette.erase(); // Erase cassette before recording

        // in replay mode, if cassette is empty, should throw an exception
        Assert.assertThrows(Exception.class, () -> GetExchangeRatesRequest(cassette, Mode.Replay));
        Assert.assertEquals(cassette.numInteractions(), 0); // Make sure cassette is still empty

        // in auto mode, if cassette is empty, should make and record a real request
        FakeDataService.ExchangeRates exchangeRates = GetExchangeRatesRequest(cassette, Mode.Auto);
        Assert.assertNotNull(exchangeRates);
        Assert.assertTrue(cassette.numInteractions() > 0); // Make sure cassette is no longer empty
    }

    @Test
    public void testInteractionElements() throws Exception {
        Cassette cassette = TestUtils.getCassette("test_interaction_elements");
        cassette.erase(); // Erase cassette before recording

        RecordableHttpsURLConnection connection =
                (RecordableHttpsURLConnection) HttpClients.newClient(HttpClientType.HttpsUrlConnection,
                        FakeDataService.URL, cassette, Mode.Record);
        FakeDataService.HttpsUrlConnection fakeDataService = new FakeDataService.HttpsUrlConnection(connection);

        // Most elements of a VCR request are black-boxed, so we can't test them here.
        // Instead, we can get the recreated HttpResponseMessage and check the details.
        RecordableHttpsURLConnection response = (RecordableHttpsURLConnection) fakeDataService.getExchangeRatesRawResponse();
        Assert.assertNotNull(response);
    }

    @Test
    public void testCensors() throws Exception {
        Cassette cassette = TestUtils.getCassette("test_censors");
        cassette.erase(); // Erase cassette before recording

        // set up advanced settings
        String censorString = "censored-by-test";
        List<String> headers = new ArrayList<>();
        headers.add("Date");
        Censors censors = new Censors(censorString).censorHeadersByKeys(headers);

        AdvancedSettings advancedSettings = new AdvancedSettings();
        advancedSettings.censors = censors;

        // record cassette with advanced settings first
        RecordableHttpsURLConnection connection =
                (RecordableHttpsURLConnection) HttpClients.newClient(HttpClientType.HttpsUrlConnection,
                        FakeDataService.URL, cassette, Mode.Record, advancedSettings);
        FakeDataService.HttpsUrlConnection fakeDataService = new FakeDataService.HttpsUrlConnection(connection);
        fakeDataService.getExchangeRatesRawResponse();

        // now replay cassette
        connection = (RecordableHttpsURLConnection) HttpClients.newClient(HttpClientType.HttpsUrlConnection,
                FakeDataService.URL, cassette, Mode.Replay, advancedSettings);
        fakeDataService = new FakeDataService.HttpsUrlConnection(connection);
        RecordableHttpsURLConnection response = (RecordableHttpsURLConnection) fakeDataService.getExchangeRatesRawResponse();

        // check that the replayed response contains the censored header
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getHeaderField("Date"));
        String censoredHeader = response.getHeaderField("Date");
        Assert.assertNotNull(censoredHeader);
        Assert.assertEquals(censoredHeader, censorString);
    }

    @Test
    public void testMatchSettings() throws Exception {
        Cassette cassette = TestUtils.getCassette("test_match_settings");
        cassette.erase(); // Erase cassette before recording

        // record cassette with advanced settings first
        RecordableHttpsURLConnection connection =
                (RecordableHttpsURLConnection) HttpClients.newClient(HttpClientType.HttpsUrlConnection,
                        FakeDataService.URL, cassette, Mode.Record);
        FakeDataService.HttpsUrlConnection fakeDataService = new FakeDataService.HttpsUrlConnection(connection);
        fakeDataService.getExchangeRatesRawResponse();

        // replay cassette with default match rules, should find a match
        connection = (RecordableHttpsURLConnection) HttpClients.newClient(HttpClientType.HttpsUrlConnection,
                FakeDataService.URL, cassette, Mode.Replay);
        connection.setRequestProperty("X-Custom-Header",
                "custom-value"); // add custom header to request, shouldn't matter when matching by default rules
        fakeDataService = new FakeDataService.HttpsUrlConnection(connection);
        RecordableHttpsURLConnection response = (RecordableHttpsURLConnection) fakeDataService.getExchangeRatesRawResponse();
        Assert.assertNotNull(response);

        // replay cassette with custom match rules, should not find a match because request is different (throw exception)
        MatchRules matchRules = new MatchRules().byEverything();
        AdvancedSettings advancedSettings = new AdvancedSettings();
        advancedSettings.matchRules = matchRules;

        connection = (RecordableHttpsURLConnection) HttpClients.newClient(HttpClientType.HttpsUrlConnection,
                FakeDataService.URL, cassette, Mode.Replay, advancedSettings);
        connection.setRequestProperty("X-Custom-Header",
                "custom-value"); // add custom header to request, causing a match failure when matching by everything
        fakeDataService = new FakeDataService.HttpsUrlConnection(connection);
        FakeDataService.HttpsUrlConnection finalFakeDataService = fakeDataService;
        Assert.assertThrows(Exception.class, () -> finalFakeDataService.getExchangeRates());
    }

    @Test
    public void testDelay() throws Exception {
        Cassette cassette = TestUtils.getCassette("test_delay");
        cassette.erase(); // Erase cassette before recording

        // record cassette first
        RecordableHttpsURLConnection connection =
                (RecordableHttpsURLConnection) HttpClients.newClient(HttpClientType.HttpsUrlConnection,
                        FakeDataService.URL, cassette, Mode.Record);
        FakeDataService.HttpsUrlConnection fakeDataService = new FakeDataService.HttpsUrlConnection(connection);
        fakeDataService.getExchangeRatesRawResponse();

        // baseline - how much time does it take to replay the cassette?
        connection = (RecordableHttpsURLConnection) HttpClients.newClient(HttpClientType.HttpsUrlConnection,
                FakeDataService.URL, cassette, Mode.Replay);
        fakeDataService = new FakeDataService.HttpsUrlConnection(connection);

        Instant start = Instant.now();
        FakeDataService.ExchangeRates exchangeRates = fakeDataService.getExchangeRates();
        Instant end = Instant.now();

        // confirm the normal replay worked, note time
        Assert.assertNotNull(exchangeRates);
        int normalReplayTime = (int) Duration.between(start, end).toMillis();

        // set up advanced settings
        int delay = normalReplayTime + 3000; // add 3 seconds to the normal replay time, for good measure
        AdvancedSettings advancedSettings = new AdvancedSettings();
        advancedSettings.manualDelay = delay;
        connection = (RecordableHttpsURLConnection) HttpClients.newClient(HttpClientType.HttpsUrlConnection,
                FakeDataService.URL, cassette, Mode.Replay, advancedSettings);
        fakeDataService = new FakeDataService.HttpsUrlConnection(connection);

        // time replay request
        start = Instant.now();
        exchangeRates = fakeDataService.getExchangeRates();
        end = Instant.now();

        // check that the delay was respected
        Assert.assertNotNull(exchangeRates);
        Assert.assertTrue((int) Duration.between(start, end).toMillis() >= delay);
    }

    @Test
    public void testIgnoreElementsFailMatch() throws URISyntaxException, IOException, RecordingExpirationException {
        Cassette cassette = TestUtils.getCassette("test_ignore_elements_fail_match");
        cassette.erase(); // Erase cassette before recording

        String bodyData1 = "{'name': 'Upendra', 'job': 'Programmer'}";
        String bodyData2 = "{'name': 'NewName', 'job': 'Programmer'}";

        // record baseline request first
        RecordableHttpsURLConnection connection = HttpClients.newHttpsURLConnection(FakeDataService.URL, cassette, Mode.Record);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        // use bodyData1 to make request
        OutputStream output = null;
        try {
            output = connection.getOutputStream();
            output.write(bodyData1.getBytes(StandardCharsets.UTF_8));
        } finally {
            if (output != null) {
                output.close();
            }
        }
        connection.connect();

        // try to replay with slightly different data
        AdvancedSettings advancedSettings = new AdvancedSettings();
        // matching strictly by body
        advancedSettings.matchRules = new MatchRules().byMethod().byFullUrl().byBody();
        connection = (RecordableHttpsURLConnection) HttpClients.newClient(HttpClientType.HttpsUrlConnection,
                FakeDataService.URL, cassette, Mode.Replay, advancedSettings);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        // use bodyData2 to make request
        output = null;
        try {
            output = connection.getOutputStream();
            output.write(bodyData2.getBytes(StandardCharsets.UTF_8));
        } finally {
            if (output != null) {
                output.close();
            }
        }
        connection.connect();

        // should fail since we're strictly in replay mode and there's no exact match
        int statusCode = connection.getResponseCode();
        Assert.assertEquals(0, statusCode);
    }

    @Test
    public void testIgnoreElementsPassMatch() throws URISyntaxException, IOException, RecordingExpirationException {
        Cassette cassette = TestUtils.getCassette("test_ignore_elements_pass_match");
        cassette.erase(); // Erase cassette before recording

        String bodyData1 = "{'name': 'Upendra', 'job': 'Programmer'}";
        String bodyData2 = "{'name': 'NewName', 'job': 'Programmer'}";

        // record baseline request first
        RecordableHttpsURLConnection connection = HttpClients.newHttpsURLConnection(FakeDataService.URL, cassette, Mode.Record);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        // use bodyData1 to make request
        OutputStream output = null;
        try {
            output = connection.getOutputStream();
            output.write(bodyData1.getBytes(StandardCharsets.UTF_8));
        } finally {
            if (output != null) {
                output.close();
            }
        }
        connection.connect();

        // try to replay with slightly different data, but ignoring the differences
        AdvancedSettings advancedSettings = new AdvancedSettings();
        // ignore the element that is different
        List<CensorElement> ignoredElements = new ArrayList<CensorElement>() {{
            add(new CensorElement("name", false));
        }};
        advancedSettings.matchRules = new MatchRules().byMethod().byFullUrl().byBody(ignoredElements);
        connection = (RecordableHttpsURLConnection) HttpClients.newClient(HttpClientType.HttpsUrlConnection,
                FakeDataService.URL, cassette, Mode.Replay, advancedSettings);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        // use bodyData2 to make request
        output = null;
        try {
            output = connection.getOutputStream();
            output.write(bodyData2.getBytes(StandardCharsets.UTF_8));
        } finally {
            if (output != null) {
                output.close();
            }
        }
        connection.connect();

        // should not fail since we're ignoring the body elements that differ
        int statusCode = connection.getResponseCode();
        Assert.assertNotEquals(0, statusCode);
    }

    @Test
    public void testExpirationSettings() throws Exception {
        Cassette cassette = TestUtils.getCassette("test_expiration_settings");
        cassette.erase(); // Erase cassette before recording

        // record cassette first
        RecordableHttpsURLConnection connection =
                (RecordableHttpsURLConnection) HttpClients.newClient(HttpClientType.HttpsUrlConnection,
                        FakeDataService.URL, cassette, Mode.Record);
        FakeDataService.HttpsUrlConnection fakeDataService = new FakeDataService.HttpsUrlConnection(connection);
        fakeDataService.getExchangeRatesRawResponse();

        // replay cassette with default expiration rules, should find a match
        connection = (RecordableHttpsURLConnection) HttpClients.newClient(HttpClientType.HttpsUrlConnection,
                FakeDataService.URL, cassette, Mode.Replay);
        fakeDataService = new FakeDataService.HttpsUrlConnection(connection);
        RecordableHttpsURLConnection response = (RecordableHttpsURLConnection) fakeDataService.getExchangeRatesRawResponse();
        Assert.assertNotNull(response);

        // replay cassette with custom expiration rules, should not find a match because recording is expired (throw exception)
        AdvancedSettings advancedSettings = new AdvancedSettings();
        advancedSettings.timeFrame = TimeFrame.never();
        advancedSettings.whenExpired = ExpirationActions.Throw_Exception;  // throw exception when recording is expired
        Thread.sleep(1000); // allow 1 second to lapse to ensure recording is now "expired"
        connection = (RecordableHttpsURLConnection) HttpClients.newClient(HttpClientType.HttpsUrlConnection,
                FakeDataService.URL, cassette, Mode.Replay, advancedSettings);
        fakeDataService = new FakeDataService.HttpsUrlConnection(connection);
        FakeDataService.HttpsUrlConnection finalFakeDataService = fakeDataService;
        // this throws a RuntimeException rather than a RecordingExpirationException because the exceptions are coalesced internally
        Assert.assertThrows(Exception.class, () -> finalFakeDataService.getExchangeRates());

        // replay cassette with bad expiration rules, should throw an exception because settings are bad
        advancedSettings = new AdvancedSettings();
        advancedSettings.timeFrame = TimeFrame.never();
        advancedSettings.whenExpired = ExpirationActions.Record_Again;  // invalid settings for replay mode, should throw exception
        AdvancedSettings finalAdvancedSettings = advancedSettings;
        Assert.assertThrows(RecordingExpirationException.class, () -> HttpClients.newClient(HttpClientType.HttpsUrlConnection,
                FakeDataService.URL, cassette, Mode.Replay, finalAdvancedSettings));
    }
}
