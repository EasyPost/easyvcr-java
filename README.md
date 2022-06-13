# EasyVCR

[![CI](https://github.com/EasyPost/easyvcr-java/workflows/CI/badge.svg)](https://github.com/EasyPost/easyvcr-java/actions?query=workflow%3ACI)

EasyVCR is a library for recording and replaying HTTP interactions in your test suite.

This can be useful for speeding up your test suite, or for running your tests on a CI server which doesn't have connectivity to the HTTP endpoints you need to interact with.

## Supported HTTP Clients

- Java 8's [HttpUrlConnection](https://docs.oracle.com/javase/8/docs/api/java/net/HttpURLConnection.html)

## How to use EasyVCR

#### Step 1.

Run your test suite locally against a real HTTP endpoint in recording mode

```java
import com.easypost.easyvcr;
import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.Mode;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpsURLConnection;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableURL;

public class Example {
    public static void main(String[] args) {

        // Create a cassette to handle HTTP interactions
        Cassette cassette = new Cassette("path/to/cassettes", "my_cassette");

        // create a RecordableURL using the cassette
        RecordableURL recordableURL = new RecordableURL("https://www.example.com", cassette, Mode.Record);

        // open the connection to get a Http(s)URLConnection
        RecordableHttpsURLConnection connection = recordableURL.openConnectionSecure();

        // A RecordableHttp(s)URLConnection extends a normal Http(s)URLConnection, so you can use it as you would a normal Http(s)URLConnection
        connection.setConnectTimeout(1000);
        connection.connect();
        int responseCode = connection.getResponseCode();
    }
}
```

Real HTTP calls will be made and recorded to the cassette file.

#### Step 2.

Switch to replay mode:

```java
import com.easypost.easyvcr;
import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.Mode;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpsURLConnection;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableURL;

public class Example {
    public static void main(String[] args) {

        // Create a cassette to handle HTTP interactions
        Cassette cassette = new Cassette("path/to/cassettes", "my_cassette");

        // create a RecordableURL using the cassette
        RecordableURL recordableURL = new RecordableURL("https://www.example.com", cassette, Mode.Replay);

        // open the connection to get a Http(s)URLConnection
        RecordableHttpsURLConnection connection = recordableURL.openConnectionSecure();

        // A RecordableHttp(s)URLConnection extends a normal Http(s)URLConnection, so you can use it as you would a normal Http(s)URLConnection
        int responseCode = connection.getResponseCode();
    }
}
```
Now when tests are run, no real HTTP calls will be made. Instead, the HTTP responses will be replayed from the cassette file.

### Available modes

- `Mode.Auto`:  Play back a request if it has been recorded before, or record a new one if not. (default mode for `VCR`)
- `Mode.Record`: Record a request, including overwriting any existing matching recording.
- `Mode.Replay`: Replay a request. Throws an exception if no matching recording is found.
- `Mode.Bypass`:  Do not record or replay any requests (client will behave like a normal HttpClient).

## Features

`EasyVCR` comes with a number of features, many of which can be customized via the `AdvancedOptions` class.

### Censoring

Censor sensitive data in the request and response bodies and headers, such as API keys and auth tokens.

NOTE: Censors can only be applied to JSON request and response bodies. Attempting to apply censors to non-JSON data will throw an exception.

**Default**: *Disabled*

```java
import com.easypost.easyvcr;
import com.easypost.easyvcr.AdvancedSettings;
import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.CensorElement;
import com.easypost.easyvcr.Censors;
import com.easypost.easyvcr.Mode;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpsURLConnection;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableURL;

import java.util.ArrayList;

public class Example {
    public static void main(String[] args) {
        Cassette cassette = new Cassette("path/to/cassettes", "my_cassette");

        AdvancedSettings advancedSettings = new AdvancedSettings();
        List<String> headersToCensor = new ArrayList<>();
        headersToCensor.add("Authorization"); // Hide the Authorization header
        advancedSettings.censors = new Censors().censorHeadersByKeys(headersToCensor);
        advancedSettings.censors.censorBodyElements(new ArrayList<>() {{
            add(new CensorElement("table", true)); // Hide the table element (case-sensitive) in the request and response body
        }});
        // or
        advancedSettings.censors =
                Censors.strict(); // use the built-in strict censoring mode (hides common sensitive data)

        RecordableURL recordableURL =
                new RecordableURL("https://www.example.com", cassette, Mode.Replay, advancedSettings);

        RecordableHttpsURLConnection connection = recordableURL.openConnectionSecure();
    }
}
```

### Delay

Simulate a delay when replaying a recorded request, either using a specified delay or the original request duration.

**Default**: *No delay*

```java
import com.easypost.easyvcr;
import com.easypost.easyvcr.AdvancedSettings;
import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.Mode;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpsURLConnection;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableURL;

public class Example {
    public static void main(String[] args) {
        Cassette cassette = new Cassette("path/to/cassettes", "my_cassette");
        
        AdvancedSettings advancedSettings = new AdvancedSettings();
        advancedSettings.manualDelay = 1000; // Simulate a delay of 1000 milliseconds when replaying
        advancedSettings.simulateDelay = true; // Simulate a delay of the original request duration when replaying (overrides manualDelay)
        
        RecordableURL recordableURL = new RecordableURL("https://www.example.com", cassette, Mode.Replay, advancedSettings);
        
        RecordableHttpsURLConnection connection = recordableURL.openConnectionSecure();
    }
}
```

### Matching

Customize how a recorded request is determined to be a match to the current request.

**Default**: *Method and full URL must match*

```java
import com.easypost.easyvcr;
import com.easypost.easyvcr.AdvancedSettings;
import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.MatchRules;
import com.easypost.easyvcr.Mode;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpsURLConnection;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableURL;

public class Example {
    public static void main(String[] args) {
        Cassette cassette = new Cassette("path/to/cassettes", "my_cassette");

        AdvancedSettings advancedSettings = new AdvancedSettings();
        advancedSettings.matchRules = new MatchRules().byBody().byHeader("X-My-Header"); // Match recorded requests by request body (i.e. POST data) and a specific header
        // or
        advancedSettings.matchRules = MatchRules.strict(); // use the built-in strict matching mode (matches by method, full URL and request body; useful for POST/PATCH/PUT requests)
        
        RecordableURL recordableURL =
                new RecordableURL("https://www.example.com", cassette, Mode.Replay, advancedSettings);

        RecordableHttpsURLConnection connection = recordableURL.openConnectionSecure();
    }
}
```

## VCR

In addition to individual recordable HttpClient instances, `EasyVCR` also offers a built-in VCR, which can be used to easily switch between multiple cassettes and/or modes. Any advanced settings applied to the VCR will be applied on every request made using the VCR's HTTP client.

```java
import com.easypost.easyvcr;
import com.easypost.easyvcr.AdvancedSettings;
import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.Censors;
import com.easypost.easyvcr.Mode;
import com.easypost.easyvcr.VCR;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpsURLConnection;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableURL;

public class Example {
    public static void main(String[] args) {
        AdvancedSettings advancedSettings = new AdvancedSettings();
        List<String> censoredQueryParams = new ArrayList<String>();
        censoredQueryParams.add("api_key"); // hide the api_key query parameter
        advancedSettings.censors = new Censors().hideQueryParameter(censoredQueryParams);

        // Create a VCR with the advanced settings applied
        VCR vcr = new VCR(advancedSettings);

        // Create a cassette and add it to the VCR
        Cassette cassette = new Cassette("path/to/cassettes", "my_cassette");
        vcr.insert(cassette);
        
        // Set the VCR to record mode
        vcr.record();
        
        // Get a RecordableURL instance from the VCR
        RecordableURL recordableURL = vcr.getHttpUrlConnection("https://www.example.com");

        // Use the client as you would normally.
        RecordableHttpsURLConnection connection = recordableURL.openConnectionSecure();
        connection.connect();

        // Remove the cassette from the VCR            
        vcr.eject();
    }
}
```

## Development

### Tests

```bash
# Build project
mvn clean install -DskipTests -Dgpg.skip

# Run tests
mvn clean test -B

# Run tests with coverage
mvn clean test -B jacoco:report
```

### Testing

The test suite in this project was specifically built to produce consistent results on every run, regardless of when they run or who is running them.

The cassettes used in the test suite are stored in a "cassettes" directory in the project root. Most of the cassettes produced by the test suite are erased and recreated on each run. Nevertheless, the test suite may complain if the cassettes are not present, so please do not delete them manually.

#### Credit

- [C# EasyVCR](https://github.com/EasyPost/easyvcr-csharp), upon which this library is based
- [Scotch by Martin Leech](https://github.com/mleech/scotch), whose core functionality inspired the C# version of EasyVCR
