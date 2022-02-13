import com.easypost.easyvcr.VCR;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpURLConnection;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpsURLConnection;
import com.easypost.easyvcr.internalutilities.json.Serialization;

import static com.easypost.easyvcr.internalutilities.Tools.readFromInputStream;

public class FakeDataService {

    public final static String GET_POSTS_URL = "https://jsonplaceholder.typicode.com/posts";

    private interface FakeDataServiceBaseInterface {

        Post[] getPosts() throws Exception;

        Object getPostsRawResponse() throws Exception;
    }

    public static class Post {
        public int userId;
        public int id;
        public String title;
        public String body;
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
        public Post[] getPosts() throws Exception {
            RecordableHttpURLConnection client = (RecordableHttpURLConnection) getPostsRawResponse();
            String json = readFromInputStream(client.getInputStream());

            return Serialization.convertJsonToObject(json, Post[].class);
        }

        @Override
        public Object getPostsRawResponse() throws Exception {
            RecordableHttpURLConnection client = getClient(GET_POSTS_URL);
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

        @Override
        public Post[] getPosts() throws Exception {
            RecordableHttpsURLConnection client = (RecordableHttpsURLConnection) getPostsRawResponse();
            String json = readFromInputStream(client.getInputStream());

            return Serialization.convertJsonToObject(json, Post[].class);
        }

        @Override
        public Object getPostsRawResponse() throws Exception {
            RecordableHttpsURLConnection client = getClient(GET_POSTS_URL);
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
