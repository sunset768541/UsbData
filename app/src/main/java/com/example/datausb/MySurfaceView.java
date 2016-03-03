package com.example.datausb;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.view.MotionEvent;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class MySurfaceView extends GLSurfaceView 
{
	private final float TOUCH_SCALE_FACTOR = 180.0f/320;//角度缩放比例
    private SceneRenderer mRenderer;//场景渲染器    
    RotateThread rthread;
    private float mPreviousY;//上次的触控位置Y坐标
    private float mPreviousX;//上次的触控位置X坐标
    
    int textureId;//系统分配的纹理id
	
	public MySurfaceView(Context context) {
        super(context);
        this.setEGLContextClientVersion(2); //设置使用OPENGL ES2.0
        mRenderer = new SceneRenderer();	//创建场景渲染器
        setRenderer(mRenderer);				//设置渲染器		        
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);//设置渲染模式为主动渲染   
    }
	
	//触摸事件回调方法
    @Override 
    public boolean onTouchEvent(MotionEvent e) 
    {
        float y = e.getY();
        float x = e.getX();
        switch (e.getAction()) {
        case MotionEvent.ACTION_MOVE:
            float dy = y - mPreviousY;//计算触控笔Y位移
            float dx = x - mPreviousX;//计算触控笔X位移
            mRenderer.yAngle += dx * TOUCH_SCALE_FACTOR;//设置沿x轴旋转角度
            mRenderer.xAngle+= dy * TOUCH_SCALE_FACTOR;//设置沿z轴旋转角度
            requestRender();//重绘画面
        }
        mPreviousY = y;//记录触控笔位置
        mPreviousX = x;//记录触控笔位置
        return true;
    }

	private class SceneRenderer implements Renderer
    {  
		float yAngle;//绕Y轴旋转的角度
    	float xAngle; //绕Z轴旋转的角度
    	//从指定的obj文件中加载对象
		LoadedObjectVertexNormalTexture lovo;
		LoadedObjectVertexNormalTextureLINE lovo1;

        public void onDrawFrame(GL10 gl) 
        { 
        	//清除深度缓冲与颜色缓冲
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

            //坐标系推远
            MatrixState.pushMatrix();
            MatrixState.translate(0, -2f, -25f);   //ch.obj
            //绕Y轴、Z轴旋转
            MatrixState.rotate(yAngle, 0, 1, 0);
            MatrixState.rotate(xAngle, 1, 0, 0);
            //若加载的物体部位空则绘制物体
            if(lovo1!=null)
            {
            	lovo.drawSelf(textureId);//画笔2
            lovo1.drawSelf();//画笔一
            }
            MatrixState.popMatrix();                  
        }  

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            //设置视窗大小及位置 
        	GLES20.glViewport(0, 0, width, height); 
        	//计算GLSurfaceView的宽高比
            float ratio = (float) width / height;
            //调用此方法计算产生透视投影矩阵
            MatrixState.setProjectFrustum(-ratio, ratio, -1, 1, 2, 100);
            //调用此方法产生摄像机9参数位置矩阵
            MatrixState.setCamera(0,0,0,0f,0f,-1f,0f,1.0f,0.0f);
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) 
        {
            //设置屏幕背景色RGBA
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            //打开深度检测
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            //打开背面剪裁   
            GLES20.glEnable(GLES20.GL_CULL_FACE);
            //初始化变换矩阵
            MatrixState.setInitStack();
            //初始化光源位置
            MatrixState.setLightLocation(40, 10, 20);
            //加载要绘制的物体
          lovo=LoadUtil.loadFromFile("ch_t.obj", MySurfaceView.this.getResources(),MySurfaceView.this);
            lovo1=LoadUtilLINE.loadFromFile("wy.obj", MySurfaceView.this.getResources(),MySurfaceView.this);
            rthread=new RotateThread();
            rthread.start();
            //加载纹理
            textureId=initTexture(R.drawable.ghxp);
        }
    }
  	public int initTexture(int drawableId)//textureId
	{
		//生成纹理ID
		int[] textures = new int[1];
		//创建纹理，用函数 glGenTextures() 完成，函数返回新创建的纹理的 ID。此函数可以创建 n 个纹理，并将纹理ID 放在 textures 中:
		GLES20.glGenTextures
		(
				1,          //产生的纹理id的数量
				textures,   //纹理id的数组
				0           //偏移量
		);    
		int textureId=textures[0];    
		//设置过滤器
		/*
		一般我们设置两个， 一个放大器的: GL_TEXTURE_MAG_FILTER, 一个缩小器的: GL_TEXTURE_MIN_FILTER.
		当它比放大得原始的纹理大 ( GL_TEXTURE_MAG_FILTER )或缩小得比原始得纹理小( GL_TEXTURE_MIN_FILTER )时OpenGL采用的滤波方式。
		通常这两种情况下我都采用 GL_LINEAR 。这使得纹理从很远处到离屏幕很近时都平滑显示。使用 GL_LINEAR 需要CPU和显卡做更多的运算。
		如果您的机器很慢，您也许应该采用 GL_NEAREST 。
		过滤的纹理在放大的时候，看起来斑驳的很(马赛克)。您也可以结合这两种滤波方式。在近处时使用 GL_LINEAR ，远处时 GL_NEAREST 。
		**/
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_REPEAT);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_REPEAT);
        
        //通过输入流加载图片===============begin===================
        InputStream is = this.getResources().openRawResource(drawableId);
        Bitmap bitmapTmp;
        try 
        {
        	bitmapTmp = BitmapFactory.decodeStream(is);
        } 
        finally 
        {
            try 
            {
                is.close();
            } 
            catch(IOException e) 
            {
                e.printStackTrace();
            }
        }
        //通过输入流加载图片===============end===================== 
        /*
        OpenGL 提供了三个函数来指定纹理: glTexImage1D(), glTexImage2D(), glTexImage3D().
        这三个版本用于相应维数的纹理，我们用到的是 2D 版本: glTexImage2D().
				void glTexImage2D (int target, int level, int internalformat, int width, int
				height, int border, int format, int type, Buffer pixels) 参数过多，可以使用 GLUtils 中的
				texImage2D() 函数，好处是直接将 Bitmap 数据作为参数:
				void texImage2D (int target, int level, Bitmap bitmap, int border) 参数:
				target 操作的目标类型，设为 GL_TEXTURE_2D 即可 level 纹理的级别，本节不涉及，设为 0 即可 bitmap 图像 border
				边框，一般设为0
        **/
	   	GLUtils.texImage2D
	    (
	    		GLES20.GL_TEXTURE_2D, //纹理类型
	     		0, 
	     		GLUtils.getInternalFormat(bitmapTmp), 
	     		bitmapTmp, //纹理图像
	     		GLUtils.getType(bitmapTmp), 
	     		0 //纹理边框尺寸
	     );
	    bitmapTmp.recycle(); 		  //纹理加载成功后释放图片
        return textureId;
	}
    public class RotateThread extends Thread
    {
        public boolean flag=true;
        @Override
        public void run()
        {
            while(flag)
            {
                Random rand = new Random();
                float [] colors=new float[( mRenderer.lovo.vCount*4)];
                for(int i=0;i<colors.length;i++){
                    colors[i]=rand.nextFloat();
                }

                mRenderer.lovo1.co=colors;
                try
                {
                    Thread.sleep(200);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
