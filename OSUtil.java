import java.nio.file.Path;
import java.util.Locale;

/*
-=[ About ]=-
Helper to get the user's OS.
Used to determine where to store files.

-=[ Usage ]=-
Check OS type with OSUtil.getOS().
Should only be needed by storage component.
*/

public class OSUtil {
    public enum OS {
        WINDOWS, LINUX, MAC
    }

    private static OS os = null;

    private static final String DATA_FOLDER_NAME = "RoulettePlanner";
    private static Path dataFolder = null;

    // Hide constructor
    private OSUtil() {}

    public static OS getOS() {
        if (os == null) {
            String operatingSys = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
            if (operatingSys.contains("win")) {
                os = OS.WINDOWS;
            } else if (operatingSys.contains("nix") || operatingSys.contains("nux") || operatingSys.contains("aix")) {
                os = OS.LINUX;
            } else if (operatingSys.contains("mac")) {
                os = OS.MAC;
            }
        }
        return os;
    }

    public static Path getDataFolder() {
        if (dataFolder == null) {
            switch (OSUtil.getOS()) {
                case WINDOWS -> dataFolder = Path.of(System.getenv("APPDATA"), DATA_FOLDER_NAME);
                case LINUX -> {
                    String xdg = System.getenv("XDG_CONFIG_HOME");
                    if (xdg != null) dataFolder = Path.of(xdg, DATA_FOLDER_NAME);
                    else dataFolder = Path.of(System.getProperty("user.home"), ".config", DATA_FOLDER_NAME);
                }
                case MAC -> dataFolder = Path.of(System.getProperty("user.home"), "Library", "Application Support", DATA_FOLDER_NAME);
                default -> System.out.println("Unknown operating system. I don't know where to save files!");
            }
        }
        System.out.println(dataFolder);

        return dataFolder;
    }
}
