set(DEFAULT_PKGNAME ${CMAKE_PROJECT_NAME})
set(PKGNAME ${DEFAULT_PKGNAME} CACHE STRING
	"Distribution package name (default: ${DEFAULT_PKGNAME})")
set(PKGVENDOR "The VirtualGL Project" CACHE STRING
	"Vendor name to be included in distribution package descriptions (default: The VirtualGL Project)")
set(PKGURL "http://www.${CMAKE_PROJECT_NAME}.org" CACHE STRING
	"URL of project web site to be included in distribution package descriptions (default: http://www.${CMAKE_PROJECT_NAME}.org)")
set(PKGEMAIL "information@${CMAKE_PROJECT_NAME}.org" CACHE STRING
	"E-mail of project maintainer to be included in distribution package descriptions (default: information@${CMAKE_PROJECT_NAME}.org)")
string(TOLOWER ${PKGNAME} PKGNAME_LC)
set(PKGID "com.virtualgl.${PKGNAME_LC}" CACHE STRING
	"Globally unique package identifier (reverse DNS notation) (default: com.virtualgl.${PKGNAME_LC})")

if(WIN32)

set(INST_NAME ${CMAKE_PROJECT_NAME}-${VERSION})
if(BITS EQUAL 64)
	set(INST_DEFS -DWIN64)
endif()

set(INST_DEPENDS java turbovnchelper)
if(TVNC_INCLUDEJRE)
	set(INST_DEFS ${INST_DEFS} "-DINCLUDEJRE")
	set(INST_DEPENDS ${INST_DEPENDS} jrebuild)
endif()

if(MSVC_IDE)
	set(INSTALLERDIR ${CMAKE_CFG_INTDIR})
	set(INST_DEFS ${INST_DEFS} "-DBUILDDIR=${INSTALLERDIR}\\")
else()
	set(INSTALLERDIR .)
	set(INST_DEFS ${INST_DEFS} "-DBUILDDIR=")
endif()

configure_file(release/installer.iss.in pkgscripts/installer.iss)

add_custom_target(installer
	iscc -o${INSTALLERDIR} ${INST_DEFS} -F${INST_NAME} pkgscripts/installer.iss
	DEPENDS ${INST_DEPENDS}
	SOURCES pkgscripts/installer.iss)

endif()

add_custom_target(dist
	COMMAND git archive --prefix=${CMAKE_PROJECT_NAME_LC}-${VERSION}/ HEAD |
		gzip > ${CMAKE_BINARY_DIR}/${CMAKE_PROJECT_NAME_LC}-${VERSION}.tar.gz
	WORKING_DIRECTORY ${CMAKE_SOURCE_DIR})

configure_file(release/maketarball.in pkgscripts/maketarball)

add_custom_target(tarball pkgscripts/maketarball
	SOURCES pkgscripts/maketarball)
