cmake_minimum_required(VERSION 3.22.1)
project("edgedetection")

# Set C++ standard
set(CMAKE_CXX_STANDARD 14)

# OpenCV configuration
set(OpenCV_DIR "C:\Users\Prem\OneDrive\Desktop\prer\OpenCV\OpenCV-android-sdk\sdk\native\jni")
find_package(OpenCV REQUIRED)

# Include directories
include_directories(${OpenCV_INCLUDE_DIRS})

# Add library
add_library(
        edgedetection
        SHARED
        native-lib.cpp
        opencv_processor.cpp
)

# Link libraries
target_link_libraries(
        edgedetection
        ${OpenCV_LIBS}
        log
        jnigraphics
        android
)