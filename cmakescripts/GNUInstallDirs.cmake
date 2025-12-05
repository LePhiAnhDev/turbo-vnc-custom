macro(GNUInstallDirs_set_install_dir var docstring)
  set(_GNUInstallDirs_CMAKE_INSTALL_FORCE_${var} "")
  if(NOT DEFINED CMAKE_INSTALL_${var})
    set(_GNUInstallDirs_CMAKE_INSTALL_DEFAULT_${var} 1 CACHE INTERNAL
      "CMAKE_INSTALL_${var} has default value")
  elseif(DEFINED _GNUInstallDirs_CMAKE_INSTALL_LAST_DEFAULT_${var} AND
    NOT _GNUInstallDirs_CMAKE_INSTALL_LAST_DEFAULT_${var} STREQUAL
      CMAKE_INSTALL_DEFAULT_${var} AND
    _GNUInstallDirs_CMAKE_INSTALL_DEFAULT_${var} AND
    _GNUInstallDirs_CMAKE_INSTALL_LAST_${var} STREQUAL
      CMAKE_INSTALL_${var})
    set(_GNUInstallDirs_CMAKE_INSTALL_FORCE_${var} "FORCE")
  endif()
  if(DEFINED CMAKE_INSTALL_${var} AND NOT CMAKE_INSTALL_${var} MATCHES "^/")
    set_property(CACHE CMAKE_INSTALL_${var} PROPERTY TYPE PATH)
  endif()
  set(CMAKE_INSTALL_${var} "${CMAKE_INSTALL_DEFAULT_${var}}" CACHE PATH
    "${docstring} (Default: ${CMAKE_INSTALL_DEFAULT_${var}})"
    ${_GNUInstallDirs_CMAKE_INSTALL_FORCE_${var}})
  if(NOT CMAKE_INSTALL_${var} STREQUAL CMAKE_INSTALL_DEFAULT_${var})
    unset(_GNUInstallDirs_CMAKE_INSTALL_DEFAULT_${var} CACHE)
  endif()
  set(_GNUInstallDirs_CMAKE_INSTALL_LAST_${var} "${CMAKE_INSTALL_${var}}"
    CACHE INTERNAL "CMAKE_INSTALL_${var} during last run")
  set(_GNUInstallDirs_CMAKE_INSTALL_LAST_DEFAULT_${var}
    "${CMAKE_INSTALL_DEFAULT_${var}}" CACHE INTERNAL
    "CMAKE_INSTALL_DEFAULT_${var} during last run")
endmacro()
if(NOT DEFINED CMAKE_INSTALL_DEFAULT_BINDIR)
  set(CMAKE_INSTALL_DEFAULT_BINDIR "bin")
endif()
GNUInstallDirs_set_install_dir(BINDIR
  "Directory into which user executables should be installed")
if(NOT DEFINED CMAKE_INSTALL_DEFAULT_SBINDIR)
  set(CMAKE_INSTALL_DEFAULT_SBINDIR "sbin")
endif()
GNUInstallDirs_set_install_dir(SBINDIR
  "Directory into which system admin executables should be installed")
if(NOT DEFINED CMAKE_INSTALL_DEFAULT_LIBEXECDIR)
  set(CMAKE_INSTALL_DEFAULT_LIBEXECDIR "libexec")
endif()
GNUInstallDirs_set_install_dir(LIBEXECDIR
  "Directory under which executables run by other programs should be installed")
if(NOT DEFINED CMAKE_INSTALL_DEFAULT_SYSCONFDIR)
  set(CMAKE_INSTALL_DEFAULT_SYSCONFDIR "etc")
endif()
GNUInstallDirs_set_install_dir(SYSCONFDIR
  "Directory into which machine-specific read-only ASCII data and configuration files should be installed")
if(NOT DEFINED CMAKE_INSTALL_DEFAULT_SHAREDSTATEDIR)
  set(CMAKE_INSTALL_DEFAULT_SHAREDSTATEDIR "com")
endif()
GNUInstallDirs_set_install_dir(SHAREDSTATEDIR
  "Directory into which architecture-independent run-time-modifiable data files should be installed")
if(NOT DEFINED CMAKE_INSTALL_DEFAULT_LOCALSTATEDIR)
  set(CMAKE_INSTALL_DEFAULT_LOCALSTATEDIR "var")
