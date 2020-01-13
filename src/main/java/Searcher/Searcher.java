package Searcher;

import IR.Document;
import com.medallia.word2vec.Word2VecModel;
import com.medallia.word2vec.util.Common;
import datamuse.*;
import Indexer.Indexer;
import Parser.AParser;
import Parser.MainParse;
import Ranker.Ranker;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Searcher {
    String corpusPath;
    Ranker ranker;
    Indexer myIndexer;
    HashMap<String,String> corpusDictionary;

    public Searcher(String corpusPath) {
        this.corpusPath = corpusPath;
        ranker = Ranker.getInstance();
        myIndexer = Indexer.getInstance();
        corpusDictionary = myIndexer.getCorpusDictionary();
    }

    public List<String> searchQuery(IR.Document query,IR.Document desc, int withSemantic){
        Path termFilePathTemp;
        List<String> result = new ArrayList<>();


        if(corpusDictionary!=null){
            HashMap<String, String> descInText;
            AParser parser = new MainParse();
            AParser parser2 = new MainParse();
            parser.setPathToCorpus(corpusPath);
            parser2.setPathToCorpus(corpusPath);
            ((MainParse) parser).parse(query);
            System.out.println("Finishing parsing query: " +query.getDocNo());
            
            HashMap<String,String> termInText = parser.getTermsInText();
//            if(desc.getTextArray() != null) {
//                ((MainParse) parser2).parse(desc);
//                 descInText = parser2.getTermsInText();
//            }
//            else{
//                descInText = new HashMap<>();
//
//            }
            HashMap<String,String> termswithPosting = new HashMap<>();
            HashMap<String,String> termswithPostingDesc = new HashMap<>();

            HashMap<String,String> termswithSemanticInText = new HashMap<>();
            HashMap<String,String> termswithSemanticPosting = new HashMap<>();

            String[] res = null;

            /**
             * Datamuse API
             */
            if(withSemantic == 2) {

                String arraWords = "";
                DatamuseQuery dQuery = new DatamuseQuery();

                for (String term : termInText.keySet()) {
                    String s = dQuery.findSimilar(term);
                    res = JSONParse.parseWords(s);
                    arraWords = arraWords + " " + res[0] + " " + res[1];
                }

                res = StringUtils.split(arraWords," ");
            }

            /**
             * Word2Vec model
             */
            else if(withSemantic == 1){
                String word2Vec = "";

                final String filename = "word2vec.c.output.model.txt";
                try {
                    for (String term: termInText.keySet()) {
                        Word2VecModel model = Word2VecModel.fromTextFile(new File(filename));
                        List<com.medallia.word2vec.Searcher.Match> matches = model.forSearch().getMatches(term, 3);
                        for (com.medallia.word2vec.Searcher.Match match : matches) {
                            word2Vec = word2Vec + " " + match.match();
                        }
                        res = StringUtils.split(word2Vec," ");
                    }


                } catch (IOException | com.medallia.word2vec.Searcher.UnknownWordException e) {
                    System.out.println("Didnt found the word");
                }
            }
            if(withSemantic!=0){
                AParser parser3 = new MainParse();
                parser3.setPathToCorpus(corpusPath);
                IR.Document semanticDoc = new Document();
                semanticDoc.setDocNo("1");
                semanticDoc.setTextArray(res);
                ((MainParse) parser3).parse(semanticDoc);
                termswithSemanticInText = parser3.getTermsInText();

//                String valueFromCorpus;
//                String corpusPathAndLineDelim = "#";
//                for (String specificTermKey: termswithSemanticInText.keySet()) {
//                    ArrayList<String> allTermsOfLetter = new ArrayList<>();
//                    if(corpusDictionary.containsKey(specificTermKey.toLowerCase())){
//                        valueFromCorpus = corpusDictionary.get(specificTermKey.toLowerCase());
//                    }
//                    else{
//                        valueFromCorpus = corpusDictionary.get(specificTermKey.toUpperCase());
//                    }
//
//                    if (valueFromCorpus != null) {
//
//                        termswithSemanticPosting.put(specificTermKey, getPostLine(valueFromCorpus));
//                    }
//                }
                termInText.putAll(termswithSemanticInText);
            }


            /**
             * Getting posting line for query
             */
            String valueFromCorpus;
            String corpusPathAndLineDelim = "#";
            for (String specificTermKey: termInText.keySet()) {
                ArrayList<String> allTermsOfLetter = new ArrayList<>();
                if(corpusDictionary.containsKey(specificTermKey.toLowerCase())){
                    valueFromCorpus = corpusDictionary.get(specificTermKey.toLowerCase());
                }
                else{
                    valueFromCorpus = corpusDictionary.get(specificTermKey.toUpperCase());
                }

                if (valueFromCorpus != null) {

                    termswithPosting.put(specificTermKey, getPostLine(valueFromCorpus));

                    //System.out.println(allTermsOfLetter.get(lineNumberInFile-1));
                }
            }

            /**
             * Getting posting lines for description
             */
//            valueFromCorpus = null;
//            if(desc.getTextArray()!=null){
//                for (String specificTermKey: descInText.keySet()) {
//                    ArrayList<String> allTermsOfLetter = new ArrayList<>();
//                    if(corpusDictionary.containsKey(specificTermKey.toLowerCase())){
//                        valueFromCorpus = corpusDictionary.get(specificTermKey.toLowerCase());
//                    }
//                    else{
//                        valueFromCorpus = corpusDictionary.get(specificTermKey.toUpperCase());
//                    }
//
//                    if (valueFromCorpus != null) {
//                        termswithPostingDesc.put(specificTermKey, getPostLine(valueFromCorpus));
//
//                        //System.out.println(allTermsOfLetter.get(lineNumberInFile-1));
//                    }
//                }
//            }
            System.out.println("Finishing posting query: " +query.getDocNo());
            ranker.resetQuery();
            result =ranker.rankQueryDocs(termswithPosting,termInText);
            System.out.println("Finishing ranking query: " +query.getDocNo());
        }

       // result= Arrays.asList("doc1","doc2","doc3");

        return result;
    }

    public String getPostLine(String valueFromCorpus){
        String corpusPathAndLineDelim = "#";
        Path termFilePathTemp;
        String[] splittedValue = StringUtils.split(valueFromCorpus,corpusPathAndLineDelim);
        termFilePathTemp = Paths.get(splittedValue[0]);
        try {
        BufferedReader bf = new BufferedReader(new FileReader(termFilePathTemp.toFile()));
            int lineNumberInFile = Integer.parseInt(splittedValue[1]);
            String wantedLine = "";
            for (int i = 0; i < lineNumberInFile; i++) {
                wantedLine = bf.readLine();
            }

            bf.close();
            return wantedLine;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
