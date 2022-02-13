package com.easypost.easyvcr.interactionconverters;

import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.MatchRules;
import com.easypost.easyvcr.VCRException;
import com.easypost.easyvcr.requestelements.HttpInteraction;
import com.easypost.easyvcr.requestelements.Request;
import com.easypost.easyvcr.requestelements.Response;

/**
 * Base for custom interaction converters to convert requests/responses to/from EasyVCR requests/responses.
 */
public abstract class BaseInteractionConverter {

    /**
     * Search for an existing interaction that matches the request.
     *
     * @param cassette   The cassette to use to search for an existing interaction.
     * @param request    The request to search for.
     * @param matchRules The match rules to use to determine if an interaction matches the request.
     * @return The matching interaction, or null if no matching interaction was found.
     * @throws VCRException If an error occurs while searching for an existing interaction.
     */
    public HttpInteraction findMatchingInteraction(Cassette cassette, Request request, MatchRules matchRules)
            throws VCRException {
        for (HttpInteraction recordedInteraction : cassette.read()) {
            if (matchRules.requestsMatch(request, recordedInteraction.getRequest())) {
                return recordedInteraction;
            }
        }
        return null;
    }

    /**
     * Create an HttpInteraction from a request and response.
     *
     * @param request  The request to create an HttpInteraction from.
     * @param response The response to create an HttpInteraction from.
     * @param duration The duration of the interaction.
     * @return The created HttpInteraction.
     */
    protected HttpInteraction createInteraction(Request request, Response response, long duration) {
        return new HttpInteraction(request, response, duration);
    }

    public static class ResponseAndTime {
        public final Response response;
        public final long time;

        /**
         * Constructor for ResponseAndTime.
         *
         * @param response Response
         * @param time     long
         */
        public ResponseAndTime(Response response, long time) {
            this.response = response;
            this.time = time;
        }
    }
}
