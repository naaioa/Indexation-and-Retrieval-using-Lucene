import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.queryparser.classic.ParseException;

public class searchTRECtopics {

	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
		List<ArrayList<String>> qList = getQueryList();
		ArrayList<String> titleList = qList.get(0);
		ArrayList<String> descList = qList.get(1); 
		//System.out.println(descList.size());
		//System.out.println(titleList.size());
		
		String mySearchAlgoShortFile = ".\\docs\\mySearchAlgoShortQuery.txt"; 
		String mySearchAlgoLongFile = ".\\docs\\mySearchAlgoLongQuery.txt"; 
		
        FileWriter fw=new FileWriter(mySearchAlgoShortFile);   
        FileWriter fw1=new FileWriter(mySearchAlgoLongFile);    
		
		easySearch obj = new easySearch();
		HashMap<String, Double> titleSearchScores = new HashMap<String, Double>();
		HashMap<String, Double> descSearchScores = new HashMap<String, Double>();
		for(int i=0; i<50; i++) {
			System.out.println("Working on Query "+(i+51));
			titleSearchScores = obj.getRelevanceScores(titleList.get(i), "TEXT");
			descSearchScores = obj.getRelevanceScores(descList.get(i), "TEXT");
			writeScoresToFile(titleSearchScores,fw,i,"short");
			writeScoresToFile(descSearchScores,fw1,i,"long");			
		}
		fw.close(); 
		fw1.close();
		System.out.print("Done!");
	}
	
	private static void writeScoresToFile(HashMap<String, Double> SearchScores, FileWriter fw, int qNo, String queryType) throws IOException {
		Set<String> keys = SearchScores.keySet();
		int r=1;
        for(String key: keys){
            //System.out.println(key+ ": "+SearchScores.get(key));
    		fw.write(Integer.toString(qNo+51)+" Q"+qNo+" "+key.substring(key.indexOf(":")+1)+" "+r+" "+SearchScores.get(key)+" run-"+(1)+"\n");  
    		r++;
        }		
	}

	public static List<ArrayList<String>> getQueryList() throws IOException {
		List<ArrayList<String>> qList = new ArrayList<ArrayList<String>>();
		File file = new File("./docs/topics.51-100");
		StringBuffer sb = new StringBuffer();
		ArrayList<String> titleList = new ArrayList<String>(); 
		ArrayList<String> descList = new ArrayList<String>(); 
		if (file.isFile()) {
			try (BufferedReader br  = new BufferedReader(new FileReader(file))) {
				String strCurrentLine;
				Boolean start = false;
				Boolean stop = false;
				while ((strCurrentLine = br.readLine()) != null) {
					//System.out.println(strCurrentLine);
					if(strCurrentLine.startsWith("<title>")) {
						start = true;
						stop = false;
					}
					if(strCurrentLine.startsWith("<smry>")) {
						sb.append("<");
						stop = true;
						start = false;
					}
					if(start == true && stop != true) {
						sb.append(" " + strCurrentLine);
					}						
				}
			}
		}
		Matcher m = Pattern.compile("(?<=<title>).+?(?=<desc>)", Pattern.DOTALL)
				.matcher(sb.toString().replace("Topic:", ""));
		while (m.find()) {
			titleList.add(m.group());
		}
		m = Pattern.compile("(?<=<desc>).+?(?=<)", Pattern.DOTALL)
				.matcher(sb.toString());
		while (m.find()) {
			descList.add(m.group().replace("Description:", ""));
		}
		qList.add(titleList);
		qList.add(descList);
		return qList;
	}
}
