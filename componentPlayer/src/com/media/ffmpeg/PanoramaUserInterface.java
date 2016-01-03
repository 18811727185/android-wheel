package com.media.ffmpeg;

import com.letv.component.player.utils.LogTag;

public class PanoramaUserInterface {
			//向native层传递的,相对初始值(0,0,0),(total_angle_x,total_angle_y,total_angle_z)存储累计偏移量
			public float total_angle_x=0;
			public float total_angle_y=0;
			public float total_angle_z=0;
			//public float gravity_z;
			private final float TOUCH_SCALE_FACTOR = (180.0f / 360)/5;// 角度缩放比例
		    //向native层传递的,相对初始值比例1存储累计缩放比例total_zoom
			public float total_zoom=1;
			public  boolean gravity_yro_valid_info=true; //判断用户设置的重力感应开关是否有效
	
			public PanoramaUserInterface(float total_angle_x,
					float total_angle_y, float total_angle_z, float gravity_z,
					float total_zoom) {
				super();
				this.total_angle_x = total_angle_x;
				this.total_angle_y = total_angle_y;
				this.total_angle_z = total_angle_z;
				//this.gravity_z = gravity_z;
				this.total_zoom = total_zoom;
			}
			
			public PanoramaUserInterface() {
				super();
				// TODO Auto-generated constructor stub
			}

			//屏幕分辨率,可能根据机型适配的需要,需要传入(0.0)点的位置.现在尚未发现异常机型.正常机型左下角的位置为坐标系(0,0)点
			public int setMachineInfomation(float ScreenResolution){
				return 0;
			}
			//单指touch屏幕,1次触发,开始的坐标begin_x,begin_y,结束的坐标end_x,end_y.
			public int setOneFingertouchInfomation(float begin_x,float begin_y,float end_x,float end_y){
				float dy;
				float dx;
				dy = end_x-begin_x;// 计算触控笔Y位移
				dx = end_y-begin_y;// 计算触控笔X位移
//				dy=-dy;
//				dx=-dx;
//				total_angle_y += dy * TOUCH_SCALE_FACTOR/5;// 设置绕y轴旋转角度
//				total_angle_x += dx * TOUCH_SCALE_FACTOR/5;// 设置绕z轴旋转角度
				//**************************************************************
				//**************************************************************
				//根据重力感应判断手机状态和朝向，对应调整触摸屏输入方向
				//*************************************************************
				if(total_angle_z<-135|total_angle_z>135){
					total_angle_y -= dy * TOUCH_SCALE_FACTOR;// 设置绕y轴旋转角度
					total_angle_x -= dx * TOUCH_SCALE_FACTOR;// 设置绕z轴旋转角度
//					
//					total_angle_y -= dx * TOUCH_SCALE_FACTOR;
//					total_angle_x -= dy * TOUCH_SCALE_FACTOR;
				}
				else if(45<=total_angle_z&total_angle_z<=135){
//					total_angle_y -= dy * TOUCH_SCALE_FACTOR;
//					total_angle_x += dx * TOUCH_SCALE_FACTOR;
					total_angle_y -= dx * TOUCH_SCALE_FACTOR;// 设置绕y轴旋转角度
					total_angle_x += dy * TOUCH_SCALE_FACTOR;// 设置绕z轴旋转角度
				}
				else if(-45<=total_angle_z&total_angle_z<45){
					total_angle_y += dy * TOUCH_SCALE_FACTOR;// 设置绕y轴旋转角度
					total_angle_x += dx * TOUCH_SCALE_FACTOR;// 设置绕z轴旋转角度
				}
				else if(-135<=total_angle_z&total_angle_z<-45){
					total_angle_y += dx * TOUCH_SCALE_FACTOR;// 设置绕y轴旋转角度
					total_angle_x -= dy * TOUCH_SCALE_FACTOR;// 设置绕z轴旋转角度
				}
//				if(total_angle_x>=90){
//					total_angle_x=90;
//					}
//				if(total_angle_x<=-90){
//					total_angle_x=-90;
//				}
//				total_angle_x = -total_angle_x;
//				total_angle_y = -total_angle_y;
				return 0;
			}

