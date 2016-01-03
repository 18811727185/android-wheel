package com.letv.sysletvplayer.setting;

import android.app.SystemWriteManager;
import android.os.SystemProperties;

import com.amlogic.view.DisplaySetting;
import com.letv.mobile.core.log.Logger;
import com.letv.mobile.core.utils.DeviceUtils;
import com.letv.mobile.core.utils.DeviceUtils.DeviceType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author caiwei
 */
public class Video3DSetting {
    private static final int BUFFER_SIZE = 32;
    private static final String _3DTB_MODE = "3dtb";
    private static final String VIDEO_3D_MODE_NORMAL = "0";
    private static final String _3DOFF = "3doff";
    private static final String REQUEST2XSCALE_1080P_C1 = "8 1";
    private static final String REQUEST2XSCALE_720P_C1 = "17";
    private static final String _3DLR_MODE = "3dlr";
    private static final String REQUEST2XSCALE_1080P_C2 = "8 2";
    private static final String REQUEST2XSCALE_720P_C2 = "18";
    private static final String VIDEO_3D_MODE_TB = "2";
    private static final String OSD_BLANK_3D = "0";
    private static final String OSD_BLANK_2D = "1";
    private static final String VIDEO_3D_MODE_LR = "1";
    private static final String VIDEOAXISFILE_SETTING_1080P = "0 0 1919 1079";
    private static final String _1080P50HZ = "1080p50hz";
    private static final String _1080P = "1080p";
    private static final String VIDEOAXISFILE_SETTING_720P = "0 0 1279 719";
    private static final String _0X1 = "0x1";
    private static final String _720P_MODE = "720p mode";
    private static final String _720P50HZ = "720p50hz";
    private static final String _720P = "720p";
    private static final String VIDEOAXISFILE_SETTING = "0 0 1280 720";
    private static final String DISPAXISFILE_SETTING = "0 0 1280 720 0 0 18 18";
    private final String mHDMIConfigFile = "/sys/class/amhdmitx/amhdmitx0/config";// force
    // //3D----Normal=2----
    private final String mRequest2XScaleFile = "/sys/class/graphics/fb0/request2XScale";
    private final String VideoAxisFile = "/sys/class/video/axis";
    private final String DispAxisFile = "/sys/class/display/axis";
    private final String FreeScaleFile = "/sys/class/graphics/fb0/free_scale";
    // 0 0 0 0
    private final String ScaleAxis = "/sys/class/graphics/fb0/scale_axis";
    // 设置为1会出现UI不显示，“”或者0正常
    private final String OsdBlank = "/sys/class/graphics/fb0/blank";
    public static final String mKeyVideoMode3D = "mbx.video.mode.3d";
    private static SystemWriteManager sw;
    private String LastRequest2XScale = null;
    private String LastVideoAxis = null;
    private String LastDispAxis = null;
    private String LastScaleAxis = null;
    public static final String TAG = "Video3dsetting";
    Logger logger = new Logger(TAG);

    public static void setSystemWrite(SystemWriteManager sysWrite) {
        sw = sysWrite;
    }

