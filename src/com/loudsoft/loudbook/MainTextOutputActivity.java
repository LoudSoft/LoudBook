package com.loudsoft.loudbook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainTextOutputActivity extends Activity {

	private LogPrinter LOG;
	private File mFile;
	private TextView mMainTextView;
	private Button mBackButton;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_text_output_activity);
        LOG = new LogPrinter(true, this.getString(R.string.app_name), this.getClass().getSimpleName());

        mMainTextView = (TextView) findViewById(R.id.main_text_view);
        mBackButton = (Button) findViewById(R.id.back_button);
        mBackButton.setOnClickListener(mBackListener );
        
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            LOG.E("onCreate()", "Unable to retrieve extras");
            return;            
        }
        String fileName = extras.getString(LoudBook.EXTRA_FILE_NAME);
        if(fileName == null || fileName.isEmpty()){
            LOG.E("onCreate()", "Unable to retrieve file name");
            return;            
        }
        
        //Get the file
        File mFile = new File(fileName);

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
        	BufferedReader br = new BufferedReader(new FileReader(mFile));
        	String line;

        	while ((line = br.readLine()) != null) {
        		text.append(line);
        		text.append('\n');
        	}
        } catch (IOException e) {
            LOG.E("onCreate()", "Unable to read file: " + mFile, e);
            return;            
        }
        mMainTextView.setText(text);
    }
    
    /**
     * Back button
     */
    OnClickListener mBackListener = new OnClickListener() {
        public void onClick(View v) {
            //finish();
        	LOG.D("onClick()", "Go back to LoudBook main activity.");

    		startActivity(new Intent(MainTextOutputActivity.this, LoudBook.class));
        }
    };
}
