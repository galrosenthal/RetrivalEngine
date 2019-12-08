package Indexer;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class ReadWriteTempDic {

    private static volatile ReadWriteTempDic mInstance;
    private static final String pathToTempDicQ = "./dicTemp/";
    //    private static FileWriter writeToDicQ;
    private static volatile AtomicInteger writeIndex;
    private static volatile AtomicInteger readIndex;
    private static Semaphore readIndexSemaphore;
    private static HashSet<Integer> fileWritten;

    private ReadWriteTempDic()
    {
        writeIndex = new AtomicInteger(0);
        readIndex = new AtomicInteger(0);
        fileWritten = new HashSet<>();
        readIndexSemaphore = new Semaphore(1);



    }

    public boolean writeToDic(ConcurrentHashMap<String,String> map)
    {
        int currIndex = writeIndex.getAndIncrement();
        String newFilePath = pathToTempDicQ +  currIndex;
        Path pathForNewFile = Paths.get(newFilePath);

        try
        {
            pathForNewFile.toFile().deleteOnExit();
            if(!Files.exists(pathForNewFile))
            {
                Files.createDirectories(pathForNewFile.getParent());
                Files.createFile(pathForNewFile);
            }

            ObjectOutputStream writeToDicFile = new ObjectOutputStream(new FileOutputStream(pathForNewFile.toFile()));
            writeToDicFile.writeObject(map);
            writeToDicFile.flush();
            writeToDicFile.close();
            fileWritten.add(currIndex);
            System.out.println("Written " + currIndex);
        }
        catch (Exception e)
        {
            System.out.println("Could not load file");
            return false;
        }

        return true;
    }

    public static int hashSize()
    {
        return fileWritten.size();
    }

    public ConcurrentHashMap<String,String> readFromDic()
    {

        readIndexSemaphore.tryAcquire();
        int currIndex = readIndex.getAndIncrement();
        readIndexSemaphore.release();
        String newFilePath = pathToTempDicQ +  currIndex;
        Path pathForNewFile = Paths.get(newFilePath);

        if (!fileWritten.contains(currIndex))
        {
            readIndex.decrementAndGet();
            return null;
        }

//        System.out.println("Found " + newFilePath);
//        writeMutx.lock();
//        readSem.release();


        try{

            ObjectInputStream readDicObjInptStrm = new ObjectInputStream(new FileInputStream(pathForNewFile.toFile()));
            Object mapReadFromFile = readDicObjInptStrm.readObject();
            readDicObjInptStrm.close();
            while(!pathForNewFile.toFile().delete()) {
            }
            fileWritten.remove(currIndex);
            System.out.println("Read " + currIndex);
            return (ConcurrentHashMap<String,String>)mapReadFromFile;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;

//        readSem.tryAcquire();
//        writeMutx.unlock();
    }

    public static ReadWriteTempDic getInstance() {
        if (mInstance == null) {
            synchronized (ReadWriteTempDic.class) {
                if (mInstance == null) {
                    mInstance = new ReadWriteTempDic();
                }
            }
        }
        return mInstance;
    }
}
