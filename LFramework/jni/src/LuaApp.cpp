#include <LuaApp.h>
#include <android/log.h>
#include <vector>
#include "Platform.h"

#include <sys/types.h>  
#include <stdlib.h>  
#include <android/asset_manager_jni.h>  
#include <android/asset_manager.h>  

#define  NS_CLASS    "com/ry/lframework/LFramework"
#define  NS_CLASS_METHOD    "call"
#define  LOG_TAG    "LUA"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

static JNIEnv* env = 0;
static JavaVM *g_JavaVM = 0;
static jclass callClass = 0;
static jobject assets = 0;
static jmethodID callMethod = 0;

static JNIEnv *getEnv(){
	if(!g_JavaVM){
		return 0;
	}
	int status;
	JNIEnv *envnow = 0;
	status = g_JavaVM->GetEnv((void **)&envnow, JNI_VERSION_1_4);
	if(status < 0){
		status = g_JavaVM->AttachCurrentThread(&envnow, NULL);
		if(status < 0){ 
            return 0;
        }
    }
    return envnow;
}  
//-----------------------------------------------
extern "C"{
JNIEXPORT void JNICALL Java_com_ry_lframework_LCallBack_onCallImpl(JNIEnv* env,jobject ins,jint cbid,jstring event,jobject args){
	lua_State* vm = LuaApp::getInstance()->getLuaState();
	if(!vm)return;
	
	int pc = lua_gettop(vm);
	
	const char* data = env->GetStringUTFChars(event, NULL);
	
	lua_getglobal(vm,"sys");
	lua_getfield(vm,pc+1,"callbacks");
	lua_rawgeti(vm,pc+2,cbid);
	int t = lua_type(vm,pc+3);
	
	//LOGD("will dispatch event%d %s have pc %d stocktype%d",cbid,data,pc,t);
	
	if(t == LUA_TFUNCTION){
		unsigned long long n = (unsigned long long)args;
		lua_pushstring(vm,data);
		lua_pushnumber(vm,n);
		lua_pcall(vm, 2, 0, 0);
	}
	lua_pop(vm,lua_gettop(vm));
	
	env->ReleaseStringUTFChars(event, data);
}
}
//-----------------------------------------------
void setLuaNumber(lua_State* vm,const char* name,int num){
	lua_pushnumber(vm,num);
	lua_setglobal(vm,name);
}
void setLuaString(lua_State* vm,const char* name,const char* value){
	lua_pushstring(vm,value);
	lua_setglobal(vm,name);
}
jobject getJavaObject(lua_State* vm,int tableTop){
	if(!lua_istable(vm, tableTop))
		return 0;
		
	int argsn = lua_gettop(vm);
	lua_getfield(vm,tableTop,"__NSRef");
	unsigned long long n = lua_tonumber(vm,argsn+1);
	lua_pop(vm,1);
	
	return (jobject)n;
}
jobject luaValueFormat(lua_State* L,JNIEnv* env,int index,int arrayLen,bool isArray,std::vector<jobject>& localObjects){
	jobject result = 0;
	
	if(isArray){
		jclass argsClass = env->FindClass("java/lang/Object");
		localObjects.push_back(argsClass);
		result = env->NewObjectArray(arrayLen,argsClass,NULL);
		for(int i=0;i<arrayLen;++i){
			int ltype = lua_type(L,i+index);
			if(ltype == LUA_TNUMBER){
				int64_t fvalue = (int64_t)lua_tonumber(L,i+index);
				jclass tclass = env->FindClass("java/lang/Integer");
				jobject tmp = env->NewObject(tclass,env->GetMethodID(tclass,"<init>","(I)V"),(int)fvalue);
				localObjects.push_back(tmp);
				localObjects.push_back(tclass);
				env->SetObjectArrayElement((jobjectArray)result,i,tmp);
			}else if(ltype == LUA_TNIL){
				env->SetObjectArrayElement((jobjectArray)result,i,0); 
			}else if(ltype == LUA_TBOOLEAN){
				jclass tclass = env->FindClass("java/lang/Boolean");
				jobject tmp = env->NewObject(tclass,env->GetMethodID(tclass,"<init>","(Z)V"),lua_toboolean(L,i+index));
				localObjects.push_back(tmp);
				localObjects.push_back(tclass);
				env->SetObjectArrayElement((jobjectArray)result,i,tmp); 
			}else if(ltype == LUA_TSTRING){
				const char* str = lua_tostring(L,i+index);
				jstring tmp = env->NewStringUTF(str == 0 ? "" : str);
				localObjects.push_back(tmp);
				env->SetObjectArrayElement((jobjectArray)result,i,tmp); 
			}else if(ltype == LUA_TTABLE){
				
				int argsn = lua_gettop(L);
				lua_getfield(L,i+index,"__NSClass");
				if(lua_isboolean(L,argsn+1)){//isObject
					jobject tmp = getJavaObject(L,i+index);
					env->SetObjectArrayElement((jobjectArray)result,i,tmp); 
				}else{//isArray
					int len = luaL_getn(L,i+index);
					for(int j=0;j<len;++j){
						lua_rawgeti(L, i+index, j+1);
					}
					jobject tmp = luaValueFormat(L,env,argsn+2,len,true,localObjects);
					localObjects.push_back(tmp);
					env->SetObjectArrayElement((jobjectArray)result,i,tmp); 
					lua_pop(L,len);
				}
				lua_pop(L,1);
				
			}else{
				env->SetObjectArrayElement((jobjectArray)result,i,0); 
			}
		}
	}else{//javaObject
		result = getJavaObject(L,index);
	}
	return result;
}
static int AndroidLog(lua_State* L){
	const char* value = lua_tostring(L, -1);
	if(value){
		LOGD("[Lua] %s",value);
	}
	return 0;
}
static int AndroidRetain(lua_State* L){
	if(!env){
		LOGD("call no env %s","JNIEnv");
		return 0;
	}
	unsigned long long value = lua_tonumber(L, -1);
	if(value){
		jobject jv = (jobject)value;
		unsigned long long result = (unsigned long long)env->NewGlobalRef(jv);
		lua_pushnumber(L,result);
		return 1;
	}
	return 0;
}
static int AndroidOpenAssets(lua_State* L){
	if(!env){
		LOGD("call no env %s","JNIEnv");
		return 0;
	}
	const char* value = lua_tostring(L, -1);
	if(value){
		AAssetManager* mgr = AAssetManager_fromJava(env,assets);
		if(!mgr)
			return 0;
		AAsset* asset = AAssetManager_open(mgr, value, AASSET_MODE_UNKNOWN);
		if(!asset)
			return 0;
		off_t bufferSize = AAsset_getLength(asset);
		char* buffer = (char *)malloc(bufferSize+1);
		buffer[bufferSize] = 0;
		int numBytesRead = AAsset_read(asset, buffer, bufferSize);
		lua_pushlstring(L,buffer,bufferSize);
		free(buffer);
		AAsset_close(asset);
		return 1;
	}
	return 0;
}
static int AndroidImportData(lua_State* L){
	if(!env){
		LOGD("call no env %s","JNIEnv");
		return 0;
	}
	const char* value = lua_tostring(L, -3);
	int len = lua_tonumber(L, -2);
	const char* name = lua_tostring(L, -1);
	if(value && name){
	
    	int top = lua_gettop(L);
        lua_getglobal(L, "package");
        lua_getfield(L, top+1, "preload");
        
		int r = luaL_loadbuffer(L, (char*)value, len, name);
		if (r){
	        switch (r){
	            case LUA_ERRSYNTAX:
	                LOGD("[LUA ERROR] load \"%s\", error: syntax error during pre-compilation.", name);
	                break;
	
	            case LUA_ERRMEM:
	                LOGD("[LUA ERROR] load \"%s\", error: memory allocation error.", name);
	                break;
	
	            case LUA_ERRFILE:
	                LOGD("[LUA ERROR] load \"%s\", error: cannot open/read file.", name);
	                break;
	
	            default:
	                LOGD("[LUA ERROR] load \"%s\", error: unknown.", name);
	        }
	    }
	    if(r == 0){
	    	lua_setfield(L, top+2, name);
	    	lua_pop(L,lua_gettop(L));
	    }
	}
	return 0;
}
static int AndroidRelease(lua_State* L){
	if(!env){
		LOGD("call no env %s","JNIEnv");
		return 0;
	}
	unsigned long long value = lua_tonumber(L, -1);
	if(value){
		//LOGD("release object %lld",value);
		jobject jv = (jobject)value;
		env->DeleteGlobalRef(jv);
	}
	return 0;
}
static int AndroidToString(lua_State* L){
	if(!env){
		LOGD("call no env %s","JNIEnv");
		return 0;
	}
	
	jobject jv = (jobject)getJavaObject(L,1);
	if(jv){
		jclass clazz = env->GetObjectClass(jv);
		jmethodID mId = env->GetMethodID(clazz, "toString", "()Ljava/lang/String;");    
		jstring packName = static_cast<jstring>(env->CallObjectMethod(jv, mId));
		
		const char* data = env->GetStringUTFChars(packName, NULL);
		lua_pushstring(L,data);
		env->ReleaseStringUTFChars(packName, data);
		env->DeleteLocalRef(clazz);
		
		return 1;
	}
	return 0;
}
static int AndroidCall(lua_State* L){
	//env = getEnv();
	if(!env){
		LOGD("call no env %s","JNIEnv");
		return 0;
	}
	int argsn = lua_gettop(L);
	if(argsn < 3){
		LOGD("call args error %d need class name and method name.",argsn);
		return 0;
	}
	
	const char* nsclass = lua_tostring(L,1);
	const char* nsmethod = lua_tostring(L,2);
	jobject ins = getJavaObject(L,3);
	
	int clen = strlen(nsclass);
	
	jstring nclazz = clen == 0 ? 0 : env->NewStringUTF(nsclass);
	jstring nmethod = env->NewStringUTF(nsmethod);
	jobject result = 0;
	
	///////////////////////////////////////////
	int margs = argsn - 3;
	std::vector<jobject> localObjects;
	jobject args = luaValueFormat(L,env,4,margs,true,localObjects);
	
	result = env->CallStaticObjectMethod(callClass,callMethod,nclazz,nmethod,ins,args);
	///////////////////////////////////////////
	
	if(nclazz)env->DeleteLocalRef(nclazz);
	env->DeleteLocalRef(nmethod);
	env->DeleteLocalRef(args);
	
	for(int i=0;i<localObjects.size();++i){
		env->DeleteLocalRef(localObjects[i]);
	}
	
	argsn = lua_gettop(L);
	lua_pop(L,argsn);
	
	if(!result)
		return 0;
	
	unsigned long long val = (unsigned long long)result;
	/**
	lua_getglobal(L,"sys");
	lua_getfield(L,1,"bindRef");
	lua_pushnumber(L,val);
	lua_pcall(L, 1, 1, 0);
	**/
	
	lua_pushnumber(L,val);
	
	return 1;
}
static LuaApp* instance = 0;

