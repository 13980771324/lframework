
#ifndef PLATFORM_H_
#define PLATFORM_H_

#define PLATFORM_UNKNOWN            0
#define PLATFORM_IOS                1
#define PLATFORM_ANDROID            2
#define PLATFORM_WIN32              3
#define PLATFORM_MAC                4

#define TARGET_PLATFORM             PLATFORM_UNKNOWN

#if defined(__APPLE__) && !defined(ANDROID)
	#include <TargetConditionals.h>
    #if TARGET_OS_IPHONE // TARGET_OS_IPHONE includes TARGET_OS_IOS TARGET_OS_TV and TARGET_OS_WATCH. see TargetConditionals.h
        #undef  TARGET_PLATFORM
        #define TARGET_PLATFORM         PLATFORM_IOS
    #elif TARGET_OS_MAC
        #undef  TARGET_PLATFORM
        #define TARGET_PLATFORM         PLATFORM_MAC
    #endif
#endif

#if defined(ANDROID)
    #undef  TARGET_PLATFORM
    #define TARGET_PLATFORM         PLATFORM_ANDROID
#endif

#if defined(_WIN32) && defined(_WINDOWS)
    #undef  TARGET_PLATFORM
    #define TARGET_PLATFORM         PLATFORM_WIN32
#endif

#endif /* PLATFORM_H_ */
