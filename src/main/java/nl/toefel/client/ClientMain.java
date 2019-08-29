package nl.toefel.client;

public class ClientMain {
    public static void main(String[] args) {
        // Running like this avoids SDK runtime errors
        // See GameWindow.init() for initializations
        // https://stackoverflow.com/questions/52578072/gradle-openjfx11-error-javafx-runtime-components-are-missing
        GameWindow.main(args);
    }
}
