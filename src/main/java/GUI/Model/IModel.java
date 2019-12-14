package GUI.Model;

public interface IModel {
    public void startParse(String corpusPath,String postingPath,boolean w);

    void setReset(String corpusPath,String postingPath);
}
