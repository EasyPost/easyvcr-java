package com.easypost.easyvcr;

import java.util.logging.Logger;

public final class AdvancedSettings {
    public MatchRules matchRules = MatchRules.regular();

    public Censors censors = Censors.regular();

    public boolean simulateDelay = false;

    public int manualDelay = 0;

    public TimeFrame timeFrame = TimeFrame.forever();

    public ExpirationActions whenExpired = ExpirationActions.Warn;

    public Logger logger = null;
}
