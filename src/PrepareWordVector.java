
import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/*
 * Class that builds our WordVector, which ultimately is what we use to determine what
 * is classified as what in our Neural Network. Takes in a text file, which it then
 * vectorizes, formatting it appropriately. We then build our model and fit the data.
 * Lastly, we write it out to a file so we can use it in our other classes.
 */
public class PrepareWordVector {

    private static Logger log = LoggerFactory.getLogger(PrepareWordVector.class);

    public static void main(String[] args) throws Exception {

        // Gets Path to Text file
    	String filePath = new File("raw.txt").getAbsolutePath();

        log.info("Load & Vectorize Sentences....");
        // Strip white space before and after for each line
        SentenceIterator iter = new BasicLineIterator(filePath);
        // Split on white spaces in the line to get words
        TokenizerFactory t = new DefaultTokenizerFactory();

        //CommonPreprocessor will apply the following regex to each token: [\d\.:,"'\(\)\[\]|/?!;]+
        //So, effectively all numbers, punctuation symbols and some special symbols are stripped off.
        //Additionally it forces lower case for all tokens.
        t.setTokenPreProcessor(new CommonPreprocessor());

        log.info("Building model....");
        Word2Vec vec = new Word2Vec.Builder()
            .minWordFrequency(2)
            .iterations(5)
            .layerSize(100)
            .seed(42)
            .windowSize(20)
            .iterate(iter)
            .tokenizerFactory(t)
            .build();

        log.info("Fitting Word2Vec model....");
        vec.fit();

        log.info("Writing word vectors to text file....");

        // Write word vectors to file
        WordVectorSerializer.writeWordVectors(vec, "TweetWordVector.txt");
    }
}
