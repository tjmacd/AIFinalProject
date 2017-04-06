import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;

public class TestClassifier {
	private static TokenizerFactory tokenizerFactory;
	private static WordVectors wordVectors;
	private static List<String> labels;
	private static MultiLayerNetwork model;
	
	public static void main(String args[]) {
		try {
			String wordVectorsFile = "TweetWordVector.txt";
			tokenizerFactory = new DefaultTokenizerFactory();
			tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
			model = ModelSerializer.restoreMultiLayerNetwork(
					"TwitterModel.net");
			wordVectors = WordVectorSerializer.loadTxtVectors(
					new File(wordVectorsFile));
		} catch (Exception e){
			e.printStackTrace();
		}
		
		String categoriesFile = "LabelledData" + File.separator + "categories.txt";
		
		
		try(BufferedReader br = new BufferedReader(new FileReader(categoriesFile))){
			labels = new ArrayList<>();
			String temp = "";
			while((temp = br.readLine()) != null) {
				labels.add(temp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		List<String> tweets = new ArrayList<String>();
		tweets.add("Honoured to join @IGTeamCanada's @CanadianForces members "
				+ "training for @InvictusToronto. Go Canada go! #IAm #IG2017");
		tweets.add("In the lab, talking about Canadian innovation & products "
				+ "that improve people's lives. Thanks to the @3M_Canada team"
				+ " in London for the tour.");
		
		DataSet dataSet = processTweets(tweets);
		INDArray feat = dataSet.getFeatureMatrix();
		INDArray predicted = model.output(feat, false);
		int arrsiz[] = predicted.shape();
		
		double max = 0;
		int category = 0;
		for (int i = 0; i< arrsiz[1]; i++) {
			if(max < (double) predicted.getColumn(i).sumNumber()){
				max = (double) predicted.getColumn(i).sumNumber();
				category = i;
			}
		}
		System.out.println("Tweets belong to "+ labels.get(category).split(",")[1]);
	}
	
	private static DataSet processTweets(List<String> tweets) {
		int[] category = new int[tweets.size()];
		List<List<String>> allTokens = new ArrayList<>(tweets.size());
		int maxLength = 0;
		for(String s : tweets) {
			List<String> tokens = tokenizerFactory.create(s).getTokens();
			List<String> tokensFiltered = new ArrayList<>();
			for(String t : tokens) {
				if (wordVectors.hasWord(t))
					tokensFiltered.add(t);
			}
			allTokens.add(tokensFiltered);
			maxLength = Math.max(maxLength, tokensFiltered.size());
		}
		
		INDArray featuresArray = Nd4j.create(tweets.size(), 
				wordVectors.lookupTable().layerSize(), maxLength);
		INDArray labelsArray = Nd4j.create(tweets.size(), labels.size(), maxLength);
		INDArray featuresMask = Nd4j.zeros(tweets.size(), maxLength);
		INDArray labelsMask = Nd4j.zeros(tweets.size(), maxLength);
		
		int[] temp = new int[2];
		for(int i=0; i<tweets.size(); i++) {
			List<String> tokens = allTokens.get(i);
			temp[0] = i;
			for(int j=0; j<tokens.size() && j < maxLength; j++) {
				String token = tokens.get(j);
				INDArray vector = wordVectors.getWordVectorMatrix(token);
				featuresArray.put(new INDArrayIndex[]{NDArrayIndex.point(i),
						NDArrayIndex.all(),
						NDArrayIndex.point(j)}, 
						vector);
				temp[1] = j;
				featuresMask.putScalar(temp, 1.0);
			}
			int idx = category[i];
			int lastIdx = Math.min(tokens.size(), maxLength);
			labelsArray.putScalar(new int[]{i, idx, lastIdx-1}, 1.0);
			labelsMask.putScalar(new int[]{i,  lastIdx-1}, 1.0);
		}
		
		DataSet ds = new DataSet(featuresArray, labelsArray, featuresMask, labelsMask);
		return ds;
	}
}