endif()
GNUInstallDirs_set_install_dir(LOCALSTATEDIR
  "Directory into which machine-specific run-time-modifiable data files should be installed")
if(NOT DEFINED CMAKE_INSTALL_DEFAULT_LIBDIR)
  set(CMAKE_INSTALL_DEFAULT_LIBDIR "lib")
  if(CMAKE_SYSTEM_NAME MATCHES "^(Linux|kFreeBSD|GNU)$"
      AND NOT CMAKE_CROSSCOMPILING)
    if (EXISTS "/etc/debian_version")
      if(CMAKE_LIBRARY_ARCHITECTURE)
        if("${CMAKE_INSTALL_PREFIX}" MATCHES "^/usr/?$")
          set(CMAKE_INSTALL_DEFAULT_LIBDIR "lib/${CMAKE_LIBRARY_ARCHITECTURE}")
        endif()
      endif()
    else()
      if(NOT DEFINED CMAKE_SIZEOF_VOID_P)
        message(AUTHOR_WARNING
          "Unable to determine default CMAKE_INSTALL_LIBDIR directory because no target architecture is known. "
          "Please enable at least one language before including GNUInstallDirs.")
      else()
        if("${CMAKE_SIZEOF_VOID_P}" EQUAL "8")
          set(CMAKE_INSTALL_DEFAULT_LIBDIR "lib64")
        endif()
      endif()
    endif()
  endif()
endif()
GNUInstallDirs_set_install_dir(LIBDIR
  "Directory into which object files and object code libraries should be installed")
if(NOT DEFINED CMAKE_INSTALL_DEFAULT_INCLUDEDIR)
  set(CMAKE_INSTALL_DEFAULT_INCLUDEDIR "include")
endif()
GNUInstallDirs_set_install_dir(INCLUDEDIR
  "Directory into which C header files should be installed")
if(NOT DEFINED CMAKE_INSTALL_DEFAULT_OLDINCLUDEDIR)
  set(CMAKE_INSTALL_DEFAULT_OLDINCLUDEDIR "/usr/include")
endif()
GNUInstallDirs_set_install_dir(OLDINCLUDEDIR
  PATH "Directory into which C header files for non-GCC compilers should be installed")
if(NOT DEFINED CMAKE_INSTALL_DEFAULT_DATAROOTDIR)
  set(CMAKE_INSTALL_DEFAULT_DATAROOTDIR "share")
endif()
GNUInstallDirs_set_install_dir(DATAROOTDIR
  "The root of the directory tree for read-only architecture-independent data files")
if(NOT DEFINED CMAKE_INSTALL_DEFAULT_DATADIR)
  set(CMAKE_INSTALL_DEFAULT_DATADIR "<CMAKE_INSTALL_DATAROOTDIR>")
endif()
GNUInstallDirs_set_install_dir(DATADIR
  "The directory under which read-only architecture-independent data files should be installed")
if(NOT DEFINED CMAKE_INSTALL_DEFAULT_INFODIR)
  if(CMAKE_SYSTEM_NAME MATCHES "^(([^kF].*)?BSD|DragonFly)$")
    set(CMAKE_INSTALL_DEFAULT_INFODIR "info")
  else()
    set(CMAKE_INSTALL_DEFAULT_INFODIR "<CMAKE_INSTALL_DATAROOTDIR>/info")
  endif()
endif()
GNUInstallDirs_set_install_dir(INFODIR
  "The directory into which info documentation files should be installed")
if(NOT DEFINED CMAKE_INSTALL_DEFAULT_MANDIR)
  if(CMAKE_SYSTEM_NAME MATCHES "^(([^k].*)?BSD|DragonFly)$" AND NOT CMAKE_SYSTEM_NAME MATCHES "^(FreeBSD)$")
    set(CMAKE_INSTALL_DEFAULT_MANDIR "man")
  else()
    set(CMAKE_INSTALL_DEFAULT_MANDIR "<CMAKE_INSTALL_DATAROOTDIR>/man")
  endif()
endif()
GNUInstallDirs_set_install_dir(MANDIR
  "The directory under which man pages should be installed")
