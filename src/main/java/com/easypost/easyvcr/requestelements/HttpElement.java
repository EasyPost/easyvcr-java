package com.easypost.easyvcr.requestelements;

import com.easypost.easyvcr.internal.json.Serialization;

/**
 * Base class for all EasyVCR request/response objects.
 */
public abstract class HttpElement {
    /**
     * Serialize this object to a JSON string.
     *
     * @return JSON string representation of this HttpElement object.
     */
    public String toJson() {
        return Serialization.convertObjectToJson(this);
    }
}