			//双指touch屏幕,1次触发,开始的两个手指的坐标begin_x0,begin_y0,begin_x1,begin_y1,结束时两个手指的坐标end_x0,end_y0,end_x1,end_y1.
			public int setTwoFingertouchInfomation(float begin_x0,float begin_y0,float begin_x1,float begin_y1,float end_x0,float end_y0,float end_x1,float end_y1){
				
				float _nx = begin_x1 - begin_x0;
				float _ny = begin_y1 - begin_y0;
				float oldDest=(float) Math.sqrt(_nx*_nx+_ny*_ny);
				float _nx1 = end_x1 - end_x0;
				float _ny1 = end_y1 - end_y0;
				float newDest=(float) Math.sqrt(_nx1*_nx1+_ny1*_ny1);
				LogTag.i("oldDest="+oldDest+"newDest"+newDest);
				
				
				if((total_zoom>=0.5)&&(total_zoom<=2)){
					total_zoom*=newDest/oldDest;
					LogTag.i("");
					LogTag.i("(total_zoom>=0.5)&&(total_zoom<=2)####"+"oldDest="+oldDest+"newDest"+newDest);
				}else if(total_zoom<0.5){
					LogTag.i("(total_zoom<0.5)####"+"oldDest="+oldDest+"newDest"+newDest);
					if(newDest>oldDest){
						total_zoom*=newDest/oldDest;
					}else{
						total_zoom=total_zoom;
					}
				}else if(total_zoom>2){
					LogTag.i("(total_zoom>2)####"+"oldDest="+oldDest+"newDest"+newDest);
					if(newDest<oldDest){
						total_zoom*=newDest/oldDest;
					}else{
						total_zoom=total_zoom;
					}
				}else{
					LogTag.i("####"+"oldDest="+oldDest+"newDest"+newDest);
				}
				return 0;
           }
			//传入陀螺仪获取的值gravity_yro_x,gravity_yro_y,gravity_yro_z
			public int setgravity_yroInfomation(float gravity_yro_x,float gravity_yro_y,float gravity_yro_z){
				if(Math.abs(gravity_yro_x)>0.05)
				{
					if(total_angle_z<-135|total_angle_z>135)
					{
						total_angle_y -= gravity_yro_x;
						//triangle.total_angle_y -= gravity_yro_x;
						
					}
					else if(45<=total_angle_z&total_angle_z<=135)
					{
						//triangle.total_angle_y -= gravity_yro_y;
						total_angle_x += gravity_yro_x;
					}
					else if(-45<=total_angle_z&total_angle_z<45)
					{
						total_angle_y += gravity_yro_x;
					}
					else if(-135<=total_angle_z&total_angle_z<-45)
					{
						total_angle_x -= gravity_yro_x;
					}
					}
			    if((Math.abs(gravity_yro_y)>0.05))
				{
			    	if(total_angle_z<-135|total_angle_z>135)
					{
						total_angle_x += gravity_yro_y;
					}
					else if(45<=total_angle_z&total_angle_z<=135)
					{
						total_angle_y += gravity_yro_y;
					}
					else if(-45<=total_angle_z&total_angle_z<45)
					{
						total_angle_x -= gravity_yro_y;
					}
					else if(-135<=total_angle_z&total_angle_z<-45)
					{
						total_angle_y -= gravity_yro_y;
					}
				}

				return 0;
			}
			//传入重力感应器获取的值gravity_x,gravity_y,gravity_z
			public int setGravityInfomation(float gravity_x,float gravity_y,float gravity_z){
			     float g=0.0f;
				if(gravity_y<=0)
				{
					if(gravity_x<=0)
					{g=(float) ((Math.asin(gravity_y/Math.sqrt(gravity_x*gravity_x+gravity_y*gravity_y
							)))*180/Math.PI);}
					else{g=-180-(float) ((Math.asin(gravity_y/Math.sqrt(gravity_x*gravity_x+gravity_y*gravity_y)))*180/Math.PI);}
				}
				else if(gravity_y>0)
				{
					if(gravity_x<=0)
					{g=(float) ((Math.asin(gravity_y/Math.sqrt(gravity_x*gravity_x+gravity_y*gravity_y)))*180/Math.PI);}
					else{g=180-(float) ((Math.asin(gravity_y/Math.sqrt(gravity_x*gravity_x+gravity_y*gravity_y)))*180/Math.PI);}
				}
				
//			    if(Math.abs( gravity_z)>9){
//			    	total_angle_z=total_angle_z;
//				}else{
//					total_angle_z=-g;
//				}

			if(Math.abs( gravity_x)>1 || Math.abs(gravity_y) > 1){
				
					//total_angle_z=-g;
				    if(-g<0)
				    {total_angle_z=-g+180;}
				    else if(-g>0)
				    {total_angle_z=-g-180;}
			}else{
//		    	total_angle_z=total_angle_z;
			}
			     

				return 0;
			}
			//传入陀螺仪是否有效的开关状态
			public int setgravity_yroValidInfomation(boolean gravityValid){
				gravity_yro_valid_info=gravityValid;
				return 0;
			}
			//用户touch屏幕上的归零按钮,要求旋转角度归零
			public int setAngleInit(){
				return 0;
			}
			//用户touch缩放
			public int setTwoFingerZoom(float zoom){
				total_zoom=zoom;
				return 0;
			}
}
