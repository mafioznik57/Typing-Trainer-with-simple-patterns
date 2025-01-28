package com.example.demo1;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

// Singleton Pattern
class UserRecordManager {
    private static UserRecordManager instance;
    private final Map<String, Double> userRecords = new HashMap<>();

    private UserRecordManager() {}

    public static UserRecordManager getInstance() {
        if (instance == null) {
            instance = new UserRecordManager();
        }
        return instance;
    }

    public double getRecord(String username) {
        return userRecords.getOrDefault(username, 0.0);
    }

    public void updateRecord(String username, double wpm) {
        userRecords.put(username, wpm);
    }

    public Map<String, Double> getAllRecords() {
        return userRecords;
    }
}

// Strategy Pattern
interface WPMCalculationStrategy {
    double calculateWPM(String typedText, int timeInSeconds);
}

class StandardWPMCalculationStrategy implements WPMCalculationStrategy {
    @Override
    public double calculateWPM(String typedText, int timeInSeconds) {
        double totalTimeInMinutes = timeInSeconds / 60.0;
        return (typedText.length() / 5.0) / totalTimeInMinutes;
    }
}

// Factory Method Pattern
abstract class TextGenerator {
    public abstract String generateText();
}

class LanguageTextGenerator extends TextGenerator {
    private final String language;
    private final Map<String, List<String>> textsByLanguage;

    public LanguageTextGenerator(String language) {
        this.language = language;
        textsByLanguage = new HashMap<>();

        textsByLanguage.put("Kazakh", Arrays.asList(
                "Жылдам теру дағдыларыңызды дамыту маңызды.",
                "Жазу практикасы жылдамдығыңыз бен дәлдігіңізді жақсартады.",
                "Java - әмбебап және танымал бағдарламалау тілі.",
                "Үздіксіз практика шеберлікке жетелейді.",
                "Java-ны үйрену түрлі мүмкіндіктерге жол ашады."
        ));
        textsByLanguage.put("Russian", Arrays.asList(
                "Быстрая печать помогает развивать навыки.",
                "Практика печати улучшает вашу скорость и точность.",
                "Java - универсальный и популярный язык программирования.",
                "Постоянная практика приводит к мастерству.",
                "Изучение Java открывает новые возможности."
        ));
        textsByLanguage.put("English", Arrays.asList(
                "Typing quickly is an essential skill.",
                "Typing practice improves your speed and accuracy.",
                "Java is a versatile and popular programming language.",
                "Consistent practice leads to mastery.",
                "Learning Java opens doors to various opportunities."
        ));
    }

    @Override
    public String generateText() {
        List<String> texts = textsByLanguage.getOrDefault(language, textsByLanguage.get("English"));
        Random random = new Random();
        return texts.get(random.nextInt(texts.size()));  // Randomly select a text
    }
}

// Adapter Pattern
class TextAdapter {
    private final String text;

    public TextAdapter(String text) {
        this.text = text;
    }

    public List<Text> getTextFlow() {
        List<Text> textNodes = new ArrayList<>();
        for (char c : text.toCharArray()) {
            Text textNode = new Text(String.valueOf(c));
            textNode.setFill(Color.YELLOWGREEN);
            textNodes.add(textNode);
        }
        return textNodes;
    }
}

// Observer Pattern
interface Observer {
    void update(String message);
}

interface Subject {
    void attach(Observer observer);
    void detach(Observer observer);
    void notifyObservers(String message);
}

class TypingTestSubject implements Subject {
    private final List<Observer> observers = new ArrayList<>();

    @Override
    public void attach(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String message) {
        for (Observer observer : observers) {
            observer.update(message);
        }
    }

    public void updateWPM(double wpm) {
        // This method would be triggered when WPM is calculated
        notifyObservers("Your current WPM is: " + wpm);
    }

    public void changeLanguage(String language) {
        // This method would be triggered when the language changes
        notifyObservers("Language changed to: " + language);
    }
}

class ScoreBoardObserver implements Observer {
    private final Text scoreText;

    public ScoreBoardObserver(Text scoreText) {
        this.scoreText = scoreText;
    }

    @Override
    public void update(String message) {
        Platform.runLater(() -> scoreText.setText(message)); // Update UI on the JavaFX thread
    }
}

