package com.payroll.controller;

import com.payroll.config.Messages;
import com.payroll.i18n.MessageKeys;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Loads FXML screens with Spring-managed controllers and the shared stylesheet.
 * {@code prepareController} runs after loading and before the screen is shown.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScreenNavigator {

    public static final WindowSize MAIN_WINDOW = new WindowSize(1200, 800);

    private static final String STYLESHEET = "/css/ios-style.css";

    private final ApplicationContext applicationContext;
    private final Messages messages;

    public record WindowSize(double width, double height) {
    }

    /** Replaces the screen in the window that {@code nodeOnCurrentScreen} belongs to. */
    public <C> void showScreen(Node nodeOnCurrentScreen, String fxmlPath, String title, Consumer<C> prepareController) {
        Stage stage = (Stage) nodeOnCurrentScreen.getScene().getWindow();
        showScreen(stage, fxmlPath, title, prepareController);
    }

    public <C> void showScreen(Stage stage, String fxmlPath, String title, Consumer<C> prepareController) {
        try {
            stage.setScene(loadScene(fxmlPath, MAIN_WINDOW, prepareController));
            stage.setTitle(title);
        } catch (IOException exception) {
            reportLoadFailure(title, fxmlPath, exception);
        }
    }

    public void showMainMenu(Node nodeOnCurrentScreen, Integer companyId, String companyName) {
        this.<MainMenuController>showScreen(nodeOnCurrentScreen, "/fxml/MainMenu.fxml",
                "Salary Calculator - " + companyName, controller -> {
                    controller.setCompanyId(companyId);
                    controller.setCompanyName(companyName);
                });
    }

    /** Opens a modal dialog over the owner's window and waits until it closes. */
    public <C> void showModalDialog(Node nodeOnOwnerWindow, String fxmlPath, String title, WindowSize size,
                                    Consumer<C> prepareController) {
        try {
            Scene scene = loadScene(fxmlPath, size, prepareController);
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(nodeOnOwnerWindow.getScene().getWindow());
            dialog.setTitle(title);
            dialog.setScene(scene);
            dialog.setMinWidth(size.width());
            dialog.setMinHeight(size.height());
            dialog.showAndWait();
        } catch (IOException exception) {
            reportLoadFailure(title, fxmlPath, exception);
        }
    }

    /** Opens an independent, fixed-size window and returns immediately. */
    public <C> void openWindow(String fxmlPath, String title, WindowSize size, Consumer<C> prepareController) {
        try {
            Scene scene = loadScene(fxmlPath, size, prepareController);
            Stage window = new Stage();
            window.setTitle(title);
            window.setScene(scene);
            window.setResizable(false);
            window.show();
        } catch (IOException exception) {
            reportLoadFailure(title, fxmlPath, exception);
        }
    }

    private <C> Scene loadScene(String fxmlPath, WindowSize size, Consumer<C> prepareController) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), messages.bundle());
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();
        prepareController.accept(loader.getController());

        Scene scene = new Scene(root, size.width(), size.height());
        scene.getStylesheets().add(getClass().getResource(STYLESHEET).toExternalForm());
        return scene;
    }

    private void reportLoadFailure(String screenTitle, String fxmlPath, IOException exception) {
        log.error("Could not load screen {}", fxmlPath, exception);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(messages.text(MessageKeys.ERROR_TITLE));
        alert.setHeaderText(null);
        alert.setContentText(messages.format(MessageKeys.SCREEN_LOAD_FAILED, screenTitle, exception.getMessage()));
        alert.showAndWait();
    }
}
