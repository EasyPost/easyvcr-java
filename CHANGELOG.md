# CHANGELOG

## v0.1.0 (2022-05-18)

- Initial release
- Allows record and replay HTTP requests via Java 8's `HttpUrlConnection` client
- Advanced settings:
  - Allows censoring of request and response bodies and parameters
  - Allow custom rules when determining if a request matches an existing recording
  - Allow custom delay for replayed requests to simulate real-world latency
