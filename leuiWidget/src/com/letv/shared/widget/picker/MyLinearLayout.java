package com.letv.shared.widget.picker;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.letv.shared.R;
//import com.letv.leui.R;

/**
 * MyLinearLayout
 * @author mengfengxiao@letv.com
 *
 */

public class MyLinearLayout extends LinearLayout {

	public MyLinearLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MyLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyLinearLayout(Context context) {
		super(context);
        // liangchao do not applay transformation

		this.setStaticTransformationsEnabled(true);
		mCamera = new Camera();
	}

	public WheelView getWhellView() {
		return whellView;
	}

	public void setWhellView(WheelView whellView) {
		this.whellView = whellView;
	}

    private Camera mCamera;
    private Matrix imageMatrix;
    private WheelView whellView ;
//	private float maxAngle = 150;
	private float alpha = 0;
	/*private float angle;
	float translate_Z = 0;
	float translate_Y = 0;*/


    @Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {
		int visibleItem = whellView.getVisibleItems();
		int itemCount = whellView.getViewAdapter().getItemsCount();
        int childHeight = whellView.getItemHeight();
        int textSize = whellView.getTextSize();
        float textSize_ItemColor = 16;
        float textSize_CenterColor = 22;
        int cnt = this.getChildCount();

        int uScrollingOffset = whellView.getUOffset();
        int scrollingOffset = whellView.getScrollingOffset();

        if(0==scrollingOffset&&uScrollingOffset!=0) {
            uScrollingOffset = 0;
            whellView.setUOffset(0);
        }
        if(whellView.getIsVertical()) {
        	int pCY =whellView.getHeight()/2;
    		for (int i=0; i<cnt; i++) {
    			View v = this.getChildAt(i);
    			if (v == child) {
                    int cCY = 0;
                    int cCY_new = 0;
                    int diffPos = 0;
                    int currentItem = 0;
                    currentItem = whellView.getCurrentItem();

    				if(itemCount<visibleItem){
                        if(currentItem<visibleItem/2)
                            diffPos = (visibleItem/2-currentItem)*childHeight;
                        else
                            diffPos = 0;
                        if(uScrollingOffset>0){
                            cCY_new =((child.getBottom()-child.getTop())>>1)+child.getTop()+uScrollingOffset-child.getHeight();
                            cCY = cCY_new+diffPos;
                        }
                        else {
                            cCY_new =((child.getBottom()-child.getTop())>>1)+child.getTop()+uScrollingOffset;
                            cCY = cCY_new+diffPos;
                        }
    				} else {
    					if(currentItem<visibleItem/2&&!whellView.isCyclic()){
    						diffPos = (visibleItem/2-currentItem)*childHeight;
    						if(uScrollingOffset>0){
    							cCY_new =((child.getBottom()-child.getTop())>>1)+child.getTop()+uScrollingOffset-child.getHeight();
    							cCY = cCY_new+diffPos;
    						}
    						else {
    							cCY_new =((child.getBottom()-child.getTop())>>1)+child.getTop()+uScrollingOffset;
    							cCY = cCY_new+diffPos;
    						}
    					} else {
    						if(uScrollingOffset>0)
    							cCY =((child.getBottom()-child.getTop())>>1)+child.getTop()+uScrollingOffset-child.getHeight();
    						else 
    							cCY =((child.getBottom()-child.getTop())>>1)+child.getTop()+uScrollingOffset;
    					}
    				}//if(itemCount<visibleItem)
    				/*rotate&translate&alpha*/
    				t.clear();
    				t.setTransformationType(Transformation.TYPE_MATRIX);
//    				angle = -(float)(cCY-pCY)/pCY*(maxAngle/2f);
//    				translate_Z = (float)Math.abs(cCY-pCY)/pCY*(1.0f+0.8f*(float)Math.abs(cCY-pCY)/pCY)*childHeight*1.5f;

    				float Y = ((float)Math.abs(cCY-pCY)/pCY)*(5.0f+4.0f*(float)Math.abs(cCY-pCY)/pCY)*childHeight*0.2f;
                    //float Y = ((float)Math.abs(cCY-pCY)/pCY)*(1.0f+5.0f*(float)Math.abs(cCY-pCY)/pCY)*childHeight*0.2f;
    				float half = childHeight/2.0f;			
    				int base_Y = (int) ((float)(cCY-pCY)/pCY*Y);
//    				translate_Y = base_Y;
         			//translate_Y = (float)(cCY-pCY)/pCY*Y-((float)(cCY%pCY)/pCY)*child.getHeight()/10;
    				alpha = (float) (1f - (float)Math.abs(cCY-pCY)/pCY*0.3);
    				imageMatrix = t.getMatrix();
    				mCamera.save();
                    //liangchao set Y = 0,Z = 0;
//                    translate_Y = 0;
//                    translate_Z = 0;
//    				mCamera.translate(0, translate_Y, translate_Z);
                    // liangchao do not rotate
//    				mCamera.rotateX(angle);
    				mCamera.getMatrix(imageMatrix);
    				imageMatrix.preTranslate(-child.getWidth()/2 , -child.getHeight()/2);
    				imageMatrix.postTranslate(child.getWidth()/2, child.getHeight()/2);
    				mCamera.restore();
    				TextView tv = (TextView)v.findViewById(R.id.text);
                    //liangchao set alpha =1f;
                    alpha = 1f;
    				tv.setAlpha(alpha);
    				//set gradient color
    				int space = child.getHeight()/10;
    				int topCenter = childHeight*(visibleItem/2-1+visibleItem/2)/2;//2
    				int center = childHeight*visibleItem/2;//3
    				int bottomCenter = childHeight*(visibleItem/2+1+visibleItem/2+2)/2;//4
    				int centerColor = whellView.getCenterTextColot();
    				int itemColor = whellView.getNormalTextColor();
    				int color=0;
    				ArgbEvaluator colorEvaluator = new ArgbEvaluator();
                    float fraction = 0f;

                    tv.getPaint().setFakeBoldText(true);
    				if((cCY>=0&&cCY<=topCenter+space)||(cCY>=bottomCenter-space&&cCY<=child.getHeight()*visibleItem)){
                        color = itemColor;
                        tv.setTextSize(textSize_ItemColor);
                    }else if(cCY>=center-space&&cCY<=center+space) {
    					color = centerColor;

                        tv.setTextSize(textSize_CenterColor);

    				}else if(cCY>center+space&&cCY<bottomCenter-space){
    					if(whellView.getUOffset()>0){
                            fraction = Math.abs(cCY-center-space)/(float)(bottomCenter-center-2*space);
                            tv.setTextSize(textSize_CenterColor-(textSize_CenterColor-textSize_ItemColor)*fraction);
    						//blue--->gray
    						color = (Integer)colorEvaluator.evaluate(Math.abs(cCY-center-space)/(float)(bottomCenter-center-2*space), centerColor, itemColor);
    					}else if(whellView.getUOffset()<0){
    						//gray-->blue
    						fraction = Math.abs(cCY-bottomCenter+space)/(float)(bottomCenter-center-2*space);
                            tv.setTextSize(textSize_ItemColor+(textSize_CenterColor-textSize_ItemColor)*fraction);
    						color = (Integer)colorEvaluator.evaluate(Math.abs(cCY-bottomCenter+space)/(float)(bottomCenter-center-2*space), itemColor, centerColor);
    					}else if(whellView.getUOffset()==0) {
                            color = itemColor;
                            tv.setTextSize(textSize_ItemColor);
                        }
    				}else if(cCY>topCenter+space&&cCY<center-space){
    					if(whellView.getUOffset()>0){
    						//gray--->blue
                            fraction = Math.abs(cCY-topCenter-space)/(float)(center-topCenter-2*space);
                            tv.setTextSize(textSize_ItemColor+(textSize_CenterColor-textSize_ItemColor)*fraction);
    						color = (Integer)colorEvaluator.evaluate(Math.abs(cCY-topCenter-space)/(float)(center-topCenter-2*space), itemColor, centerColor);
    					}
    					else if(whellView.getUOffset()<0){
    						//blue-->gray
                            fraction =Math.abs(cCY-center+space)/(float)(center-topCenter-2*space);
                            tv.setTextSize(textSize_CenterColor-(textSize_CenterColor-textSize_ItemColor)*fraction);
    						color = (Integer)colorEvaluator.evaluate(Math.abs(cCY-center+space)/(float)(center-topCenter-2*space), centerColor, itemColor);
    					}else if(whellView.getUOffset()==0){
                            color = itemColor;
                            tv.setTextSize(textSize_ItemColor);
                        }

    				}

    				tv.setTextColor(color);


    			}//==
    		}//for
        }//vertical
        else {
            int childWidth = whellView.getItemWidth();
            int w = child.getWidth();
            int pCX =whellView.getWidth()/2;
            int padding = (whellView.getWidth()-childWidth*visibleItem)/2;
            for (int i=0; i<cnt; i++) {
                View v = this.getChildAt(i);
                if(v==child) {
                    int cCX = 0;
                    int cCX_new = 0;
                    int currentItem = whellView.getCurrentItem();
                    if(itemCount<visibleItem){
                        cCX = pCX+(currentItem-i)*childWidth+padding;
                    } else {
                        if(currentItem<visibleItem/2&&!whellView.isCyclic()){
                            int diffPos = (visibleItem/2-currentItem)*childWidth;
                            if(uScrollingOffset>0){
                                cCX_new =((child.getRight()-child.getLeft())>>1)+child.getLeft()+uScrollingOffset-child.getWidth();
                                cCX = cCX_new+diffPos+padding;
                            }
                            else {
                                cCX_new =((child.getRight()-child.getLeft())>>1)+child.getLeft()+uScrollingOffset;
                                cCX = cCX_new+diffPos+padding;
                            }
                        } else {
                            if(uScrollingOffset>0)
                                cCX =((child.getRight()-child.getLeft())>>1)+child.getLeft()+uScrollingOffset-child.getWidth()+padding;
                            else
                                cCX =((child.getRight()-child.getLeft())>>1)+child.getLeft()+uScrollingOffset+padding;
                        }
                    }//if(itemCount<visibleItem)
    				/*rotate&translate&alpha*/
                    t.clear();
                    t.setTransformationType(Transformation.TYPE_MATRIX);
//                    angle = -(float)(cCX-pCX)/pCX*(100.0f/2f);
//                    translate_Z = (float)Math.abs(cCX-pCX)/pCX*(1.0f+0.8f*(float)Math.abs(cCX-pCX)/pCX)*childHeight*1.5f;
                    float Y = ((float)Math.abs(cCX-pCX)/pCX)*(5.0f+4.0f*(float)Math.abs(cCX-pCX)/pCX)*childWidth*0.2f;
                    int base_Y = (int) ((float)(cCX-pCX)/pCX*Y);
//                    translate_Y = base_Y;
                    //translate_Y = (float)(cCX-pCX)/pCX*Y-((float)(cCX%pCX)/pCX)*child.getWidth()/10;
                    alpha = (float) (1f - (float)Math.abs(cCX-pCX)/pCX*0.2);
                    imageMatrix = t.getMatrix();
                    mCamera.save();
                    //liangchao set Y = 0,Z = 0;
//                    translate_Y = 0;
//                    translate_Z = 0;
//                    mCamera.translate(-translate_Y, 0, translate_Z);
                    // liangchao do not rotate
//                    mCamera.rotateY(angle);
                    mCamera.getMatrix(imageMatrix);
                    imageMatrix.preTranslate(-child.getWidth()/2 , -child.getHeight()/2);
                    imageMatrix.postTranslate(child.getWidth()/2, child.getHeight()/2);
                    mCamera.restore();
                    TextView tv = (TextView)v.findViewById(R.id.text);
                    //liangchao set alpha =1f;
                    alpha = 1f;
                    tv.setAlpha(alpha);
                    //set gradient color
                    int space = child.getWidth()/10;
                    int leftCenter = childWidth*(visibleItem/2-1+visibleItem/2)/2;//2
                    int center = child.getWidth()*visibleItem/2;//3
                    int rightCenter = childWidth*(visibleItem/2+1+visibleItem/2+2)/2;//4
                    int centerColor = whellView.getCenterTextColot();
                    int itemColor = whellView.getNormalTextColor();
                    int color=0;

                    float fraction = 0f;
                    tv.getPaint().setFakeBoldText(true);
                    ArgbEvaluator colorEvaluator = new ArgbEvaluator();
                    if((cCX>=0&&cCX<=leftCenter+space)||(cCX>=rightCenter-space&&cCX<=child.getHeight()*visibleItem)){
                        color = itemColor;
                        tv.setTextSize(textSize_ItemColor);
                    }else if(cCX>=center-space&&cCX<=center+space) {
                        color = centerColor;

                        tv.setTextSize(textSize_CenterColor);

                    }else if(cCX>center+space&&cCX<rightCenter-space){
                        if(whellView.getUOffset()>0){
                            //blue--->gray
                            fraction = Math.abs(cCX-center-space)/(float)(rightCenter-center-2*space);
                            tv.setTextSize(textSize_CenterColor-(textSize_CenterColor-textSize_ItemColor)*fraction);
                            color = (Integer)colorEvaluator.evaluate(Math.abs(cCX-center-space)/(float)(rightCenter-center-2*space), centerColor, itemColor);
                        }else if(whellView.getUOffset()<0){
                            //gray-->blue
                            fraction = Math.abs(cCX-rightCenter+space)/(float)(rightCenter-center-2*space);
                            tv.setTextSize(textSize_ItemColor+(textSize_CenterColor-textSize_ItemColor)*fraction);
                            color = (Integer)colorEvaluator.evaluate(Math.abs(cCX-rightCenter+space)/(float)(rightCenter-center-2*space), itemColor, centerColor);
                        }else if(whellView.getUOffset()==0){
                            color = itemColor;
                            tv.setTextSize(textSize_ItemColor);
                        }

                    }else if(cCX>leftCenter+space&&cCX<center-space){
                        if(whellView.getUOffset()>0){
                            //gray--->blue
                            fraction = Math.abs(cCX-leftCenter-space)/(float)(center-leftCenter-2*space);
                            tv.setTextSize(textSize_ItemColor+(textSize_CenterColor-textSize_ItemColor)*fraction);
                            color = (Integer)colorEvaluator.evaluate(Math.abs(cCX-leftCenter-space)/(float)(center-leftCenter-2*space), itemColor, centerColor);
                        }
                        else if(whellView.getUOffset()<0){
                            //blue-->gray
                            fraction = Math.abs(cCX-center+space)/(float)(center-leftCenter-2*space);
                            tv.setTextSize(textSize_CenterColor-(textSize_CenterColor-textSize_ItemColor)*fraction);
                            color = (Integer)colorEvaluator.evaluate(Math.abs(cCX-center+space)/(float)(center-leftCenter-2*space), centerColor, itemColor);
                        }else if(whellView.getUOffset()==0){
                            color = itemColor;
                            tv.setTextSize(textSize_ItemColor);
                        }

                    }

                    tv.setTextColor(color);

                }
            }
        }
		return true;
	}

    /*private float getTextSize(int complexUnitDip, int size) {
        Context c = getContext();
        Resources r;

        if (c == null)
            r = Resources.getSystem();
        else
            r = c.getResources();

        return  TypedValue.applyDimension(
                complexUnitDip, size, r.getDisplayMetrics());
    }*/
}