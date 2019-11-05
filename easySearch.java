import java.io.IOException;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class easySearch {
	public static void main(String[] args) throws ParseException, IOException {
		String queryString = "military";
		getRelevanceScores(queryString, "TEXT");
	}
	
	public static HashMap<String, Double> getRelevanceScores(String queryString, String field) throws ParseException, IOException {
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("./index")));
		IndexSearcher searcher = new IndexSearcher(reader);
		
		// Get the preprocessed query terms
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser(field, analyzer);
		Query query = parser.parse(queryString.replace("/", " "));
		Set<Term> queryTerms = new LinkedHashSet<Term>();
		searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);

		int N = reader.maxDoc();
		//System.out.println("Total number of documents in the corpus: "+ N);
		
		ClassicSimilarity dSimi = new ClassicSimilarity(); // Get the segments of the index 
		List<LeafReaderContext> leafContexts = reader.getContext().reader().leaves(); // Processing each segment 
		HashMap<String, Float> docLengths = new HashMap<String, Float>();
		
		HashMap<String, Double> relevanceScores = new HashMap<String, Double>();
		
		for (int i = 0; i < leafContexts.size(); i++) 
		{ 
			// Get document length 
			LeafReaderContext leafContext = leafContexts.get(i); 
			int startDocNo = leafContext.docBase; 
			int numberOfDoc = leafContext.reader().maxDoc(); 
			
			int doc; 
			for (int docId = 0; docId < numberOfDoc; docId++) 
			{ 
				// Get normalized length (1/sqrt(numOfTokens)) of the document 
				float normDocLeng = dSimi.decodeNormValue(leafContext.reader().getNormValues(field).get(docId)); 
				// Get length of the document 
				float docLeng = 1 / (normDocLeng * normDocLeng);
				//System.out.println("Length of doc(" + (docId + startDocNo) + ", " + searcher.doc(docId + startDocNo).get("DOCNO") + ") is " + docLeng); 
				String docKey = Integer.toString(docId + startDocNo) + ":" + searcher.doc(docId + startDocNo).get("DOCNO");
				docLengths.put(docKey, docLeng);

			}
		}	
		//System.out.println("Lengths of docs : "+docLengths.size());
		
		for (Term t : queryTerms) {
			//System.out.println(t.text());
			int kt=reader.docFreq(new Term(field, t.text()));
			//System.out.println("The total number of documents that have the term for field: "+kt);
			double idf=0;
			if(kt !=0)
				idf = Math.log((1+N/kt));
			//System.out.println("Inverse Document Frequency : "+idf);
			//System.out.println();
			
			for (int i = 0; i < leafContexts.size(); i++) 
			{ 
				//System.out.println("Loop "+i);
				LeafReaderContext leafContext = leafContexts.get(i); 
				int startDocNo = leafContext.docBase; 
				
				// Get frequency of the term "police" from its postings
				PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(), field,	new BytesRef(t.text()));
				int doc; 
				//HashMap<String, Integer> termCounts = new HashMap<String, Integer>();
				if (de != null) 
				{ 
					while ((doc = de.nextDoc()) != PostingsEnum.NO_MORE_DOCS) 
					{ 
						//System.out.println("\"police\" occurs " + de.freq() + " time(s) in doc(" + (de.docID() + startDocNo) + ")"); 
						//termCounts.put(Integer.toString(de.docID() + startDocNo), de.freq());
						String docKey = Integer.toString(de.docID() + startDocNo) + ":" + searcher.doc(de.docID() + startDocNo).get("DOCNO");
						double tfidf = de.freq()/docLengths.get(docKey) * idf;
						
						if (relevanceScores.containsKey(docKey)) {
							relevanceScores.put(docKey,relevanceScores.get(docKey)+tfidf);
						}
						else {
							relevanceScores.put(docKey,tfidf);	
						}
					} 
				}
			}	

		}
		

  		HashMap<String, Double> SortedRelevanceScores = new LinkedHashMap<String, Double>();
  		relevanceScores.entrySet()
  	    	.stream()
  	    	.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
 	    	.forEachOrdered(x -> SortedRelevanceScores.put(x.getKey(), x.getValue()));
  		//System.out.println(SortedRelevanceScores);

		Set<String> keys = SortedRelevanceScores.keySet();
  		HashMap<String, Double> result = new LinkedHashMap<String, Double>();
		int c = 0;
        for(String key: keys){
        	if (c > 999) {
        		break;
        	}
            //System.out.println(key+ ": "+SortedRelevanceScores.get(key));
        	result.put(key,SortedRelevanceScores.get(key));
            c++;
        }
        return result;
	

	}
}
