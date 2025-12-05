###############################################################################
# SETUP
###############################################################################

cmake_minimum_required(VERSION 3.16...3.28)

project(TurboVNC NONE)
string(TOLOWER ${CMAKE_PROJECT_NAME} CMAKE_PROJECT_NAME_LC)
set(VERSION 3.2.90)
set(DOCVERSION 3.3)
set(COPYRIGHT_YEAR "1999-2025")
set(COPYRIGHT "The VirtualGL Project and many others (see README.md)")
set(URLTEXT "Visit http://www.TurboVNC.org for more information on TurboVNC")

macro(boolean_number var)
	if(${var})
		set(${var} 1 ${ARGN})
	else()
		set(${var} 0 ${ARGN})
	endif()
endmacro()

macro(report_option var desc)
	if(${var})
		message(STATUS "${desc} enabled (${var} = ${${var}})")
	else()
		message(STATUS "${desc} disabled (${var} = ${${var}})")
	endif()
endmacro()

set(DEFAULT_TVNC_BUILDVIEWER 1)
option(TVNC_BUILDVIEWER "Build TurboVNC Viewer"
	${DEFAULT_TVNC_BUILDVIEWER})
boolean_number(TVNC_BUILDVIEWER)
report_option(TVNC_BUILDVIEWER "TurboVNC Viewer")

set(DEFAULT_TVNC_BUILDSERVER 0)
option(TVNC_BUILDSERVER "Build TurboVNC server components"
	${DEFAULT_TVNC_BUILDSERVER})
boolean_number(TVNC_BUILDSERVER)
report_option(TVNC_BUILDSERVER "TurboVNC Server")

if(NOT TVNC_BUILDVIEWER AND NOT TVNC_BUILDSERVER)
	message(FATAL_ERROR "Either TVNC_BUILDVIEWER or TVNC_BUILDSERVER must be enabled.")
endif()

enable_language(C)

if(NOT CMAKE_BUILD_TYPE)
	set(CMAKE_BUILD_TYPE Release)
endif()

message(STATUS "CMAKE_BUILD_TYPE = ${CMAKE_BUILD_TYPE}")

function(disable_compiler_warnings)
	if(MSVC)
		# Windows-specific warning suppression if needed
	endif()
endfunction()

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)
set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)

string(TIMESTAMP DEFAULT_BUILD "%Y%m%d")
set(BUILD ${DEFAULT_BUILD} CACHE STRING "Build string (default: ${DEFAULT_BUILD})")

message(STATUS "VERSION = ${VERSION}, BUILD = ${BUILD}")

message(STATUS "CMAKE_INSTALL_PREFIX = ${CMAKE_INSTALL_PREFIX}")

# Windows-specific installation paths
set(CMAKE_INSTALL_DEFAULT_DATAROOTDIR "")
set(CMAKE_INSTALL_DEFAULT_DOCDIR "<CMAKE_INSTALL_DATAROOTDIR>/doc")
set(CMAKE_INSTALL_DEFAULT_BINDIR ".")

include(cmakescripts/GNUInstallDirs.cmake)

macro(report_directory var)
	if(CMAKE_INSTALL_${var} STREQUAL CMAKE_INSTALL_FULL_${var})
		message(STATUS "CMAKE_INSTALL_${var} = ${CMAKE_INSTALL_${var}}")
	else()
		message(STATUS "CMAKE_INSTALL_${var} = ${CMAKE_INSTALL_${var}} (${CMAKE_INSTALL_FULL_${var}})")
	endif()
	mark_as_advanced(CLEAR CMAKE_INSTALL_${var})
endmacro()

set(DIRLIST "BINDIR;DATAROOTDIR;DATADIR;DOCDIR")
foreach(dir ${DIRLIST})
	report_directory(${dir})
endforeach()

# Detect CPU type and word size (Windows only)
math(EXPR BITS "${CMAKE_SIZEOF_VOID_P} * 8")
string(TOLOWER ${CMAKE_SYSTEM_PROCESSOR} CMAKE_SYSTEM_PROCESSOR_LC)
if(CMAKE_SYSTEM_PROCESSOR_LC MATCHES "x86_64" OR
	CMAKE_SYSTEM_PROCESSOR_LC MATCHES "amd64" OR
	CMAKE_SYSTEM_PROCESSOR_LC MATCHES "i[0-9]86" OR
	CMAKE_SYSTEM_PROCESSOR_LC MATCHES "x86" OR
	CMAKE_SYSTEM_PROCESSOR_LC MATCHES "ia32")
	if(BITS EQUAL 64)
		set(CPU_TYPE x86_64)
	else()
		set(CPU_TYPE i386)
	endif()
	if(NOT CMAKE_SYSTEM_PROCESSOR STREQUAL ${CPU_TYPE})
		set(CMAKE_SYSTEM_PROCESSOR ${CPU_TYPE})
	endif()
else()
	set(CPU_TYPE ${CMAKE_SYSTEM_PROCESSOR_LC})
endif()
message(STATUS "${BITS}-bit build (${CPU_TYPE})")
include(cmakescripts/FindTurboJPEG.cmake)


###############################################################################
# BUILD
###############################################################################

if(TVNC_BUILDVIEWER)
	add_subdirectory(java)
endif()
if(WIN32 AND TVNC_BUILDVIEWER)
	add_subdirectory(win)
endif()
add_subdirectory(doc)


###############################################################################
# INSTALLATION AND PACKAGING
###############################################################################

include(cmakescripts/BuildPackages.cmake)

configure_file("${CMAKE_SOURCE_DIR}/cmakescripts/cmake_uninstall.cmake.in"
	"cmake_uninstall.cmake" IMMEDIATE @ONLY)

add_custom_target(uninstall COMMAND ${CMAKE_COMMAND} -P cmake_uninstall.cmake)

install(FILES README.md DESTINATION .)
