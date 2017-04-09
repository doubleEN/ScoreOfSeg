package com.rui.scoreOfSeg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Hashtable;

public class Score {

	private Hashtable<String, Integer> dict = new Hashtable<String, Integer>();

	private static int sumInsertions = 0;
	private static int sumDeletions = 0;
	private static int sumSubstitutions = 0;
	private static int sumChanges = 0;
	private static int sumTruth = 0;
	private static int sumTest = 0;
	private static int oov = 0;

	//问题：1.循环中频繁开启关闭流;2.空行的容错性;3.命令行的容错性
	//参数：词典输入路径，黄金分割文本路径，自定义分割文本路径，分词评测结果，自定义输出路径left，自定义输出路径right，自定义中间结果路径
	public void toScore(String dictDest, String goldDest, String segDest, String outcomeDest, String left, String right,
			String mid) throws IOException {
		
		setDict(dictDest);
		File goldFile = new File(goldDest);
		File segFile = new File(segDest);

		FileInputStream fis1 = new FileInputStream(goldFile);
		FileInputStream fis2 = new FileInputStream(segFile);

		BufferedReader goldBr = new BufferedReader(new InputStreamReader(fis1, "UTF-8"));
		BufferedReader segBr = new BufferedReader(new InputStreamReader(fis2, "UTF-8"));

		String goldLine = null;
		String segLine = null;
		int lineNum = 0;
		// 换行符不算null

		// 用于向结果文档里面追加内容的输出流
		FileOutputStream fos = new FileOutputStream(new File(outcomeDest), true);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

		// 每次循环，midOutcome内容是变化的
		while ((goldLine = goldBr.readLine()) != null && (segLine = segBr.readLine()) != null) {
			// 统计相关频数
			int insertions = 0;
			int deletions = 0;
			int substitutions = 0;
			int changes = 0;// changes=substitutions+deletions+insertions
			int truthNum = 0;
			int testNum = 0;

			lineNum++;
			/*
			 * if(goldLine==null&&segLine!=null){ System.out.
			 * println("Warning:training is 0 but test is nonzero, possible misalignment at line "
			 * +lineNum+"."); continue; }else if(goldLine!=null&&segLine==null){
			 * System.out.
			 * println("Warning: No output in test data where there is in training data, line "
			 * +lineNum+"."); continue; }
			 */
			String[] goldWords = goldLine.split("\\s+");
			String[] segWords = segLine.split("\\s+");

			System.out.println(Arrays.toString(goldWords));
			System.out.println(Arrays.toString(segWords));
			// int
			// maxLength=goldWords.length>segWords.length?goldWords.length:segWords.length;

			// 同一个流中，只会在同一文件中追加写入的内容，不会产生覆盖的情况
			FileOutputStream goldFos = new FileOutputStream(new File(left), false);
			FileOutputStream segFos = new FileOutputStream(new File(right), false);

			int i = 0;
			while (i < goldWords.length) {
				if (i != goldWords.length - 1) {
					goldFos.write((goldWords[i] + "\n").getBytes());
				} else {
					goldFos.write(goldWords[i].getBytes());
				}
				goldFos.flush();
				i++;
			}
			int j = 0;
			while (j < segWords.length) {
				if (j != segWords.length - 1) {
					segFos.write((segWords[j] + "\n").getBytes());
				} else {
					segFos.write(segWords[j].getBytes());
				}
				segFos.flush();
				j++;
			}
			
			segFos.close();
			goldFos.close();
			// 调用diff命令
			OrderDiff od=new OrderDiff();
			od.getOutcome(left, right, mid);

			FileInputStream fis = new FileInputStream(new File(mid));
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));

			String midLine = null;

