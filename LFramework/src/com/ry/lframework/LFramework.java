package com.ry.lframework;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import com.ry.lframework.ui.Image;
import com.ry.lframework.ui.Label;
import com.ry.lframework.ui.Layout;
import com.ry.lframework.ui.Scene;
import com.ry.lframework.ui.Window;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;

/**
 * jclass clazz = (*env)->FindClass(env, "com/mwp/ccalljava2/MainActivity");
    //jmethodID   (*GetMethodID)(JNIEnv*, jclass, const char*, const char*);
    jmethodID methodID = (*env)->GetMethodID(env, clazz, "show", "(Ljava/lang/String;)V");
    //void        (*CallVoidMethod)(JNIEnv*, jobject, jmethodID, ...);
    (*env)->CallVoidMethod(env,obj,methodID, (*env)->NewStringUTF(env, ""))
    
    
	JNIEXPORT void JNICALL Java_com_ry_lframework_LFramework_init(JNIEnv* env,jobject object){
		LuaApp::getInstance()->execute(LUA_LAUNCH);
	}
 *
 */
 public class LFramework extends Activity {
	static{
		System.loadLibrary("LFramework");
	}

	public static int displayWidth;
	public static int displayHeight;
	private static LFramework instance;
	private static HashMap<String, Vector<Method>> cacheCaller = new HashMap<String, Vector<Method>>();
	private Scene root;
	@SuppressLint("NewApi") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(instance == null)
        	instance = this;
        int[] screenSize = getScreenSize();
        displayWidth = screenSize[0];
        displayHeight = screenSize[1];
        root = new Scene(this);
        setContentView(root.getView());
        
        LFramework.init(getAssets());
        LFramework.reg("app", this);
        LFramework.reg("displayWidth", screenSize[0]);
        LFramework.reg("displayHeight", screenSize[1]);
        LFramework.reg("writePath", getFilesDir().getAbsolutePath());
        LFramework.reg("stage", root);
        
        executeBuiltin("_builtin_");
//        
//        //test layout
//        Layout l = new Layout(this, String.format("color=%d&width=100&height=100&x=50&y=50", 0xFF000000));
//        root.addChild(l);
//        System.out.println(l.getX()+"x"+l.getY()+"x"+l.getWidth()+"x"+l.getHeight());
//        l = new Layout(this, String.format("color=%d&width=300&height=300&x=70&y=70", 0xff00ffff));
//        root.addChild(l);
//        System.out.println(l.getX()+"x"+l.getY()+"x"+l.getWidth()+"x"+l.getHeight());
//
//        Label t = new Label(this, String.format("color=%d&width=100&height=100&x=70&y=70&text='HelloWorld'&fontSize=20&autoSize=true&bgcolor=%s", 0xFF000000,0x33666666));
//        root.addChild(t);
//        System.out.println(t.getWidth() + "xx" + t.getHeight());
//        t.setText("Impks---------1\nHHHHHHH\nwer.$#");
//        t.setColor(0xffff0000);
//        System.out.println(t.getWidth() + "xx" + t.getHeight());
//        
//        Label t2 = new Label(this, String.format("color=%d&width=100&height=100&x=10&y=10&text='HelloWorld'&fontSize=20&autoSize=true", 0xFF000000));
//        t.addChild(t2);
//        
//        t2.getView().setBackgroundColor(0xFF888888);
//        t2.setListener(new LCallBack(1));
//        t.setListener(new LCallBack(1));
//        
//        TranslateAnimation ta = new TranslateAnimation(0, 0, 100, 100, 0, 0, 0, 0);
//        ta.setDuration(3000);
//        t2.getView().startAnimation(ta);
//        
//        Scene s = new Scene(this);
//        s.setColor(0x66000000);
//        root.addChild(s);

        w = new Window(this);
        w.open(true);

        String s = "local e=sys.ns.app:getW(); local c = os.clock();" +
        		"for i=1,200 do " +
        		"local f = string.format([[width=100&height=100&x=%d&y=%d&src='add.png'&autoSize=true]],i%300,i%400);" +
        		"if i == 1 then print(f); end;" +
        		"local img = sys.new('com.ry.lframework.ui.Image',sys.ns.app,f);" +
        		"e:addChild(img);" +
        		"sys.ns.app:test(i);" +
        		"end print('>>>',os.clock()-c)";
        System.out.println(s);
        LFramework.execute(s);
        
        
//        long now = System.currentTimeMillis();
//        for(int i=0;i<1000;++i){
//			try {
//				call("", "test", this, new Object[]{new Integer(i+1)});
//				w.addChild(new Image(this, String.format("color=%d&width=100&height=100&x=%d&y=%d&src='add.png'&autoSize=true", 0xFF00FF00,i%300,i%400)));
//			} catch (Throwable t) {
//			}
//        }
//        System.out.println("--->"+(System.currentTimeMillis()-now));
        
	}
	private void executeBuiltin(String string) {
		try {
			InputStream in = getAssets().open(string);
			byte[] data = new byte[in.available()];
			in.read(data);
			execute(new String(data));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public Window getW(){
		return w;
	}
	private Window w;
	public static LFramework getInstance(){
		return instance;
	}
	
	public void test(int i){
		if(i % 100 == 0)
			System.out.println("ddd" + i);
	}
	
	public Scene getScene(){ return root;}
	
	@SuppressLint("NewApi")
	private int[] getScreenSize(){
        WindowManager manager = this.getWindowManager();
		DisplayMetrics outMetrics = new DisplayMetrics();
		manager.getDefaultDisplay().getMetrics(outMetrics);
		return new int[]{outMetrics.widthPixels, outMetrics.heightPixels};
	}
	
	public static native void init(AssetManager assetManager);
	public static native int executeFile(String file);
	public static native int execute(String code);
	public static native int reg(String name,Object instance);//注册常态对象，到lua使用池

	public static Object call(String clazz,String method,Object ins,Object[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		Object result = null;
		if(method == null)
			return null;
		try {
			if(ins == null){
				Class<?> c = null;
				String[] inners = clazz.split("@");
				for(int i=0;i<inners.length;++i){
					if(c == null){
						c = Class.forName(inners[i]);
					}else{
						Class<?>[] array = c.getDeclaredClasses();
						for(int j=0;j<array.length;++j){
							if(array[j].getName().equals(c.getName() + "$" + inners[i])){
								c = array[j];
								break;
							}
						}
					}
				}
				
				if(method.equals("new")){
					result = ins(c,args);
				}else{
					result = callStaticVoid(c, method, args);
				}
			}else{
				result = callVoid(ins,method,args);
			}
		} catch (Throwable t) {
			System.out.println("[Err]" + clazz + "->" + method + "|" + (ins == null ? "" : ins));
			t.printStackTrace();
		}
		return result;
	}

	private static Object callStaticVoid(Class<?> clazz, String method,Object ...args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		return callVoid(clazz,method,args);
	}
	private static boolean isInterfacesOf(Class<?>[] classes, Class<?> as){
		for(int j=0;j<classes.length;++j){
			Class<?> c = classes[j];
			if(c.equals(as))
				return true;
			if(isInterfacesOf(c.getInterfaces(),as))
				return true;
		}
		return false;
	}
	private static boolean isSignOfTypes(Class<?>[] classes, Object[] args){
		if(classes == null || classes.length == 0)
			return true;
		for(int i=0;i<classes.length;++i){
			if(args[i] == null || classes[i] == Object.class)
				continue;
			Class<?> a = args[i].getClass();
			Class<?> b = classes[i];
			if(a.equals(b))
				continue;
			if(b == boolean.class && a == Boolean.class)
				continue;
			if(b == int.class && a == Integer.class)
				continue;
			if(b == float.class && a == Float.class)
				continue;
			if(b == double.class && a == Double.class)
				continue;
			if(b == long.class && a == Long.class)
				continue;
			if(b == byte.class && a == Byte.class)
				continue;
			if(b == char.class && a == Character.class)
				continue;
			//check supers
			boolean isUpser = false;
			Class<?> c = a;
			while(true){
				c = c.getSuperclass();
				if(c == Object.class)break;
				if(c.equals(b)){
					isUpser = true;
					break;
				}
			}
			if(isUpser)
				continue;

			if(!isInterfacesOf(a.getInterfaces(),b))
				return false;
		}
		return true;
	}
	private static Object callVoid(Object object, String method,Object ...args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		if(object == null)
			return null;
		Class<?> clazz = null;
		if(!(object instanceof Class<?>))
			clazz = object.getClass();
		else
			clazz = (Class<?>) object;
		
		String ckey = clazz.getName() + "@" + method;
		Vector<Method> array = cacheCaller.get(ckey);
		Method m = null;
		if(array != null){
			Method t = null;
			for(int i=0;i<array.size();++i){
				t = array.get(i);
				if(t.getParameterTypes().length == args.length && isSignOfTypes(t.getParameterTypes(),args)){
					m = t;
					break;
				}
			}
		}
		if(m == null){
			Method[] ms = clazz.getMethods();
			for(int i=0;i<ms.length;++i){
				if(ms[i].getName().equals(method) && ms[i].getParameterTypes().length == args.length  && isSignOfTypes(ms[i].getParameterTypes(),args)){
					m = ms[i];
					if(array == null){
						array = new Vector<Method>();
						array.add(m);
						cacheCaller.put(ckey, array);
					}
					break;
				}
			}
		}

		if(m == null)
			return null;
		
		return m.invoke(object, args);
	}
	private static Object ins(Class<?> clazz,Object ...args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object instance = null;
		if(args == null || args.length == 0){
			return clazz.newInstance();
		}
		@SuppressWarnings("rawtypes")
		Constructor[] cs = clazz.getDeclaredConstructors();
		for(int i=0;i<cs.length;++i){
			if(cs[i].getParameterTypes().length == args.length && isSignOfTypes(cs[i].getParameterTypes(),args))
				return cs[i].newInstance(args);
		}
		return instance;
	}
}
