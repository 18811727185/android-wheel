package com.letv.component.player.hardwaredecode;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.media.ffmpeg.FFMpegPlayer;
import com.media.ffmpeg.MediaDecoder;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecInfo.CodecProfileLevel;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

public class CodecWrapper {

	private static final int RENDER_SUCCESS = 0; // 渲染成功
	private static final int INPUTBUFFER_FULL = -1; // inputbuffer满，没有可用的
	private static final int IO_EXCEPTION = -2; // io exception
	private static final int DATAT_ERROR = -3; // 数据为空，数据错误
	private static final int OUTPUT_BUFFERS_CHANGED = 1; // 重新初始化output buffer队列
	private static final int OUTPUT_FORMAT_CHANGED = 2; // 重置分辨率。
	private static final int DECODER_TIMEOUT = 3; // 解码超时，丢掉outputbuffer中的待渲染帧
	private static final int DECODER_FAIL = 4; // 解码未完成，没有可用的output buffer

	public static MediaCodec codec;
	public static MediaFormat format;
	public static ByteBuffer[] codecInputBuffers;
	public static ByteBuffer[] codecOutputBuffers;

	public static final String LOG_TAG = "MediaDecoder";
	public static boolean recorded = false;

	final long kTimeOutUs = 10000;//
	FFMpegPlayer mPlayer = null;
	BufferInfo info = new BufferInfo();

	private int mWidth;
	private int mHeight;
	private Surface mSurface;
	private int first_frame = 0;
	private boolean bRender = false;

	public CodecWrapper() {

	}

	public void setPlayer(FFMpegPlayer player) {
		mPlayer = player;
	}

	public int createDecoder(int videoWidth, int videoHeight, Surface surface) throws IOException {
		Log.d(LOG_TAG,
				"start create decoder, videoWidth:"
						+ String.valueOf(videoWidth) + "videoHeight:"
						+ String.valueOf(videoHeight));
		if (videoWidth <= 0 || videoHeight <= 0) {
			Log.d(LOG_TAG, "invalid width or height");
			return -1;
		}
		/*
		 * try{ getCapbility(); }catch( Exception e ) { e.printStackTrace();
		 * return -1; }
		 */
		mWidth = videoWidth;
		mHeight = videoHeight;
		mSurface = surface;
		codec = MediaCodec.createDecoderByType("video/avc");
		if (codec == null) {
			Log.d(LOG_TAG, "Hardware codec is not available");
			return -1;
		}

		try {
			format = MediaFormat.createVideoFormat("video/avc", videoWidth,
					videoHeight);
			// Log.d( LOG_TAG, "Track Format: " + mime );
			// format.setInteger( MediaFormat.KEY_BIT_RATE, 125000 );
			// format.setInteger( MediaFormat.KEY_FRAME_RATE, 15 );
			// format.setInteger( MediaFormat.KEY_MAX_INPUT_SIZE, 2097152 );
			format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
					MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
			// format.setInteger( MediaFormat.KEY_I_FRAME_INTERVAL, 5 );
			Log.d(LOG_TAG, "configure mediacodec");
			codec.configure(format, surface, null, 0);
			codec.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
			Log.d(LOG_TAG, "start mediacodec");
			codec.start();

			Log.d(LOG_TAG, "get input and output buffer");
			codecInputBuffers = codec.getInputBuffers();
			codecOutputBuffers = codec.getOutputBuffers();
//			Log.d(LOG_TAG,
//					"input buffer length"
//							+ String.valueOf(codecInputBuffers.length));
//			Log.d(LOG_TAG,
//					"output buffer length"
//							+ String.valueOf(codecOutputBuffers.length));
		} catch (IllegalStateException e) {
			Log.d(LOG_TAG, "Exception catched in createDecoder");
			return -1;
		}

		return 1;
	}

