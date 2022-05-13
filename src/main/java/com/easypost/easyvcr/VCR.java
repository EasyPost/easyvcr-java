package com.easypost.easyvcr;

import com.easypost.easyvcr.clients.httpurlconnection.RecordableURL;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Built-in VCR tool for EasyVCR.
 */
public final class VCR {
    /**
     * The current cassette in the VCR.
     */
    private Cassette currentCassette;
    /**
     * The operating mode of the VCR.
     */
    private Mode mode;
    /**
     * Advanced settings for the VCR.
     */
    private AdvancedSettings advancedSettings;

    /**
     * Constructor for VCR.
     *
     * @param advancedSettings Advanced settings for the VCR.
     */
    public VCR(AdvancedSettings advancedSettings) {
        this.advancedSettings = advancedSettings;
    }

    /**
     * Constructor for VCR.
     */
    public VCR() {
        this.advancedSettings = new AdvancedSettings();
    }

    /**
     * Gets the name of the current cassette in the VCR.
     *
     * @return The name of the current cassette in the VCR.
     */
    public String getCassetteName() {
        if (this.currentCassette == null) {
            return null;
        }
        return this.currentCassette.name;
    }

    /**
     * Retrieve a pre-configured RecordableURL object that will use the VCR.
     *
     * @param url The URL to use to create the RecordableURL object.
     * @return A pre-configured RecordableURL object that will use the VCR.
     * @throws MalformedURLException If the URL is malformed.
     * @throws VCRException          If the cassette is not loaded.
     */
    public RecordableURL getHttpUrlConnection(URL url) throws MalformedURLException, VCRException {
        if (this.currentCassette == null) {
            throw new VCRException("No cassette is currently loaded.");
        }
        return new RecordableURL(url, this.currentCassette, this.mode, this.advancedSettings);
    }

    /**
     * Retrieve a pre-configured RecordableURL object that will use the VCR.
     *
     * @param url The URL string to use to create the RecordableURL object.
     * @return A pre-configured RecordableURL object that will use the VCR.
     * @throws MalformedURLException If the URL is malformed.
     * @throws VCRException          If the cassette is not loaded.
     */
    public RecordableURL getHttpUrlConnection(String url) throws MalformedURLException, VCRException {
        if (this.currentCassette == null) {
            throw new VCRException("No cassette is currently loaded.");
        }
        return new RecordableURL(url, this.currentCassette, this.mode, this.advancedSettings);
    }

    /**
     * Gets the current operating mode of the VCR.
     *
     * @return The current operating mode of the VCR.
     */
    public Mode getMode() {
        if (this.mode == Mode.Bypass) {
            return Mode.Bypass;
        }
        Mode environmentMode = getModeFromEnvironment();
        return environmentMode != null ? environmentMode : this.mode;
    }

    /**
     * Set the mode for the VCR.
     *
     * @param mode The mode to set for the VCR.
     */
    private void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * Get the advanced settings for the VCR.
     *
     * @return Advanced settings for the VCR.
     */
    public AdvancedSettings getAdvancedSettings() {
        return this.advancedSettings;
    }

    /**
     * Set the advanced settings for the VCR.
     *
     * @param advancedSettings Advanced settings for the VCR.
     */
    public void setAdvancedSettings(AdvancedSettings advancedSettings) {
        this.advancedSettings = advancedSettings;
    }

    /**
     * Remove the current cassette from the VCR.
     */
    public void eject() {
        this.currentCassette = null;
    }

    /**
     * Erase the cassette in the VCR.
     */
    public void erase() {
        if (this.currentCassette != null) {
            this.currentCassette.erase();
        }
    }

    /**
     * Add a cassette to the VCR (or replace the current one).
     *
     * @param cassette The cassette to add to the VCR.
     */
    public void insert(Cassette cassette) {
        this.currentCassette = cassette;
    }

    /**
     * Enable passthrough mode on the VCR (HTTP requests will be made as normal).
     */
    public void pause() {
        setMode(Mode.Bypass);
    }

    /**
     * Enable recording mode on the VCR.
     */
    public void record() {
        setMode(Mode.Record);
    }

    /**
     * Enable playback mode on the VCR.
     */
    public void replay() {
        setMode(Mode.Replay);
    }

    /**
     * Enable auto mode on the VCR (record if needed, replay otherwise).
     */
    public void recordIfNeeded() {
        setMode(Mode.Auto);
    }

    /**
     * Get the current operating mode of the VCR from an environment variable.
     *
     * @return The current operating mode of the VCR from an environment variable.
     */
    private Mode getModeFromEnvironment() {
        final String keyName = "EASYVCR_MODE";
        try {
            String keyValue = System.getenv(keyName);
            if (keyValue == null) {
                return null;
            }
            return Mode.valueOf(keyValue);
        } catch (Exception ignored) {
            return null;
        }
    }
}
