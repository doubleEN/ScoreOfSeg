package com.rui.scoreOfSeg;

import java.io.IOException;

public class Exec {
	public static void main(String[] args) throws IOException {
		Score s=new Score();
		s.toScore(args[0],args[1], args[2],args[3]);
		//s.toScore("input/pku_training_words.utf8","input/pku_test_gold.utf8", "input/pku_test_seg.utf8","output/outcome");
	}
}
