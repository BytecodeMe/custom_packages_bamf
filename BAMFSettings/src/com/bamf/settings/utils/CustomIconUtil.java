package com.bamf.settings.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.bamf.settings.R;

public class CustomIconUtil {
    public static final String SETTING_ICON = "custom_setting.png";
    private static final String SYSUI = "com.android.systemui";
    
    private static CustomIconUtil INSTANCE;
    
    private Context mContext;
    private Context mSysUIContext;
    private Fragment mFragment;
    
    public static CustomIconUtil getInstance(Context context){
        if(INSTANCE==null){
        	INSTANCE = new CustomIconUtil(context);
        }else{
        	INSTANCE.mContext = context;
        }
        return INSTANCE;
    }
    
    private CustomIconUtil(Context context){
    	mContext = context;
    	mSysUIContext = null;
    	try{
    		mSysUIContext = mContext.createPackageContext(SYSUI, Context.CONTEXT_IGNORE_SECURITY);
    	}catch(Exception e){}
    }
    
    public void setFragment(Fragment fragment){
    	mFragment = fragment;
    }
    
    public void showContextMenu(){
        final int GALLERY = 0;
        final int GALLERY_CROP = 1;
        final int ICON_PACK = 2;
        final int REMOVE = 3;
        
        //items in the context menu list
        final Item[] items = {
            new Item("Gallery", com.android.internal.R.drawable.ic_menu_gallery),
            new Item("Gallery Cropped", com.android.internal.R.drawable.ic_menu_crop),
            new Item("ADW Icon Pack", com.android.internal.R.drawable.ic_menu_archive),
            new Item("Default Icon", com.android.internal.R.drawable.ic_menu_revert),
        };
        
        final ListAdapter adapter = new ArrayAdapter<Item>(
            mContext,
            R.layout.select_icon_source_item,
            R.id.select_photo_which,
            items){
                public View getView(int position, View convertView, ViewGroup parent) {
                    //User super class to create the View
                    View v = super.getView(position, convertView, parent);
                    TextView tv = (TextView)v.findViewById(R.id.select_photo_which);
                    ImageView iv = (ImageView)v.findViewById(R.id.select_photo_icon);
                    tv.setText(items[position].text);
                    iv.setBackgroundResource(items[position].icon);
                    
                    return v;
                }
            };
                
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
            .setAdapter(adapter, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch(which){
                    case GALLERY:
                    	fromGallery(false);
                        break;
                    case GALLERY_CROP:
                    	fromGallery(true);
                        break;
                    case ICON_PACK:
                    	fromIconPack();
                        break;
                    case REMOVE:
                        dialog.cancel();
                        removeCustomIcon();
                }
            }
            
        })
        .setOnCancelListener((OnCancelListener) mFragment)
        .setTitle("Select custom icon");
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void fromGallery(boolean crop){
        
        Intent intent = new Intent();
        intent.setType("image/*");
        
        if(crop){
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 55);
            intent.putExtra("outputY", 55);
            intent.putExtra("scale", false);
        }
        
        intent.putExtra("return-data", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
    
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mFragment.getActivity().startActivityForResult(Intent.createChooser(intent, "Select Icon"),1);
    }
    
    private void fromIconPack(){
        Intent intent = new Intent();
        
        intent.setAction("org.adw.launcher.icons.ACTION_PICK_ICON");
        mFragment.getActivity().startActivityForResult(Intent.createChooser(intent, "Select Icon Pack"),1);
    }
    
    private void removeCustomIcon(){
        File f = new File(mSysUIContext.getDir("bamf", Context.MODE_WORLD_WRITEABLE),SETTING_ICON);
        if(f.exists()){
            f.delete();
        }
    }
    
    public synchronized File getTempFile() {
        //File f = new File(getFilesDir(),RIGHT_BUTTON_ICON);
        File f = null;
        try{
            f = new File(mSysUIContext.getDir("bamf", Context.MODE_WORLD_WRITEABLE),SETTING_ICON);
            f.setReadable(true, false);
            
        }catch(Exception e){
            e.printStackTrace();
        }
        return f;
    }
    
    public Drawable loadFromFile(){
        File f = getTempFile();
        Drawable temp = null;
        if(f.exists()){
            Bitmap b = readBitmap(Uri.fromFile(f));
            if(b != null){
                try{
                    temp = new BitmapDrawable(mSysUIContext.getResources(), b);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        
        return temp;
    }
    
    public Bitmap readBitmap(Uri selectedImage) {
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inSampleSize = 2; //reduce quality 
        AssetFileDescriptor fileDescriptor =null;
        try {
            fileDescriptor = mContext.getContentResolver().openAssetFileDescriptor(selectedImage,"r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        finally{
            try {
                bm = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);
                fileDescriptor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bm;
    }
    
    public static class Item{
        public final String text;
        public final int icon;
        public Item(String text, Integer icon) {
            this.text = text;
            this.icon = icon;
        }
        @Override
        public String toString() {
            return text;
        }
    }
    
}