// Main application class
public class BahaYeraBaq extends Application {
    private String currentUser = null;
    private String currentText = "";
    private int timeRemaining = 30;
    private int selectedTime = 30;
    private Timeline timer;
    private TextFlow textFlow;
    private TextArea typingArea;
    private Text timerText;
    private ComboBox<Integer> timeSelector;
    private ComboBox<String> languageSelector;
    private boolean isTestRunning = true;
    private boolean isTypingStarted = false;
    private String currentLanguage = "English";

    private final Map<String, String> userCredentials = new HashMap<>();
    private final UserRecordManager recordManager = UserRecordManager.getInstance();
    private WPMCalculationStrategy wpmCalculationStrategy = new StandardWPMCalculationStrategy();

    // Observer and Subject
    private TypingTestSubject typingTestSubject;
    private ScoreBoardObserver scoreBoardObserver;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        showLoginScreen(primaryStage);
    }

    private void showLoginScreen(Stage primaryStage) {
        GridPane loginPane = new GridPane();
        loginPane.setHgap(10);
        loginPane.setVgap(10);
        loginPane.setStyle("-fx-padding: 20;");

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

        loginPane.add(new Label("Username:"), 0, 0);
        loginPane.add(usernameField, 1, 0);
        loginPane.add(new Label("Password:"), 0, 1);
        loginPane.add(passwordField, 1, 1);
        loginPane.add(loginButton, 1, 2);
        loginPane.add(registerButton, 1, 3);

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (userCredentials.containsKey(username) && userCredentials.get(username).equals(password)) {
                currentUser = username;
                showTypingTrainer(primaryStage);
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Incorrect username or password.");
            }
        });

        registerButton.setOnAction(e -> showRegisterScreen(primaryStage));

        Scene scene = new Scene(loginPane, 400, 250);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login");
        primaryStage.show();
    }

    private void showRegisterScreen(Stage primaryStage) {
        GridPane registerPane = new GridPane();
        registerPane.setHgap(10);
        registerPane.setVgap(10);
        registerPane.setStyle("-fx-padding: 20;");

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        PasswordField confirmPasswordField = new PasswordField();
        Button registerButton = new Button("Register");
        Button backButton = new Button("Back");

        registerPane.add(new Label("Username:"), 0, 0);
        registerPane.add(usernameField, 1, 0);
        registerPane.add(new Label("Password:"), 0, 1);
        registerPane.add(passwordField, 1, 1);
        registerPane.add(new Label("Confirm Password:"), 0, 2);
        registerPane.add(confirmPasswordField, 1, 2);
        registerPane.add(registerButton, 1, 3);
        registerPane.add(backButton, 1, 4);

        registerButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (userCredentials.containsKey(username)) {
                showAlert(Alert.AlertType.ERROR, "Registration Failed", "Username already exists.");
            } else if (!password.equals(confirmPassword)) {
                showAlert(Alert.AlertType.ERROR, "Registration Failed", "Passwords do not match.");
            } else {
                userCredentials.put(username, password);
                recordManager.updateRecord(username, 0.0);
                showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "You can now log in.");
                showLoginScreen(primaryStage);
            }
        });

        backButton.setOnAction(e -> showLoginScreen(primaryStage));

        Scene scene = new Scene(registerPane, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Register");
        primaryStage.show();
    }

    private void showTypingTrainer(Stage primaryStage) {
        typingTestSubject = new TypingTestSubject(); // Create the subject

        // Create a score text label that will be updated by the observer
        Text scoreText = new Text("Your current WPM: 0.0");
        scoreBoardObserver = new ScoreBoardObserver(scoreText); // Create the observer

        // Attach the observer to the subject
        typingTestSubject.attach(scoreBoardObserver);

        BorderPane root = new BorderPane();
        textFlow = new TextFlow();
        textFlow.setStyle("-fx-padding: 20; -fx-font-size: 16px;");
        root.setCenter(textFlow);

        typingArea = new TextArea();
        typingArea.setWrapText(true);
        typingArea.setPromptText("Start typing...");
        typingArea.setOnKeyReleased(e -> {
            if (!isTypingStarted) {
                isTypingStarted = true;
                timer.play();
            }
            checkTyping(typingArea.getText());
        });

        Button generateButton = new Button("Generate New Text");
        generateButton.setOnAction(e -> resetTest());

        Button retryButton = new Button("Retry");
        retryButton.setOnAction(e -> retryTest());

        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            currentUser = null;
            timer.stop();
            showLoginScreen(primaryStage);
        });

        Button recordsButton = new Button("Records");
        recordsButton.setOnAction(e -> showRecords());

        timerText = new Text("Time: " + timeRemaining + "s");
        timerText.setStyle("-fx-font-size: 20px; -fx-padding: 10;");

        timeSelector = new ComboBox<>();
        timeSelector.getItems().addAll(15, 30, 45, 60);
        timeSelector.setValue(selectedTime);
        timeSelector.setOnAction(e -> {
            selectedTime = timeSelector.getValue();
            if (!isTestRunning) {
                resetTest();
            }
        });

        languageSelector = new ComboBox<>();
        languageSelector.getItems().addAll("English", "Kazakh", "Russian");
        languageSelector.setValue(currentLanguage);
        languageSelector.setOnAction(e -> {
            currentLanguage = languageSelector.getValue();
            changeLanguage();
        });

        HBox topBox = new HBox(10, timerText, timeSelector, languageSelector, generateButton, retryButton, logoutButton, recordsButton, scoreText);
        topBox.setStyle("-fx-padding: 10; -fx-alignment: center;");

        root.setTop(topBox);
        root.setBottom(typingArea);

        setupTimer();

        Scene scene = new Scene(root, 950, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Typing Trainer - Logged in as " + currentUser);
        primaryStage.show();

        generateNewText();
    }

    private int countErrors() {
        int totalErrors = 0;
        String typedText = typingArea.getText();

        // Check each character of the text
        for (int i = 0; i < currentText.length(); i++) {
            if (i < typedText.length()) {
                if (typedText.charAt(i) != currentText.charAt(i)) {
                    totalErrors++; // Error found
                }
            } else {
                totalErrors++; // Characters not typed yet
            }
        }

        return totalErrors; // Total errors count
    }

    private void setupTimer() {
        // Timer works every second
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeRemaining--; // Decrease remaining time
            timerText.setText("Time: " + timeRemaining + "s"); // Update the time on screen

            // Stop the test when the timer reaches zero
            if (timeRemaining <= 0) {
                timer.stop(); // Stop the timer
                isTestRunning = false; // Test finished
                showTestResults(countErrors()); // Show results and calculate errors
            }
        }));

        timer.setCycleCount(selectedTime); // Timer works only for `selectedTime` seconds
    }

    private void generateNewText() {
        TextGenerator generator = new LanguageTextGenerator(currentLanguage);
        currentText = generator.generateText();
        updateTextFlow();
    }

    private void resetTest() {
        timer.stop();
        isTestRunning = true;
        isTypingStarted = false;
        timeRemaining = selectedTime;
        timerText.setText("Time: " + timeRemaining + "s");
        typingArea.clear();
        generateNewText();  // Generate new text
    }

    private void retryTest() {
        resetTest();
    }

    private void updateTextFlow() {
        textFlow.getChildren().clear();
        TextAdapter adapter = new TextAdapter(currentText);
        textFlow.getChildren().addAll(adapter.getTextFlow());
    }

    private void checkTyping(String typedText) {
        List<Text> textNodes = new ArrayList<>();
        int totalErrors = 0;

        // Check each character of the text
        for (int i = 0; i < currentText.length(); i++) {
            Text textNode = new Text(String.valueOf(currentText.charAt(i)));
            if (i < typedText.length()) {
                if (typedText.charAt(i) == currentText.charAt(i)) {
                    textNode.setFill(Color.GREEN); // Correctly typed
                } else {
                    textNode.setFill(Color.RED); // Incorrectly typed
                    totalErrors++; // Error found
                }
            } else {
                textNode.setFill(Color.BLACK); // Not typed yet
                totalErrors++; // Not fully typed
            }
            textNodes.add(textNode);
        }

        // Update the text display
        textFlow.getChildren().setAll(textNodes);

        // Show results when the text is completely typed
        if (typedText.length() >= currentText.length()) {
            timer.stop(); // Stop the timer
            isTestRunning = false; // Test stopped
            showTestResults(totalErrors); // Show results
        }
    }

    private void changeLanguage() {
        resetTest();
    }

    private void showTestResults(int totalErrors) {
        // Calculate error percentage
        double errorPercentage = (totalErrors / (double) currentText.length()) * 100;

        // Calculate WPM
        double wpm = wpmCalculationStrategy.calculateWPM(typingArea.getText(), selectedTime - timeRemaining);

        // Reduce WPM based on error percentage
        double penalty = errorPercentage * 0.1; // Reduce 0.1% for each error
        wpm -= wpm * (penalty / 100); // Reduce WPM by the error percentage

        // Show results
        showAlert(Alert.AlertType.INFORMATION, "Test Completed", "WPM: " + String.format("%.2f", wpm) +
                "\nErrors: " + totalErrors + " (" + String.format("%.2f", errorPercentage) + "% errors)");

        // Update record if new best score
        if (wpm > recordManager.getRecord(currentUser)) {
            recordManager.updateRecord(currentUser, wpm);
        }
    }

    private void showRecords() {
        Map<String, Double> records = recordManager.getAllRecords();
        StringBuilder recordList = new StringBuilder("User Records:\n");
        for (String username : records.keySet()) {
            recordList.append(username).append(": ").append(String.format("%.2f", records.get(username))).append(" WPM\n");
        }
        showAlert(Alert.AlertType.INFORMATION, "Records", recordList.toString());
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}




