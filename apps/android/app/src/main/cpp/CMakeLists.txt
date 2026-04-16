cmake_minimum_required(VERSION 3.22.1)
project("aaudio-bridge")

add_library(
    aaudio-bridge
    SHARED
    aaudio_bridge.cpp
)

find_library(
    aaudio-lib
    aaudio
)

target_link_libraries(
    aaudio-bridge
    ${aaudio-lib}
    log
)
