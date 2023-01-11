package com.easypost.easyvcr.internal;

import com.easypost.easyvcr.ExpirationActions;
import com.easypost.easyvcr.Mode;
import com.easypost.easyvcr.RecordingExpirationException;

public abstract class ExpirationActionExtensions {
    /**
     * Check if the expiration action is valid for the given mode.
     *
     * @param action ExpirationAction to check.
     * @param mode   Mode to check.
     * @throws RecordingExpirationException If the action is not valid for the given mode.
     */
    public static void checkCompatibleSettings(ExpirationActions action, Mode mode)
            throws RecordingExpirationException {
        if (action == ExpirationActions.RecordAgain && mode == Mode.Replay) {
            throw new RecordingExpirationException(
                    "Cannot use the RecordAgain expiration action in combination with Replay mode.");
        }
    }
}
