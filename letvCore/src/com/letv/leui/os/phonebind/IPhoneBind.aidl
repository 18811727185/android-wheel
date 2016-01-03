package com.letv.leui.os.phonebind;

/**
 * add for imei bind key
 * @author fengzihua
 * {@hide}
 */
interface IPhoneBind {

    /**
     * read key from special block
     * @param token
     */
    String getLeTVSNValue(in String token);
}