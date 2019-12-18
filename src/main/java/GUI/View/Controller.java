package GUI.View;

import GUI.ViewModel.*;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.*;

import javax.swing.*;
import java.io.File;
import java.util.*;

public class Controller implements Observer {



    ViewModel viewModel;
    Stage primaryStage;

    public void initialize(ViewModel viewModel, Stage primaryStage){
        this.viewModel = viewModel;
        this.primaryStage = primaryStage;
        btn_reset.setDisable(true);
        btn_loadDic.setDisable(true);
        btn_showDic.setDisable(true);
    }

    @FXML
    public javafx.scene.control.Button btn_strtPrs;
    public TextField txt_field_Corpus;
    public TextField txt_field_Posting;
    public CheckBox chk_Stemm;
    public Button btn_reset;
    public TextArea text_Dictionary;
    public TilePane tilePane;
    public Button btn_showDic;
    public Button btn_loadDic;


    public void startParse(){
        btn_strtPrs.setDisable(true);
        if(txt_field_Corpus.getText()!= null && txt_field_Posting!= null){
            viewModel.startParse(txt_field_Corpus.getText(),txt_field_Posting.getText(),chk_Stemm.isSelected());
        }
        else{
            btn_strtPrs.setDisable(false);
        }

    }

    public void choosePathToPosting() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose Posting files path");
        File defaultDirectory = new File("c:/Users");
        chooser.setInitialDirectory(defaultDirectory);
        File selectedDirectory = chooser.showDialog(primaryStage);
        if (selectedDirectory != null) {
            txt_field_Posting.setText(selectedDirectory.toString());
        }
    }


    public void choosePathForCorpus() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose corpus path");
        File defaultDirectory = new File("c:/Users");
        chooser.setInitialDirectory(defaultDirectory);
        File selectedDirectory = chooser.showDialog(primaryStage);

        if (selectedDirectory != null) {
            txt_field_Corpus.setText(selectedDirectory.toString());
        }
    }

    public void reset(){
        viewModel.setReset(txt_field_Corpus.getText(),txt_field_Posting.getText());
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == viewModel) {
            int num = (int) arg;
            btn_strtPrs.setDisable(false);
            btn_reset.setDisable(false);

            if(num == 1){
                btn_loadDic.setDisable(false);
                btn_showDic.setDisable(false);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText(viewModel.getAlertToShowFinish());
                alert.setTitle("Finish!");
                alert.showAndWait();
            }
            else if(num==2){
                btn_loadDic.setDisable(true);
                btn_showDic.setDisable(true);
            }
        }
    }

    public void loadDirectory(ActionEvent actionEvent) {

        viewModel.loadDictinary(chk_Stemm.isSelected());
    }

    public void showDirectory(ActionEvent actionEvent) {
        try {
            HashMap<String ,String> dic = viewModel.getDictionary();

            if(dic == null){
                viewModel.loadDictinary(chk_Stemm.isSelected());
            }
            ArrayList<String> sortedKeys = new ArrayList<String>(dic.keySet());
            Collections.sort(sortedKeys);

            TableView tableView = new TableView<>();
            TableColumn<String, Map> column1 = new TableColumn("Term");
            column1.setCellValueFactory(new PropertyValueFactory<>("term"));

            TableColumn<String, Map> column2 = new TableColumn("Amount");
            column2.setCellValueFactory(new PropertyValueFactory<>("amount"));

            tableView.getColumns().add(column1);
            tableView.getColumns().add(column2);

            for (String term:sortedKeys) {
                tableView.getItems().add(new Map(term,dic.get(term).split("#")[2]));
            }

            StackPane stkPane = new StackPane();
            stkPane.getChildren().add(tableView);
            Scene scene = new Scene(stkPane);
            Stage stage = new Stage();
            stage.setTitle("Show Dictionary");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {

        }
    }

    public static class Map{
        private SimpleStringProperty term;
        private SimpleIntegerProperty amount;

        public Map(String term, String amount) {
            this.amount = new SimpleIntegerProperty(Integer.parseInt(amount));
            this.term = new SimpleStringProperty(term);
        }

        public void setTerm(String term) {
            this.term.set(term);
        }

        public void setAmount(int amount) {
            this.amount.set(amount);
        }

        public String getTerm() {
            return term.get();
        }

        public SimpleStringProperty termProperty() {
            return term;
        }

        public int getAmount() {
            return amount.get();
        }

        public SimpleIntegerProperty amountProperty() {
            return amount;
        }
    }
}
