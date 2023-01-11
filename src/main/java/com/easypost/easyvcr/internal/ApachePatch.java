package com.easypost.easyvcr.internal;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

/**
 * This class is a collection of Apache functions that have been extracted for use in this application.
 * This class is required because Apache's libraries are notorious for security vulnerabilities, so we'll only
 * extract the safe functions that we need, rather than importing an entire vulnerable dependency.
 * <p>
 * A lot of the functions in here are lifted straight from Apache's source code with minor alterations,
 * so any janky code complaints should be directed at the Apache developers who wrote them probably 20 years ago.
 */
public abstract class ApachePatch {

    /**
     * This class was lifted and modified from
     * <a href="https://mvnrepository.com/artifact/org.apache.httpcomponents/httpcore/4.4.16">
     * org.apache.httpcomponents:httpcore version 4.4.16</a>,
     * namespace org.apache.http.NameValuePair,
     * released under the <a href="https://www.apache.org/licenses/LICENSE-2.0.txt">Apache License 2.0</a>.
     */
    public static class NameValuePair {

        private final String name;

        private final Object value;

        NameValuePair(final String name, final Object value) {
            this.name = name;
            this.value = value;
        }

        /**
         * Gets the name of this pair.
         *
         * @return the name of this pair.
         */
        public final String getName() {
            return name;
        }

        /**
         * Gets the value of this pair.
         *
         * @return the value of this pair, as a string.
         */
        public final String getValue() {
            return value.toString();
        }
    }

    /**
     * This class was lifted and modified from
     * <a href="https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient/4.5.14">
     * org.apache.httpcomponents:httpclient version 4.5.14</a>,
     * namespace org.apache.http.client.utils.URLEncodedUtils,
     * released under the <a href="https://www.apache.org/licenses/LICENSE-2.0.txt">Apache License 2.0</a>.
     */
    public static class URLEncodedUtils {
        private static final BitSet URLENCODER;

        /**
         * Constructs a new instance.
         */
        public URLEncodedUtils() {
        }

        /**
         * Parses the given URI query parameters as a list of name-value pairs.
         *
         * @param uri     the URI to parse
         * @param charset the charset to use
         * @return the list of name-value pairs
         */
        public static List<NameValuePair> parse(@NotNull URI uri, Charset charset) {
            String query = uri.getRawQuery();
            return query != null && !query.isEmpty() ? parse(query, charset) : createEmptyList();
        }

        /**
         * Parses the given string as a list of name-value pairs.
         *
         * @param s       the string to parse
         * @param charset the charset to use
         * @return the list of name-value pairs
         */
        public static List<NameValuePair> parse(String s, Charset charset) {
            if (s == null) {
                return createEmptyList();
            } else {
                return parse(s, charset, '&', ';');
            }
        }

        private static NameValuePair buildNameValuePair(String name, String value, Charset charset) {
            return new NameValuePair(decodeFormFields(name, charset), decodeFormFields(value, charset));
        }

        /**
         * Parses the given string as a list of name-value pairs.
         *
         * @param s          the string to parse
         * @param charset    the charset to use
         * @param separators separators splitting a name-value pair from another name-value pair
         * @return the list of name-value pairs
         */
        public static List<NameValuePair> parse(@NotNull String s, Charset charset, char... separators) {
            // Set a collection of delimiters between query entries (e.g. '&', ';')
            List<Character> delimiters = new ArrayList<>();
            for (char separator : separators) {
                delimiters.add(separator);
            }

            List<NameValuePair> list = new ArrayList<>();

            // Temporary storage for the name and value of a query entry
            StringBuilder nameBuilder = new StringBuilder();
            boolean trackingName = true;
            StringBuilder valueBuilder = new StringBuilder();

            // Iterate through the chars
            for (char c : s.toCharArray()) {
                if (delimiters.contains(c)) {
                    // Found a delimiter ending the name-value pair
                    // Build and add the name-value pair to the list
                    String name = nameBuilder.toString();
                    String value = valueBuilder.toString();
                    list.add(buildNameValuePair(name, value, charset));

                    // Reset the name and value builders
                    nameBuilder = new StringBuilder();
                    valueBuilder = new StringBuilder();

                    // start tracking name again
                    trackingName = true;
                } else if (c == '=') {
                    // Found a name-value separator
                    // Start tracking value
                    trackingName = false;
                } else {
                    // Just a normal character, add to either name or value
                    if (trackingName) {
                        nameBuilder.append(c);
                    } else {
                        valueBuilder.append(c);
                    }
                }
            }

            // Build and add the last name-value pair to the list
            String name = nameBuilder.toString();
            String value = valueBuilder.toString();
            list.add(buildNameValuePair(name, value, charset));

            return list;
        }

