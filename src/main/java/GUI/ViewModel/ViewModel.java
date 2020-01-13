package GUI.ViewModel;

import GUI.Model.IModel;
import GUI.Model.Model;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Connecting between the view to the model
 */
public class ViewModel extends Observable implements Observer {
    private IModel model;

    public ViewModel(IModel model)
    {
        this.model = model;
    }

    public void startParse(String corpusPath,String postingPath, boolean withStemm){
        model.startParse(corpusPath,postingPath,withStemm);
    }

    public void setReset(String corpusPath,String postingPath) {
        model.setReset(corpusPath,postingPath);
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o==model){
            setChanged();
            notifyObservers(arg);
        }

    }

    public void loadDictinary(boolean withStemm,String postPath,String corpusPath) {
        model.loadDictionary(withStemm,postPath,corpusPath);
    }

    public HashMap<String,String> getDictionary() {
       return model.getDictionary();
    }

    public String getAlertToShowFinish(){
        return model.getAlertToShowFinish();
    }

    public void runSearch(String text,String corpusPath, int withSemantic,double b,double alpha,double k) {
        model.runSearchQuery(text,corpusPath,withSemantic,b,alpha,k);
    }

    public void runSearch(File fileToRead,String corpusPath, int withSemantic,double b,double alpha,double k) {
        model.runSearchUsingFile(fileToRead,corpusPath,withSemantic,b,alpha,k);
    }

    public HashMap<String, List<String>> getqueryResUsingFile() {
        return model.getqueryResUsingFile();
    }

    public List<String> getqueryResUsingSearch() {
        return model.getQueryResUsingSearch();
    }

    public void searchEntities(String docNo) {
        model.searchEntities(docNo);
    }

    public HashMap<String, Double> getEntityResult() {
        return model.getEntityResult();
    }
}
