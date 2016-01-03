package com.letv.shared.os.phonecontrol;

import com.letv.shared.os.phonecontrol.PhoneControlData;

/**
 * phone control interface for lock screen or other.
 *
 * @author fengzihua
 * {@hide}
 */
interface IPhoneControlService {
    PhoneControlData unLockPhone(in String password);
}