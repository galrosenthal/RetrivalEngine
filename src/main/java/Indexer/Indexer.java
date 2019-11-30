package Indexer;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Indexer implements Runnable{
    private static volatile Indexer mInstance;
    private ConcurrentLinkedQueue<HashMap<String,String>> parsedWordsQueue;
    private String postFiles;

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
        return parsedWordsQueue.remove();
    }

    public void setPathToPostFiles(String path)
    {
        this.postFiles = path;
    }


    @Override
    public void run() {
        while(true)
        {
            createPostFiles();
        }
    }

    private synchronized void createPostFiles() {
        if(!this.parsedWordsQueue.isEmpty())
        {

        }

    }
}
