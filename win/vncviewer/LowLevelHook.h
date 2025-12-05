#pragma once

#include <windows.h>
#include "omnithread.h"
#include <map>


class LowLevelHook
{
  public:
    static void Initialize(HINSTANCE);
    static void Activate(HWND);
    static bool isActive(HWND);
    static void Deactivate();
    static void Release();

    static DWORD WINAPI HookThreadProc(LPVOID);

  private:
    static LRESULT CALLBACK VncLowLevelKbHookProc(INT, WPARAM, LPARAM);

    static HWND g_hwndVNCViewer;
    static DWORD g_VncProcessID;
    static HHOOK g_HookID;

    // adzm 2009-09-25 - Hook installed on separate thread
    static HANDLE g_hThread;
    static DWORD g_nThreadID;

    static omni_mutex g_Mutex;

    static std::map<UINT, HWND> g_PressedKeys;
};
