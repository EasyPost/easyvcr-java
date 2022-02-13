import com.easypost.easyvcr.AdvancedSettings;
import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.Censors;
import com.easypost.easyvcr.HttpClientType;
import com.easypost.easyvcr.HttpClients;
import com.easypost.easyvcr.MatchRules;
import com.easypost.easyvcr.Mode;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpsURLConnection;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

import static com.easypost.easyvcr.internalutilities.Tools.readFromInputStream;

public class HttpUrlConnectionTest {

    private static FakeDataService.Post[] GetFakePostsRequest(Cassette cassette, Mode mode) throws Exception {
        RecordableHttpsURLConnection connection = TestUtils.getSimpleHttpsURLConnection(cassette.name, mode, null);

        FakeDataService.HttpsUrlConnection fakeDataService = new FakeDataService.HttpsUrlConnection(connection);

        return fakeDataService.getPosts();
    }

    @Test
    public void testPOSTRequest() throws Exception {
        AdvancedSettings advancedSettings = new AdvancedSettings();
        advancedSettings.matchRules = new MatchRules().byMethod().byBody().byFullUrl();
        RecordableHttpsURLConnection connection =
                TestUtils.getSimpleHttpsURLConnection("https://www.google.com", "test_post_request", Mode.Record, advancedSettings);
        connection.setDoOutput(true);
        String jsonInputString = "{'name': 'Upendra', 'job': 'Programmer'}";
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
    public void testClient() throws IOException {
        RecordableHttpsURLConnection connection =
                TestUtils.getSimpleHttpsURLConnection("https://www.google.com", "test_client", Mode.Bypass, null);

        Assert.assertNotNull(connection);
    }

    @Test
    public void testFakeDataServiceClient() throws IOException {
        RecordableHttpsURLConnection connection =
                TestUtils.getSimpleHttpsURLConnection("https://www.google.com", "test_client", Mode.Bypass, null);

        FakeDataService.HttpsUrlConnection fakeDataService = new FakeDataService.HttpsUrlConnection(connection);

        Assert.assertNotNull(fakeDataService);
    }

    @Test
    public void testErase() throws Exception {
        Cassette cassette = TestUtils.getCassette("test_erase");

        // record something to the cassette
        FakeDataService.Post[] posts = GetFakePostsRequest(cassette, Mode.Record);
        Assert.assertTrue(cassette.numInteractions() > 0);

        // erase the cassette
        cassette.erase();
        Assert.assertEquals(0, cassette.numInteractions());
    }

    @Test
    public void testEraseAndRecord() throws Exception {
        Cassette cassette = TestUtils.getCassette("test_erase_and_record");
        cassette.erase(); // Erase cassette before recording

        FakeDataService.Post[] posts = GetFakePostsRequest(cassette, Mode.Record);

        Assert.assertNotNull(posts);
        Assert.assertEquals(posts.length, 100);
        Assert.assertTrue(cassette.numInteractions() > 0); // Make sure cassette is not empty
    }

    @Test
    public void testEraseAndPlayback() {
        Cassette cassette = TestUtils.getCassette("test_erase_and_record");
        cassette.erase(); // Erase cassette before recording

        // cassette is empty, so replaying should throw an exception
        Assert.assertThrows(Exception.class, () -> GetFakePostsRequest(cassette, Mode.Replay));
    }

    @Test
    public void testAutoMode() throws Exception {
        Cassette cassette = TestUtils.getCassette("test_auto_mode");
        cassette.erase(); // Erase cassette before recording

        // in replay mode, if cassette is empty, should throw an exception
        Assert.assertThrows(Exception.class, () -> GetFakePostsRequest(cassette, Mode.Replay));
        Assert.assertEquals(cassette.numInteractions(), 0); // Make sure cassette is still empty

        // in auto mode, if cassette is empty, should make and record a real request
        FakeDataService.Post[] posts = GetFakePostsRequest(cassette, Mode.Auto);
        Assert.assertNotNull(posts);
        Assert.assertTrue(cassette.numInteractions() > 0); // Make sure cassette is no longer empty
    }

    @Test
    public void testInteractionElements() throws Exception {
        Cassette cassette = TestUtils.getCassette("test_interaction_elements");
        cassette.erase(); // Erase cassette before recording

        RecordableHttpsURLConnection connection =
                (RecordableHttpsURLConnection) HttpClients.newClient(HttpClientType.HttpsUrlConnection,
                        FakeDataService.GET_POSTS_URL, cassette, Mode.Record);
        FakeDataService.HttpsUrlConnection fakeDataService = new FakeDataService.HttpsUrlConnection(connection);

        // Most elements of a VCR request are black-boxed, so we can't test them here.
        // Instead, we can get the recreated HttpResponseMessage and check the details.
        RecordableHttpsURLConnection response = (RecordableHttpsURLConnection) fakeDataService.getPostsRawResponse();
        Assert.assertNotNull(response);
    }

    @Test
    public void testCensors() throws Exception {
        Cassette cassette = TestUtils.getCassette("test_censors");
        cassette.erase(); // Erase cassette before recording

        // set up advanced settings
        String censorString = "censored-by-test";
        Censors censors = new Censors(censorString).hideHeader("Date");

        AdvancedSettings advancedSettings = new AdvancedSettings();
        advancedSettings.censors = censors;

        // record cassette with advanced settings first
        RecordableHttpsURLConnection connection =
                (RecordableHttpsURLConnection) HttpClients.newClient(HttpClientType.HttpsUrlConnection,
                        FakeDataService.GET_POSTS_URL, cassette, Mode.Record, advancedSettings);
        FakeDataService.HttpsUrlConnection fakeDataService = new FakeDataService.HttpsUrlConnection(connection);
        Object ignore = fakeDataService.getPostsRawResponse();

        // now replay cassette
        connection = (RecordableHttpsURLConnection) HttpClients.newClient(HttpClientType.HttpsUrlConnection,
                FakeDataService.GET_POSTS_URL, cassette, Mode.Replay, advancedSettings);
        fakeDataService = new FakeDataService.HttpsUrlConnection(connection);
        RecordableHttpsURLConnection response = (RecordableHttpsURLConnection) fakeDataService.getPostsRawResponse();

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
                        FakeDataService.GET_POSTS_URL, cassette, Mode.Record);
        FakeDataService.HttpsUrlConnection fakeDataService = new FakeDataService.HttpsUrlConnection(connection);
        Object ignore = fakeDataService.getPostsRawResponse();

        // replay cassette with default match rules, should find a match
        connection = (RecordableHttpsURLConnection) HttpClients.newClient(HttpClientType.HttpsUrlConnection,
                FakeDataService.GET_POSTS_URL, cassette, Mode.Replay);
        connection.setRequestProperty("X-Custom-Header",
                "custom-value"); // add custom header to request, shouldn't matter when matching by default rules
        fakeDataService = new FakeDataService.HttpsUrlConnection(connection);
        RecordableHttpsURLConnection response = (RecordableHttpsURLConnection) fakeDataService.getPostsRawResponse();
        Assert.assertNotNull(response);

        // replay cassette with custom match rules, should not find a match because request is different (throw exception)
        MatchRules matchRules = new MatchRules().byEverything();
        AdvancedSettings advancedSettings = new AdvancedSettings();
        advancedSettings.matchRules = matchRules;

        connection = (RecordableHttpsURLConnection) HttpClients.newClient(HttpClientType.HttpsUrlConnection,
                FakeDataService.GET_POSTS_URL, cassette, Mode.Replay, advancedSettings);
        connection.setRequestProperty("X-Custom-Header",
                "custom-value"); // add custom header to request, causing a match failure when matching by everything
        fakeDataService = new FakeDataService.HttpsUrlConnection(connection);
        FakeDataService.HttpsUrlConnection finalFakeDataService = fakeDataService;
        Assert.assertThrows(Exception.class, () -> finalFakeDataService.getPosts());
    }

