package com.bamf.settings.preferences.performance;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.bamf.settings.R;
import com.bamf.settings.activities.BaseSettingsActivity;
import com.bamf.settings.activities.SettingsActivity;
import com.bamf.settings.adapters.VoltageAdapter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PerformanceVoltageFragment extends ListFragment implements OnClickListener,OnSeekBarChangeListener {

	private static final String TAG = "VoltageFragment";
	
	private static final int MSG_VOLTAGE = 100;	
	
	public static final String VOLTAGE_TABLE = "/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table";
	public static final String VOLTAGE_TABLE1 = "/sys/devices/system/cpu/cpu1/cpufreq/UV_mV_table";	
	
	public static final String PREF_VOLTAGE_TABLE_KEY = "pref_voltage_table";	
	
	//These are the stock frequencies (and voltages) from the stock kernel source to be used as a guideline for safe undervolting.
	public static String[] STOCK_FREQUENCIES = {"192 MHz","350 MHz","700 MHz","920 MHz","1200 MHz","1350 MHz","1420 MHz","1536 MHz"};
	public static String[] STOCK_VOLTAGES = {"1025 mV","1025 mV","1200 mV","1325 mV","1375 mV","1375 mV","1375 mV","1375 mV"};	
	
	/*
	 * These values may need adjusted in the future based on user feedback.  
	 * Currently, we allow a span of -300 --> +100 from stock.  This should hopefully prevent bootlooping and hardware damage.
	 * We shall see. :)
	 */
	public static final int MAX_SPAN = 17;
	public static final int CENTER_INDEX = 12;
	
	private ArrayList<HashMap<String,String>> mVoltages;
	private HashMap<String,String> mVoltage;
	
	private BaseSettingsActivity mSettings;	
	private TextView mCurrentVolt;	
	private String[] mCurrentSpan;
	private VoltageAdapter mAdapter;
	private View mSettingsLayout;
	private View mViewLayout;	
	public static SeekBar mSeekBar;
	private SharedPreferences mPrefs;
	private Editor mEdit;
	private String mVoltageToWrite;
	
	ProgressDialog mProgress;
	
	private Handler mHandler = new RootHandler();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	this.setRetainInstance(true);
    	
    	Log.d(TAG,"Voltage onCreate()");
    	mSettings = (BaseSettingsActivity) getActivity();
    	mAdapter = new VoltageAdapter(mSettings);      	
    	
    	mVoltages = getVoltageLevels();
    	mAdapter.setData(mVoltages);    	
        
    	mPrefs = PreferenceManager.getDefaultSharedPreferences(mSettings);
    	mEdit = mPrefs.edit();
    	
    	setListAdapter(mAdapter);
    	
    	this.setHasOptionsMenu(true);
    	    	
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setPadding(30, 0, 30, 0);
	}	
	
	public ArrayList<HashMap<String,String>> getVoltageLevels() {
		
		/*
		 * We're using hashmaps as it's an easy way to store multiple values for a single voltage.  
		 * The arraylist just holds all of the voltages for us.
		 */
		
        final ArrayList<HashMap<String,String>> volts = new ArrayList<HashMap<String,String>>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(VOLTAGE_TABLE), 256);
            String line = "";
            while ((line = br.readLine()) != null) {
            	
                final String[] values = line.split("\\s+");
                if (values != null) {
                    if (values.length >= 2) {
                        final String freq = values[0].replace("mhz:", " MHz");
                        final String currentMv = values[1] + " mV";                        
                        final HashMap<String,String> voltage = new HashMap<String,String>();
                        voltage.put("freq", freq);
                        voltage.put("currentMv",currentMv);
                        volts.add(voltage);
                    }
                }
            }
            br.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, VOLTAGE_TABLE + " does not exist");
        } catch (IOException e) {
            Log.d(TAG, "Error reading " + VOLTAGE_TABLE);
        }
        return volts;
    }

	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        
		if(mViewLayout == null || mViewLayout.getAlpha() != 0f){		
			mSettingsLayout = v.findViewById(R.id.setting_layout);		
			mViewLayout = v.findViewById(R.id.view_layout);		
			if(mSettingsLayout.getAlpha() == 0f){
				mVoltage = mVoltages.get(position);
				getValuesForVoltage(getNearestVoltage(mVoltage.get("freq")));
				mSeekBar = (SeekBar) mSettingsLayout.findViewById(R.id.voltage_seek);
				makeSettingsActive();				
				animateTransition(mViewLayout,mSettingsLayout);
			}
		}		
        
    }

	/**
	 * We have to toggle the focusability and visibility of the settings controls otherwise 
	 * they can't be interacted with when displayed and can't be clicked in the listview.
	 */
	
	private void makeSettingsActive() {				
		
		mSeekBar.setVisibility(View.VISIBLE);
		mSeekBar.setFocusable(true);
		mSeekBar.setMax(MAX_SPAN-1);
		mSeekBar.setProgress(getNearestValue(mVoltage.get("currentMv")));
		mSeekBar.setOnSeekBarChangeListener(this);
		
		View cancel = mSettingsLayout.findViewById(R.id.cancel_button);
		cancel.setVisibility(View.VISIBLE);
		cancel.setOnClickListener(this);
		
		View set = mSettingsLayout.findViewById(R.id.set_button);
		set.setVisibility(View.VISIBLE);
		set.setOnClickListener(this);
		
		mCurrentVolt = (TextView) mSettingsLayout.findViewById(R.id.voltage_selected);
		mCurrentVolt.setText(mCurrentSpan[mSeekBar.getProgress()]);
		
	}
	
	private void makeSettingsInactive() {		
		
		mSeekBar.setVisibility(View.GONE);
		mSeekBar.setFocusable(false);
		mSeekBar.setOnSeekBarChangeListener(null);
		
		View cancel = mSettingsLayout.findViewById(R.id.cancel_button);
		cancel.setFocusable(false);	
		cancel.setVisibility(View.INVISIBLE);
		cancel.setOnClickListener(null);
		
		View set = mSettingsLayout.findViewById(R.id.set_button);
		set.setFocusable(false);
		set.setVisibility(View.INVISIBLE);
		set.setOnClickListener(null);		
	}

	private void animateTransition(final View hiding, final View showing) {
		
		final int duration = 175;

        // We post a runnable here because there is a delay while the first page is loading and
        // the feedback from having changed the tab almost feels better than having it stick
        Runnable r = new Runnable() {
            public void run() {          

                // Animate the transition
                ObjectAnimator outAnim = ObjectAnimator.ofFloat(hiding, "alpha", 1f,0f);
                outAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {  
                    	View seek = hiding.findViewById(R.id.voltage_seek);
                    	if(seek != null){
                    		makeSettingsInactive();
                    	}
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        
                    }
                });
                ObjectAnimator inAnim = ObjectAnimator.ofFloat(showing, "alpha", 0f,1f);
                inAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {                    	
                    	
                    }
                });
                AnimatorSet animSet = new AnimatorSet();
                animSet.playTogether(outAnim, inAnim);
                animSet.setDuration(duration * 2);
                animSet.start();
            }
        };
		r.run();
	}
	
	private void getValuesForVoltage(String voltage) {
		
		//Build our range of available voltages based on the limits set out above.
		int baseline = Integer.parseInt(voltage.replace(" mV", ""));
		int dec = baseline - 25;
		int inc = baseline;
		mCurrentSpan = new String[MAX_SPAN];
		for(int i = CENTER_INDEX-1;i >= 0;i--){
			mCurrentSpan[i] = String.valueOf(dec) + " mV";			
			dec = dec - 25;
		}
		for(int j = CENTER_INDEX;j < mCurrentSpan.length;j++){
			mCurrentSpan[j] = String.valueOf(inc) + " mV";			
			inc = inc + 25;
		}		
	}
	
	private int getNearestValue(final String value) {
		
		//Figure out where we on the seekbar.
        int index = 0;
        for (int i = 0; i < mCurrentSpan.length; i++) {
        	int temp = Integer.parseInt(value.replace(" mV",""));
            if (temp > Integer.parseInt(mCurrentSpan[i].replace(" mV",""))) {
                index++;
            } else {
                break;
            }
        }
        return index;
    }
	
	public static String getNearestVoltage(String freq) {
		
		/*
		 * We use this if there are extra voltage steps not normally in the kernel.  
		 * Although useless, people feel the need to add them anyway, so we have to find a voltage level 
		 * that is close to stock based on the available frequencies.
		 */
		
		int index = STOCK_VOLTAGES.length -1;
		int key = Integer.parseInt(freq.replace(" MHz", ""));
		for(int i = 0;i< STOCK_VOLTAGES.length;i++){
			if(STOCK_FREQUENCIES[i].equalsIgnoreCase(freq)){				
				return STOCK_VOLTAGES[i];
			}else{				
				int[] intFreq = new int[STOCK_FREQUENCIES.length];
				for(int j = 0;j<intFreq.length;j++){
					intFreq[j] = Integer.parseInt(STOCK_FREQUENCIES[j].replace(" MHz", ""));
				}
				for(int k = 0;k<intFreq.length;k++){
					if(key > intFreq[k]){
						index = k;						
					}
				}				
			}
		}		
		return STOCK_VOLTAGES[index];
	}

	public void onClick(View v) {
		
		switch(v.getId()){
			case R.id.set_button:
				applyVoltage();					
				break;
			default:
				break;
		}
		animateTransition(mSettingsLayout,mViewLayout);
		
	}

	private void applyVoltage() {
		
		int newVolt = mSeekBar.getProgress();
		HashMap<String, String> tempVoltage = new HashMap<String,String>();
		tempVoltage.put("currentMv", mCurrentSpan[newVolt]);		
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0;i < mVoltages.size();i++){			
			if(mVoltages.get(i).equals(mVoltage)){				
				sb.append(tempVoltage.get("currentMv"));
			}else {
				sb.append(mVoltages.get(i).get("currentMv"));
			}
			sb.append(" ");			
		}
		mVoltageToWrite = sb.toString();
		Log.d(TAG, mVoltageToWrite);
		mHandler.sendEmptyMessage(MSG_VOLTAGE);		

		mEdit.putString(PREF_VOLTAGE_TABLE_KEY, mVoltageToWrite);
		mEdit.commit();	
		forceWait(1500);
	}		

	public void onProgressChanged(SeekBar seek, int progress, boolean fromUser) {
		
		mCurrentVolt.setText(mCurrentSpan[progress]);
	}

	public void onStartTrackingTouch(SeekBar arg0) {		
	}

	public void onStopTrackingTouch(SeekBar arg0) {		
	}
	
	public class RootHandler extends Handler {
	    
	    @Override
	    public void handleMessage(Message msg){            
            switch(msg.what){
                case MSG_VOLTAGE:
                    try {
                        //need to check the result in case it was denied root access
                        SettingsActivity.getRootService().setVoltage(mVoltageToWrite);                        
                    } catch (RemoteException e) {
                        e.printStackTrace();                        
                    }
                    break;                 
            }
	    }
	}
	
	private void forceWait(final int sleepTime) {
		
		mProgress = new ProgressDialog(mSettings);
        mProgress.setCancelable(false);
		mProgress.setMessage("Please wait...");
		mProgress.show();
		
		final Handler handler = new Handler();
		final Runnable finished = new Runnable() {
		    public void run() {			    	
		    	mVoltages.clear();
				mVoltages = getVoltageLevels();
				mAdapter = new VoltageAdapter(mSettings);
				mAdapter.setData(mVoltages);
				setListAdapter(mAdapter);
		    	mProgress.dismiss();
		    }
		};
		
		new Thread() {		    
			@Override public void run() {					
				try {
					sleep(sleepTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
				handler.post(finished);					
			}
		}.start();        
		
	}

	
}
