package GUI.View;

import GUI.ViewModel.ViewModel;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.*;
import java.util.*;

/**
 * Controller of the JavaFx, Runs all the logic for viewing
 */
public class Controller implements Observer {
    File fileToRead;
    ViewModel viewModel;
    Stage primaryStage;
    HashMap<String,List<String>> queryResFile;
    List<String> queryRes;

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
    public Button btn_browseQuery;
    public Button btn_saveQueryResult;
    public Button srchRun;
    public CheckBox chk_searchEntities;
    public TextField txt_search;
    public CheckBox chk_addSemantic;

    public void initialize(ViewModel viewModel, Stage primaryStage){
        this.viewModel = viewModel;
        this.primaryStage = primaryStage;
        btn_reset.setDisable(true);
        btn_loadDic.setDisable(true);
        btn_showDic.setDisable(true);
        srchRun.setDisable(true);
    }

    /**
     * Implements the startParsing button, check if the user enter the right paths to the corpus and posting files and call the viewmodel
     * to run the program
     */
    public void startParse(){
        if(txt_field_Corpus.getCharacters().length() > 0 && txt_field_Posting.getCharacters().length() > 0){
            btn_strtPrs.setDisable(true);

            viewModel.startParse(txt_field_Corpus.getText(),txt_field_Posting.getText(),chk_Stemm.isSelected());
        }
        else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please choose paths for corpus and posting and try again");
            alert.setTitle("Error");
            alert.showAndWait();
            //btn_strtPrs.setDisable(false);
        }

    }

    /**
     * Saves the path the user enter for the posting file
     */
    public void choosePathToPosting() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose Posting files path");
        File defaultDirectory = new File("c:/Users");
        chooser.setInitialDirectory(defaultDirectory);
        File selectedDirectory = chooser.showDialog(primaryStage);
        if (selectedDirectory != null) {
            txt_field_Posting.setText(selectedDirectory.toString());
        }
        btn_loadDic.setDisable(false);
    }


    /**
     * Saves the path the user enter for the corpus file
     */
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

    /**
     * Implement the reset button, call the viewmodel to run the reset function
     */
    public void reset(){
        viewModel.setReset(txt_field_Corpus.getText(),txt_field_Posting.getText());
    }

    /**
     * run when the viewmodel notify when somthing new us updated
     * @param o
     * @param arg number which represnt what has been update
     */
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
            else if(num==3){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Dictionary Loaded successfully ");
                alert.setTitle("Load Dictionary!");
                alert.showAndWait();
                btn_showDic.setDisable(false);
                srchRun.setDisable(false);

            }
            else if(num == 4)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Error While Trying to load the Dictionary, Please Try changing the Posting Files folder or Parsing again");
                alert.setTitle("Could Not Load Dictionary!");
                alert.showAndWait();
            }

            //query result using file
            else if(num == 5){
                queryResFile = viewModel.getqueryResUsingFile();
                showQueryResultUsingfFile();
            }

            //query result using search text
            else if(num == 6){
                queryRes = viewModel.getqueryResUsingSearch();
                showQueryUsingSearch();
            }
        }
    }

    /**
     * Showing the results of the related docs of the query using query search text
     */
    private void showQueryUsingSearch() {
        try{
            ArrayList<String> sortedKeys = new ArrayList<String>(queryRes);
            String query = sortedKeys.remove(sortedKeys.size()-1);
            TableView tableView = new TableView<>();
            TableColumn<String, Map> column1 = new TableColumn("QueryId");
            column1.setCellValueFactory(new PropertyValueFactory<>("QueryId"));

            TableColumn<String, Map> column2 = new TableColumn("Document");
            column2.setCellValueFactory(new PropertyValueFactory<>("Document"));

            tableView.getColumns().add(column1);
            tableView.getColumns().add(column2);

            for (String doc :sortedKeys) {
                    tableView.getItems().add(new queryFile(query,doc));
            }
            queryRes.add(query);
            StackPane stkPane = new StackPane();
            stkPane.getChildren().add(tableView);
            Scene scene = new Scene(stkPane);
            Stage stage = new Stage();
            stage.setTitle("Show query result");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {

        }
    }

    /**
     * Showing the results of the related docs of the query using query file
     */
    private void showQueryResultUsingfFile() {
        try {

            ArrayList<String> sortedKeys = new ArrayList<String>(queryResFile.keySet());

            TableView tableView = new TableView<>();
            TableColumn<String, Map> column1 = new TableColumn("QueryId");
            column1.setCellValueFactory(new PropertyValueFactory<>("QueryId"));

            TableColumn<String, Map> column2 = new TableColumn("Document");
            column2.setCellValueFactory(new PropertyValueFactory<>("Document"));

            tableView.getColumns().add(column1);
            tableView.getColumns().add(column2);

            for (String query :sortedKeys) {
                for (String doc:queryResFile.get(query)) {
                    tableView.getItems().add(new queryFile(query,doc));
                }
            }

            StackPane stkPane = new StackPane();
            stkPane.getChildren().add(tableView);
            Scene scene = new Scene(stkPane);
            Stage stage = new Stage();
            stage.setTitle("Show query result");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {

        }
    }

    /**
     * Implements the load Dictionary button
     * @param actionEvent
     */
    public void loadDirectory(ActionEvent actionEvent) {

        viewModel.loadDictinary(chk_Stemm.isSelected(),txt_field_Posting.getText());
    }

    /**
     * Implements the Open Directory button, enter all the data of the dictionary to table view which enter into StackPane
     * @param actionEvent
     */
    public void showDirectory(ActionEvent actionEvent) {
        try {
            HashMap<String ,String> dic = viewModel.getDictionary();

            if(dic == null){
                viewModel.loadDictinary(chk_Stemm.isSelected(),txt_field_Posting.getText());
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

    /**
     * Choose queries to search from browse
     * @param actionEvent
     */
    public void choosePathToQueries(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose queries path");
        File defaultDirectory = new File("c:/Users");
        chooser.setInitialDirectory(defaultDirectory);
        fileToRead = chooser.showOpenDialog(primaryStage);

        if (fileToRead != null) {
            txt_search.setText(fileToRead.toString());
        }
    }

    /**
     * Run search queries
     * @param actionEvent
     */
    public void RunSearch(ActionEvent actionEvent) {
        if(txt_search.getText().length() > 0 && fileToRead == null && txt_field_Corpus.getText().length() > 0){
            viewModel.runSearch(txt_search.getText(),txt_field_Corpus.getText(),chk_addSemantic.isSelected());
        }
        else if(txt_field_Corpus.getText().length() == 0){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Error While Trying to run the query search, Please choose corpus path");
                alert.setTitle("Could not run query");
                alert.showAndWait();
        }
        else if(fileToRead != null){
                viewModel.runSearch(fileToRead,txt_field_Corpus.getText(),chk_addSemantic.isSelected());
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error While Trying to run the query search, Please Try to choose different file or insert query string");
            alert.setTitle("Could not run query");
            alert.showAndWait();
        }
    }

    /**
     * Saves the query to a file in format that enable TREC_EVAL program using it
     * @param actionEvent
     */
    public void saveQueryResult(ActionEvent actionEvent) {
        if(queryResFile == null && queryRes == null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error While Trying to save the query search result, Please run search again");
            alert.setTitle("Could not save results");
            alert.showAndWait();
        }
        else if(queryResFile != null || queryRes != null){
            //file chooser
            FileChooser fc = new FileChooser();
            fc.setTitle("Save query to File");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Text files", "*.txt");
            fc.getExtensionFilters().add(extFilter);
            Window primaryStage = null;
            File f = fc.showSaveDialog(primaryStage);
            if(f != null) {
                if(queryResFile!= null) {
                    try {

                        BufferedWriter writeBuffer = new BufferedWriter(new FileWriter(f, true));
                        for (String res : queryResFile.keySet()) {
                            for (String doc : queryResFile.get(res)) {
                                String toWrite = res + "," + "0," + doc + "," + "1,1.1,og";
                                writeBuffer.append(toWrite);
                                writeBuffer.newLine();
                            }

                        }
                        writeBuffer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if(queryRes!=null){
                    String id = queryRes.remove(queryRes.size()-1);
                    try {
                        BufferedWriter writeBuffer = new BufferedWriter(new FileWriter(f, true));

                        for (String doc:queryRes) {
                            String toWrite = id + "," + "0," + doc + "," + "1,1.1,og";
                            writeBuffer.append(toWrite);
                            writeBuffer.newLine();
                        }

                        writeBuffer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            }
        }
    }

    /**
     * Wrapper class that is used for entering the data of the dictionary to the tableview and
     * presents them
     */
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

    public static class queryFile {
        private SimpleStringProperty queryId;
        private SimpleStringProperty document;

        public queryFile(String queryId, String document) {
            this.queryId = new SimpleStringProperty(queryId);
            this.document = new SimpleStringProperty(document);
        }

        public String getQueryId() {
            return queryId.get();
        }

        public SimpleStringProperty queryIdProperty() {
            return queryId;
        }

        public String getDocument() {
            return document.get();
        }

        public SimpleStringProperty documentProperty() {
            return document;
        }

        public void setQueryId(String queryId) {
            this.queryId.set(queryId);
        }

        public void setDocument(String document) {
            this.document.set(document);
        }
    }

}
