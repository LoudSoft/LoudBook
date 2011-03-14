package com.loudsoft.loudbook;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
	private ListView mRecentUsedFiles;
	private File mFile = null;
	private ArrayList<fileItem> mRecentUsedFileNames = null;
	private ArrayList<HashMap<String, Object>> mList;
	public static final String EXTRA_FILE_NAME = "com.loudsoft.loudbook.extra_file_name";
	private static final String RESENT_USED_BASE_FILE_NAME_KEY = "com.loudsoft.loudbook.recent_used_base_file_name_key";
	private static final String RESENT_USED_DIR_NAME_KEY = "com.loudsoft.loudbook.recent_used_dir_name_key";
	private static final int REQUEST_SELECT_LOUD_BOOK_FILE = 1;
	private static final String PREFS_KEY = "com.loudsoft.loudbook.prefense_key";
    private static final int MAX_COUNT = 10;
	
    private class fileItem {
    	public String dir = null;
    	public String file = null;
    	
    	public fileItem(String newDir, String newFile) {
    		dir = newDir;
    		file = newFile;
    	}
    }
    
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
        mRecentUsedFiles = (ListView) findViewById(R.id.recent_used_files_list);

        mList = new ArrayList<HashMap<String, Object>>();
        SimpleAdapter fileList = new SimpleAdapter(this, mList,
				R.layout.file_dialog_row,
				new String[] { FileDialog.ITEM_KEY, FileDialog.ITEM_IMAGE }, new int[] {
						R.id.fdrowtext, R.id.fdrowimage });
        
        mRecentUsedFiles.setAdapter(fileList);

        SharedPreferences settings = getSharedPreferences(PREFS_KEY, 0);
        
        mRecentUsedFileNames = new ArrayList<fileItem>();
        String fileName = "";
        String dirName = "";
        for(int i = 0; i < MAX_COUNT; i++) {
        	fileName = settings.getString(RESENT_USED_BASE_FILE_NAME_KEY + "_" + i, "");
        	dirName = settings.getString(RESENT_USED_DIR_NAME_KEY + "_" + i, "");
        	if(!fileName.isEmpty() && !dirName.isEmpty()) {
        		mRecentUsedFileNames.add(new fileItem(dirName, fileName));
        		addItem(fileName, R.drawable.file);
        	}
        }
        if(!mRecentUsedFileNames.isEmpty()) {
        	mSearchFolderName.setText(mRecentUsedFileNames.get(0).dir);
        	mBookConfigFileName.setText(mRecentUsedFileNames.get(0).file);
        }

        fileList.notifyDataSetChanged();
        
        mRecentUsedFiles.setOnItemClickListener(new OnItemClickListener() {
        	@Override
        	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        		setDirAndFile(mRecentUsedFileNames.get(position).dir, mRecentUsedFileNames.get(position).file);
        	}
        });
        	
        	checkExternalStorage();
        
    	if(mExternalStorageAvailable) {
    		File sdDir = Environment.getExternalStorageDirectory();
    		mSearchFolderName.setText(sdDir.getPath() + "/loudsoft/loudbook/");
            LOG.I("onCreate()","mSearchFolderName = " + mSearchFolderName.getText().toString());
    	}
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
    
	// This method is called once the menu is selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.preferences:
			// Launch Preference activity
			Intent i = new Intent(LoudBook.this, Preferences.class);
			startActivity(i);
			break;
		}
		return true;
	}
	
	private void addItem(String tmpFileName, int imageId) {
		LOG.I("addItem()","fileName = " + tmpFileName);
		HashMap<String, Object> item = new HashMap<String, Object>();
		item.put(FileDialog.ITEM_KEY, tmpFileName);
		item.put(FileDialog.ITEM_IMAGE, imageId);
		mList.add(item);
	}
	
	private void setDirAndFile(String dir, String file) {
		mSearchFolderName.setText(dir);
		mBookConfigFileName.setText(file);
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
        		
        		SharedPreferences settings = getSharedPreferences(PREFS_KEY, 0);
        		SharedPreferences.Editor editor = settings.edit();
        		editor.putString(RESENT_USED_BASE_FILE_NAME_KEY + "_" + 0, mFile.getName());
        		editor.putString(RESENT_USED_DIR_NAME_KEY + "_" + 0, mFile.getParent());
        		LOG.I("mOpenFileListener", "Put file # 0: dir:" + mFile.getParent() + " file:" + mFile.getName());
        		int z = 1;
                for(int i = 0; i < mRecentUsedFileNames.size(); i++) {
                	if(mFile.getName().compareTo(mRecentUsedFileNames.get(i).file) != 0 
                			|| mFile.getParent().compareTo(mRecentUsedFileNames.get(i).dir) != 0) 
                	{
                		editor.putString(RESENT_USED_BASE_FILE_NAME_KEY + "_" + z, mRecentUsedFileNames.get(i).file);
                		editor.putString(RESENT_USED_DIR_NAME_KEY + "_" + z, mRecentUsedFileNames.get(i).dir);
                		LOG.I("mOpenFileListener", "Put file # " + z + ": dir=" + mRecentUsedFileNames.get(i).dir + " file=" + mRecentUsedFileNames.get(i).file);
                		z++;
                	}
                }
                for(int i = z; i < mRecentUsedFileNames.size(); i++) {
                	editor.putString(RESENT_USED_BASE_FILE_NAME_KEY + "_" + i, "");
                	editor.putString(RESENT_USED_DIR_NAME_KEY + "_" + i, "");
                	LOG.I("mOpenFileListener", "Put file # " + i + ": dir=" + " " + " file=" + " ");
                }
        	    editor.commit();

/*
        		if(deleteFile(mFile)) {
        			Log.i(TAG, "Deleted " + mFile);        		    
        		}
*/
        		finish();
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
                		setDirAndFile(mDirectoryName, mFileBaseName);
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