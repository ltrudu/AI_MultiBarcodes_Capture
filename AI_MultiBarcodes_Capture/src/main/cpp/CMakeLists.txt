cmake_minimum_required(VERSION 3.22.1)

project("yuvprocessor")

add_library(${CMAKE_PROJECT_NAME} SHARED
    yuv_processor.cpp
)

target_link_libraries(${CMAKE_PROJECT_NAME}
    android
    log
    jnigraphics
)

# Enable optimizations
target_compile_options(${CMAKE_PROJECT_NAME} PRIVATE -O3 -ffast-math)

# Enable NEON SIMD on 32-bit ARM only (arm64 has NEON by default)
if(${ANDROID_ABI} STREQUAL "armeabi-v7a")
    target_compile_options(${CMAKE_PROJECT_NAME} PRIVATE -mfpu=neon)
endif()
