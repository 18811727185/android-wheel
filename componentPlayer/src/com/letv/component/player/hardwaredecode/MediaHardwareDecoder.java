package com.letv.component.player.hardwaredecode;

import java.io.IOException;

import android.view.Surface;

import com.media.ffmpeg.FFMpegPlayer;
import com.media.ffmpeg.MediaDecoder;

public class MediaHardwareDecoder implements MediaDecoder{
	
	private CodecWrapper mDecoder;

	public MediaHardwareDecoder(){
		mDecoder = new CodecWrapper();
	}
	
	@Override
	public void setPlayer(FFMpegPlayer player) {
		mDecoder.setPlayer(player);
	}

	@Override
	public int createDecoder(int videoWidth, int videoHeight, Surface surface) throws IOException {
		return mDecoder.createDecoder(videoWidth, videoHeight, surface);
	}

	@Override
	public void stopCodec() {
		mDecoder.stopCodec();
		
	}

	@Override
	public int flushCodec() {
		return mDecoder.flushCodec();
	}

	@Override
	public int fillInputBuffer(byte[] data, long pts, int flush) {
		return mDecoder.fillInputBuffer(data, pts, flush);
	}

	@Override
	public int getCapbility() {
		return mDecoder.getCapbility();
	}

}
