package com.loudsoft.loudbook;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoudBook extends Activity {
	
	private LogPrinter LOG; 
	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;
	private Context context;
	private int duration = Toast.LENGTH_LONG;
	private String mFileBaseName = "";
	private String mDirectoryName = "";
	private EditText mSearchFolderName;
	private EditText mBookConfigFileName;
	private Button mOpenConfigFileButton;
	private Button mBrowseButton;
	private File mFile = null;
	public static final String EXTRA_FILE_NAME = "com.loudsoft.loudbook.extra_file_name";
	private static final int REQUEST_SELECT_LOUD_BOOK_FILE = 1;
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        LOG = new LogPrinter(true, this.getString(R.string.app_name), this.getClass().getSimpleName());
    	context = getApplicationContext();

    	mSearchFolderName = (EditText) findViewById(R.id.search_folder_name);
        mBookConfigFileName = (EditText) findViewById(R.id.book_config_file_name);
        mOpenConfigFileButton = (Button) findViewById(R.id.open_config_file_button);
        mOpenConfigFileButton.setOnClickListener(mOpenFileListener);
        mBrowseButton = (Button) findViewById(R.id.browse_button);
        mBrowseButton.setOnClickListener(mBrowseListener);

        checkExternalStorage();

    	if(mExternalStorageAvailable) {
    		File sdDir = Environment.getExternalStorageDirectory();
    		mSearchFolderName.setText(sdDir.getPath() + "/loudsoft/loudbook/");
            LOG.I("onCreate()","mSearchFolderName = " + mSearchFolderName.getText().toString());
    	}
    }
    
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
    
    /**
     * Browse button
     */
    OnClickListener mBrowseListener = new OnClickListener() {
        public void onClick(View v) {
        	LOG.D("mBrowseListener", "start");

        	mFileBaseName = mBookConfigFileName.getText().toString();
			mDirectoryName = mSearchFolderName.getText().toString();
			checkExternalStorage();
        	if(!mExternalStorageAvailable) {
        		LOG.E("onClick()", "External storage is not available");   
        		//Toast.makeText(context, "SD card is not available!", duration).show();
				new AlertDialog.Builder(context).setIcon(R.drawable.icon)
					.setTitle(getText(R.string.sd_card_isnt_available))
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,	int which) {}
						}).show();
        		return;
        	}
        	/*
        	File directory = new File(mDirectoryName);
        	if(!directory.exists() && !directory.mkdirs()) {
        		LOG.E("onClick()", "Unable to create new file: dir = " + mDirectoryName + " file = " + mFileBaseName);
        		return;
            }
            */
        	Intent intent = new Intent(LoudBook.this, FileDialog.class);
        	intent.putExtra(FileDialog.START_PATH, mDirectoryName);
        	intent.putExtra(FileDialog.FILE_EXTENSION, context.getString(R.string.loud_book_file_extension));
        	intent.putExtra(FileDialog.DIR_OR_FILE, FileDialog.SEARCH_FILE);
			startActivityForResult(intent, REQUEST_SELECT_LOUD_BOOK_FILE);
        }
    };

    /**
     * Open config file button
     */
    OnClickListener mOpenFileListener = new OnClickListener() {
        public void onClick(View v) {
            //finish();
        	LOG.D("mOpenFileListener", "start");

        	mFileBaseName = mBookConfigFileName.getText().toString();
			mDirectoryName = mSearchFolderName.getText().toString();
			
        	if(!mExternalStorageAvailable) {
        		LOG.E("onClick()", "External storage is not available"); 
        		Toast.makeText(context, "SD card is not available!", duration).show();
        		return;
        	}
        	/*
        	File directory = new File(mDirectoryName);
        	if(!directory.exists() && !directory.mkdirs()) {
        		LOG.E("onClick()", "Unable to create new dir: dir = " + mDirectoryName);
        		return;
            }
            */
        	mFile = new File(mDirectoryName, mFileBaseName);
        	if(mFile == null){
        		LOG.E("onClick()", "Unable to create new file: dir = " + mDirectoryName + " file = " + mFileBaseName);
        		return;
        	}
        	//createExternalStoragePrivatePicture(mFile);
        		
        	if(mFile.canRead()) {
        		//Toast.makeText(context, mFile + " exist!", duration).show();
        		
        		Intent myIntent = new Intent();
        		myIntent.setClass(LoudBook.this, MainTextOutputActivity.class);
        		myIntent.putExtra(EXTRA_FILE_NAME, mFile.getAbsolutePath());
        		startActivity(myIntent);
/*
        		if(deleteFile(mFile)) {
        			Log.i(TAG, "Deleted " + mFile);        		    
        		}
*/
        	} else {
        		Toast.makeText(context, "Unable to read file: " + mFile, duration).show();
        	}
        }
    };
    
 // Listen for results.
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        // See which child activity is calling us back.
        switch (requestCode) {
            case REQUEST_SELECT_LOUD_BOOK_FILE:
                // This is the standard resultCode that is sent back if the
                // activity crashed or didn't doesn't supply an explicit result.
                if (resultCode == RESULT_OK){
                	String newFileName = data.getStringExtra(FileDialog.RESULT_PATH); 
                	mFile = new File(newFileName);
                	if(mFile.canRead()) {
                		mDirectoryName = mFile.getParent();
                		mFileBaseName = mFile.getName();
                		mSearchFolderName.setText(mDirectoryName);
                		mBookConfigFileName.setText(mFileBaseName);
                		LOG.I("onActivityResult()", "Path: " + mDirectoryName + " File: " + mFileBaseName);
                	} else {
                    	Toast.makeText(context, "Unable to read file: " + newFileName, duration).show();
                    	LOG.E("onActivityResult()", "Unable to read file: " + newFileName);
                		return;
                	}
                } 
                else {
                	Toast.makeText(context, "Unable to retrive file name", duration).show();
                	LOG.E("onActivityResult()", "Unable to retrive file name");
            		return;
                }
            default:
                break;
        }
    }

