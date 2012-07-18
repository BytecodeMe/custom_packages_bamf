package com.bamf.settings.preferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.bamf.settings.R;
import com.bamf.settings.activities.SettingsActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class VoltageBootFragment extends PreferenceFragment implements OnPreferenceChangeListener {
	
	public static final String PREF_APPLY_ON_BOOT = "pref_voltage_AOB";
	
	private CheckBoxPreference mPrefAOB;
	
	private Activity mSettings;
	private SharedPreferences mPrefs;
	private Editor mEdit;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.voltage_on_boot);       
       
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        mSettings = getActivity();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mSettings);
    	mEdit = mPrefs.edit();
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		
		mEdit.putBoolean(PREF_APPLY_ON_BOOT, (Boolean) newValue);
		mEdit.commit();
		return false;
	}   
    
    
}
