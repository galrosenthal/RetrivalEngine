package GUI.ViewModel;

import GUI.Model.IModel;
import GUI.Model.Model;

import java.util.Observable;
import java.util.Observer;

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
}