/*
    void createExternalStoragePrivatePicture(File file) {

    	LOG.E("createExternalStoragePrivatePicture()", "file: " + file);
        // Create a path where we will place our picture in our own private
        // pictures directory.  Note that we don't really need to place a
        // picture in DIRECTORY_PICTURES, since the media scanner will see
        // all media in these directories; this may be useful with other
        // media types such as DIRECTORY_MUSIC however to help it classify
        // your media for display to the user.

        try {
            // Very simple code to copy a picture from the application's
            // resource into the external file.  Note that this code does
            // no error checking, and assumes the picture is small (does not
            // try to copy it in chunks).  Note that if external storage is
            // not currently mounted this will silently fail.
            InputStream is = getResources().openRawResource(R.drawable.icon);
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            is.read(data);
            os.write(data);
            is.close();
            os.close();

            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(this,
                    new String[] { file.toString() }, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
				@Override
                public void onScanCompleted(String path, Uri uri) {
					LOG.I("createExternalStoragePrivatePicture()", "Scanned " + path + ":");
					LOG.I("createExternalStoragePrivatePicture()", "-> uri=" + uri);
                }
            });
        } catch (IOException e) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
        	LOG.E("createExternalStoragePrivatePicture()", "Error writing " + file, e);
        }
    }
*/
    /*
    boolean deleteFile(File file) {
    	LOG.I("deleteFile()", "file: " + file);
        boolean res = false;
        // Create a path where we will place our picture in the user's
        // public pictures directory and delete the file.  If external
        // storage is not currently mounted this will fail.
        try {
        	res = file.delete();
        } catch (SecurityException e ) {
        	LOG.E("deleteFile()", "Unable to delete " + file, e);
            res = false;
        }
		return res;
    }
    */
}