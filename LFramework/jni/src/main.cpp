#include <jni.h>
#include "LuaApp.h"
#include <android/log.h>

#define  LOG_TAG    "main"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

#define LUA_LAUNCH "\n\
	print = function(...)\n\
		local args={...};local array={};\n\
		for k,v in pairs(args) do\n\
			table.insert(array,tostring(v));\n\
		end\n\
		sys.log(table.concat(array,\" \"));\n\
	end\n\
	sys.version=LFrameworkVersion;\n\
	sys.platform=LPlatform;\n\
	sys.ns={};\n\
	sys.gc=function() collectgarbage('collect') end\n\
	sys.bindRef=function(addr,name) \
		local ref = {__NSClass=true,__NSBack=tonumber(addr) or 0};\
		ref.__NSRef=sys.retainRef(ref.__NSBack);\
		local prox = newproxy(true)\
		local lib = {__bindProxy=prox,__index = function(t,k)return function(i,...) return sys.call('',k,t,...) end;end,__gc = function()sys.releaseRef(ref.__NSRef);end}\
		getmetatable(prox).__gc = lib.__gc;\
		setmetatable(ref,lib);\
		if name then sys.ns[name]=ref; end\
		return ref;\
	end\n\
	sys.callbacks={id=0;}\
	sys.callback=function(f)\
		sys.callbacks.id = sys.callbacks.id + 1;\
		sys.callbacks[sys.callbacks.id]=function(e,o) f(e,o ~= 0 and sys.bindRef(o) or o) end;\
		local rid = sys.callbacks.id;\
		local result = sys.new('com.ry.lframework.LCallBack',rid);\
		result.proxy = newproxy(true)\
		getmetatable(result.proxy).__gc = function() sys.callbacks[rid]=nil; end;\
		return result;\
	end\
	local _syscall=sys.call;sys.call=function(...) local r=_syscall(...);if r then return sys.bindRef(r); end end\
	sys.float=function(v) return sys.new('java.lang.Float',v) end\
	sys.int=function(v) return sys.new('java.lang.Integer',v) end\
	sys.long=function(v) return sys.new('java.lang.Long',v) end\
	sys.double=function(v) return sys.new('java.lang.Double',v) end\
	sys.byte=function(v) return sys.new('java.lang.Byte',v) end\
	sys.char=function(v) return sys.new('java.lang.Character',v) end\
	sys.new=function(clazz,...) return sys.call(clazz,'new',nil,...); end\n\
	print(string.format('LFramework %s on %s > start...',sys.version or '*',sys.platform or '*'));\n"

#define LUA_VM_LAUNCH "\n\
	sys.ns = {};\
	sys.bindRef=function(addr,name) \
		local ref = {__NSClass=true,__NSBack=tonumber(addr) or 0};\
		ref.__NSRef=sys.retainRef(ref.__NSBack);\
		local prox = newproxy(true)\
		local lib = {__bindProxy=prox,__index = function(t,k)return function(i,...) return sys.call('',k,t,...) end;end,__gc = function()sys.releaseRef(ref.__NSRef);end}\
		getmetatable(prox).__gc = lib.__gc;\
		setmetatable(ref,lib);\
		if name then sys.ns[name]=ref; end\
		return ref;\
	end\
	print('lvm...');"

extern "C"{
JNIEXPORT void JNICALL Java_com_ry_lframework_LFramework_init(JNIEnv* env,jclass object,jobject assetsManager){
	LuaApp::getInstance()->setJNIEnv(env,assetsManager);
	LuaApp::getInstance()->execute(LUA_VM_LAUNCH);
}
JNIEXPORT jint JNICALL Java_com_ry_lframework_LFramework_execute(JNIEnv* env,jclass object,jstring string){
	const char* data = env->GetStringUTFChars(string, NULL);
	int result = LuaApp::getInstance()->execute(data);
	env->ReleaseStringUTFChars(string, data);
	return result;
}
JNIEXPORT jint JNICALL Java_com_ry_lframework_LFramework_executeFile(JNIEnv* env,jclass object,jstring string){
	const char* data = env->GetStringUTFChars(string, NULL);
	int result = LuaApp::getInstance()->executeFile(data);
	env->ReleaseStringUTFChars(string, data);
	return result;
}
JNIEXPORT void JNICALL Java_com_ry_lframework_LFramework_reg(JNIEnv* env,jclass object,jstring ns,jobject obj){
	const char* data = env->GetStringUTFChars(ns, NULL);
	LuaApp::getInstance()->reg(data,(void*)obj);
	env->ReleaseStringUTFChars(ns, data);
}
}
