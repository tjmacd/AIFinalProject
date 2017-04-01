import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.List;

import twitter4j.TwitterException;

public class TextProcessor {

	public HashMap<String, Integer> getWordOccurences(String text){
		
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		
		for (String word: text.split(" ")){
			String key = word.toLowerCase().trim();
			Integer currentCount = 0;
			
			if(result.containsKey(key)){
				currentCount = result.get(key);
			}
			
			result.put(key, currentCount + 1);
		}
		
		return result;
	}
	
	public int filterByOccurenceCount(int threshold, HashMap<String, Integer> wordOccurrences){
		int countRemoved = 0;
		List<String> toBeRemoved = new ArrayList<String>();
		
		// Get a set of the keys and an iterator for the hashmap
		Set<String> keySet = wordOccurrences.keySet();
		Iterator<String> i = keySet.iterator();
  
		// find words that need to be remove
		while(i.hasNext()) {
			
			String word = i.next();
			
			if(wordOccurrences.get(word) < threshold){
				// need to remove it
				toBeRemoved.add(word);
			}
			
		}
		
		// now do the actual removal
		for(String removeWord: toBeRemoved){
			wordOccurrences.remove(removeWord);
			countRemoved++;
		}
		
		return countRemoved;
	}
	
	public int filterByFrequency(double threshold, HashMap<String, Integer> wordOccurrences){
		int countRemoved = 0;
		List<String> toBeRemoved = new ArrayList<String>();
		double totalWords = 0;
		
		// Get a set of the keys and an iterator for the hashmap
		Set<String> keySet = wordOccurrences.keySet();
		Iterator<String> i = keySet.iterator();
		  
		// count total words
		while(i.hasNext()) {
			String word = i.next();
			totalWords += wordOccurrences.get(word);	
		}
		
		i = keySet.iterator();
  
		// find words that need to be remove
		while(i.hasNext()) {
			
			String word = i.next();
			double frequency = wordOccurrences.get(word) / totalWords;
			
			if(frequency < threshold){
				// need to remove it
				toBeRemoved.add(word);
			}		
		}
		
		// now do the actual removal
		for(String removeWord: toBeRemoved){
			wordOccurrences.remove(removeWord);
			countRemoved++;
		}
		
		return countRemoved;
	}
	
	public int filterByOccurenceCount(List<String> ignoreWords, HashMap<String, Integer> wordOccurences){
		int countRemoved = 0;
		
		// now do the actual removal
		for(String removeWord: ignoreWords){
			wordOccurences.remove(removeWord);
			countRemoved++;
		}
		
		return countRemoved;
	}
	
	public int getSize(HashMap<String, Integer> hm){
		return hm.size();
	}
	
	public static void main(String[] args) throws TwitterException {
		
		TextProcessor tp = new TextProcessor();
		
		HashMap<String, Integer> result = tp.getWordOccurences("hello my name is joe, and i really like my name but i dont want to say bye so i will say Hello . Hello !");
		
		System.out.println(result.toString());
		
		int removedCount = tp.filterByOccurenceCount(2, result);
		
		System.out.println(result.toString() + " removed: "+ removedCount);
		
		List<String> ignoredWordsList = new ArrayList<String>(Arrays.asList("i", "my"));
		removedCount = tp.filterByOccurenceCount(ignoredWordsList, result);
		
		System.out.println(result.toString() + " removed: "+ removedCount);
		
		removedCount = tp.filterByFrequency(0.30, result);
		
		System.out.println(result.toString() + " removed: "+ removedCount);
	}
	
}
