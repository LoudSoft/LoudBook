package com.loudsoft.loudbook;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {

	private Preference mSearchFolderPreference;
    private final int REQUEST_CHOOSE_SEARCH_DIR = 3;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preferences);
	    
	    mSearchFolderPreference = findPreference(getString(R.string.search_dir_pref_key));
	    mSearchFolderPreference.setOnPreferenceClickListener(onSearchFolderPreferenceClick);
	}

	public OnPreferenceClickListener onSearchFolderPreferenceClick = new OnPreferenceClickListener() {

		public boolean onPreferenceClick(Preference pref) {
	        Intent intent = new Intent(Preferences.this, FileDialog.class);
	        intent.putExtra(FileDialog.START_PATH, "/");
	        intent.putExtra(FileDialog.DIR_OR_FILE, FileDialog.SEARCH_DIR);
	        intent.putExtra(FileDialog.FILE_EXTENSION, Preferences.this.getString(R.string.audio_file_extension));
	    	startActivityForResult(intent, REQUEST_CHOOSE_SEARCH_DIR );
	        return false;
	    }

	};
}
