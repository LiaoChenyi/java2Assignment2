
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.stage.StageStyle;

public class Main extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("test.fxml"));
        stage.setScene(new Scene(fxmlLoader.load()));
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.show();
    }
}
