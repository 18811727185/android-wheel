package com.letv.shared.widget;
    /**
     * {@hide}
     * [LEUI-2578]ListView's Divider
     * @author wangziming
     * zhangyd port for seperate it from android code
     */
    public interface DividerFilter {
        /** Whether to draw topDivider. When {@link ListView#mStackFromBottom} = false, 
         *  the function is valid.*/
        public boolean topDividerEnabled();
        
        /** Whether to draw bottomDivider. When {@link ListView#mStackFromBottom} = true, 
         *  the function is valid.*/
        public boolean bottomDividerEnabled();
        
        /** Whether to draw the divider at postion. */
        public boolean dividerEnabled(int position);

        /** LeftMargin of the divider at postion. */
        public int leftDividerMargin(int position);

        /** RightMargin of the divider at postion. */
        public int rightDividerMargin(int position);
        
        /** Force to draw the divider at postion. */
        public boolean forceDrawDivider(int position);
    }
