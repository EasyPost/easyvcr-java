package com.easypost.easyvcr;

import com.easypost.easyvcr.internalutilities.Files;
import com.easypost.easyvcr.internalutilities.Tools;
import com.easypost.easyvcr.internalutilities.json.Serialization;
import com.easypost.easyvcr.requestelements.HttpInteraction;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Cassette used to store and retrieve requests and responses for EasyVCR.
 */
public final class Cassette {

    /**
     * The name of the cassette.
     */
    public final String name;

    /**
     * The path to the cassette file.
     */
    private final String filePath;

    /**
     * Boolean indicating if cassette is locked.
     */
    private boolean locked;

    /**
     * Constructor for Cassette.
     *
     * @param folderPath   The path to the folder where the cassette file will be stored.
     * @param cassetteName The name of the cassette.
     */
    public Cassette(String folderPath, String cassetteName) {
        name = cassetteName;
        filePath = Tools.getFilePath(folderPath, cassetteName + ".json");
    }

    /**
     * Gets the number of interactions in the cassette.
     *
     * @return The number of interactions in the cassette.
     */
    public int numInteractions() {
        try {
            return read().size();
        } catch (VCRException ex) {
            return 0;
        }
    }

    /**
     * Gets the cassette file as a File object.
     *
     * @return The cassette file as a File object.
     */
    private File getFile() {
        return Tools.getFile(filePath);
    }

    /**
     * Erase this cassette by deleting the file.
     */
    public void erase() {
        getFile().delete();
    }

    /**
     * Lock this cassette (prevent reading or writing).
     */
    public void lock() {
        locked = true;
    }

    /**
     * Unlock this cassette (allow the cassette to be used).
     */
    public void unlock() {
        locked = false;
    }

    /**
     * Read all the interactions recorded on this cassette.
     *
     * @return A list of HttpInteractions
     * @throws VCRException If the cassette could not be read
     */
    public List<HttpInteraction> read() throws VCRException {
        checkIfLocked();

        if (!fileExists()) {
            return new ArrayList<>();
        }

        ArrayList<HttpInteraction> interactions = new ArrayList<>();

        String jsonString = Files.readFile(filePath);
        if (jsonString == null) {
            return interactions; // empty list because file doesn't exist or is empty
        }
        JsonElement cassetteParseResult = JsonParser.parseString(jsonString);

        for (JsonElement interaction : cassetteParseResult.getAsJsonArray()) {
            interactions.add(Serialization.convertJsonToObject(interaction, HttpInteraction.class));
        }
        return interactions;
    }

    /**
     * Overwrite an existing interaction on this cassette, or add a new one if it doesn't exist.
     *
     * @param httpInteraction The interaction to write to the cassette
     * @param matchRules      The rules to match the interaction against
     * @param bypassSearch    If true, the cassette will not be searched for an existing interaction
     * @throws VCRException If the cassette could not be written to
     */
    public void updateInteraction(HttpInteraction httpInteraction, MatchRules matchRules, boolean bypassSearch)
            throws VCRException {
        List<HttpInteraction> existingInteractions = read();
        int matchingIndex = -1;
        if (!bypassSearch) {
            for (int i = 0; i < existingInteractions.size(); i++) {
                if (matchRules.requestsMatch(existingInteractions.get(i).getRequest(), httpInteraction.getRequest())) {
                    matchingIndex = i;
                    break;
                }
            }
        }
        if (matchingIndex < 0) {
            existingInteractions.add(httpInteraction);
        } else {
            existingInteractions.set(matchingIndex, httpInteraction);
        }

        try {
            write(existingInteractions);
        } catch (IOException ex) {
            throw new VCRException("Could not write to to cassette file");
        }
    }

    /**
     * Check if this cassette is locked.
     *
     * @throws VCRException If the cassette is locked
     */
    private void checkIfLocked() throws VCRException {
        if (locked) {
            throw new VCRException("Cassette is locked.");
        }
    }

    /**
     * Check if this cassette's file exists.
     *
     * @return True if the cassette file exists
     */
    private boolean fileExists() {
        return getFile().exists();
    }

    /**
     * Write a list of interactions to this cassette.
     *
     * @param httpInteractions The list of interactions to write to the cassette
     * @throws VCRException If the cassette could not be written to
     */
    private void write(List<HttpInteraction> httpInteractions) throws VCRException, IOException {
        checkIfLocked();

        String cassetteString = Serialization.convertObjectToJson(httpInteractions);

        Files.writeFile(filePath, cassetteString);
    }
}