//1. Singleton Pattern
//        Purpose: The Singleton pattern ensures that only one instance of the UserRecordManager class is created and used throughout the application. This is particularly useful for managing shared resources, such as user records. By using this pattern, you avoid multiple instances of the UserRecordManager, ensuring consistency and controlled access to user data.
//        2. Strategy Pattern
//        Purpose: The Strategy pattern allows the TypingTrainerApp to dynamically change the way Words Per Minute (WPM) is calculated based on different algorithms. In your application, the WPMCalculationStrategy interface defines a strategy for calculating WPM, and the StandardWPMCalculationStrategy provides one implementation. If you want to introduce other ways of calculating WPM in the future (for example, based on errors or accuracy), you can do so by implementing new strategies without changing the existing code.
//        3. Factory Method Pattern
//        Purpose: The Factory Method pattern is used in the LanguageTextGenerator class. This pattern allows the application to create different types of text content based on the selected language (English, Kazakh, Russian) without specifying the exact class to instantiate. The pattern makes it easy to introduce new languages or text sources by extending the TextGenerator class, ensuring the system is flexible and extensible.
//4. Adapter Pattern
//        Purpose: The Adapter pattern is used to convert a String (the text to be typed) into a format that JavaFX can handle for display. In this case, the TextAdapter class takes a string and transforms it into Text nodes, which are used by the TextFlow container. This pattern is useful when you want to integrate a system that works with incompatible interfaces, allowing them to work together without modifying the original code.
//5. Observer Pattern (Implicitly used)
//        Purpose: The Observer pattern is not explicitly implemented in your code, but its principles are followed. The user’s actions (such as typing or selecting different options) trigger changes in the UI. For example, as the user types, the checkTyping method updates the color of text characters (green for correct, red for incorrect). The Observer pattern would formalize this by having observers (UI elements) respond to changes in the application state (such as user input) and update the display accordingly.
//        Overall Comments:
//        These design patterns together help make your application flexible, maintainable, and scalable.
//        The Singleton Pattern ensures the user record manager has a single point of access.
//        The Strategy Pattern makes it easy to adjust the WPM calculation method as needed.
//        The Factory Method Pattern facilitates the creation of different text sources, enhancing flexibility.
//        The Adapter Pattern integrates text handling with the JavaFX UI seamlessly.
//        The Observer Pattern (though not explicitly implemented) allows the UI to respond dynamically to user inputs.
//        By using these patterns, your application is prepared for future extensions, such as adding more languages, different typing test modes, or additional features without modifying existing logic. The application can evolve while maintaining its structure and flexibility.
//