    @Test
    public void testDelay() throws Exception {
        Cassette cassette = TestUtils.getCassette("test_delay");
        cassette.erase(); // Erase cassette before recording

        // record cassette first
        RecordableHttpsURLConnection connection =
                (RecordableHttpsURLConnection) HttpClients.newClient(HttpClientType.HttpsUrlConnection,
                        FakeDataService.GET_POSTS_URL, cassette, Mode.Record);
        FakeDataService.HttpsUrlConnection fakeDataService = new FakeDataService.HttpsUrlConnection(connection);
        Object ignore = fakeDataService.getPosts();

        // baseline - how much time does it take to replay the cassette?
        connection = (RecordableHttpsURLConnection) HttpClients.newClient(HttpClientType.HttpsUrlConnection,
                FakeDataService.GET_POSTS_URL, cassette, Mode.Replay);
        fakeDataService = new FakeDataService.HttpsUrlConnection(connection);

        Instant start = Instant.now();
        FakeDataService.Post[] posts = fakeDataService.getPosts();
        Instant end = Instant.now();

        // confirm the normal replay worked, note time
        Assert.assertNotNull(posts);
        int normalReplayTime = (int) Duration.between(start, end).toMillis();

        // set up advanced settings
        int delay = normalReplayTime + 3000; // add 3 seconds to the normal replay time, for good measure
        AdvancedSettings advancedSettings = new AdvancedSettings();
        advancedSettings.manualDelay = delay;
        connection = (RecordableHttpsURLConnection) HttpClients.newClient(HttpClientType.HttpsUrlConnection,
                FakeDataService.GET_POSTS_URL, cassette, Mode.Replay, advancedSettings);
        fakeDataService = new FakeDataService.HttpsUrlConnection(connection);

        // time replay request
        start = Instant.now();
        posts = fakeDataService.getPosts();
        end = Instant.now();

        // check that the delay was respected
        Assert.assertNotNull(posts);
        Assert.assertTrue((int) Duration.between(start, end).toMillis() >= delay);
    }
}
