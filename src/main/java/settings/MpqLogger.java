package settings;

import java.io.PrintStream;

import static settings.MpqSettings.LogSettings.*;

public final class MpqLogger {

    private PrintStream out;
    private MpqSettings.LogSettings logSettings;

    /**
     * Defines a PrintStream to send messages to
     *
     * @param out      PrintStream to log to
     * @param settings MPQ Settings
     */
    public MpqLogger(PrintStream out, MpqSettings settings) {
        this.out = out;
        this.logSettings = settings.getLogSettings();
    }

    /**
     * This constructor uses System.out to log messages
     *
     * @param settings MPQ Settings
     */
    public MpqLogger(MpqSettings settings) {
        this(System.out, settings);
    }

    /**
     * Uses default log stream (System.out) and default settings (WARN)
     */
    public MpqLogger() {
        this(System.out, new MpqSettings());
    }

    public void debug(String message) {
        if (logSettings == DEBUG) {
            out.println("DEBUG: " + message);
        }
    }

    public void info(String message) {
        if (logSettings == INFO || logSettings == DEBUG) {
            out.println("INFO: " + message);
        }
    }

    public void warn(String message) {
        if (logSettings == WARN || logSettings == INFO || logSettings == DEBUG) {
            out.println("WARN: " + message);
        }
    }

    public void error(String message) {
        if (logSettings != NONE) {
            out.println("ERROR: " + message);
        }
    }
}
