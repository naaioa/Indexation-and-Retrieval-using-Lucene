import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory; 


public class generateIndex {
	static int count = 0;
	public static void main(String []args) throws FileNotFoundException, IOException {
		File folder = new File("./corpus");
		File[] listOfFiles = folder.listFiles();
		
		ArrayList<String> indexedTags = new ArrayList<String>(Arrays.asList("DOCNO","HEAD","BYLINE","DATELINE","TEXT"));
        HashMap<String, Pattern> patterns = new HashMap<String, Pattern>();
        for (String tag: indexedTags) {
        	patterns.put(tag,Pattern.compile("<"+tag+">(.*)</"+tag+">", Pattern.DOTALL));	
        }
        IndexWriter writer = indexer();
        ArrayList<HashMap<String, String>> documents;
		for (File file : listOfFiles) {
			if (file.isFile()) {
				try (BufferedReader br  = new BufferedReader(new FileReader(file))) {
					documents = processDocs(file, br, indexedTags, patterns);
				}
				for (HashMap<String, String> document : documents) {
					indexDoc(writer, document);
				}
			}
		}
		writer.close();
		System.out.print("Done!"+count);
	}
	
	private static ArrayList<HashMap<String, String>> processDocs(File file, BufferedReader br, ArrayList<String> indexedTags, HashMap<String, Pattern> patterns) throws IOException {
        String line;        
		StringBuffer sb = new StringBuffer();
		ArrayList<HashMap<String, String>> documents = new ArrayList();
        while((line = br.readLine()) != null) {
        	sb.append(" " + line);
    		if (line.toString().equalsIgnoreCase("</DOC>")) {
    			documents.add(processDoc(sb.toString(), indexedTags, patterns));
        		sb = new StringBuffer();
    		}
        }
        return documents;
	}

	private static HashMap<String, String> processDoc(String doc, ArrayList<String> indexedTags, HashMap<String, Pattern> patterns) {
		Matcher matcher;
        Pattern currentPattern;
        HashMap<String, String> dict = new HashMap<String, String>();
		for (String tag: indexedTags) {
			currentPattern = patterns.get(tag);
			matcher = currentPattern.matcher(doc);
			while(matcher.find()) {
				if (dict.containsKey(tag)) {
					dict.put(tag, dict.get(tag)+ " " + matcher.group(1).trim());
				}
				else {
					dict.put(tag, matcher.group(1).trim());
				}
			}
		}
		count++;
		return dict;
	}
	
	private static IndexWriter indexer() {
		String indexPath = "./index/Part_1";
		
		try {
			
			System.out.println("Indexing to directory '" + indexPath + "'...");

			Directory dir = FSDirectory.open(Paths.get(indexPath));
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);

			IndexWriter writer = new IndexWriter(dir, iwc);
			return writer;
		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
			return null;
		}
		
	}
	
	private static void indexDoc(IndexWriter writer, HashMap<String, String> document) throws IOException {
		// make a new, empty document
		Document lDoc = new Document();
		if (document.get("DOCNO") != null)
			lDoc.add(new StringField("DOCNO", document.get("DOCNO"),Field.Store.YES));
		if (document.get("HEAD") != null)
			lDoc.add(new TextField("HEAD", document.get("HEAD"), Field.Store.NO));
		if (document.get("BYLINE") != null)
			lDoc.add(new TextField("BYLINE", document.get("BYLINE"), Field.Store.NO));
		if (document.get("DATELINE") != null)
			lDoc.add(new TextField("DATELINE", document.get("DATELINE"), Field.Store.NO));
		if (document.get("TEXT") != null)
			lDoc.add(new TextField("TEXT", document.get("TEXT"), Field.Store.NO));
				
		writer.addDocument(lDoc);
	}

}