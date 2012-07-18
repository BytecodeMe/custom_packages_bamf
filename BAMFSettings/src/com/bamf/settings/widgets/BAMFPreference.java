package com.bamf.settings.widgets;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.preference.Preference;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Toast;

public class BAMFPreference extends Preference implements OnLongClickListener {
	
	private Context mContext;
	private ArrayList<String> sounds;
	private Random generator;
	
	public BAMFPreference(Context context, AttributeSet attrib) {
		super(context, attrib);
		// TODO Auto-generated constructor stub
		mContext = context;
		generator = new Random();
		
		sounds = new ArrayList<String>();
		sounds.add("http://www.anonymomma.com/lol.wav");
		sounds.add("http://www.fionasplace.net/creepysounds/LAUGH8.WAV");
		sounds.add("http://rna.chem.rochester.edu/turner.wav");
		sounds.add("http://www.thepocket.com/wavs/laughcrowd3.wav");
		sounds.add("http://www.earlyisd.netxv.net/tutorials/Audacity/audio/muhahaha.wav");
	}
	
    private void eggEnable(boolean enable){
    	Settings.System.putInt(mContext.getContentResolver(), Settings.System.NAVBAR_EASTER_EGG, enable?1:0);
	}
    
    private boolean eggEnabled(){
        return Settings.System.getInt(mContext.getContentResolver(), Settings.System.NAVBAR_EASTER_EGG, 0)==1;
    }

	@Override
	public boolean onLongClick(View view) {

		 int random_sound = generator.nextInt(sounds.size());
		 final String alert = sounds.get(random_sound);
		 final MediaPlayer mMediaPlayer = new MediaPlayer();

		 if(!eggEnabled()){
			 Toast.makeText(mContext, "Oh no! Rainbows??!!", 3000).show();
			 eggEnable(true);
		 }else{
			 Toast.makeText(mContext, "It's ok to be boring.", 3000).show();
			 eggEnable(false);
		 }
		 
		 new Thread(new Runnable(){
			 @Override
				public void run() {
					 try{
						 mMediaPlayer.setDataSource(alert);
						 final AudioManager audioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
						 if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) != 0) {
							 mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
							 mMediaPlayer.setLooping(false);
							 mMediaPlayer.prepare();
							 mMediaPlayer.start();
						  }
					 }catch(Exception e){
						 //ignore
						 e.printStackTrace();
					 }
			
			}
		 }).start();
		 
		return true;
	}
}