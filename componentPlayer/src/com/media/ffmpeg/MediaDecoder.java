package com.media.ffmpeg;

import java.io.IOException;

import android.view.Surface;

public interface MediaDecoder {
	
	public static final String VERSION = "20140428";
	
	public static final int NOT_SUPPORT_TS = -1;
	
	public static final int TS_300KTS = 4;
	
	public static final int TS_600KTS = 8;
	
	public static final int TS_1_1_MTS = 16;
	
	public static final int TS_180_KTS = 128;
	
	public void setPlayer( FFMpegPlayer player );

	public int createDecoder( int videoWidth, int videoHeight, Surface surface ) throws IOException;
	
	public void stopCodec();
	
	public int flushCodec( );

	public int fillInputBuffer( byte[] data, long pts, int flush ) ;
	
	public int getCapbility();
}
