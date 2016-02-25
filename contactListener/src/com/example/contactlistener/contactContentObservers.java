package com.example.contactlistener;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;

public class contactContentObservers extends ContentObserver {

	private static String TAG="ContentObserver";
	private  int CONTACT_CHANGE = 1; 
	private Context mContext;
	private Handler mHandler;
	
	public contactContentObservers(Context context, Handler handler) {
		super(handler);
		mContext=context;
		mHandler=handler;
		// TODO 自动生成的构造函数存根
	}
	@Override 
	public void onChange(boolean selfChange)
	{
		Log.v(TAG,"the contacts has changed");
	//	mHandler.obtainMessage()
		mHandler.obtainMessage(CONTACT_CHANGE,"gaibian").sendToTarget();
	}

}
