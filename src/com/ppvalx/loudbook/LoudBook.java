package com.ppvalx.loudbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoudBook extends Activity {

	private static final String TAG = "LoudBook";
	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;
	private Context context;
	private int duration = Toast.LENGTH_SHORT;
	private String mFileBaseName = "";
	private String mDirectoryName = "";
	private EditText mSearchFolderName;
	private EditText mBookConfigFileName;
	private File mFile = null;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

    	context = getApplicationContext();

    	mSearchFolderName = (EditText) findViewById(R.id.search_folder_name);
        mBookConfigFileName = (EditText) findViewById(R.id.book_config_file_name);
        Button mOpenConfigFileButton = (Button) findViewById(R.id.open_config_file_button);
        mOpenConfigFileButton.setOnClickListener(mOpenFileListener );

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

    	if(mExternalStorageAvailable) {
    		File sdDir = Environment.getExternalStorageDirectory();
    		mSearchFolderName.setText(sdDir.getPath() + "/loudsoft/loudbook/");
            Log.i(TAG, "onCreate(): mSearchFolderName = " + mSearchFolderName);
    	}
    }
    
    /**
     * Open config file button
     */
    OnClickListener mOpenFileListener = new OnClickListener() {
        public void onClick(View v) {
            //finish();
        	Log.d(TAG, "onClick()");

        	//mFileBaseName = mBookConfigFileName.getText().toString();
			mFileBaseName = "my_icon.png";
			mDirectoryName = mSearchFolderName.getText().toString();
			
        	if(!mExternalStorageAvailable) {
        		Log.e(TAG, "External storage is not available");        		
        		return;
        	}
        	File directory = new File(mDirectoryName);
        	if(!directory.exists() && !directory.mkdirs()) {
        		Log.e(TAG, "Unable to create new file: dir = " + mDirectoryName + " file = " + mFileBaseName);
        		return;
            }
        	mFile = new File(mDirectoryName, mFileBaseName);
        	if(mFile == null){
        		Log.e(TAG, "Unable to create new file: dir = " + mDirectoryName + " file = " + mFileBaseName);
        		return;
        	}
        	createExternalStoragePrivatePicture(mFile);
        		
        	if(mFile.exists()) {
        		Toast.makeText(context, mFile + " exist!", duration).show();
        		if(deleteFile(mFile)) {
        			Log.i(TAG, "Deleted " + mFile);        		    
        		}
        	} else {
        		Toast.makeText(context, mFile + " does not exist!", duration).show();
        	}
        }
    };
    
    void createExternalStoragePrivatePicture(File file) {
        Log.i(TAG, "createExternalStoragePrivatePicture(): " + file);
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
                    Log.i(TAG, "Scanned " + path + ":");
                    Log.i(TAG, "-> uri=" + uri);
                }
            });
        } catch (IOException e) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.e(TAG, "Error writing " + file, e);
        }
    }

    boolean deleteFile(File file) {
        Log.i(TAG, "deleteFile()" + file);
        boolean res = false;
        // Create a path where we will place our picture in the user's
        // public pictures directory and delete the file.  If external
        // storage is not currently mounted this will fail.
        try {
        	res = file.delete();
        } catch (SecurityException e ) {
            Log.e(TAG, "Unable to delete " + file, e);
            res = false;
        }
		return res;
    }
}