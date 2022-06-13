# CHANGELOG

## v0.3.0 (2022-06-13)

- Improvements to censoring
  - Ability to define censored elements individually, with per-element case sensitivity
- Improvements to matching
  - Ability to ignore certain elements when matching by body

## v0.2.0 (2022-05-31)

- Enhance censoring to work on nested data
  - Censoring only works on JSON data; attempting to censor non-JSON data will throw an exception
- Fix request body not being sent on POST, PUT, PATCH requests

## v0.1.0 (2022-05-18)

- Initial release
- Allows record and replay HTTP requests via Java 8's `HttpUrlConnection` client
- Advanced settings:
  - Allows censoring of request and response bodies and parameters
  - Allow custom rules when determining if a request matches an existing recording
  - Allow custom delay for replayed requests to simulate real-world latency
