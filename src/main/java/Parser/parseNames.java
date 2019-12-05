package Parser;

import IR.Document;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import sun.security.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class parseNames extends AParser {


    @Override
    public void parse(Document document) {
        //TokenizerModel tm = new TokenizerModel(document);
        //InputStream in = org.apache.commons.io.IOUtils.toInputStream(document.getDocText().text(), "UTF-8");
        InputStream targetStream = new ByteArrayInputStream(document.getDocText().text().getBytes());
        try {
            TokenNameFinderModel nameFinder = new TokenNameFinderModel(targetStream);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
