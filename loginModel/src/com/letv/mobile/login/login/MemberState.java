/**
 *
 * Copyright 2015 LeTV Technology Co. Ltd., Inc. All rights reserved.
 *
 * @Author : qingxia
 *
 * @Description :
 *
 */

package com.letv.mobile.login.login;

/**
 * This class indicates the member states.
 * @author xiaqing
 */
public enum MemberState {
    MEMBER_STATE_NO_INIT("-1"),
    MEMBER_STATE_NO_VIP("0"),
    MEMBER_STATE_ORDINARY_VIP("1"),
    MEMBER_STATE_SENIOR_VIP("2");

    private String mId = "-1";

    public String getId() {
        return this.mId;
    }

    private void setId(String mId) {
        this.mId = mId;
    }

    private MemberState(String id) {
        this.setId(id);
    }

    public static MemberState getStateById(String id) {
        for (MemberState state : MemberState.values()) {
            if (state.getId().equals(id)) {
                return state;
            }
        }

        return MEMBER_STATE_NO_INIT;
    }
}
