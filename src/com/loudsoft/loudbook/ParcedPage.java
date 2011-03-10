package com.loudsoft.loudbook;

public class ParcedPage {
	public String audioFile;
	public String text;
	
	public ParcedPage(String file, String newText) {
		audioFile = file;
		text = newText;
	}
}
