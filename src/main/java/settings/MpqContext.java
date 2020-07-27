package settings;

import io.FileWriter;

public class MpqContext {

    private MpqLogger logger;
    private MpqSettings settings;
    private MpqErrorHandler errorHandler;
    private FileWriter fileWriter;

    public MpqContext(MpqLogger logger, MpqSettings settings) {
        this.logger = logger;
        this.settings = settings;
        this.errorHandler = new MpqErrorHandler(settings, logger);
        this.fileWriter = new FileWriter();
    }

    public MpqContext() {
        this(new MpqLogger(), new MpqSettings());
    }

    public MpqLogger getLogger() {
        return logger;
    }

    public void setLogger(MpqLogger logger) {
        this.logger = logger;
    }

    public MpqSettings getSettings() {
        return settings;
    }

    public void setSettings(MpqSettings settings) {
        this.settings = settings;
    }

    public MpqErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(MpqErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public FileWriter getFileWriter() {
        return fileWriter;
    }

    public void setFileWriter(FileWriter fileWriter) {
        this.fileWriter = fileWriter;
    }
}
