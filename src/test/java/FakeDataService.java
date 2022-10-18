import com.easypost.easyvcr.VCR;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpURLConnection;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpsURLConnection;
import com.easypost.easyvcr.internalutilities.json.Serialization;

import static com.easypost.easyvcr.internalutilities.Tools.readFromInputStream;

public class FakeDataService {

    public class IPAddressData {
        public String IPAddress;
    }

    public static final String URL = "https://api.ipify.org/?format=json";

    private interface FakeDataServiceBaseInterface {

        IPAddressData getIPAddressData() throws Exception;

        Object getIPAddressDataRawResponse() throws Exception;
    }

    public static class HttpUrlConnection extends FakeDataServiceBase implements FakeDataServiceBaseInterface {
        protected RecordableHttpURLConnection client;

        public HttpUrlConnection(RecordableHttpURLConnection client) {
            this.client = client;
        }

        public HttpUrlConnection(VCR vcr) {
            this.vcr = vcr;
        }

        public RecordableHttpURLConnection getClient(String url) throws Exception {
            if (client != null) {
                return client;
            } else if (vcr != null) {
                return vcr.getHttpUrlConnection(url).openConnection();
            }
            throw new Exception("No VCR or client has been set.");
        }

        @Override
        public IPAddressData getIPAddressData() throws Exception {
            RecordableHttpURLConnection client = (RecordableHttpURLConnection) getIPAddressDataRawResponse();
            String json = readFromInputStream(client.getInputStream());

            return Serialization.convertJsonToObject(json, IPAddressData.class);
        }

        @Override
        public Object getIPAddressDataRawResponse() throws Exception {
            RecordableHttpURLConnection client = getClient(URL);
            client.connect();
            return client;
        }
    }

    public static class HttpsUrlConnection extends FakeDataServiceBase implements FakeDataServiceBaseInterface {
        protected RecordableHttpsURLConnection client;

        public HttpsUrlConnection(RecordableHttpsURLConnection client) {
            this.client = client;
        }

        public HttpsUrlConnection(VCR vcr) {
            this.vcr = vcr;
        }

        public RecordableHttpsURLConnection getClient(String url) throws Exception {
            if (client != null) {
                return client;
            } else if (vcr != null) {
                return vcr.getHttpUrlConnection(url).openConnectionSecure();
            }
            throw new Exception("No VCR or client has been set.");
        }

        public IPAddressData getIPAddressData() throws Exception {
            RecordableHttpsURLConnection client = (RecordableHttpsURLConnection) getIPAddressDataRawResponse();
            String json = readFromInputStream(client.getInputStream());

            return Serialization.convertJsonToObject(json, IPAddressData.class);
        }

        public Object getIPAddressDataRawResponse() throws Exception {
            RecordableHttpsURLConnection client = getClient(URL);
            client.connect();
            return client;
        }

        public RecordableHttpsURLConnection makeBadRequest() throws Exception {
            RecordableHttpsURLConnection client = getClient(URL);
            client.setRequestMethod("POST");
            client.connect();

            return client;
        }
    }

    private static class FakeDataServiceBase {
        protected VCR vcr;

        public FakeDataServiceBase() {
        }
    }
}
