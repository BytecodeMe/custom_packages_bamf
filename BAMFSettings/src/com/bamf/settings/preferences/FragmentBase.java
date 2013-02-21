package com.bamf.settings.preferences;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentBase extends Fragment {

	private int mLayoutId;
	private View mView = null;
	private boolean mInvert = false;

	public FragmentBase(int layoutId) {
		mLayoutId = layoutId;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mView == null) {

			mView = inflater.inflate(mLayoutId, null);
		}

		return mView;
	}

	public int getLayoutId() {
		return mLayoutId;
	}

}