void LuaApp::initLuaVm(){
	vm = lua_open();
    luaL_openlibs(vm);
	setLuaString(vm,"LFrameworkVersion",LFrameworkVersion);
#if TARGET_PLATFORM == PLATFORM_UNKNOWN 
	setLuaString(vm,"LPlatform","unknown");
#elif TARGET_PLATFORM == PLATFORM_IOS
	setLuaString(vm,"LPlatform","ios");
#elif TARGET_PLATFORM == PLATFORM_ANDROID
	setLuaString(vm,"LPlatform","android");
#elif TARGET_PLATFORM == PLATFORM_WIN32
	setLuaString(vm,"LPlatform","windows");
#elif TARGET_PLATFORM == PLATFORM_MAC
	setLuaString(vm,"LPlatform","mac");
#endif

	static const struct luaL_Reg funcs[] = {
	    {"log", AndroidLog},
	    {"call", AndroidCall},
	    {"openAssets", AndroidOpenAssets},
	    {"importData", AndroidImportData},
	    {"retainRef", AndroidRetain},
	    {"releaseRef", AndroidRelease},
	    {"toString", AndroidToString},
	    {0, 0}
	};
	luaL_register(vm, "sys", funcs);
}

LuaApp* LuaApp::getInstance(){
	if(!instance){
		instance = new LuaApp();
		instance->initLuaVm();
	}
	return instance;
}

