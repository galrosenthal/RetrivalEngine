package GUI.View;

import GUI.Model.Model;
import GUI.ViewModel.ViewModel;
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
        //URL res = getClass().getResource("../../irgui.fxml");
        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(getClass().getResource("../../irgui.fxml").openStream());
        Controller controller = loader.getController();
        Model model = new Model();
        ViewModel viewModel = new ViewModel(model);
        model.addObserver(viewModel);
        viewModel.addObserver(controller);


        primaryStage.setTitle("SearchEngine");
        primaryStage.setScene(new Scene(root, 500 ,500));
        primaryStage.show();
        controller.initialize(viewModel,primaryStage);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
