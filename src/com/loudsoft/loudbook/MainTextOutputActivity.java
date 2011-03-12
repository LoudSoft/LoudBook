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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainTextOutputActivity extends Activity {

	private LogPrinter LOG;
	private Context context;
	private int duration = Toast.LENGTH_LONG;
	private TextView mMainTextView;
	private Button mBackButton;
	private Button mPreviousPageButton;
	private Button mNextPageButton;
	private String mBaseAudioDir;
	private int mPageNumber = -1;
	private File mAudioFile;
	private MediaPlayer mMediaPlayer = null;
	private File mXMLFile = null;
	private boolean mParsingResult = false;
	private boolean mCancelParsing = false;
	private boolean mFirstStart = true;
	private ProgressDialog mParcingProgressDialog = null;
	
	public static final String BASE_AUDIO_DIR_TAG_NAME = "base_audio_dir";
	public static final String PAGE_TAG_NAME = "page";
	public static final String PAGE_AUDIO_FILE_ATTRIBUTE_NAME = "audio_file";
	private Map<Integer, ParcedPage> mParcedData = new HashMap<Integer, ParcedPage>();
	
//	private boolean mExternalStorageAvailable;
//	private boolean mExternalStorageWriteable;
	private static final int REQUEST_CHOOSE_AUDIO_DIR = 2;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_text_output_activity);
        LOG = new LogPrinter(true, this.getString(R.string.app_name), this.getClass().getSimpleName());
        context = this;//getApplicationContext();
        
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
        mXMLFile = new File(fileName);
        
        mMediaPlayer = new MediaPlayer();
    }
        
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mFirstStart) {
            mParcingProgressDialog = new ProgressDialog(context);
            mParcingProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mParcingProgressDialog.setMessage(getText(R.string.parsing_please_wait));
            mParcingProgressDialog.setButton(getText(R.string.cancel), 
    				new DialogInterface.OnClickListener() {
    					@Override
    					public void onClick(DialogInterface dialog,	int which) {
    						mCancelParsing = true;
    					}
    				});
            mParcingProgressDialog.show();
        	//mParcingProgressDialog.setProgress(0);
        
        	threadParseLoudBookXML();
        	mFirstStart = false;
        }
    }

    // Need handler for callbacks to the UI thread
    final Handler mHandler = new Handler();

    // Create runnable for posting
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
        	onParcingComplete();
    		mParcingProgressDialog.cancel();
    		Toast.makeText(context, "Parced " + mParcedData.size() + " pages", duration).show();
        }
    };
    
    private boolean parseLoudBookXML() {
    	LOG.D("parseLoudBookXML()", "Parcing: " + mXMLFile);
    	
        //StringBuilder text = new StringBuilder();
        String tagName = "";
        String audioFileName = "";
        String parcedText = "";
        mBaseAudioDir = "/";
        int count = 0;
        boolean isPageTag = false;
        boolean isBaseAudioDirTag = false;
        
        try {
        	XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        	factory.setNamespaceAware(true);
        	XmlPullParser xpp = factory.newPullParser();
        	FileReader xmlFileReader = new FileReader(mXMLFile);
        	xpp.setInput(xmlFileReader);
        	int eventType = xpp.getEventType();
        	while (eventType != XmlPullParser.END_DOCUMENT && !mCancelParsing) {
        		if(eventType == XmlPullParser.START_DOCUMENT) {
        		} else if(eventType == XmlPullParser.START_TAG) {
        			tagName = xpp.getName();
					//LOG.I("parseLoudBookXML()", "Parced tagName: " + tagName);
        			if(tagName.equalsIgnoreCase(PAGE_TAG_NAME)) {
        				isPageTag = true;
        				audioFileName = xpp.getAttributeValue(null, PAGE_AUDIO_FILE_ATTRIBUTE_NAME);
        				if(audioFileName == null)
        				{
        					audioFileName = "";
        				}
        			} else if(tagName.equalsIgnoreCase(BASE_AUDIO_DIR_TAG_NAME)) {
        				isBaseAudioDirTag = true;
        			}

        		} else if(eventType == XmlPullParser.END_TAG) {
        			isPageTag = false;
        			isBaseAudioDirTag = false;
        		} else if(eventType == XmlPullParser.TEXT) {
        			parcedText = xpp.getText();
        			if(audioFileName != null && isPageTag)
        			{
        				count++;
        				mParcedData.put(count, new ParcedPage(audioFileName, parcedText));
        				//mParcingProgressDialog.setMessage(getText(R.string.parsing_please_wait) + " \nPage: " + count);
        			} else if(isBaseAudioDirTag) {
        				mBaseAudioDir = parcedText;
        				LOG.I("parseLoudBookXML()", "Parced mBaseAudioDir: " + mBaseAudioDir);
        			}
        		}
        		//mParcingProgressDialog.setProgress();
        		eventType = xpp.next();
        	}
        } catch (XmlPullParserException e) {
            LOG.E("parseLoudBookXML()", "XmlPullParser error.", e);
            return false;
        } catch (FileNotFoundException e) {
            LOG.E("parseLoudBookXML()", "File not found. File: " + mXMLFile, e);
            return false;
		} catch (IOException e) {
            LOG.E("parseLoudBookXML()", "IO error. File: " + mXMLFile, e);
            return false;
		}
		return true;
    }


    protected void threadParseLoudBookXML() {
        Thread t = new Thread() {
            public void run() {
            	/*
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    LOG.E("threadParseLoudBookXML()", "Thread Interrupted");
                }
                */
            	mParsingResult = parseLoudBookXML();
                mHandler.post(mUpdateResults);
            }
        };
        t.start();
    }


    private void onParcingComplete() {
		mPageNumber = 1;
		if(!mParsingResult) {
			mMainTextView.setText(getText(R.string.parsing_error));
			Toast.makeText(context, getText(R.string.parsing_error), duration).show();
			LOG.E("onParcingComplete()", getText(R.string.parsing_error).toString());
			return;
		}
		if(mParcedData.containsKey(mPageNumber)) {
			mMainTextView.setText(mParcedData.get(mPageNumber).text);
			if(mParcedData.size() > 1)
				mNextPageButton.setEnabled(true);

		} else {
            LOG.E("onParcingComplete()", "Empty hash map! Size = " + mParcedData.size() + ", mPageNumber = " + mPageNumber);
            return;
		}

		String sdDir = Environment.getExternalStorageDirectory().getPath();
		if(mBaseAudioDir != null && !mBaseAudioDir.isEmpty()) {
			LOG.I("onParcingComplete()", "mBaseAudioDir before: " + mBaseAudioDir);
			mBaseAudioDir = mBaseAudioDir.replaceAll("_sdcard_", sdDir);
			LOG.I("onParcingComplete()", "mBaseAudioDir after : " + mBaseAudioDir);
			File audioDir = new File(mBaseAudioDir); 
			if(!audioDir.canRead()) {
				selectDir();
			} else {
				if(!playAudio(mParcedData.get(mPageNumber).audioFile))
					selectDir();
			}
		} else {
			mBaseAudioDir = sdDir;
			selectDir();
		}
    }

    
    private boolean playAudio(String file) {
    	if(file.isEmpty())
    		return true;
    	
    	if(mMediaPlayer.isPlaying()) {    		
    		mMediaPlayer.stop();
    	}
		//mMediaPlayer.release();
		mMediaPlayer.reset();
		
    	mAudioFile = new File(mBaseAudioDir, file);
    	if(mAudioFile.canRead()) {
    		Toast.makeText(context, "Play: '" + mAudioFile + "'", duration).show();
    		try {
				mMediaPlayer.setDataSource(mAudioFile.getPath());
	    		mMediaPlayer.prepare();
			} catch (IllegalArgumentException e) {
				LOG.E("playAudio()", "Illegal argument. File: " + mAudioFile, e);
				return false;
			} catch (IllegalStateException e) {
				LOG.E("playAudio()", "Illegal state. File: " + mAudioFile, e);
				return false;
			} catch (IOException e) {
				LOG.E("playAudio()", "IO error. File: " + mAudioFile, e);
				return false;
			}
    		mMediaPlayer.start();
    		return true;
    	} else {
    		Toast.makeText(context, "Unable to read file: '" + mAudioFile + "'", duration).show();
    		return false;
    	}
    }
    
    private void selectDir() {
    	Intent intent = new Intent(MainTextOutputActivity.this, FileDialog.class);
    	intent.putExtra(FileDialog.START_PATH, mBaseAudioDir);
    	intent.putExtra(FileDialog.DIR_OR_FILE, FileDialog.SEARCH_DIR);
    	intent.putExtra(FileDialog.FILE_EXTENSION, this.getString(R.string.audio_file_extension));
		startActivityForResult(intent, REQUEST_CHOOSE_AUDIO_DIR );
		mAudioFile = new File(mBaseAudioDir, mParcedData.get(mPageNumber).audioFile);
    }
    
    // Listen for results.
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        // See which child activity is calling us back.
        switch (requestCode) {
            case REQUEST_CHOOSE_AUDIO_DIR:
                // This is the standard resultCode that is sent back if the
                // activity crashed or didn't doesn't supply an explicit result.
                if (resultCode == RESULT_OK){
                	String newAudioDir = data.getStringExtra(FileDialog.RESULT_PATH); 
                	File audioDir = new File(newAudioDir);
                	if(audioDir.canRead()) {
                		mBaseAudioDir = newAudioDir;
                		playAudio(mParcedData.get(mPageNumber).audioFile);
                		LOG.I("onActivityResult()", "Path: " + mBaseAudioDir);
                	} else {
                    	Toast.makeText(context, "Unable to read dir: " + newAudioDir, duration).show();
                    	LOG.E("onActivityResult()", "Unable to read dir: " + newAudioDir);
                		return;
                	}
                } 
                else {
                	Toast.makeText(context, "Unable to retrive dir", duration).show();
                	LOG.E("onActivityResult()", "Unable to retrive dir");
            		return;
                }
            default:
                break;
        }
    }
