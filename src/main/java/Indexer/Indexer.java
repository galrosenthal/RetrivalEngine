package Indexer;

import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Indexer implements Runnable{
    private static volatile Indexer mInstance;
    private ConcurrentLinkedQueue<HashMap<String,String>> parsedWordsQueue;
    private String postFiles;
    private BufferedWriter fileWriter;
    public static volatile boolean stopThreads = false;

    private Indexer() {
        this.parsedWordsQueue = new ConcurrentLinkedQueue<>();
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
