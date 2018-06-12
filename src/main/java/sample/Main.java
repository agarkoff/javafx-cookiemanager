package sample;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.w3c.dom.html.HTMLInputElement;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class Main extends Application {

    private WebView webView;
    private WebEngine webEngine;
    private StackPane stackPane;
    private Stage stage;

    private String login;
    private String password;

    @Override
    public void start(Stage primaryStage) throws IOException {
        webView = new WebView();

        webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        webEngine.setUserAgent("Mozilla/5.0 (compatible; ABrowse 0.4; Syllable)");

        stackPane = new StackPane();
        stackPane.getChildren().add(webView);

        stage = new Stage();
        stage.setScene(new Scene(stackPane, 600, 600));
        stage.show();

        loadCredentials();
        CookieUtils.loadCookies();
        webEngine.getLoadWorker().stateProperty().addListener(changeListener);
        webEngine.load("https://gmail.com/");
    }

    private ChangeListener<Worker.State> changeListener = new ChangeListener<Worker.State>() {

        @Override
        public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
            if (newValue != Worker.State.SUCCEEDED) {
                return;
            }
            if (webEngine.getLocation().startsWith("https://accounts.google.com/ServiceLogin")) {
                webEngine.getDocument().getElementById("Email").setAttribute("value", login);
                ((HTMLInputElement) webEngine.getDocument().getElementById("next")).click();
            }
            if (webEngine.getLocation().startsWith("https://accounts.google.com/signin/challenge/pwd/")) {
                webEngine.getDocument().getElementById("password").setAttribute("value", password);
                ((HTMLInputElement) webEngine.getDocument().getElementById("submit")).click();
            }
            if (webEngine.getLocation().startsWith("https://mail.google.com/mail/")) {
                CookieUtils.saveCookies();
            }
        }
    };

    private void loadCredentials() throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get("credentials.json"));
        String json = new String(bytes, StandardCharsets.UTF_8);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> m = gson.fromJson(json, type);
        login = m.get("login");
        password = m.get("password");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
