package Indexer;

import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Indexer implements Runnable{
    private static volatile Indexer mInstance;
    private ConcurrentLinkedQueue<HashMap<String,String>> parsedWordsQueue;
    private String postFiles;
    private BufferedWriter fileWriter;
    public static volatile boolean stopThreads = false;
    public ConcurrentHashMap<String,String> corpusDictionary;

    private Indexer() {
        this.parsedWordsQueue = new ConcurrentLinkedQueue<>();
        corpusDictionary = new ConcurrentHashMap<>();
    }

    public static Indexer getInstance() {
        if (mInstance == null) {
            synchronized (Indexer.class) {
                if (mInstance == null) {
                    mInstance = new Indexer();
                }
            }
        }
        return mInstance;
    }


    public synchronized boolean enqueue(HashMap<String,String> parsedWords)
    {
        return parsedWordsQueue.add(parsedWords);
    }

    private synchronized HashMap<String,String> dequeue()
    {
        return parsedWordsQueue.poll();
    }

    public void setPathToPostFiles(String path)
    {
        this.postFiles = path;
    }


    @Override
    public void run() {
        while(!stopThreads)
        {
            createPostFiles();
        }

    }

    private synchronized void createPostFiles() {
        while (!this.parsedWordsQueue.isEmpty())
        {
            //TODO: For each word check if exists in the CorpusDictionary,
            // find the relevant posting file (from the Dictionary or by first letter),
            // append the relevant data to the posting file in the relevant line
            HashMap<String,String> dqdHshMap = dequeue();



            if(dqdHshMap == null)
            {
                return;
            }
            else
            {
                //System.out.println("test");
            }

//            for (String term :
//                    dqdHshMap.keySet()) {
//
//
//            }
        }

    }
}