        /**
         * Format the given name-value pairs into a query string.
         *
         * @param parameters the name-value pairs to format
         * @param charset    the charset to use
         * @return a query string
         */
        public static String format(Iterable<? extends NameValuePair> parameters, Charset charset) {
            return format(parameters, '&', charset);
        }

        /**
         * Format the given name-value pairs into a query string.
         *
         * @param parameters         the name-value pairs to format
         * @param parameterSeparator the separator to use between name-value pairs
         * @param charset            the charset to use
         * @return a query string
         */
        public static String format(@NotNull Iterable<? extends NameValuePair> parameters, char parameterSeparator,
                                    Charset charset) {
            StringBuilder result = new StringBuilder();
            Iterator i = parameters.iterator();

            while (i.hasNext()) {
                NameValuePair parameter = (NameValuePair) i.next();
                String encodedName = encodeFormFields(parameter.getName(), charset);
                String encodedValue = encodeFormFields(parameter.getValue(), charset);
                if (result.length() > 0) {
                    result.append(parameterSeparator);
                }

                result.append(encodedName);
                if (encodedValue != null) {
                    result.append("=");
                    result.append(encodedValue);
                }
            }

            return result.toString();
        }

        private static List<NameValuePair> createEmptyList() {
            return new ArrayList<>(0);
        }

        private static String urlEncode(String content, Charset charset, BitSet safechars, boolean blankAsPlus) {
            if (content == null) {
                return null;
            } else {
                StringBuilder buf = new StringBuilder();
                ByteBuffer bb = charset.encode(content);

                while (true) {
                    while (bb.hasRemaining()) {
                        int b = bb.get() & 255;
                        if (safechars.get(b)) {
                            buf.append((char) b);
                        } else if (blankAsPlus && b == 32) {
                            buf.append('+');
                        } else {
                            buf.append("%");
                            char hex1 = Character.toUpperCase(Character.forDigit(b >> 4 & 15, 16));
                            char hex2 = Character.toUpperCase(Character.forDigit(b & 15, 16));
                            buf.append(hex1);
                            buf.append(hex2);
                        }
                    }

                    return buf.toString();
                }
            }
        }

        private static String urlDecode(String content, Charset charset, boolean plusAsBlank) {
            if (content == null) {
                return null;
            } else {
                ByteBuffer bb = ByteBuffer.allocate(content.length());
                CharBuffer cb = CharBuffer.wrap(content);

                while (true) {
                    while (true) {
                        while (cb.hasRemaining()) {
                            char c = cb.get();
                            if (c == '%' && cb.remaining() >= 2) {
                                char uc = cb.get();
                                char lc = cb.get();
                                int u = Character.digit(uc, 16);
                                int l = Character.digit(lc, 16);
                                if (u != -1 && l != -1) {
                                    bb.put((byte) ((u << 4) + l));
                                } else {
                                    bb.put((byte) 37);
                                    bb.put((byte) uc);
                                    bb.put((byte) lc);
                                }
                            } else if (plusAsBlank && c == '+') {
                                bb.put((byte) 32);
                            } else {
                                bb.put((byte) c);
                            }
                        }

                        bb.flip();
                        return charset.decode(bb).toString();
                    }
                }
            }
        }

        private static String decodeFormFields(String content, Charset charset) {
            return content == null ? null :
                    urlDecode(content, charset != null ? charset : StandardCharsets.UTF_8, true);
        }

        private static String encodeFormFields(String content, Charset charset) {
            return content == null ? null :
                    urlEncode(content, charset != null ? charset : StandardCharsets.UTF_8, URLENCODER, true);
        }

        static {
            URLENCODER = new BitSet(256);
        }
    }
}
