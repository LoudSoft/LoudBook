package com.loudsoft.loudbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainTextOutputActivity extends Activity {

	private LogPrinter LOG;
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
        File xmlFile = new File(fileName);

        StringBuilder text = new StringBuilder();
        
        try {
        	XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        	factory.setNamespaceAware(true);
        	XmlPullParser xpp = factory.newPullParser();

        	xpp.setInput(new FileReader(xmlFile));
        	int eventType = xpp.getEventType();
        	while (eventType != XmlPullParser.END_DOCUMENT) {
        		if(eventType == XmlPullParser.START_DOCUMENT) {
        			text.append("Start document");
        			text.append('\n');
        		} else if(eventType == XmlPullParser.START_TAG) {
        			text.append("Start tag " + xpp.getName());
        			text.append('\n');
        		} else if(eventType == XmlPullParser.END_TAG) {
        			text.append("End tag " + xpp.getName());
        			text.append('\n');
        		} else if(eventType == XmlPullParser.TEXT) {
        			text.append("Text " + xpp.getText());
        			text.append('\n');
        		}
        		eventType = xpp.next();
        	}
        } catch (XmlPullParserException e) {
            LOG.E("onCreate()", "XmlPullParser error.", e);
            return;
        } catch (FileNotFoundException e) {
            LOG.E("onCreate()", "File not found. File: " + xmlFile, e);
            return;
		} catch (IOException e) {
            LOG.E("onCreate()", "IO error. File: " + xmlFile, e);
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
