package GUI.View;

import GUI.Model.Model;
import GUI.ViewModel.ViewModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        String pathTofxml = "irgui.fxml";
//        System.out.println(System.getProperty("user.dir"));
        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(getClass().getClassLoader().getResource(pathTofxml).openStream());
//        Parent root = loader.load(new FileInputStream(pathTofxml));
        Controller controller = loader.getController();
        Model model = new Model();
        ViewModel viewModel = new ViewModel(model);
        model.addObserver(viewModel);
        viewModel.addObserver(controller);


        primaryStage.setTitle("SearchEngine");
        primaryStage.setScene(new Scene(root, 600 ,500));
        primaryStage.show();
        controller.initialize(viewModel,primaryStage);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
