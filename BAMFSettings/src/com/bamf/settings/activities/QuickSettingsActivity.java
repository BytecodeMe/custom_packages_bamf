package com.bamf.settings.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.bamf.settings.R;
import com.bamf.settings.preferences.QuickSettingsFragment;
import com.bamf.settings.preferences.QuickSettingsOrderFragment;
import com.bamf.settings.utils.QuickSettingsUtil;

import java.io.FileOutputStream;

public class QuickSettingsActivity extends Activity {
    
    public final FragmentManager mManager = getFragmentManager();
    private MenuItem mMenuButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.quick_settings_container);
        QuickSettingsUtil.CustomIconUtil.setContext(this);

        if (savedInstanceState == null) {
            
            mManager.beginTransaction()
                    .add(R.id.container, Fragment.instantiate(this, QuickSettingsFragment.class.getName(),
                            getIntent().getExtras()), "basic")
                    .commit();
            mManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                
                @Override
                public void onBackStackChanged() {
                    if(mManager.findFragmentByTag("basic").isVisible()){
                        setupActionBar(R.string.quick_settings_title, true);
                    }else{
                        setupActionBar(R.string.quick_settings_order_title, false);
                    }
                }
            });          
        }
        setupActionBar(R.string.quick_settings_title, true);
    }
        
    private void setupActionBar(int title, boolean iconVisible) {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setTitle(title);
        //if(mMenuButton != null){
        //    mMenuButton.setVisible(iconVisible);
        //}
    }
    
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_actionbar, menu);
        mMenuButton = menu.findItem(R.id.setup);
        return true;
    }*/
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked;
                if(mManager.getBackStackEntryCount() > 0){
                    mManager.popBackStack();
                }else{
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) { 

        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                //most likely, the icon is in here
                final Bundle extras = data.getExtras();
                Bitmap b = null;
                
                if (extras!= null) {
                    /* return from a cropped image */
                    if(extras.containsKey("data")) {
                        b = (Bitmap)extras.get("data");
                    /* returns from an icon pack */
                    }else if(extras.containsKey("icon")){
                        b = (Bitmap)extras.get("icon");
                    }
                    if(b == null){
                        /* returns from the gallery */
                        b = QuickSettingsUtil.CustomIconUtil.readBitmap(this, data.getData()); 
                    }
                }else{
                    b = QuickSettingsUtil.CustomIconUtil.readBitmap(this, data.getData());
                }
                
                //if we found an icon, write it to a file
                if(b != null){
                    FileOutputStream outStream;
                    try {
                        outStream = new FileOutputStream(QuickSettingsUtil.CustomIconUtil.getTempFile());
                        b.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                        outStream.flush();
                        outStream.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
   