if(NOT DEFINED CMAKE_INSTALL_DEFAULT_LOCALEDIR)
  set(CMAKE_INSTALL_DEFAULT_LOCALEDIR "<CMAKE_INSTALL_DATAROOTDIR>/locale")
endif()
GNUInstallDirs_set_install_dir(LOCALEDIR
  "The directory under which locale-specific message catalogs should be installed")
if(NOT DEFINED CMAKE_INSTALL_DEFAULT_DOCDIR)
  set(CMAKE_INSTALL_DEFAULT_DOCDIR "<CMAKE_INSTALL_DATAROOTDIR>/doc/${PROJECT_NAME}")
endif()
GNUInstallDirs_set_install_dir(DOCDIR
  "The directory into which documentation files (other than info files) should be installed")
mark_as_advanced(
  CMAKE_INSTALL_BINDIR
  CMAKE_INSTALL_SBINDIR
  CMAKE_INSTALL_LIBEXECDIR
  CMAKE_INSTALL_SYSCONFDIR
  CMAKE_INSTALL_SHAREDSTATEDIR
  CMAKE_INSTALL_LOCALSTATEDIR
  CMAKE_INSTALL_LIBDIR
  CMAKE_INSTALL_INCLUDEDIR
  CMAKE_INSTALL_OLDINCLUDEDIR
  CMAKE_INSTALL_DATAROOTDIR
  CMAKE_INSTALL_DATADIR
  CMAKE_INSTALL_INFODIR
  CMAKE_INSTALL_LOCALEDIR
  CMAKE_INSTALL_MANDIR
  CMAKE_INSTALL_DOCDIR
  )
macro(GNUInstallDirs_get_absolute_install_dir absvar var)
  string(REGEX REPLACE "[<>]" "@" ${var} "${${var}}")
  if(NOT CMAKE_INSTALL_DATAROOTDIR AND
    ${var} MATCHES "\@CMAKE_INSTALL_DATAROOTDIR\@/")
    string(CONFIGURE "${${var}}" ${var} @ONLY)
    string(REGEX REPLACE "^/" "" ${var} "${${var}}")
  else()
    string(CONFIGURE "${${var}}" ${var} @ONLY)
  endif()
  if(NOT IS_ABSOLUTE "${${var}}")
    if("${CMAKE_INSTALL_PREFIX}" STREQUAL "/")
      if("${dir}" STREQUAL "SYSCONFDIR" OR "${dir}" STREQUAL "LOCALSTATEDIR")
        set(${absvar} "/${${var}}")
      else()
        if (NOT "${${var}}" MATCHES "^usr/")
          set(${var} "usr/${${var}}")
        endif()
        set(${absvar} "/${${var}}")
      endif()
    elseif("${CMAKE_INSTALL_PREFIX}" MATCHES "^/usr/?$")
      if("${dir}" STREQUAL "SYSCONFDIR" OR "${dir}" STREQUAL "LOCALSTATEDIR")
        set(${absvar} "/${${var}}")
      else()
        set(${absvar} "${CMAKE_INSTALL_PREFIX}/${${var}}")
      endif()
    elseif("${CMAKE_INSTALL_PREFIX}" MATCHES "^/opt/.*")
      if("${dir}" STREQUAL "SYSCONFDIR" OR "${dir}" STREQUAL "LOCALSTATEDIR")
        set(${absvar} "/${${var}}${CMAKE_INSTALL_PREFIX}")
      else()
        set(${absvar} "${CMAKE_INSTALL_PREFIX}/${${var}}")
      endif()
    else()
      set(${absvar} "${CMAKE_INSTALL_PREFIX}/${${var}}")
    endif()
  else()
    set(${absvar} "${${var}}")
  endif()
  string(REGEX REPLACE "/$" "" ${absvar} "${${absvar}}")
endmacro()
foreach(dir
    BINDIR
    SBINDIR
    LIBEXECDIR
    SYSCONFDIR
    SHAREDSTATEDIR
    LOCALSTATEDIR
    LIBDIR
    INCLUDEDIR
    OLDINCLUDEDIR
    DATAROOTDIR
    DATADIR
    INFODIR
    LOCALEDIR
    MANDIR
    DOCDIR
    )
  GNUInstallDirs_get_absolute_install_dir(CMAKE_INSTALL_FULL_${dir} CMAKE_INSTALL_${dir})
endforeach()
