import com.easypost.easyvcr.VCR;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpURLConnection;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpsURLConnection;
import com.easypost.easyvcr.internalutilities.json.Serialization;

import java.util.List;

import static com.easypost.easyvcr.internalutilities.Tools.readFromInputStream;

public class FakeDataService {

    public class ExchangeRates {
        public String code;
        public String currency;
        public List<Rate> rates;
        public String table;
    }

    public class Rate {
        public String effectiveDate;
        public float mid;
        public String no;
    }

    public static final String URL = "https://api.nbp.pl/api/exchangerates/rates/a/gbp/last/10/?format=json";

    private interface FakeDataServiceBaseInterface {

        ExchangeRates getExchangeRates() throws Exception;

        Object getExchangeRatesRawResponse() throws Exception;
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
        public ExchangeRates getExchangeRates() throws Exception {
            RecordableHttpURLConnection client = (RecordableHttpURLConnection) getExchangeRatesRawResponse();
            String json = readFromInputStream(client.getInputStream());

            return Serialization.convertJsonToObject(json, ExchangeRates.class);
        }

        @Override
        public Object getExchangeRatesRawResponse() throws Exception {
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

        @Override
        public ExchangeRates getExchangeRates() throws Exception {
            RecordableHttpsURLConnection client = (RecordableHttpsURLConnection) getExchangeRatesRawResponse();
            String json = readFromInputStream(client.getInputStream());

            return Serialization.convertJsonToObject(json, ExchangeRates.class);
        }

        @Override
        public Object getExchangeRatesRawResponse() throws Exception {
            RecordableHttpsURLConnection client = getClient(URL);
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
