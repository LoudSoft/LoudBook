package com.loudsoft.loudbook;

import android.util.Log;

public class LogPrinter {
	
	private boolean mDBG = true;	
	private String mTAG = "LoudBook";
	private String mClassName = "";
	
	public LogPrinter(boolean dbg, String tag, String className) {
		mDBG = dbg;
		mClassName = className;
		mTAG = tag;
	}

	void D(String methodName, String msg) {
		if(mDBG)
			Log.d(mTAG, mClassName + ": " + methodName + ": " + msg);
	}

	void I(String methodName, String msg) {
		if(mDBG)
			Log.i(mTAG, mClassName + ": " + methodName + ": " + msg);
	}

	void E(String methodName, String msg) {
		//if(mDBG)
			Log.e(mTAG, mClassName + ": " + methodName + ": " + msg);
	}

	void E(String methodName, String msg, Throwable tr) {
		//if(mDBG)
			Log.e(mTAG, mClassName + ": " + methodName + ": " + msg, tr);
	}

	void V(String methodName, String msg) {
		if(mDBG)
			Log.v(mTAG, mClassName + ": " + methodName + ": " + msg);
	}

	void W(String methodName, String msg) {
		if(mDBG)
			Log.w(mTAG, mClassName + ": " + methodName + ": " + msg);
	}
}
