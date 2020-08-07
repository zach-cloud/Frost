package settings;

public final class MpqSettings {

    public enum LogSettings {
        DEBUG,
        INFO,
        WARN,
        ERROR,
        NONE
    }

    public enum MpqOpenSettings {
        ANY,
        CRITICAL,
        NONE
    }

    public enum CompressionSettings {
        MAX,
        DEFLATE,
        NONE
    }

    public enum SecuritySettings {
        ENCRYPTION_ENABLED,
        ENCRYPTION_DISABLED
    }

    private LogSettings logSettings = LogSettings.WARN;
    private MpqOpenSettings mpqOpenSettings = MpqOpenSettings.ANY;
    private CompressionSettings compressionSettings = CompressionSettings.DEFLATE;
    private SecuritySettings securitySettings = SecuritySettings.ENCRYPTION_ENABLED;

    public MpqSettings(LogSettings logSettings, MpqOpenSettings mpqOpenSettings,
                       CompressionSettings compressionSettings, SecuritySettings securitySettings) {
        this.logSettings = logSettings;
        this.mpqOpenSettings = mpqOpenSettings;
        this.compressionSettings = compressionSettings;
        this.securitySettings = securitySettings;
    }

    public MpqSettings(LogSettings logSettings) {
        this.logSettings = logSettings;
    }

    public MpqSettings(MpqOpenSettings mpqOpenSettings) {
        this.mpqOpenSettings = mpqOpenSettings;
    }

    public MpqSettings(CompressionSettings compressionSettings) {
        this.compressionSettings = compressionSettings;
    }

    public MpqSettings(SecuritySettings securitySettings) {
        this.securitySettings = securitySettings;
    }

    public MpqSettings(LogSettings logSettings, MpqOpenSettings mpqOpenSettings) {
        this.logSettings = logSettings;
        this.mpqOpenSettings = mpqOpenSettings;
    }

    public MpqSettings(LogSettings logSettings, MpqOpenSettings mpqOpenSettings,
                       CompressionSettings compressionSettings) {
        this.logSettings = logSettings;
        this.mpqOpenSettings = mpqOpenSettings;
        this.compressionSettings = compressionSettings;
    }

    public MpqSettings(MpqOpenSettings mpqOpenSettings, CompressionSettings compressionSettings) {
        this.mpqOpenSettings = mpqOpenSettings;
        this.compressionSettings = compressionSettings;
    }

    public MpqSettings(LogSettings logSettings, CompressionSettings compressionSettings) {
        this.logSettings = logSettings;
        this.compressionSettings = compressionSettings;
    }

    public MpqSettings(LogSettings logSettings, SecuritySettings securitySettings) {
        this.logSettings = logSettings;
        this.securitySettings = securitySettings;
    }

    public MpqSettings(MpqOpenSettings mpqOpenSettings, SecuritySettings securitySettings) {
        this.mpqOpenSettings = mpqOpenSettings;
        this.securitySettings = securitySettings;
    }

    public MpqSettings(CompressionSettings compressionSettings, SecuritySettings securitySettings) {
        this.compressionSettings = compressionSettings;
        this.securitySettings = securitySettings;
    }

    public MpqSettings(MpqOpenSettings mpqOpenSettings, CompressionSettings compressionSettings,
                       SecuritySettings securitySettings) {
        this.mpqOpenSettings = mpqOpenSettings;
        this.compressionSettings = compressionSettings;
        this.securitySettings = securitySettings;
    }

    public MpqSettings(LogSettings logSettings, CompressionSettings compressionSettings,
                       SecuritySettings securitySettings) {
        this.logSettings = logSettings;
        this.compressionSettings = compressionSettings;
        this.securitySettings = securitySettings;
    }

    public MpqSettings(LogSettings logSettings, MpqOpenSettings mpqOpenSettings,
                       SecuritySettings securitySettings) {
        this.logSettings = logSettings;
        this.mpqOpenSettings = mpqOpenSettings;
        this.securitySettings = securitySettings;
    }

    public MpqSettings() {
    }

    public LogSettings getLogSettings() {
        return logSettings;
    }

    public void setLogSettings(LogSettings logSettings) {
        this.logSettings = logSettings;
    }

    public MpqOpenSettings getMpqOpenSettings() {
        return mpqOpenSettings;
    }

    public void setMpqOpenSettings(MpqOpenSettings mpqOpenSettings) {
        this.mpqOpenSettings = mpqOpenSettings;
    }

    public CompressionSettings getCompressionSettings() {
        return compressionSettings;
    }

    public void setCompressionSettings(CompressionSettings compressionSettings) {
        this.compressionSettings = compressionSettings;
    }

    public SecuritySettings getSecuritySettings() {
        return securitySettings;
    }

    public void setSecuritySettings(SecuritySettings securitySettings) {
        this.securitySettings = securitySettings;
    }
}
