package settings;

import exception.MpqException;

public final class MpqErrorHandler {

    private MpqSettings settings;
    private MpqLogger logger;

    public MpqErrorHandler(MpqSettings settings, MpqLogger logger) {
        this.settings = settings;
        this.logger = logger;
    }

    public void handleError(String errorMessage, boolean critical) {
        logger.error(errorMessage);
        if(settings.getMpqOpenSettings() == MpqSettings.MpqOpenSettings.ANY ||
                (critical && settings.getMpqOpenSettings() == MpqSettings.MpqOpenSettings.CRITICAL)) {
            throw new MpqException(errorMessage);
        }
    }

    public void handleCriticalError(String errorMessage) {
        handleError(errorMessage, true);
    }

    public void handleError(String errorMessage) {
        handleError(errorMessage, false);
    }
}
