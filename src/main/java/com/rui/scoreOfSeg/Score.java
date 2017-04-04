package com.rui.scoreOfSeg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Hashtable;

public class Score {
	
	private Hashtable<String, Integer> dict=new Hashtable<String, Integer>();
	
	public static void main(String[] args) throws IOException {
		Score s=new Score();
		//s.setDict("test/words.utf8");
		s.toScore("test/gold.utf8", "test/seg.utf8");
	}
	
	private void toScore(String goldDest, String segDest) throws IOException {
		File goldFile = new File(goldDest);
		File segFile = new File(segDest);
		
		FileInputStream fis1=new FileInputStream(goldFile);
		FileInputStream fis2=new FileInputStream(segFile);

		BufferedReader goldBr=new BufferedReader(new InputStreamReader(fis1, "UTF-8"));
		BufferedReader segBr=new BufferedReader(new InputStreamReader(fis2, "UTF-8"));

		String goldLine=null; 
		String segLine=null;
		int lineNum=0;
		//换行符不算null
		//同一个流中，只会在同一文件中追加写入的内容，不会产生覆盖的情况
		FileOutputStream goldFos=new FileOutputStream(new File("diff_src/left"));
		FileOutputStream segFos=new FileOutputStream(new File("diff_src/right"));
		
		
		while((goldLine=goldBr.readLine())!=null&&(segLine=segBr.readLine())!=null){
			/*lineNum++;
			if(goldLine==null&&segLine!=null){
				System.out.println("Warning:training is 0 but test is nonzero, possible misalignment at line "+lineNum+".");
				continue;
			}else if(goldLine!=null&&segLine==null){
				System.out.println("Warning: No output in test data where there is in training data, line "+lineNum+".");
				continue;
			}*/
			String [] goldWords=goldLine.split("\\s+");
			String [] segWords=segLine.split("\\s+");
			
			System.out.println(Arrays.toString(goldWords));
			System.out.println(Arrays.toString(segWords));
			//int maxLength=goldWords.length>segWords.length?goldWords.length:segWords.length;
			
			System.out.println(goldWords.length);
			System.out.println(segWords.length);
			
			int i=0;
			while(i<goldWords.length){
				if(i!=goldWords.length-1){
					goldFos.write((goldWords[i]+"\n").getBytes());
				}
				goldFos.flush();
				i++;
			}
			int j=0;
			while(j<segWords.length){
				if(j!=segWords.length-1){
					segFos.write((segWords[j]+"\n").getBytes());
				}
				segFos.flush();
				j++;
			}
			
			//调用diff命令
			OrderDiff.getOutcome("diff_src/left", "diff_src/right", "mid/midOutcome");
			break;
		}
		
		
	}

	public void setDict(String dictDest) throws IOException{
		File dictFile = new File(dictDest);
		FileInputStream fileInputStream=new FileInputStream(dictFile);
		BufferedReader dictBr=new BufferedReader(new InputStreamReader(fileInputStream, "UTF-8"));
		String line=null;
		int i=1;
		while((line=dictBr.readLine())!=null){
			String word=line.trim();
			dict.put(word, i++);
			System.out.println("key-no:"+word+"-"+dict.get(word));
		}
		dictBr.close();
	}
}