			bw.write(">>>>>>" + goldDest + "---" + segDest + "<<<<<<" + " Line Num:" + lineNum);
			bw.newLine();
			bw.flush();
			while ((midLine = br.readLine()) != null) {
				bw.write(midLine);
				bw.newLine();
				bw.flush();
				midLine = midLine.trim();// 习惯去首尾
				String[] parts = midLine.split("\\s+");
				if (parts.length == 3) {
					System.out.println(Arrays.toString(parts));
					String oovWord = parts[2].trim();
					// 未登录词判断
					if (!dict.containsKey(oovWord)) {
						oov++;
					}
					substitutions++;
					changes++;
				} else {
					if (parts[0].equals(">")) {
						System.out.println(Arrays.toString(parts));
						String oovWord = parts[1].trim();
						// test中多出的词
						if (!dict.containsKey(oovWord)) {
							oov++;
						}
						insertions++;
						changes++;
					} else if (parts[1].equals("<")) {
						System.out.println(Arrays.toString(parts));
						// test中少了的词
						deletions++;
						changes++;
					}
				}
			}
			br.close();
			fis.close();
			truthNum = goldWords.length;
			testNum = segWords.length;
			double recall = (testNum - substitutions - insertions) / (double) truthNum;
			double precision = (testNum - substitutions - insertions) / (double) testNum;

			System.out.println("deletions:" + deletions + "  " + "insertions:" + insertions + " " + "substitutions:"
					+ substitutions);
			System.out.println("changes:" + changes);
			System.out.println("truthNum:" + truthNum);
			System.out.println("testNum:" + testNum);
			System.out.println("true words recall:" + recall);
			System.out.println("test words precision:" + precision);

			// 将一次统计结果输入到结果文档中
			bw.write("insertions:" + insertions);
			bw.newLine();
			bw.write("deletions:" + deletions);
			bw.newLine();
			bw.write("substitutions:" + substitutions);
			bw.newLine();
			bw.write("changes:" + changes);
			bw.newLine();
			bw.write("truthNum:" + truthNum);
			bw.newLine();
			bw.write("testNum:" + testNum);
			bw.newLine();
			bw.write("true words recall:" + recall);
			bw.newLine();
			bw.write("test words precision:" + precision);
			bw.newLine();
			bw.flush();

			// 计入总量中
			sumInsertions += insertions;
			sumDeletions += deletions;
			sumSubstitutions += substitutions;
			sumChanges += changes;
			sumTruth += truthNum;
			sumTest += testNum;

		}

		double talRecall = (sumTest - sumInsertions - sumSubstitutions) / (double) sumTruth;
		double talPrecision = (sumTest - sumInsertions - sumSubstitutions) / (double) sumTest;
		double f = (2 * (talPrecision * talRecall)) / ((double) (talRecall + talPrecision));
		double oovRate = oov / (double) sumTruth;

		bw.write(">>>>>  SUMMARY  <<<<<<");
		bw.newLine();
		bw.write("> TOTAL INSERTIONS:" + sumInsertions);
		bw.newLine();
		bw.write("> TOTAL DELETIONS:" + sumDeletions);
		bw.newLine();
		bw.write("> TOTAL SUBSTITUTIONS:" + sumSubstitutions);
		bw.newLine();
		bw.write("> TOTAL CHANGE:" + sumChanges);
		bw.newLine();
		bw.write("> TOTAL TRUE WORD COUNT:" + sumTruth);
		bw.newLine();
		bw.write("> TOTAL TEST WORD COUNT:" + sumTest);
		bw.newLine();
		bw.write("> OOV NUM:" + oov);
		bw.newLine();
		bw.write("> TOTAL TRUE WORDS RECALL:" + talRecall);
		bw.newLine();
		bw.write("> TOTAL TEST WORDS PRECISION:" + talPrecision);
		bw.newLine();
		bw.write("> HARMONIC MEAN F:" + f);
		bw.newLine();
		bw.write("> OOV Rate:" + oovRate);
		bw.flush();
		
		bw.close();
		fos.close();
		
		segBr.close();
		goldBr.close();
	}

	public void setDict(String dictDest) throws IOException {
		File dictFile = new File(dictDest);
		FileInputStream fileInputStream = new FileInputStream(dictFile);
		BufferedReader dictBr = new BufferedReader(new InputStreamReader(fileInputStream, "UTF-8"));
		String line = null;
		int i = 1;
		while ((line = dictBr.readLine()) != null) {
			String word = line.trim();
			dict.put(word, i++);
			System.out.println("key-no:" + word + "-" + dict.get(word));
		}
		dictBr.close();
	}
}
