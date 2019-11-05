import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

public class compareAlgorithms {

	public static void main(String[] args) throws IOException, ParseException {
		
		searchTRECtopics obj = new searchTRECtopics();
		List<ArrayList<String>> qList = obj.getQueryList();
		ArrayList<String> titleList = qList.get(0);
		ArrayList<String> descList = qList.get(1); 
		//System.out.println(descList.size());
		//System.out.println(titleList.size());
		
		Similarity s1 = new ClassicSimilarity();	
		Similarity s2 = new BM25Similarity();
		Similarity s3 = new LMJelinekMercerSimilarity(0.7f);
		Similarity s4 = new LMDirichletSimilarity();
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("./index")));
		IndexSearcher searcher1 = new IndexSearcher(reader); 
		IndexSearcher searcher2 = new IndexSearcher(reader); 
		IndexSearcher searcher3 = new IndexSearcher(reader); 
		IndexSearcher searcher4 = new IndexSearcher(reader); 
		
		String DefaultShortQuery = ".\\docs\\DefaultShortQuery.txt"; 
		String DefaultLongQuery = ".\\docs\\DefaultLongQuery.txt"; 
		String BM25ShortQuery = ".\\docs\\BM25ShortQuery.txt"; 
		String BM25LongQuery = ".\\docs\\BM25LongQuery.txt"; 
		String LMJelinekMercerShortQuery = ".\\docs\\LMJelinekMercerShortQuery.txt"; 
		String LMJelinekMercerLongQuery = ".\\docs\\LMJelinekMercerLongQuery.txt"; 
		String LMDirichletShortQuery = ".\\docs\\LMDirichletShortQuery.txt"; 
		String LMDirichletLongQuery = ".\\docs\\LMDirichletLongQuery.txt"; 
		
		FileWriter defaultShortWriter=new FileWriter(DefaultShortQuery);  
		FileWriter defaultLongWriter=new FileWriter(DefaultLongQuery);
		
		FileWriter BM25ShortWriter=new FileWriter(BM25ShortQuery);  
		FileWriter BM25LongWriter=new FileWriter(BM25LongQuery);
		
		FileWriter LMJelinekMercerShortWriter=new FileWriter(LMJelinekMercerShortQuery);  
		FileWriter LMJelinekMercerLongWriter=new FileWriter(LMJelinekMercerLongQuery);
		
		FileWriter LMDirichletShortWriter=new FileWriter(LMDirichletShortQuery);  
		FileWriter LMDirichletLongWriter=new FileWriter(LMDirichletLongQuery);
		
		String queryString;
		
		for(int in=0; in<50; in++) {
			//System.out.println(descList.get(in));
			queryString = titleList.get(in);
			searchQueryUsingAlgo(in, "short",queryString, s1, searcher1, defaultShortWriter);
			searchQueryUsingAlgo(in, "short",queryString, s2, searcher2, BM25ShortWriter);
			searchQueryUsingAlgo(in, "short",queryString, s3, searcher3, LMJelinekMercerShortWriter);
			searchQueryUsingAlgo(in, "short",queryString, s4, searcher4, LMDirichletShortWriter);

			queryString = descList.get(in);
			searchQueryUsingAlgo(in, "long",queryString, s1, searcher1, defaultLongWriter);
			searchQueryUsingAlgo(in, "long",queryString, s2, searcher2, BM25LongWriter);
			searchQueryUsingAlgo(in, "long",queryString, s3, searcher3, LMJelinekMercerLongWriter);
			searchQueryUsingAlgo(in, "long",queryString, s4, searcher4, LMDirichletLongWriter);
		}
		defaultShortWriter.close();
		defaultLongWriter.close();
		BM25ShortWriter.close();
		BM25LongWriter.close();
		LMJelinekMercerShortWriter.close();
		LMJelinekMercerLongWriter.close();
		LMDirichletLongWriter.close();
		LMDirichletShortWriter.close();
		System.out.print("Done");
	}
	
	public static void searchQueryUsingAlgo(int qNo, String queryType, String queryString, Similarity sim, IndexSearcher searcher, FileWriter fw) throws IOException, ParseException {
		searcher.setSimilarity(sim); 
		Analyzer analyzer = new StandardAnalyzer(); 
		QueryParser parser = new QueryParser("TEXT", analyzer);     
		Query query = parser.parse(queryString.replace("/", " ")); 
		TopScoreDocCollector collector = TopScoreDocCollector.create(1000); 
		searcher.search(query, collector);          
		ScoreDoc[] docs = collector.topDocs().scoreDocs; 
		for (int i = 0; i < docs.length; i++) 
		{  
			Document doc = searcher.doc(docs[i].doc);  
			//System.out.println(doc.get("DOCNO")+" "+docs[i].score); 
			String num = "";
			if(queryType == "short")
				num = Integer.toString(qNo*2);
			else
				num = Integer.toString(qNo*2+1);
			fw.append(Integer.toString(qNo+51)+" Q"+num+" "+doc.get("DOCNO")+" "+Integer.toString(i+1)+" "+docs[i].score+" run-"+(1)+"\n");
		}       
	}

}
