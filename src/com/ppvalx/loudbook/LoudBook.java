package com.ppvalx.loudbook;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LoudBook extends Activity {

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        EditText mSearchFolderName = (EditText) findViewById(R.id.search_folder_name);
        EditText mBookConfigFileName = (EditText) findViewById(R.id.book_config_file_name);
        Button mOpenConfigFileButton = (Button) findViewById(R.id.open_config_file_button);
        mOpenConfigFileButton.setOnClickListener(mOpenFileListener );
    }
    
    /**
     * Open config file button
     */
    OnClickListener mOpenFileListener = new OnClickListener() {
        public void onClick(View v) {
            finish();
        }
    };
}