	public void stopCodec() {
		if (codec != null) {
			try {
				Log.d(LOG_TAG, "start stop codec");
				int i = 0;
				if (codecInputBuffers != null) {
					for (i = 0; i < codecInputBuffers.length; i++) {
						if (codecInputBuffers[i] != null)
							codecInputBuffers[i].clear();
					}
				}
				if (codecOutputBuffers != null) {
					for (i = 0; i < codecOutputBuffers.length; i++) {
						if (codecOutputBuffers[i] != null)
							codecOutputBuffers[i].clear();
					}
				}
				codec.flush();
				codec.stop();
				codecInputBuffers = null;
				codecOutputBuffers = null;
				format = null;
				Log.d(LOG_TAG, "after stop codec, start release codec");
				codec.release();
				codec = null;
				Log.d(LOG_TAG, "end stop codec");
			} catch (IllegalStateException e) {
				Log.d(LOG_TAG, "Exception catched in stopCodec");
			}
		}
	}

	public int flushCodec() {
		if (codec != null) {
			try {

				Log.d(LOG_TAG, "flushCodec");
				int i = 0;
				if (codecInputBuffers != null) {
					for (i = 0; i < codecInputBuffers.length; i++) {
						if (codecInputBuffers[i] != null)
							codecInputBuffers[i].clear();
					}
				}

				if (codecOutputBuffers != null) {
					for (i = 0; i < codecOutputBuffers.length; i++) {
						if (codecOutputBuffers[i] != null)
							codecOutputBuffers[i].clear();
					}
				}
				codec.flush();
			} catch (IllegalStateException e) {
				Log.d(LOG_TAG, "Exception catched in flushCodec");
			}
		}
		return 1;
	}

