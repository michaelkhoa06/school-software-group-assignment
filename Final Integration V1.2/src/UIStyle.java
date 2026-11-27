import javafx.scene.control.Button;

public class UIStyle {

    private UIStyle() {}

    // Standard button
    public static void neutral(Button button) {
        styleButton(button, "#e2e8f0", "#334155", "#cbd5e1");
    }

    // Primary action button
    public static void primary(Button button) {
        styleButton(button, "#dbeafe", "#1d4ed8", "#bfdbfe");
    }

    // Positive action button
    public static void success(Button button) {
        styleButton(button, "#dcfce7", "#166534", "#bbf7d0");
    }

    // Warning action button
    public static void warning(Button button) {
        styleButton(button, "#fef3c7", "#92400e", "#fde68a");
    }

    // Negative action button
    public static void danger(Button button) {
        styleButton(button, "#fee2e2", "#b91c1c", "#fecaca");
    }

    // Shared button styling and hover effects
    private static void styleButton(
            Button button,
            String background,
            String textColor,
            String hoverColor
    ) {

        String baseStyle =
                "-fx-background-radius: 6;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 6 12 6 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-text-fill: " + textColor + ";";

        String normalStyle = baseStyle +
                "-fx-background-color: " + background + ";";

        String hoverStyle = baseStyle +
                "-fx-background-color: " + hoverColor + ";";

        button.setStyle(normalStyle);

        button.setOnMouseEntered(event ->
                button.setStyle(hoverStyle));

        button.setOnMouseExited(event ->
                button.setStyle(normalStyle));
    }
}