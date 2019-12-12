package GUI.View;

import GUI.ViewModel.*;
import javafx.fxml.FXML;

public class Controller {

    @FXML
    public javafx.scene.control.Button btn_strtPrs;

    public void startParse(){
        btn_strtPrs.setDisable(true);

    }
}
