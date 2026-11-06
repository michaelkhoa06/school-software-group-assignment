import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/*
-=[ About ]=-
Creates, reads and writes a configuration properties file.
Provides access to a cached state of configuration values.

-=[ Usage ]=-
Access:
Components that need to access Config values should be passed a reference to the global Config instance
in their constructor, then read values via the Config's getters. Only one Config should exist!

Saving:
Edit values with edit(). Edits are available immediately, and save to file after 10 seconds of inactivity.
Manually trigger a save with saveNow(). Only call this ONCE when closing the program!
*/

public class Config {
    // [!] Edit when adding new keys
    private static final Properties DEFAULT_P = new Properties();
    static {
        DEFAULT_P.setProperty(Keys.WIDTH, "1600");
        DEFAULT_P.setProperty(Keys.HEIGHT, "900");
        DEFAULT_P.setProperty(Keys.DARK_MODE, "false");
        DEFAULT_P.setProperty(Keys.SPIN_TIME, "5");
    }
    private static final String PROPERTIES_NAME = "settings.properties";

    private final Properties p = new Properties();
    private final Path dataFolder;
    private Path cfgFile = null;
    private boolean isDirty = false;

    private final ScheduledExecutorService autoSave = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> pendingSave;
    private static final int autoSaveDelaySec = 10;

    // [!] Edit when adding new keys
    private int windowedWidth;
    private int windowedHeight;
    private boolean isDarkMode;
    private int spinTimeSec;

