package Ranker;

import java.util.ArrayList;
import java.util.HashMap;

public class Ranker {


    private static volatile Ranker mInstance;

    private Ranker() {
    }

    public static Ranker getInstance() {
        if (mInstance == null) {
            synchronized (Ranker.class) {
                if (mInstance == null) {
                    mInstance = new Ranker();
                }
            }
        }
        return mInstance;
    }


    public ArrayList<String> rankDoc(HashMap<String,String> termsAndLinesFromPost)
    {
        ArrayList<String> docList = new ArrayList<>();


        return docList;
    }
}