    private void writeFile(String file, String value) {
        File OutputFile = new File(file);
        if (!OutputFile.exists()) {
            return;
        }

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(OutputFile),
                    BUFFER_SIZE);
            try {
                this.logger.d("set" + file + ": " + value);
                out.write(value);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            this.logger.e("IOException when write " + OutputFile);
        }
    }

    private boolean writeSysFile(String pathname, String value) {
        boolean result = true;
        if (sw != null) {
            sw.writeSysfs(pathname, value);
        }
        return result;
    }

    private String readSysFile(String pathname) {
        String message = null;

        FileReader fileReader = null;
        BufferedReader bufferedReader = null;

        try {
            fileReader = new FileReader(pathname);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        bufferedReader = new BufferedReader(fileReader);

        try {
            message = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            bufferedReader.close();
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return message;
    }

    private void set3DVideoAxis() {
        String freescale = this.readSysFile(this.FreeScaleFile);
        if ((this.LastVideoAxis == null) && (this.LastDispAxis == null)) {
            this.LastVideoAxis = this.readSysFile(this.VideoAxisFile);
            this.LastDispAxis = this.readSysFile(this.DispAxisFile);
        }
        this.writeSysFile(this.DispAxisFile, DISPAXISFILE_SETTING);
        String outputmode = SystemProperties.get("ubootenv.var.outputmode");
        this.logger.d("[set3DVideoAxis], freescale is " + freescale
                + " outputmode is " + outputmode);
        if (freescale.contains(_0X1)) {
            this.logger.d("freescale is opened");
            this.writeSysFile(this.VideoAxisFile, VIDEOAXISFILE_SETTING);
        } else {
            this.logger.d("freescale is not opened");
            if (outputmode.equals(_720P) || outputmode.equals(_720P50HZ)) {
                this.logger.d(_720P_MODE);
                this.writeFile(this.VideoAxisFile, VIDEOAXISFILE_SETTING_720P);
            } else if (outputmode.equals(_1080P)
                    || outputmode.equals(_1080P50HZ)) {
                this.logger.d("1080p mode");
                this.writeFile(this.VideoAxisFile, VIDEOAXISFILE_SETTING_1080P);
            }
        }
    }

    private void set2DVideoAxis() {
        this.logger.d("---begin set 2D Video Axis---");
        if (this.LastDispAxis != null && this.LastVideoAxis != null) {
            this.writeSysFile(this.DispAxisFile, this.LastDispAxis);
            this.logger.d("---display axis is---" + this.LastDispAxis);
            this.writeSysFile(this.VideoAxisFile, this.LastVideoAxis);
            this.logger.d("---video axis is---" + this.LastVideoAxis);
            this.LastDispAxis = null;
            this.LastVideoAxis = null;
        }
    }

    private String getCurrentOutputmode() {
        String outputmode = null;
        outputmode = sw.getProperty("ubootenv.var.outputmode");
        return outputmode;
    }

    /** 设置3D模式 */
    public void changeTo3DMode(int mode) {
        // apply in 4.2
        DeviceType type = DeviceUtils.getDeviceType();
        String freescale = "";
        if (type != DeviceType.DEVICE_C2) {
            if (this.LastRequest2XScale == null) {
                this.LastRequest2XScale = this
                        .readSysFile(this.mRequest2XScaleFile);
            }
            if (this.LastScaleAxis == null) {
                this.LastScaleAxis = this.readSysFile(this.ScaleAxis);
            }
            freescale = this.readSysFile(this.FreeScaleFile);
            this.writeSysFile(this.OsdBlank, OSD_BLANK_3D);
        }
        // TB mode
        if (mode == 1) {
            sw.setProperty(mKeyVideoMode3D, VIDEO_3D_MODE_TB);
            this.writeSysFile(this.mHDMIConfigFile, _3DTB_MODE);
            this.set3DVideoAxis();
            if (type != DeviceType.DEVICE_C2) {
                if (this.getCurrentOutputmode().contains(_720P)
                        || (freescale.contains(_0X1))) {
                    this.writeSysFile(this.mRequest2XScaleFile,
                            REQUEST2XSCALE_720P_C2);
                } else if (this.getCurrentOutputmode().contains(_1080P)) {
                    this.writeSysFile(this.mRequest2XScaleFile,
                            REQUEST2XSCALE_1080P_C2);
                }
            } else {
                /*
                 * apply in 4.4
                 */
                DisplaySetting
                        .setDisplay2Stereoscopic(DisplaySetting.REQUEST_3D_FORMAT_TOP_BOTTOM);
            }

        } else if (mode == 0) {// LR mode
            sw.setProperty(mKeyVideoMode3D, VIDEO_3D_MODE_LR);
            this.writeSysFile(this.mHDMIConfigFile, _3DLR_MODE);
            this.set3DVideoAxis();
            if (type != DeviceType.DEVICE_C2) {
                if (this.getCurrentOutputmode().contains(_720P)
                        || (freescale.contains(_0X1))) {
                    this.writeSysFile(this.mRequest2XScaleFile,
                            REQUEST2XSCALE_720P_C1);
                } else if (this.getCurrentOutputmode().contains(_1080P)) {
                    this.writeSysFile(this.mRequest2XScaleFile,
                            REQUEST2XSCALE_1080P_C1);
                }
            } else {
                /*
                 * apply in android 4.4
                 */
                DisplaySetting
                        .setDisplay2Stereoscopic(DisplaySetting.REQUEST_3D_FORMAT_SIDE_BY_SIDE);
            }
        }

    }

    /** 设置普通模式 */
    public void changeTo2DMode() {
        this.logger.d("---begin change to 2D mode---");
        // 容错处理
        sw.setProperty(mKeyVideoMode3D, VIDEO_3D_MODE_NORMAL);
        this.writeSysFile(this.mHDMIConfigFile, _3DOFF);
        this.set2DVideoAxis();
        DeviceType type = DeviceUtils.getDeviceType();
        if (type == DeviceType.DEVICE_C2) {
            /*
             * Apply in android 4.4
             */
            DisplaySetting
                    .setDisplay2Stereoscopic(DisplaySetting.STEREOSCOPIC_3D_FORMAT_OFF);
        } else if (this.LastRequest2XScale != null
                && this.LastScaleAxis != null) {
            // apply in 4.2
            this.writeSysFile(this.OsdBlank, OSD_BLANK_2D);
            this.writeSysFile(this.mRequest2XScaleFile, this.LastRequest2XScale);
            this.writeSysFile(this.ScaleAxis, this.LastScaleAxis);
            this.LastRequest2XScale = null;
        }
    }
}