    protected Config() {
        dataFolder = OSUtil.getDataFolder();

        // Try to read from the file. This is the only time the file is read.
        if (dataFolder != null) {
            cfgFile = dataFolder.resolve(PROPERTIES_NAME).toFile().toPath();
            try (InputStream inputStream = Files.newInputStream(cfgFile);
                 Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {

                p.load(reader);
                load();
            }

            // Cannot read config file, attempt to recreate it then load defaults.
            catch (IOException e) {
                System.out.println("Unable to load config. Loading default values...\n" + e);
                replaceConfigFile();
                p.clear();
                p.putAll(DEFAULT_P);
                load();
            }
        }

        // Cannot find config file folder, load defaults.
        else {
            p.clear();
            p.putAll(DEFAULT_P);
            load();
        }
    }


    // =================================================================
    // Public Methods
    // =================================================================

    // Use to make changes to the config values. Changes save to file after 10 seconds of inactivity.
    public void edit(String key, String value) {
        if (value != null) {
            p.setProperty(key, value);
            updateGetterValue(key, value);
            isDirty = true;
            scheduleSave();
        }
    }

    // Treat this as private! Only call it during program shutdown.
    public void saveNow() {
        cancelPendingSave();
        autoSave.execute(this::saveToFile);
    }


    // =================================================================
    // In-Memory Config State Getters (use these!)
    // =================================================================
    // [!] Edit when adding new keys

    public int windowedWidth() {
        return  windowedWidth;
    }

    public int windowedHeight() {
        return windowedHeight;
    }

    public boolean isDarkMode() {
        return isDarkMode;
    }

    public int spinTimeSec() {
        return spinTimeSec;
    }


    // =================================================================
    // Private Methods (all sections below are also private)
    // =================================================================

    // Load property changes into memory and make accessible via getters (not read from file).
    private void load() {
        // Populate missing key fields with default values
        for (String dKey : DEFAULT_P.stringPropertyNames()) {
            if (!p.stringPropertyNames().contains(dKey)) {
                p.setProperty(dKey, DEFAULT_P.getProperty(dKey));
                System.out.println("Configuration file missing key '" + dKey + "'");
            }
        }

        // Make values accessible via getters
        for (String key : p.stringPropertyNames()) {
            String value = p.getProperty(key);

            updateGetterValue(key, value);
        }

        System.out.println("Config successfully reloaded!");
    }

    // [!] Edit when adding new keys
    private void updateGetterValue(String key, String value) {
        switch (key) {
            case Keys.WIDTH -> windowedWidth = Math.max(parseIntOrDefault(value, key), 360);
            case Keys.HEIGHT -> windowedHeight = Math.max(parseIntOrDefault(value, key), 360);
            case Keys.DARK_MODE -> isDarkMode = Boolean.parseBoolean(value);
            case Keys.SPIN_TIME -> spinTimeSec = parseIntOrDefault(value, key);
            default -> System.out.println("Skipping unrecognized config key '" + key + "'");
        }
    }

    public void saveToFile() {
        if (isDirty) {
            cancelPendingSave();
            try (Writer writer = Files.newBufferedWriter(cfgFile, StandardCharsets.UTF_8)) {
                writeFile(writer, p, "Modified Configuration");
                isDirty = false;
                System.out.println("Config saved successfully!");
            } catch (IOException e) {
                System.out.println("Failed to save/create config file.\n" + e);
            }
        }
    }

    // [!] Edit when adding new keys
    private void writeFile(Writer writer, Properties prop, String title) throws IOException {
        writer.write("# " + title + "\n");
        writer.write("\n");
        writer.write("# Display" + "\n");
        writer.write(Keys.WIDTH + "=" + prop.getProperty(Keys.WIDTH) + "\n");
        writer.write(Keys.HEIGHT + "=" + prop.getProperty(Keys.HEIGHT) + "\n");
        writer.write(Keys.DARK_MODE + "=" + prop.getProperty(Keys.DARK_MODE) + "\n");
        writer.write("\n");
        writer.write("# Misc" + "\n");
        writer.write(Keys.SPIN_TIME + "=" + prop.getProperty(Keys.SPIN_TIME) + "\n");
    }

    private void replaceConfigFile() {
        // Attempt to create the directory
        if (!Files.exists(dataFolder)) {
            try {
                Files.createDirectories(dataFolder);
            } catch (IOException e) {
                System.out.println("Failed to create config directory. You will not be able to save any changes you make!\n" + e);
                return;
            }
        }

        // If a config exists, it is unreadable. Move it to settings.properties.bad.
        if (Files.exists(cfgFile)) {
            try {
                Files.move(cfgFile, dataFolder.resolve(PROPERTIES_NAME + ".bad"), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Malformed config moved to: " + dataFolder.resolve(PROPERTIES_NAME + ".bad"));
            } catch (IOException e) {
                System.out.println("Unable to move malformed config " + cfgFile.getFileName() + " to " + dataFolder.resolve(PROPERTIES_NAME + ".bad\n") + e);
                return;
            }
        }

        // Write default config
        try (Writer writer = Files.newBufferedWriter(cfgFile, StandardCharsets.UTF_8)) {
            writeFile(writer, DEFAULT_P, "Default Configuration");
            System.out.println("Default config created successfully!");
        } catch (IOException e) {
            System.out.println("Failed to create default config file. You will not be able to save any changes you make!\n" + e);
        }
    }

    private void scheduleSave() {
        cancelPendingSave();
        pendingSave = autoSave.schedule(this::saveToFile, Config.autoSaveDelaySec, TimeUnit.SECONDS);
    }

    private void cancelPendingSave() {
        if (pendingSave != null) {
            pendingSave.cancel(false);
            pendingSave = null;
        }
    }


    // =================================================================
    // Parsing Helpers (private)
    // =================================================================

    private static int parseIntOrDefault(String value, String key) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException | NullPointerException e) {
            String defaultValue = DEFAULT_P.getProperty(key);
            System.out.println("Malformed config value (int) '" + value + "' for key '" + key +"'. Using default value: '" + defaultValue + "'\n" + e);
            return Integer.parseInt(defaultValue);
        }
    }

    private static long parseLongOrDefault(String value, String key) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException | NullPointerException e) {
            String defaultValue = DEFAULT_P.getProperty(key);
            System.out.println("Malformed config value (long) '" + value + "' for key '" + key +"'. Using default value: '" + defaultValue + "'\n" + e);
            return Long.parseLong(defaultValue);
        }
    }

    private static float parseFloatOrDefault(String value, String key) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException | NullPointerException e) {
            String defaultValue = DEFAULT_P.getProperty(key);
            System.out.println("Malformed config value (float) '" + value + "' for key '" + key +"'. Using default value: '" + defaultValue + "'\n" + e);
            return Float.parseFloat(defaultValue);
        }
    }

    private static double parseDoubleOrDefault(String value, String key) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException | NullPointerException e) {
            String defaultValue = DEFAULT_P.getProperty(key);
            System.out.println("Malformed config value (double) '" + value + "' for key '" + key +"'. Using default value: '" + defaultValue + "'\n" + e);
            return Double.parseDouble(defaultValue);
        }
    }


    // =================================================================
    // Config File Keys (private)
    // =================================================================
    // [!] Edit when adding new keys

    private static class Keys {
        private static final String WIDTH = "iWindowedWidth";
        private static final String HEIGHT = "iWindowedHeight";
        private static final String DARK_MODE = "bDarkMode";
        private static final String SPIN_TIME = "iSpinTimeSec";
    }
}
