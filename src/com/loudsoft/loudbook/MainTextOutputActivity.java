package com.loudsoft.loudbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
	private Button mPreviousPageButton;
	private Button mNextPageButton;
	private int mPageNumber = -1;
	public static final String PAGE_TAG_NAME = "page";
	public static final String PAGE_AUDIO_FILE_ATTRIBUTE_NAME = "audio_file";
	private Map<Integer, ParcedPage> mParcedData = new HashMap<Integer, ParcedPage>();

	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_text_output_activity);
        LOG = new LogPrinter(true, this.getString(R.string.app_name), this.getClass().getSimpleName());
        mPageNumber = 0;
        mMainTextView = (TextView) findViewById(R.id.main_text_view);
        mBackButton = (Button) findViewById(R.id.back_button);
        mBackButton.setOnClickListener(mBackListener);
        mPreviousPageButton = (Button) findViewById(R.id.previous_page_button);
        mPreviousPageButton.setOnClickListener(mPreviousPageListener);
        mNextPageButton = (Button) findViewById(R.id.next_page_button);
        mNextPageButton.setOnClickListener(mNextPageListener);
        
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

        //StringBuilder text = new StringBuilder();
        String tagName = "";
        String audioFileName = "";
        String parcedText = "";
        int count = 0;
        boolean isPageTag = false;
        
        try {
        	XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        	factory.setNamespaceAware(true);
        	XmlPullParser xpp = factory.newPullParser();
        	xpp.setInput(new FileReader(xmlFile));
        	int eventType = xpp.getEventType();
        	while (eventType != XmlPullParser.END_DOCUMENT) {
        		if(eventType == XmlPullParser.START_DOCUMENT) {
        		} else if(eventType == XmlPullParser.START_TAG) {
        			tagName = xpp.getName();
        			if(tagName.equalsIgnoreCase(PAGE_TAG_NAME)) {
        				isPageTag = true;
        				audioFileName = xpp.getAttributeValue(null, PAGE_AUDIO_FILE_ATTRIBUTE_NAME);
        				if(audioFileName == null)
        				{
        					audioFileName = "";
        				}
        			}
        		} else if(eventType == XmlPullParser.END_TAG) {
        			isPageTag = false;
        		} else if(eventType == XmlPullParser.TEXT) {
        			if(tagName.equalsIgnoreCase(PAGE_TAG_NAME)) {
        				parcedText = xpp.getText();
        				if(audioFileName != null && isPageTag)
        				{
        					count++;
        					mParcedData.put(count, new ParcedPage(audioFileName, parcedText));
        				}
        			}
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
		mPageNumber = 1;
		if(mParcedData.containsKey(mPageNumber)) {
			mMainTextView.setText(mParcedData.get(mPageNumber).text);
		} else {
            LOG.E("onCreate()", "Empty hash map! Size = " + mParcedData.size() + ", mPageNumber = " + mPageNumber);
            return;
		}
    }

     /**
     * Back button
     */
    OnClickListener mBackListener = new OnClickListener() {
        public void onClick(View v) {
            //finish();
        	LOG.D("mBackListener", "Go back to LoudBook main activity.");

    		startActivity(new Intent(MainTextOutputActivity.this, LoudBook.class));
        }
    };
    
    /**
     * Previous page button
     */
    OnClickListener mPreviousPageListener = new OnClickListener() {
        public void onClick(View v) {
            //finish();
        	LOG.D("mPreviousPageListener", "Go to previous page #" + (mPageNumber - 1));

    		if(mParcedData.containsKey(mPageNumber - 1)) {
    			mPageNumber--;
    			mMainTextView.setText(mParcedData.get(mPageNumber).text);
                //LOG.I("mPreviousPageListener()", "Text: " + mParcedData.get(mPageNumber).text);
    		} else {
                LOG.I("mPreviousPageListener()", "First page! Size = " + mParcedData.size() + ", mPageNumber = " + mPageNumber);
    		}
        }
    };
    
    /**
     * Next page button
     */
    OnClickListener mNextPageListener = new OnClickListener() {
        public void onClick(View v) {
            //finish();
        	LOG.D("mNextPageListener", "Go to next page #" + (mPageNumber + 1));

    		if(mParcedData.containsKey(mPageNumber + 1)) {
    			mPageNumber++;
    			mMainTextView.setText(mParcedData.get(mPageNumber).text);
                //LOG.I("mNextPageListener()", "Text: " + mParcedData.get(mPageNumber).text);
    		} else {
                LOG.I("mNextPageListener()", "Last page! Size = " + mParcedData.size() + ", mPageNumber = " + mPageNumber);
    		}
        }
    };
}
