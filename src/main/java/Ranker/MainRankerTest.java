package Ranker;

import IR.Document;
import IR.DocumentInfo;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class MainRankerTest
{

    public static void main(String[] args)
    {
        Document doc = new Document();
        doc.insertFoundTermInDoc("gal123");
        DocumentInfo docInfo = new DocumentInfo(doc);
        ConcurrentHashMap<String,DocumentInfo> myMap = new ConcurrentHashMap<>();
        myMap.put(doc.getDocNo(),docInfo);

        saveMapToDisk(myMap);
        myMap = new ConcurrentHashMap<>();
        myMap = loadMapFromDisk();

    }

    private static ConcurrentHashMap<String, DocumentInfo> loadMapFromDisk()
    {
        String pathToTempFolder = "./docsTempDir/";
        ConcurrentHashMap<String,DocumentInfo> map;
        try {
            File params = new File(pathToTempFolder + "gal123.txt");
            FileInputStream fileIn = new FileInputStream(params);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            map = (ConcurrentHashMap<String,DocumentInfo>)objectIn.readObject();
            objectIn.close();
            params.delete();
            return map;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private static void saveMapToDisk(ConcurrentHashMap<String, DocumentInfo> myMap)
    {
        String pathToTempFolder = "./docsTempDir/";
        try {
            FileOutputStream fileOut = new FileOutputStream(pathToTempFolder + "gal123.txt");
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(myMap);
            objectOut.flush();
            objectOut.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
