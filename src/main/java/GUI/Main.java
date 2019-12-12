package GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        String pathTofxml = "../irgui.fxml";
        //URL res = getClass().getResource("irgui.fxml");
        //FXMLLoader loader = new FXMLLoader(res);
        Parent root = FXMLLoader.load(getClass().getResource("../irgui.fxml"));



//        if(res != null){
//            root = FXMLLoader.load(res);
//        }

        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
