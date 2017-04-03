import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

public class TweetClassifier {
	public void train(){
		int outputNum = 2;   // number of output classes
		int batchSize = 128; // batch size for each epoch
		int rngSeed = 123;   // random number seed for reproducibility
		int numEpochs = 15;  // number of epochs to perform
		int numInputs = 0;
		int numHiddenNodes = 0;
		
		DataSetIterator iTrain = new TweetIterator(batchSize, true);
		DataSetIterator iTest = new TweetIterator(batchSize, false);
		
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
		
		MultiLayerNetwork model = new MultiLayerNetwork(conf);
		model.init();
		model.setListeners(new ScoreIterationListener(1));
		
		System.out.println("Train model...");
		for(int i=0; i<numEpochs; i++) {
			model.fit(iTrain);
		}
		
		System.out.println("Evaluate model...");
		Evaluation eval = new Evaluation(outputNum);
		while(iTest.hasNext()) {
			DataSet next = iTest.next();
			// get network's prediction
			INDArray output = model.output(next.getFeatureMatrix()); 
			// check prediction against true class
			eval.eval(next.getLabels(), output);
		}
		
		System.out.println(eval.stats());
	}
}
