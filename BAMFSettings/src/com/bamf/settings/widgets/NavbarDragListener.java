package com.bamf.settings.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.bamf.settings.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

public class NavbarDragListener implements OnDragListener {
	
	
	private Drawable enterShape;  
	private Drawable normalShape; 
	private FrameLayout mOldContainer;	
	private View mParent;
	private int badCount = 0;
	private Context mContext;

	public NavbarDragListener(Context c, View parent) {		
		
		mContext = c;
		mParent = parent;
		enterShape = c.getResources().getDrawable(
				R.drawable.shape_droptarget);
		normalShape = null;
	}

	@Override
	public boolean onDrag(View v, DragEvent event) {
		int action = event.getAction();
		switch (action) {
		case DragEvent.ACTION_DRAG_STARTED:
			mOldContainer = (FrameLayout) ((View) event.getLocalState()).getParent();
			break;
		case DragEvent.ACTION_DRAG_ENTERED:
			if(v.getId() != R.id.avail_container)
				v.setBackground(enterShape);
			break;
		case DragEvent.ACTION_DRAG_EXITED:
			if(v.getId() != R.id.avail_container)
				v.setBackground(normalShape);
			break;
		case DragEvent.ACTION_DROP:
			// Dropped, reassign View to ViewGroup
			View view = (View) event.getLocalState();
			String tag = (String) view.getTag();			
			ArrayList<String> keys = null;
			int index = 0;
			switch(v.getId()){
				case R.id.avail_container:
					if((tag.equals("home") || tag.equals("back")) && badCount < 2){	
						((ImageView) view).setImageDrawable(((NavbarDragView) 
								((View) v.getParent()).findViewById(R.id.current_container)).getDrawableForKey(v,tag,false));						
						badCount++;
						return true;
					}else if(tag.equals("home") || tag.equals("back")){
						keys = stringToList(((NavbarDragView) 
								((View) v.getParent()).findViewById(R.id.current_container)).getKeyTags().split(" "));
						updateAvailableKeys(listToString(keys),true);	
						view.setVisibility(View.VISIBLE);
						Toast.makeText(mContext, "You kinda need those keys. Knock it off!", Toast.LENGTH_SHORT).show();
						badCount =0;
						return true;
					}
					keys = stringToList(((NavbarDragView) 
						((View) v.getParent()).findViewById(R.id.current_container)).getKeyTags().split(" "));					
					if((mOldContainer.getParent() != v) && (view.getTag() == null || keys.contains(view.getTag())))						
						keys.remove(keys.indexOf(view.getTag()));
					break;
				case R.id.current_container:
					keys = stringToList(((NavbarDragView)v).getKeyTags().split(" "));
					index = ((NavbarDragView) v).getNextRight(event.getX())-1;
					if((view.getTag() == null || keys.contains(view.getTag()))){						
						keys.remove(keys.indexOf(view.getTag()));
					}					
					int count = keys.size();
					if(count > 0 && index < count-1)
						keys.add(index > -1 ? index : count, tag);
					else if(count != 0)
						keys.add(count-1,tag);
					else 
						keys.add(0,tag);					
					break;
			}
			
			updateAvailableKeys(listToString(keys),false);	
			view.setVisibility(View.VISIBLE);
			break;
		case DragEvent.ACTION_DRAG_ENDED:
			
			view = (View) event.getLocalState();
			view.setVisibility(View.VISIBLE);
			if(v.getId() != R.id.avail_container)
				v.setBackground(normalShape);
		default:
			break;
		}
		return true;
	}

	private String[] listToString(List<String> keys) {	

		String[] n = new String[keys.size()];
		for(int i = 0;i < keys.size();i++){			
			n[i] = keys.get(i);
		}		
		return n;
	}
	
	private ArrayList<String> stringToList(String[] keys) {
		ArrayList<String> n = new ArrayList<String>();
		for(int i = 0;i < keys.length;i++){
			n.add(keys[i]);
		}
		return n;
	}

	private void updateAvailableKeys(String[] keys,boolean egg) {
		
		final NavbarDragView current = (NavbarDragView) mParent.findViewById(R.id.current_container);
		current.setupViews(keys, false,null,egg);
		final NavbarDragView avail = (NavbarDragView) mParent.findViewById(R.id.avail_container);
		String[] availKeys = current.getAvailKeys(Arrays.asList(keys));
		avail.setupViews(availKeys, false,null,egg);
		
	}
	
}