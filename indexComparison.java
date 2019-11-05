import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory; 


public class indexComparison {
	static int count = 0;
	public static void main(String []args) throws FileNotFoundException, IOException {
		File folder = new File("./corpus");
		File[] listOfFiles = folder.listFiles();
		
		/* Creating writers for 4 analyzers iteratively
		 * 1: KeywordAnalyzer
		 * 2: SimpleAnalyzer
		 * 3: StopAnalyzer
		 * 4: StandardAnalyzer
		 * */

		IndexWriter writer1 = indexer(1);
		IndexWriter writer2 = indexer(2);
		IndexWriter writer3 = indexer(3);
		IndexWriter writer4 = indexer(4);
        
        ArrayList<HashMap<String, String>> documents;
		for (File file : listOfFiles) {
			if (file.isFile()) {
				try (BufferedReader br  = new BufferedReader(new FileReader(file))) {
					processDocs(file, br, writer1,writer2,writer3,writer4);
				}
			}
		}
		writer1.close();
		writer2.close();
		writer3.close();
		writer4.close();
		System.out.print("Done!"+count);
	}
	
	private static void processDocs(File file, BufferedReader br, IndexWriter writer1, IndexWriter writer2,IndexWriter writer3,IndexWriter writer4) throws IOException {
        String line;        
		StringBuffer sb = new StringBuffer();
        while((line = br.readLine()) != null) {
        	sb.append(" " + line);
    		if (line.toString().equalsIgnoreCase("</DOC>")) {
    			HashMap<String,String> doc = processDoc(sb.toString());
    			if (doc != null) {
					indexDoc(writer1, doc);
					indexDoc(writer2, doc);
					indexDoc(writer3, doc);
					indexDoc(writer4, doc);
    			}
        		sb = new StringBuffer();
    		}
        }
	}

	private static HashMap<String, String> processDoc(String doc) {
		String indexedTag = "TEXT";
		Pattern pattern = Pattern.compile("<"+indexedTag+">(.*)</"+indexedTag+">", Pattern.DOTALL);
        HashMap<String, String> dict = new HashMap<String, String>();
        Matcher matcher = pattern.matcher(doc);
        	while(matcher.find()) {
    			if (dict.containsKey(indexedTag)) {
    				dict.put(indexedTag, dict.get(indexedTag)+ " " + matcher.group(1).replaceAll("<"+indexedTag+">", " ").replaceAll("</"+indexedTag+">", " ").trim());
    			}
    			else {
    				dict.put(indexedTag, matcher.group(1).replaceAll("<"+indexedTag+">", " ").replaceAll("</"+indexedTag+">", " ").trim());
    			}
    		}	        
		count++;
		return dict;
	}
	
	private static IndexWriter indexer(int i) {
		String indexPath;
		Directory dir = null;
		Analyzer analyzer = null;
		IndexWriter writer = null;
		try {
			switch(i) {
				case 1:
					indexPath = "./index/Part_2/KeywordAnalyzer";
					System.out.println("Indexing to directory '" + indexPath + "'...");
					dir = FSDirectory.open(Paths.get(indexPath));
					analyzer = new KeywordAnalyzer();
					break;
				case 2:
					indexPath = "./index/Part_2/SimpleAnalyzer";
					System.out.println("Indexing to directory '" + indexPath + "'...");
					dir = FSDirectory.open(Paths.get(indexPath));
					analyzer = new SimpleAnalyzer();
					
					break;
				case 3:
					indexPath = "./index/Part_2/StopAnalyzer";
					System.out.println("Indexing to directory '" + indexPath + "'...");
					dir = FSDirectory.open(Paths.get(indexPath));
					analyzer = new StopAnalyzer();
					
					break;
				case 4:
					indexPath = "./index/Part_2/StandardAnalyzer";
					System.out.println("Indexing to directory '" + indexPath + "'...");
					dir = FSDirectory.open(Paths.get(indexPath));
					analyzer = new StandardAnalyzer();
					
					break;
				
			}
			
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			writer = new IndexWriter(dir, iwc);
		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}
		return writer;

	}
	
	private static void indexDoc(IndexWriter writer, HashMap<String, String> document) throws IOException {
		Document lDoc = new Document();
		if (document.get("TEXT") != null)
			lDoc.add(new TextField("TEXT", document.get("TEXT"), Field.Store.YES));
				
		writer.addDocument(lDoc);
	}

}