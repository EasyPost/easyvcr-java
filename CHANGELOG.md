# CHANGELOG

## v0.5.3 (2024-09-24)

- New `byCustomRule` function to allow users to define their own matching rule when finding a matching interaction in a cassette
- Improve error messages when a matching interaction is not found (human-readable error messages)
- Fix bug where the base URL matching rule was not comparing the scheme, host, and port of the request URL properly

## v0.5.2 (2023-01-13)

- Fix a null pointer exception triggered when trying to parse and censor URL path elements.

## v0.5.1 (2023-01-13)

- Removes dead and unused code
- Bumps dependencies to patch security vulnerabilities

## v0.5.0 (2022-12-20)

- Adds the ability to censor parts of a URL path using regex patterns.

## v0.4.2 (2022-10-20)

- Fix a bug where the error data of a bad HTTP request (4xx or 5xx) was not stored as expected in cassettes, causing
  empty error streams on replay.
  - Error data for a bad HTTP request is now stored as the "body" in the cassette just like a good HTTP request
      would, rather than needlessly stored in a separate "error" key. This more closely matches the behavior of EasyVCR C#.
  - This is a breaking change for previously-recorded "error" cassettes, which will no longer replay as expected and
      will need to be re-recorded (although likely never worked as expected in the first place).
- Fix a bug where using any expiration time frame other than "forever" and "never" would throw a NullPointerException.

## v0.4.1 (2022-10-19)

- Fix a bug where the error stream of a bad HTTP request (4xx or 5xx) was not properly recreated on replay.

## v0.4.0 (2022-10-04)

- New feature: Set expiration time for interactions (how long since it was recorded should an interaction be considered valid)
  - Can determine what to do if a matching interaction is considered invalid:
    - Warn the user, but proceed with the interaction
    - Throw an exception
    - Automatically re-record (cannot be used in `Replay` mode)
- New feature: Pass in a custom Logger instance to EasyVCR to funnel log messages into your own logging setup (fallback: logs to console)

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