void LuaApp::release(){
	if(instance){
		if(instance->vm){
			lua_close(instance->vm);
			instance->vm = 0;
		}

		delete instance;
		instance = 0;
	}
}
void LuaApp::setJNIEnv(JNIEnv* e,jobject assetsManager){
	env = e;
	env->GetJavaVM(&g_JavaVM);
	
	assets = env->NewGlobalRef(assetsManager);
	callClass = (jclass)env->NewGlobalRef(env->FindClass(NS_CLASS));
	if(!callClass){
		LOGD("call not found ns class %s",NS_CLASS);
	}else{
		callMethod = env->GetStaticMethodID(callClass, NS_CLASS_METHOD, "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
	}
}
int LuaApp::execute(const char* code){
	if(vm){
		int result = luaL_dostring(vm,code);
		if(result){
			AndroidLog(vm);
		}
		return result;
	}
	return -1;
}
int LuaApp::executeFile(const char* file){
	if(vm){
		int pc = lua_gettop(vm);
		lua_getglobal(vm,"sys");
		lua_getfield(vm,pc+1,"inc");
		lua_pushstring(vm,file);
		lua_pcall(vm, 1, 1, 0);
		lua_pop(vm,lua_gettop(vm));
	}
	return -1;
}

void LuaApp::reg(const char* name,void* obj){
	if(!vm)return;
	unsigned long long val = (unsigned long long)obj;
	int pc = lua_gettop(vm);
	lua_getglobal(vm,"sys");
	lua_getfield(vm,pc+1,"bindRef");
	lua_pushnumber(vm,val);
	lua_pushstring(vm,name);
	lua_pcall(vm, 2, 1, 0);
	lua_pop(vm,lua_gettop(vm));
}

lua_State* LuaApp::getLuaState(){
	return vm;
}
