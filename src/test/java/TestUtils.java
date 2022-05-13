import com.easypost.easyvcr.AdvancedSettings;
import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.Mode;
import com.easypost.easyvcr.VCR;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpURLConnection;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpsURLConnection;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableURL;

import java.io.IOException;
import java.net.URL;

public class TestUtils {

    public static final String cassetteFolder = "cassettes";

    public static Cassette getCassette(String cassetteName) {
        return new Cassette(cassetteFolder, cassetteName);
    }

    public static RecordableHttpURLConnection getSimpleHttpURLConnection(String url, String cassetteName, Mode mode, AdvancedSettings advancedSettings)
            throws IOException {
        Cassette cassette = getCassette(cassetteName);
        return new RecordableURL(new URL(url), cassette, mode, advancedSettings).openConnection();
    }

    public static RecordableHttpURLConnection getSimpleHttpURLConnection(String cassetteName, Mode mode, AdvancedSettings advancedSettings)
            throws IOException {
        return getSimpleHttpURLConnection(FakeDataService.GET_POSTS_URL, cassetteName, mode, advancedSettings);
    }

    public static RecordableHttpsURLConnection getSimpleHttpsURLConnection(String url, String cassetteName, Mode mode, AdvancedSettings advancedSettings)
            throws IOException {
        Cassette cassette = getCassette(cassetteName);
        return new RecordableURL(new URL(url), cassette, mode, advancedSettings).openConnectionSecure();
    }

    public static RecordableHttpsURLConnection getSimpleHttpsURLConnection(String cassetteName, Mode mode, AdvancedSettings advancedSettings)
            throws IOException {
        return getSimpleHttpsURLConnection(FakeDataService.GET_POSTS_URL, cassetteName, mode, advancedSettings);
    }

    public static VCR getSimpleVCR(Mode mode) {
        VCR vcr = new VCR();

        switch (mode) {
            case Record:
                vcr.record();
                break;
            case Replay:
                vcr.replay();
                break;
            case Bypass:
                vcr.pause();
                break;
            case Auto:
                vcr.recordIfNeeded();
                break;
            default:
                break;
        }

        return vcr;
    }
}
