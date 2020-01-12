package GUI.View;

import GUI.ViewModel.ViewModel;
import javafx.beans.property.SimpleDoubleProperty;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    HashMap<String,Double> entityResult;

    @FXML
    public CheckBox chh_addSemanticW2V;
    public CheckBox chk_addSemanticDs;
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
    public Button btn_srchRun;
    public CheckBox chk_searchEntities;
    public TextField txt_search;
    public CheckBox chk_addSemantic;
    public ComboBox choice_box;
    public Button btn_searchEntities;

    public void initialize(ViewModel viewModel, Stage primaryStage){
        this.viewModel = viewModel;
        this.primaryStage = primaryStage;
        btn_reset.setDisable(true);
        btn_loadDic.setDisable(true);
        btn_showDic.setDisable(true);
        btn_srchRun.setDisable(true);
        btn_saveQueryResult.setDisable(true);
        btn_searchEntities.setDisable(true);
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
                btn_srchRun.setDisable(false);
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
                btn_srchRun.setDisable(false);

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
                btn_saveQueryResult.setDisable(false);
                btn_searchEntities.setDisable(false);
                updateChoiceBox();
            }

            //query result using search text
            else if(num == 6){
                queryRes = viewModel.getqueryResUsingSearch();
                showQueryUsingSearch();
                btn_saveQueryResult.setDisable(false);
                btn_searchEntities.setDisable(false);
                updateChoiceBox();
            }

            //rank entities
            else if(num == 7){
                getEntityResult();
                showEntitiesResult();
            }
        }
    }

    private void showEntitiesResult() {
        ArrayList<String> sortedKeys = new ArrayList<String>(entityResult.keySet());
        String column1Value = "Entity";
        String column2Value = "Rank";

        TableView tableView = getTableView(column1Value, column2Value);

        for (String entity:entityResult.keySet()) {
            String result = entityResult.get(entity).toString();
            System.out.println(entity + " " + result);
            tableView.getItems().add(new EntityMap(entity,entityResult.get(entity)));
        }
        insertStackPaneAndShow(tableView, "Show entities result");

    }

    private void getEntityResult() {
        entityResult = viewModel.getEntityResult();
    }

    private void updateChoiceBox() {
        choice_box.getItems().clear();
        choice_box.setVisibleRowCount(10);
        if(queryRes!= null){
            queryRes.remove(queryRes.size()-1);
            for (String doc:queryRes) {
                choice_box.getItems().add(doc);
            }
        }
        else if(queryResFile != null){
            for (String query: queryResFile.keySet()) {
                for (String doc:queryResFile.get(query)) {
                    choice_box.getItems().add(doc);
                }
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
            sortedKeys.add(0,"Size: " +Integer.toString(sortedKeys.size()));
            TableView tableView = getTableView("QueryId", "Document");

            for (String doc :sortedKeys) {
                tableView.getItems().add(new queryFile(query,doc));
            }
            sortedKeys.remove(0);
            insertStackPaneAndShow(tableView, "Show query result");
        } catch (Exception e) {

        }
    }

    /**
     * Showing the results of the related docs of the query using query file
     */
    private void showQueryResultUsingfFile() {
        try {
            fileToRead = null;//Means that we finish taking care of query using file and ready to enter new search using one of the options
            ArrayList<String> sortedKeys = new ArrayList<String>(queryResFile.keySet());
            Collections.sort(sortedKeys);
            String column1Value = "QueryId";
            String column2Value = "Document";

            TableView tableView = getTableView(column1Value, column2Value);

            for (String query :sortedKeys) {
                int size = queryResFile.get(query).size();
                queryResFile.get(query).add(0,"Size: " + size);
                for (String doc:queryResFile.get(query)) {
                    tableView.getItems().add(new queryFile(query,doc));
                }
                queryResFile.get(query).remove(0);
            }

            insertStackPaneAndShow(tableView, "Show query result");
        } catch (Exception e) {

        }
    }

    private void insertStackPaneAndShow(TableView tableView, String s) {
        StackPane stkPane = new StackPane();
        stkPane.getChildren().add(tableView);
        Scene scene = new Scene(stkPane);
        Stage stage = new Stage();
        stage.setTitle(s);
        stage.setScene(scene);
        stage.show();
    }

    private TableView getTableView(String column1Value, String column2Value) {
        TableView tableView = new TableView<>();
        TableColumn<String, Map> column1 = new TableColumn(column1Value);
        column1.setCellValueFactory(new PropertyValueFactory<>(column1Value));

        TableColumn<String, Map> column2 = new TableColumn(column2Value);
        column2.setCellValueFactory(new PropertyValueFactory<>(column2Value));

        tableView.getColumns().add(column1);
        tableView.getColumns().add(column2);
        return tableView;
    }

    /**
     * Implements the load Dictionary button
     * @param actionEvent
     */
    public void loadDirectory(ActionEvent actionEvent) {

        viewModel.loadDictinary(chk_Stemm.isSelected(),txt_field_Posting.getText(),txt_field_Corpus.getText());
    }

    /**
     * Implements the Open Directory button, enter all the data of the dictionary to table view which enter into StackPane
     * @param actionEvent
     */
    public void showDirectory(ActionEvent actionEvent) {
        try {
            HashMap<String ,String> dic = viewModel.getDictionary();

            if(dic == null){
                viewModel.loadDictinary(chk_Stemm.isSelected(),txt_field_Posting.getText(),txt_field_Corpus.getText());
            }
            ArrayList<String> sortedKeys = new ArrayList<String>(dic.keySet());
            Collections.sort(sortedKeys);

            TableView tableView = getTableView("Term", "Amount");

            for (String term:sortedKeys) {
                tableView.getItems().add(new Map(term,dic.get(term).split("#")[2]));
            }

            insertStackPaneAndShow(tableView, "Show Dictionary");
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
        queryResFile = null;
        queryRes = null;
        //choice_box = new ComboBox();
        int chooseSemantic = 0;
        if(chh_addSemanticW2V.isSelected()){
            chooseSemantic = 1;
        }
        else if(chk_addSemanticDs.isSelected()){
            chooseSemantic = 2;
        }
        if(txt_search.getText().length() > 0 && fileToRead == null && txt_field_Corpus.getText().length() > 0){
            viewModel.runSearch(txt_search.getText(),txt_field_Corpus.getText(),chooseSemantic);
        }
        else if(txt_field_Corpus.getText().length() == 0){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error While Trying to run the query search, Please choose corpus path");
            alert.setTitle("Could not run query");
            alert.showAndWait();
        }
        else if(fileToRead != null){
            viewModel.runSearch(fileToRead,txt_field_Corpus.getText(),chooseSemantic);
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

            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Choose save path");
            File defaultDirectory = new File("c:/Users");
            chooser.setInitialDirectory(defaultDirectory);
            File f = chooser.showDialog(primaryStage);
            if (f != null) {
                f = new File(f.getAbsolutePath() + "\\queryResult.txt");}

            if(f != null) {
                if(queryResFile!= null) {
                    try {

                        BufferedWriter writeBuffer = new BufferedWriter(new FileWriter(f, true));
                        ArrayList<String> sortedKeys = new ArrayList<String>(queryResFile.keySet());
                        Collections.sort(sortedKeys);
                        for (String res : sortedKeys) {
                            //queryResFile.get(res).remove(0);
                            for (String doc : queryResFile.get(res)) {

                                String toWrite = res + " 0 " + doc + " 1 1.1 og";
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

    public void searchEntities(ActionEvent actionEvent) {
        String docNo = (String)choice_box.getValue();
        viewModel.searchEntities(docNo);
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

    public static class EntityMap{
        private SimpleStringProperty entity;
        private SimpleDoubleProperty rank;

        public EntityMap(String entity, Double rank) {
            this.entity = new SimpleStringProperty(entity);
            this.rank = new SimpleDoubleProperty(rank);
        }

        public String getEntity() {
            return entity.get();
        }

        public SimpleStringProperty entityProperty() {
            return entity;
        }

        public void setEntity(String entity) {
            this.entity.set(entity);
        }

        public double getRank() {
            return rank.get();
        }

        public SimpleDoubleProperty rankProperty() {
            return rank;
        }

        public void setRank(double rank) {
            this.rank.set(rank);
        }
    }

}
