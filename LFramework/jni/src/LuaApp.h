#ifndef _LUAAPP_H__
#define _LUAAPP_H__

#define LFrameworkVersion "1.0.0"

#include <jni.h>
extern "C"{
#include "lua.h"
#include "lauxlib.h"
#include "lualib.h"
}

class LuaApp {
private:
	lua_State* vm;
	void initLuaVm();
public:
	static LuaApp* getInstance();
	static void release();

	lua_State* getLuaState();
	void setJNIEnv(JNIEnv* env,jobject assetsManager);
	int execute(const char* code);
	int executeFile(const char* file);
	void reg(const char* name,void* obj);
};

#endif
