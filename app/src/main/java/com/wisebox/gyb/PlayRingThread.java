package com.wisebox.gyb;

import android.content.Context;
import android.media.MediaPlayer;

public class PlayRingThread extends Thread {
	MediaPlayer mMediaPlayer;
	Context mContext;
	public String strType = "";
	public boolean bLooping = true;

	public PlayRingThread(Context mContext) {
		mMediaPlayer = new MediaPlayer();
		this.mContext = mContext;
	}

	@Override
	public void run() {
		try {
			if (strType.equals("new")) {
				mMediaPlayer = MediaPlayer.create(mContext, R.raw.neatask);
				mMediaPlayer.setLooping(bLooping);
			} else if (strType.equals("emergency")) {
				mMediaPlayer = MediaPlayer.create(mContext, R.raw.etask);
				mMediaPlayer.setLooping(bLooping);
			} else if (strType.equals("change"))
				mMediaPlayer = MediaPlayer.create(mContext, R.raw.taskchange);
			else if (strType.equals("start"))
				mMediaPlayer = MediaPlayer.create(mContext, R.raw.taskstarted);
			else if (strType.equals("finish"))
				mMediaPlayer = MediaPlayer.create(mContext, R.raw.taskfinished);
			else if (strType.equals("select"))
				mMediaPlayer = MediaPlayer.create(mContext, R.raw.selecttask);
			else if (strType.equals("dakatishi"))
				mMediaPlayer = MediaPlayer.create(mContext, R.raw.dakatishi);
			else if (strType.equals("needfromlocation"))
				mMediaPlayer = MediaPlayer.create(mContext,
						R.raw.needfromlocation);
			else if (strType.equals("nothisbarcode"))
				mMediaPlayer = MediaPlayer
						.create(mContext, R.raw.nothisbarcode);
			else if (strType.equals("samebarcode"))
				mMediaPlayer = MediaPlayer.create(mContext, R.raw.samebarcode);
			else if(strType.equals("update"))
				mMediaPlayer = MediaPlayer
				.create(mContext, R.raw.yigengxin);
			else
				mMediaPlayer = MediaPlayer.create(mContext, R.raw.neatask);

			mMediaPlayer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void StopAlarmRing() {
		mMediaPlayer.stop();
	}
}