	public int fillInputBuffer(byte[] data, long pts, int flush) {
		int sampleSize = data.length;
		if (sampleSize <= 0) {
			// Log.d(LOG_TAG, "The data buffer is empty");
			return DATAT_ERROR;
		}
		/*
		 * if( flush == 999 ) { Log.d( LOG_TAG, "fillInputBuffer to flush" );
		 * int i = 0; for(i=0; i< codecInputBuffers.length; i++) {
		 * codecInputBuffers[i].clear(); } for(i=0; i<
		 * codecOutputBuffers.length; i++) { codecOutputBuffers[i].clear(); }
		 * codec.flush(); }
		 */

		try {
			// Log.d(LOG_TAG, "sampleSize=" + sampleSize);
			int inputBufIndex = codec.dequeueInputBuffer(10000);// -1 means
																// blocking here
																// until buffer
																// is available
																// kTimeOutUs*100
																// Log.d(LOG_TAG,
			// "InputBuf res:" + String.valueOf(inputBufIndex) + "  pts:"
			// + String.valueOf(pts) + "  datasize:"
			// + String.valueOf(sampleSize));
			if (inputBufIndex >= 0) {
				ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];
				dstBuf.clear();
				dstBuf.put(data);
				boolean sawInputEOS = false;
				long presentationTimeUs = pts;
				if (pts < 0)
					presentationTimeUs = 0L;
				// Log.d(LOG_TAG, "inputBufIndex:" +
				// String.valueOf(inputBufIndex));
				// Log.d( LOG_TAG, "presentationTimeUs:"+String.valueOf(
				// presentationTimeUs ) );
				if (first_frame == 0) {
					first_frame = 1;
					codec.queueInputBuffer(inputBufIndex, 0, sampleSize, 1,
							MediaCodec.BUFFER_FLAG_CODEC_CONFIG);
					// codec.flush();
					// Log.d(LOG_TAG, "first config sampesize=" + sampleSize);
					return RENDER_SUCCESS;
				} else {
					codec.queueInputBuffer(inputBufIndex, 0, sampleSize,
							presentationTimeUs,
							sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM
									: 0);
					// Log.d(LOG_TAG, "queueInputBuffer finished");
				}
			} else {
				getOutputBuffer();
				return INPUTBUFFER_FULL;
			}
			return getOutputBuffer();
		} catch (IllegalStateException e) {
			Log.d(LOG_TAG, "IllegalStateException catched in fillInputBuffer");
			return IO_EXCEPTION;
		}
		// return 0;
	}

	public int getOutputBuffer() {
		// Log.d(LOG_TAG, "getOutputBuffer dequeueOutputBuffer");
		int ret = RENDER_SUCCESS;
		final int res = codec.dequeueOutputBuffer(info, 10000);// -1
																// kTimeOutUs*10
																// Log.d(LOG_TAG,
																// "getOutputBuffer res="
																// +
																// String.valueOf(res));
		if (res >= 0) {
			int outputBufIndex = res;
			// ByteBuffer buf = codecOutputBuffers[ outputBufIndex ];
			// byte[] chunk = new byte[ info.size ];
			// buf.get( chunk ); // Read the buffer all at once
			// buf.clear( ); // ** MUST DO!!! OTHERWISE THE NEXT TIME YOU GET
			// THIS SAME BUFFER BAD THINGS WILL HAPPEN
			// Log.d( LOG_TAG,"output buf data size: "+String.valueOf( info.size
			// ) );
			// Log.d( LOG_TAG,"pts in info is: "+String.valueOf(
			// info.presentationTimeUs ));
			if (info.size > 0) {
				// do the things
				/*
				 * if( !recorded ) { try { File file = new
				 * File("/data/data/com.qiyi.video/files/dec.dec");
				 * DataOutputStream os = new DataOutputStream(new
				 * FileOutputStream(file));
				 * 
				 * os.write(chunk); os.close(); } catch (IOException e) { //
				 * TODO Auto-generated catch block e.printStackTrace(); }
				 * recorded = true; }
				 */
			}

			// MediaFormat format = codec.getOutputFormat( );
			// Log.d( LOG_TAG, "format width:" +
			// format.getInteger(MediaFormat.KEY_WIDTH)+" format height:" +
			// format.getInteger(MediaFormat.KEY_HEIGHT) );

			int sync_status = 0;
			if (mPlayer != null) {
				try {
					sync_status = mPlayer._native_sync(info.presentationTimeUs);
					// Log.d(LOG_TAG,
					// "call sync with pts:"+String.valueOf(info.presentationTimeUs)+
					// "return status:"+String.valueOf(sync_status));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (sync_status > 0) {
				if (!bRender) {
					mPlayer.startHwRender();
					bRender = true;
				}
				codec.releaseOutputBuffer(outputBufIndex, true /* render */);
				ret = RENDER_SUCCESS;
			} else if (sync_status == -1) {
				// Log.d(LOG_TAG, "to flush codec");
				codec.releaseOutputBuffer(outputBufIndex, false /* render */);
				int i = 0;
				if (codecInputBuffers != null) {
					for (i = 0; i < codecInputBuffers.length; i++) {
						if (codecInputBuffers[i] != null)
							codecInputBuffers[i].clear();
					}
				}
				if (codecOutputBuffers != null) {
					for (i = 0; i < codecOutputBuffers.length; i++) {
						if (codecOutputBuffers[i] != null)
							codecOutputBuffers[i].clear();
					}
				}
				codec.flush();
				ret = DECODER_TIMEOUT;
			} else {
				codec.releaseOutputBuffer(outputBufIndex, false /* render */);
				ret = DECODER_TIMEOUT;
			}

			if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
				// Log.d(LOG_TAG, "BUFFER_FLAG_END_OF_STREAM");
				// sawOutputEOS = true;
			}
			return ret;
		} else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
			// Log.d(LOG_TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
			codecOutputBuffers = codec.getOutputBuffers();
			// Log.d(LOG_TAG, "INFO_OUTPUT_BUFFERS_CHANGED buffer length:"
			// + String.valueOf(codecOutputBuffers.length));
			return OUTPUT_BUFFERS_CHANGED;
		} else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
			final MediaFormat oformat = codec.getOutputFormat();
			// Log.d(LOG_TAG, "Output format has changed to " + oformat);
			// Log.d(LOG_TAG,
			// "Output format width:"
			// + oformat.getInteger(MediaFormat.KEY_WIDTH));
			// Log.d(LOG_TAG,
			// "Output format height:"
			// + oformat.getInteger(MediaFormat.KEY_HEIGHT));
			return OUTPUT_FORMAT_CHANGED;
			/*
			 * if( mWidth != oformat.getInteger(MediaFormat.KEY_WIDTH) ||
			 * mHeight != oformat.getInteger(MediaFormat.KEY_HEIGHT) ) { Log.d(
			 * LOG_TAG, "Need to reset codec" ); mWidth =
			 * oformat.getInteger(MediaFormat.KEY_WIDTH); mHeight =
			 * oformat.getInteger(MediaFormat.KEY_HEIGHT); stopCodec();
			 * createDecoder( mWidth, mHeight, mSurface ); }
			 */
			// mAudioTrack.setPlaybackRate( oformat.getInteger(
			// MediaFormat.KEY_SAMPLE_RATE ) );
		} else {
			return DECODER_FAIL;
		}

	}

	public static int getCapbility() {

		int maxProfile = 0;
		int tsType = MediaDecoder.NOT_SUPPORT_TS;

		Log.d(LOG_TAG,
				"Build.VERSION.SDK_INT:"
						+ String.valueOf(Build.VERSION.SDK_INT));
		if (Build.VERSION.SDK_INT < 16) {
			return tsType;
		}

		final int mediaCodecListCount = MediaCodecList.getCodecCount();
		for (int i = 0; i < mediaCodecListCount; i++) {
			final MediaCodecInfo mediaCodecInfo = MediaCodecList
					.getCodecInfoAt(i);
			if (mediaCodecInfo.isEncoder()) {
				continue;
			}
			if (mediaCodecInfo.getName().startsWith("OMX.google")) {
				continue;
			}
			if (mediaCodecInfo.getName().startsWith("OMX.TI.")) {
				continue;
			}
			Log.d(LOG_TAG, "name:" + mediaCodecInfo.getName());
			// Log.d( LOG_TAG, "is encoder:" + mediaCodecInfo.isEncoder());
			for (final String type : mediaCodecInfo.getSupportedTypes()) {
				if (!type.contains("avc")) {
					continue;
				}
				Log.d(LOG_TAG, "type:" + type);
				CodecCapabilities codecCapabilities = null;
				try {
					codecCapabilities = mediaCodecInfo
							.getCapabilitiesForType(type);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				} finally {

				}

				for (final int colorFormat : codecCapabilities.colorFormats) {
					Log.d(LOG_TAG, "Color Format:" + " " + colorFormat + " "
							+ colorFormatToString(colorFormat));
				}

				for (final CodecProfileLevel codecProfileLevel : codecCapabilities.profileLevels) {
					String level = "unknown type";
					String sprofile = "unknown type";
					level = avcLevelToString(codecProfileLevel.level);
					sprofile = avcProfileToString(codecProfileLevel.profile);
					Log.d(LOG_TAG, "Codec Profile Level:" + level + " profile:"
							+ sprofile);
					if (codecProfileLevel.profile > maxProfile) {
						maxProfile = codecProfileLevel.profile;
					}
				}
			}
		}
		Log.d(LOG_TAG, "Max profile:" + maxProfile + " "
				+ avcProfileToString(maxProfile));
		if (maxProfile >= CodecProfileLevel.AVCProfileHigh) {
			tsType = MediaDecoder.TS_1_1_MTS;
		}
		return tsType;
	}

	public static int getAVCLevel() {

		int maxAVCLevel = 0;

		if (Build.VERSION.SDK_INT < 16) {
			return maxAVCLevel;
		}

		final int mediaCodecListCount = MediaCodecList.getCodecCount();
		for (int i = 0; i < mediaCodecListCount; i++) {
			final MediaCodecInfo mediaCodecInfo = MediaCodecList
					.getCodecInfoAt(i);
			if (mediaCodecInfo.isEncoder()) {
				continue;
			}
			if (mediaCodecInfo.getName().startsWith("OMX.google")) {
				continue;
			}
			if (mediaCodecInfo.getName().startsWith("OMX.TI.")) {
				continue;
			}
			Log.d(LOG_TAG, "name:" + mediaCodecInfo.getName());
			// Log.d( LOG_TAG, "is encoder:" + mediaCodecInfo.isEncoder());
			for (final String type : mediaCodecInfo.getSupportedTypes()) {
				if (!type.contains("avc")) {
					continue;
				}
				Log.d(LOG_TAG, "type:" + type);
				CodecCapabilities codecCapabilities = null;
				try {
					codecCapabilities = mediaCodecInfo
							.getCapabilitiesForType(type);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				} finally {

				}

				for (final int colorFormat : codecCapabilities.colorFormats) {
					Log.d(LOG_TAG, "Color Format:" + " " + colorFormat + " "
							+ colorFormatToString(colorFormat));
				}

				for (final CodecProfileLevel codecProfileLevel : codecCapabilities.profileLevels) {
					String level = "unknown type";
					String sprofile = "unknown type";
					level = avcLevelToString(codecProfileLevel.level);
					sprofile = avcProfileToString(codecProfileLevel.profile);
					Log.d(LOG_TAG, "Codec Profile Level:" + level + " profile:"
							+ sprofile);
					if (codecProfileLevel.level > maxAVCLevel) {
						maxAVCLevel = codecProfileLevel.level;
					}
				}
			}
		}
		Log.d(LOG_TAG, "Max AVCLevel:" + maxAVCLevel + " "
				+ avcProfileToString(maxAVCLevel));

		return maxAVCLevel;
	}

	public static int getProfile() {

		int maxProfile = 0;

		Log.d(LOG_TAG,
				"Build.VERSION.SDK_INT:"
						+ String.valueOf(Build.VERSION.SDK_INT));
		if (Build.VERSION.SDK_INT < 16) {
			return maxProfile;
		}

		final int mediaCodecListCount = MediaCodecList.getCodecCount();
		for (int i = 0; i < mediaCodecListCount; i++) {
			final MediaCodecInfo mediaCodecInfo = MediaCodecList
					.getCodecInfoAt(i);
			if (mediaCodecInfo.isEncoder()) {
				continue;
			}
			if (mediaCodecInfo.getName().startsWith("OMX.google")) {
				continue;
			}
			if (mediaCodecInfo.getName().startsWith("OMX.TI.")) {
				continue;
			}
			Log.d(LOG_TAG, "name:" + mediaCodecInfo.getName());
			// Log.d( LOG_TAG, "is encoder:" + mediaCodecInfo.isEncoder());
			for (final String type : mediaCodecInfo.getSupportedTypes()) {
				if (!type.contains("avc")) {
					continue;
				}
				Log.d(LOG_TAG, "type:" + type);
				CodecCapabilities codecCapabilities = null;
				try {
					codecCapabilities = mediaCodecInfo
							.getCapabilitiesForType(type);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				} finally {

				}

				for (final int colorFormat : codecCapabilities.colorFormats) {
					Log.d(LOG_TAG, "Color Format:" + " " + colorFormat + " "
							+ colorFormatToString(colorFormat));
				}

				for (final CodecProfileLevel codecProfileLevel : codecCapabilities.profileLevels) {
					String level = "unknown type";
					String sprofile = "unknown type";
					level = avcLevelToString(codecProfileLevel.level);
					sprofile = avcProfileToString(codecProfileLevel.profile);
					Log.d(LOG_TAG, "Codec Profile Level:" + level + " profile:"
							+ sprofile);
					if (codecProfileLevel.profile > maxProfile) {
						maxProfile = codecProfileLevel.profile;
					}
				}
			}
		}
		Log.d(LOG_TAG, "Max profile:" + maxProfile + " "
				+ avcProfileToString(maxProfile));

		return maxProfile;
	}

	private static String colorFormatToString(int colorFormat) {
		String ret = "not found(" + colorFormat + ")";
		switch (colorFormat) {
		case CodecCapabilities.COLOR_Format12bitRGB444:
			ret = "COLOR_Format12bitRGB444";
			break;
		case CodecCapabilities.COLOR_Format16bitARGB1555:
			ret = "COLOR_Format16bitARGB1555";
			break;
		case CodecCapabilities.COLOR_Format16bitARGB4444:
			ret = "COLOR_Format16bitARGB4444";
			break;
		case CodecCapabilities.COLOR_Format16bitBGR565:
			ret = "COLOR_Format16bitBGR565";
			break;
		case CodecCapabilities.COLOR_Format16bitRGB565:
			ret = "COLOR_Format16bitRGB565";
			break;
		case CodecCapabilities.COLOR_Format18BitBGR666:
			ret = "COLOR_Format18BitBGR666";
			break;
		case CodecCapabilities.COLOR_Format18bitARGB1665:
			ret = "COLOR_Format18bitARGB1665";
			break;
		case CodecCapabilities.COLOR_Format18bitRGB666:
			ret = "COLOR_Format18bitRGB666";
			break;
		case CodecCapabilities.COLOR_Format19bitARGB1666:
			ret = "COLOR_Format19bitARGB1666";
			break;
		case CodecCapabilities.COLOR_Format24BitABGR6666:
			ret = "COLOR_Format24BitABGR6666";
			break;
		case CodecCapabilities.COLOR_Format24BitARGB6666:
			ret = "COLOR_Format24BitARGB6666";
			break;
		case CodecCapabilities.COLOR_Format24bitARGB1887:
			ret = "COLOR_Format24bitARGB1887";
			break;
		case CodecCapabilities.COLOR_Format24bitBGR888:
			ret = "COLOR_Format24bitBGR888";
			break;
		case CodecCapabilities.COLOR_Format24bitRGB888:
			ret = "COLOR_Format24bitRGB888";
			break;
		case CodecCapabilities.COLOR_Format25bitARGB1888:
			ret = "COLOR_Format25bitARGB1888";
			break;
		case CodecCapabilities.COLOR_Format32bitARGB8888:
			ret = "COLOR_Format32bitARGB8888";
			break;
		case CodecCapabilities.COLOR_Format32bitBGRA8888:
			ret = "COLOR_Format32bitBGRA8888";
			break;
		case CodecCapabilities.COLOR_Format8bitRGB332:
			ret = "COLOR_Format8bitRGB332";
			break;
		case CodecCapabilities.COLOR_FormatCbYCrY:
			ret = "COLOR_FormatCbYCrY";
			break;
		case CodecCapabilities.COLOR_FormatCrYCbY:
			ret = "COLOR_FormatCrYCbY";
			break;
		case CodecCapabilities.COLOR_FormatL16:
			ret = "COLOR_FormatL16";
			break;
		case CodecCapabilities.COLOR_FormatL2:
			ret = "COLOR_FormatL2";
			break;
		case CodecCapabilities.COLOR_FormatL24:
			ret = "COLOR_FormatL24";
			break;
		case CodecCapabilities.COLOR_FormatL32:
			ret = "COLOR_FormatL32";
			break;
		case CodecCapabilities.COLOR_FormatL4:
			ret = "COLOR_FormatL4";
			break;
		case CodecCapabilities.COLOR_FormatL8:
			ret = "COLOR_FormatL8";
			break;
		case CodecCapabilities.COLOR_FormatMonochrome:
			ret = "COLOR_FormatMonochrome";
			break;
		case CodecCapabilities.COLOR_FormatRawBayer10bit:
			ret = "COLOR_FormatRawBayer10bit";
			break;
		case CodecCapabilities.COLOR_FormatRawBayer8bit:
			ret = "COLOR_FormatRawBayer8bit";
			break;
		case CodecCapabilities.COLOR_FormatRawBayer8bitcompressed:
			ret = "COLOR_FormatRawBayer8bitcompressed";
			break;
		case CodecCapabilities.COLOR_FormatYCbYCr:
			ret = "COLOR_FormatYCbYCr";
			break;
		case CodecCapabilities.COLOR_FormatYCrYCb:
			ret = "COLOR_FormatYCrYCb";
			break;
		case CodecCapabilities.COLOR_FormatYUV411PackedPlanar:
			ret = "COLOR_FormatYUV411PackedPlanar";
			break;
		case CodecCapabilities.COLOR_FormatYUV411Planar:
			ret = "COLOR_FormatYUV411Planar";
			break;
		case CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
			ret = "COLOR_FormatYUV420PackedPlanar";
			break;
		case CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
			ret = "COLOR_FormatYUV420PackedSemiPlanar";
			break;
		case CodecCapabilities.COLOR_FormatYUV420Planar:
			ret = "COLOR_FormatYUV420Planar";
			break;
		case CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
			ret = "COLOR_FormatYUV420SemiPlanar";
			break;
		case CodecCapabilities.COLOR_FormatYUV422PackedPlanar:
			ret = "COLOR_FormatYUV422PackedPlanar";
			break;
		case CodecCapabilities.COLOR_FormatYUV422PackedSemiPlanar:
			ret = "COLOR_FormatYUV422PackedSemiPlanar";
			break;
		case CodecCapabilities.COLOR_FormatYUV422Planar:
			ret = "COLOR_FormatYUV422Planar";
			break;
		case CodecCapabilities.COLOR_FormatYUV422SemiPlanar:
			ret = "COLOR_FormatYUV422SemiPlanar";
			break;
		case CodecCapabilities.COLOR_FormatYUV444Interleaved:
			ret = "COLOR_FormatYUV444Interleaved";
			break;
		case CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar:
			ret = "COLOR_QCOM_FormatYUV420SemiPlanar";
			break;
		case CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
			ret = "COLOR_TI_FormatYUV420PackedSemiPlanar";
			break;

		}

		return ret;
	}

	private static String avcProfileToString(int profile) {
		String ret = "not found(" + profile + ")";

		switch (profile) {
		case CodecProfileLevel.AVCProfileBaseline:
			ret = "AVCProfileBaseline";
			break;
		case CodecProfileLevel.AVCProfileExtended:
			ret = "AVCProfileExtended";
			break;
		case CodecProfileLevel.AVCProfileHigh:
			ret = "AVCProfileHigh";
			break;
		case CodecProfileLevel.AVCProfileHigh10:
			ret = "AVCProfileHigh10";
			break;
		case CodecProfileLevel.AVCProfileHigh422:
			ret = "AVCProfileHigh422";
			break;
		case CodecProfileLevel.AVCProfileHigh444:
			ret = "AVCProfileHigh444";
			break;
		case CodecProfileLevel.AVCProfileMain:
			ret = "AVCProfileMain";
			break;
		}
		return ret;
	}

	private static String avcLevelToString(int level) {
		String ret = "not found(" + level + ")";
		switch (level) {
		case CodecProfileLevel.AVCLevel1:
			ret = "AVCLevel1";
			break;
		case CodecProfileLevel.AVCLevel11:
			ret = "AVCLevel11";
			break;
		case CodecProfileLevel.AVCLevel12:
			ret = "AVCLevel12";
			break;
		case CodecProfileLevel.AVCLevel13:
			ret = "AVCLevel13";
			break;
		case CodecProfileLevel.AVCLevel1b:
			ret = "AVCLevel1b";
			break;
		case CodecProfileLevel.AVCLevel2:
			ret = "AVCLevel2";
			break;
		case CodecProfileLevel.AVCLevel21:
			ret = "AVCLevel21";
			break;
		case CodecProfileLevel.AVCLevel22:
			ret = "AVCLevel22";
			break;
		case CodecProfileLevel.AVCLevel3:
			ret = "AVCLevel3";
			break;
		case CodecProfileLevel.AVCLevel31:
			ret = "AVCLevel31";
			break;
		case CodecProfileLevel.AVCLevel32:
			ret = "AVCLevel32";
			break;
		case CodecProfileLevel.AVCLevel4:
			ret = "AVCLevel4";
			break;
		case CodecProfileLevel.AVCLevel41:
			ret = "AVCLevel41";
			break;
		case CodecProfileLevel.AVCLevel42:
			ret = "AVCLevel42";
			break;
		case CodecProfileLevel.AVCLevel5:
			ret = "AVCLevel5";
			break;
		case CodecProfileLevel.AVCLevel51:
			ret = "AVCLevel51";
			break;
		}
		return ret;
	}

}
