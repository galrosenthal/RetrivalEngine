package GUI.Model;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * Interface which implements all the functions the viewmodel using them
 */
public interface IModel {
    void startParse(String corpusPath, String postingPath, boolean w);

    void setReset(String corpusPath, String postingPath);

    void loadDictionary(boolean withStemm,String postPath);

    public HashMap<String, String> getDictionary();

    public String getAlertToShowFinish();

    void runSearchQuery(String text,String corpusPath,boolean withSemantic);

    void runSearchUsingFile(File fileToRead,String corpusPath,boolean withSemantic);

    HashMap<String, List<String>> getqueryResUsingFile();

    List<String> getQueryResUsingSearch();

    void searchEntities(String docNo);
}