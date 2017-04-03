import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

public class TweetIterator implements DataSetIterator {
	
	private final int batchSize;
	private int cursor = 0;
	private final List<String> labels;
	private int tweetCount = 0;
	private final List<Pair<String, List<String>>> categoryData = new ArrayList<>();
	
	TweetIterator(int batchSize, boolean train){
		this.batchSize = batchSize;
		this.labels = new ArrayList<>();
		this.loadData(train);
		for(Pair<String, List<String>> datum : categoryData){
			this.labels.add(datum.getKey());
		}
	}
	
	/*
	 * Loads data from files for testing
	 */
	private void loadData(boolean train) {
		readFile("fry", train);
		readFile("cbc", train);
	}
	
	private void readFile(String name, boolean train){
		String filename;
		if(train){
			filename = name + "Train.txt";
		} else {
			filename = name + "Test.txt";
		}
		try(BufferedReader br = new BufferedReader(new FileReader(filename))){
			String line = "";
			List<String> tempList = new ArrayList<>();
			while((line = br.readLine()) != null) {
				tempList.add(line);
				this.tweetCount++;
			}
			Pair<String, List<String>> tempPair = Pair.of(name, tempList);
			this.categoryData.add(tempPair);
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	@Override
	public boolean hasNext() {
		return cursor < numExamples();
	}

	@Override
	public DataSet next() {
		return next(batchSize);
	}

	@Override
	public boolean asyncSupported() {
		return true;
	}

	@Override
	public int batch() {
		return batchSize;
	}

	@Override
	public int cursor() {
		return cursor;
	}

	@Override
	public List<String> getLabels() {
		return this.labels;
	}

	@Override
	public DataSetPreProcessor getPreProcessor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int inputColumns() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public DataSet next(int num) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int numExamples() {
		return totalExamples();
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean resetSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setPreProcessor(DataSetPreProcessor arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public int totalExamples() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int totalOutcomes() {
		// TODO Auto-generated method stub
		return 0;
	}

}
