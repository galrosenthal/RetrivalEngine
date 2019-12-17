package GUI.Model;

import java.util.HashMap;

public interface IModel {
    void startParse(String corpusPath,String postingPath,boolean w);

    void setReset(String corpusPath,String postingPath);

    void loadDictionary(boolean withStemm);

     public HashMap<String,String> getDictionary();
    public String getAlertToShowFinish();
}
