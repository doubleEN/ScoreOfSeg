package com.rui.scoreOfSeg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class OrderDiff {

	private static Process execDiff(String comp1, String comp2) throws IOException {
		return Runtime.getRuntime().exec("diff -y " + comp1 + " " + comp2);
	}

	public static void getOutcome(String comp1, String comp2,String outcomeDest) throws IOException {
		FileOutputStream fos=new FileOutputStream(new File(outcomeDest));
		
		Process process = execDiff(comp1, comp2);
		
		InputStream fis =  process.getInputStream();
		
		BufferedReader br=new BufferedReader(new InputStreamReader(fis));
		
		String line=null;
		
		String sen="";
		
		boolean flag=false;
		while((line=br.readLine())!=null){
			if(flag){
				sen=sen+"\n"+line;
			}else{
				sen=sen+line;
				flag=true;
			}
			System.out.println(line);
		}
		
		fos.write(sen.getBytes());
	}

	public static void main(String[] args) throws IOException {
		OrderDiff.getOutcome("diff_src/1", "diff_src/2","mid/midOutcome");
	}
}