/*
    private void checkExternalStorage(){
    	String state = Environment.getExternalStorageState();

    	if (Environment.MEDIA_MOUNTED.equals(state)) {
    	    // We can read and write the media
    	    mExternalStorageAvailable = mExternalStorageWriteable = true;
    	} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
    	    // We can only read the media
    	    mExternalStorageAvailable = true;
    	    mExternalStorageWriteable = false;
    	} else {
    	    // Something else is wrong. It may be one of many other states, but all we need
    	    //  to know is we can neither read nor write
    	    mExternalStorageAvailable = mExternalStorageWriteable = false;
    	}
    }
*/
     /**
     * Back button
     */
    OnClickListener mBackListener = new OnClickListener() {
        public void onClick(View v) {
            //finish();
        	LOG.D("mBackListener", "Go back to LoudBook main activity.");

    		startActivity(new Intent(MainTextOutputActivity.this, LoudBook.class));
    		finish();
        }
    };
    
    /**
     * Previous page button
     */
    OnClickListener mPreviousPageListener = new OnClickListener() {
        public void onClick(View v) {
            //finish();
        	LOG.D("mPreviousPageListener", "Go to previous page #" + (mPageNumber - 1));
        	mNextPageButton.setEnabled(true);
    		if(mParcedData.containsKey(mPageNumber - 1)) {
    			mPageNumber--;
    			mMainTextView.setText(mParcedData.get(mPageNumber).text);
    			playAudio(mParcedData.get(mPageNumber).audioFile);
                //LOG.I("mPreviousPageListener()", "Text: " + mParcedData.get(mPageNumber).text);
        		if(!mParcedData.containsKey(mPageNumber - 1)) {
        			mPreviousPageButton.setEnabled(false);
        		}
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
        	mPreviousPageButton.setEnabled(true);
    		if(mParcedData.containsKey(mPageNumber + 1)) {
    			mPageNumber++;
    			mMainTextView.setText(mParcedData.get(mPageNumber).text);
    			playAudio(mParcedData.get(mPageNumber).audioFile);
                //LOG.I("mNextPageListener()", "Text: " + mParcedData.get(mPageNumber).text);
        		if(!mParcedData.containsKey(mPageNumber + 1)) {
        			mNextPageButton.setEnabled(false);
        		}
    		} else {
                LOG.I("mNextPageListener()", "Last page! Size = " + mParcedData.size() + ", mPageNumber = " + mPageNumber);
    		}
        }
    };
}
