import java.io.File;

import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

public class TweetClassifier {
	
	public static WordVectors wordVectors;
	
	public static void main(String[] args) throws Exception {
		int outputNum = 2;   // number of output classes
		int batchSize = 50; // batch size for each epoch
		int rngSeed = 123;   // random number seed for reproducibility
		int numEpochs = 10;  // number of epochs to perform
		
		
		String DATA_PATH = "LabelledData";
		String WORD_VECTORS_PATH = "TweetWordVector.txt";
		
		wordVectors = WordVectorSerializer.loadTxtVectors(new File(WORD_VECTORS_PATH));
		
		DataSetIterator iTrain = new TweetIterator(wordVectors, batchSize, true);
		DataSetIterator iTest = new TweetIterator(wordVectors, batchSize, false);
		
		int numInputs = wordVectors.getWordVector(wordVectors.vocab().wordAtIndex(0)).length;
		int numHiddenNodes = 200;
		
		/*
		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
				.seed(rngSeed) // random seed for reproducibility
				// use stochastic gradient descent as optimization algorithm
				.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
				.iterations(1) // one iteration
				.learningRate(0.006)
				// specify the rate of change of the learning rate
				.updater(Updater.NESTEROVS).momentum(0.9)
				.regularization(true).l2(1e-4)
				.list()
				// create first input layer with xavier linearization
				.layer(0, new DenseLayer.Builder()
						.nIn(numInputs)
						.nOut(numHiddenNodes)
						.weightInit(WeightInit.XAVIER)
						.activation(Activation.RELU)
						.build())
				.layer(1, new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD)
						.nIn(numHiddenNodes)
						.nOut(outputNum)
						.activation(Activation.SOFTMAX)
						.weightInit(WeightInit.XAVIER)
						.build())
				.pretrain(false).backprop(true) // adjust weights with backpropagation
				.build();
				*/
		
		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
	            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(1)
	            .updater(Updater.RMSPROP)
	            .regularization(true).l2(1e-5)
	            .weightInit(WeightInit.XAVIER)
	            .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue).gradientNormalizationThreshold(1.0)
	            .learningRate(0.0018)
	            .list()
	            .layer(0, new GravesLSTM.Builder()
	            		.nIn(numInputs)
	            		.nOut(numHiddenNodes)
	            		.activation(Activation.SOFTSIGN)
	            		.build())
	            .layer(1, new RnnOutputLayer.Builder()
	            		.activation(Activation.SOFTMAX)
	            		.lossFunction(LossFunctions.LossFunction.MCXENT)
	            		.nIn(numHiddenNodes)
	            		.nOut(outputNum)
	            		.build())
	            .pretrain(false).backprop(true).build();
		
		MultiLayerNetwork model = new MultiLayerNetwork(conf);
		model.init();
		model.setListeners(new ScoreIterationListener(1));
		
		System.out.println("Train model...");
		for(int i=0; i<numEpochs; i++) {
			model.fit(iTrain);
			iTrain.reset();
		}
		
		System.out.println("Evaluate model...");
		Evaluation eval = new Evaluation(outputNum);
		while(iTest.hasNext()) {
			DataSet next = iTest.next();
			INDArray features = next.getFeatureMatrix();
			INDArray labels = next.getLabels();
			INDArray inMask = next.getFeaturesMaskArray();
			INDArray outMask = next.getLabelsMaskArray();
			INDArray predicted = model.output(features, false);
			eval.evalTimeSeries(labels, predicted, outMask);
		}
		
		System.out.println(eval.stats());
	}
}
