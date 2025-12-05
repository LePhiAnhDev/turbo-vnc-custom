typedef struct _rfbRectangle {
    CARD16 x;
    CARD16 y;
    CARD16 w;
    CARD16 h;
} rfbRectangle;

#define sz_rfbRectangle 8

typedef struct _rfbPixelFormat {

    CARD8 bitsPerPixel;         

    CARD8 depth;                

    CARD8 bigEndian;            

    CARD8 trueColour;           

    CARD16 redMax;              

    CARD16 greenMax;            

    CARD16 blueMax;             

    CARD8 redShift;             

    CARD8 greenShift;           

    CARD8 blueShift;            

    CARD8 pad1;
    CARD16 pad2;

} rfbPixelFormat;

#define sz_rfbPixelFormat 16

typedef struct _rfbCapabilityInfo {

    CARD32 code;                
    CARD8 vendorSignature[4];   
    CARD8 nameSignature[8];     

} rfbCapabilityInfo;

#define sz_rfbCapabilityInfoVendor 4
#define sz_rfbCapabilityInfoName 8
#define sz_rfbCapabilityInfo 16

#define rfbStandardVendor  "STDV"
#define rfbTridiaVncVendor "TRDV"
#define rfbTightVncVendor  "TGHT"
#define rfbTurboVncVendor  "TRBO"
#define rfbVeNCryptVendor  "VENC"
#define rfbGIIVendor       "GGI_"

#define rfbProtocolVersionFormat "RFB %03d.%03d\n"

typedef char rfbProtocolVersionMsg[13]; 

#define sz_rfbProtocolVersionMsg 12

#define rfbSecTypeInvalid 0
#define rfbSecTypeNone 1
#define rfbSecTypeVncAuth 2
#define rfbSecTypeTight 16
#define rfbSecTypeVeNCrypt 19

#define rfbVeNCryptPlain 256
#define rfbVeNCryptTLSNone 257
#define rfbVeNCryptTLSVnc 258
#define rfbVeNCryptTLSPlain 259
#define rfbVeNCryptX509None 260
#define rfbVeNCryptX509Vnc 261
#define rfbVeNCryptX509Plain 262

typedef struct _rfbTunnelingCapsMsg {
    CARD32 nTunnelTypes;
    
} rfbTunnelingCapsMsg;

#define sz_rfbTunnelingCapsMsg 4

#define rfbNoTunneling 0
#define sig_rfbNoTunneling "NOTUNNEL"

typedef struct _rfbAuthenticationCapsMsg {
    CARD32 nAuthTypes;
    
} rfbAuthenticationCapsMsg;

#define sz_rfbAuthenticationCapsMsg 4

#define rfbAuthNone 1
#define rfbAuthVNC 2

#define sig_rfbAuthNone "NOAUTH__"
#define sig_rfbAuthVNC "VNCAUTH_"

#define rfbAuthUnixLogin 129
#define rfbAuthExternal 130

#define sig_rfbAuthUnixLogin "ULGNAUTH"
#define sig_rfbAuthExternal "XTRNAUTH"

#define rfbAuthVeNCrypt 19
#define sig_rfbAuthVeNCrypt "VENCRYPT"

#define rfbAuthOK 0
#define rfbAuthFailed 1
#define rfbAuthTooMany 2

typedef struct _rfbClientInitMsg {
    CARD8 shared;
} rfbClientInitMsg;

#define sz_rfbClientInitMsg 1

typedef struct _rfbServerInitMsg {
    CARD16 framebufferWidth;
    CARD16 framebufferHeight;
    rfbPixelFormat format;      
    CARD32 nameLength;
    
} rfbServerInitMsg;

#define sz_rfbServerInitMsg (8 + sz_rfbPixelFormat)

typedef struct _rfbInteractionCapsMsg {
    CARD16 nServerMessageTypes;
    CARD16 nClientMessageTypes;
    CARD16 nEncodingTypes;
    CARD16 pad;                 
    
} rfbInteractionCapsMsg;

#define sz_rfbInteractionCapsMsg 8

#define rfbFramebufferUpdate 0
#define rfbSetColourMapEntries 1
#define rfbBell 2
#define rfbServerCutText 3

#define rfbFileListData 130
#define rfbFileDownloadData 131
#define rfbFileUploadCancel 132
#define rfbFileDownloadFailed 133

#define rfbGIIServer 253

#define sig_rfbFileListData "FTS_LSDT"
#define sig_rfbFileDownloadData "FTS_DNDT"
#define sig_rfbFileUploadCancel "FTS_UPCN"
#define sig_rfbFileDownloadFailed "FTS_DNFL"
#define sig_rfbGIIServer "GII_SERV"

#define rfbSetPixelFormat 0
#define rfbFixColourMapEntries 1        
#define rfbSetEncodings 2
#define rfbFramebufferUpdateRequest 3
#define rfbKeyEvent 4
#define rfbPointerEvent 5
#define rfbClientCutText 6

#define rfbFileListRequest 130
#define rfbFileDownloadRequest 131
#define rfbFileUploadRequest 132
#define rfbFileUploadData 133
#define rfbFileDownloadCancel 134
#define rfbFileUploadFailed 135
#define rfbFileCreateDirRequest 136

#define rfbEnableContinuousUpdates 150
#define rfbEndOfContinuousUpdates 150

#define rfbSetDesktopSize 251

#define rfbGIIClient 253

#define sig_rfbFileListRequest "FTC_LSRQ"
#define sig_rfbFileDownloadRequest "FTC_DNRQ"
#define sig_rfbFileUploadRequest "FTC_UPRQ"
#define sig_rfbFileUploadData "FTC_UPDT"
#define sig_rfbFileDownloadCancel "FTC_DNCN"
#define sig_rfbFileUploadFailed "FTC_UPFL"
#define sig_rfbFileCreateDirRequest "FTC_FCDR"
#define sig_rfbGIIClient "GII_CLNT"

#define rfbFence 248
#define rfbQEMU 255

#define rfbEncodingRaw       0
#define rfbEncodingCopyRect  1
#define rfbEncodingRRE       2
#define rfbEncodingCoRRE     4
#define rfbEncodingHextile   5
#define rfbEncodingZlib      6
#define rfbEncodingTight     7
#define rfbEncodingZlibHex   8
#define rfbEncodingZRLE     16
#define rfbEncodingZYWRLE   17

#define sig_rfbEncodingRaw       "RAW_____"
#define sig_rfbEncodingCopyRect  "COPYRECT"
#define sig_rfbEncodingRRE       "RRE_____"
#define sig_rfbEncodingCoRRE     "CORRE___"
#define sig_rfbEncodingHextile   "HEXTILE_"
#define sig_rfbEncodingZlib      "ZLIB____"
#define sig_rfbEncodingTight     "TIGHT___"
#define sig_rfbEncodingZlibHex   "ZLIBHEX_"
#define sig_rfbEncodingZRLE      "ZRLE____"
#define sig_rfbEncodingZYWRLE    "ZYWRLE__"

#define rfbEncodingVMwareLEDState       0x574D5668

#define rfbEncodingExtendedClipboard    0xC0A1E5CE

#define rfbEncodingSubsamp1X            0xFFFFFD00  
#define rfbEncodingSubsamp4X            0xFFFFFD01  
#define rfbEncodingSubsamp2X            0xFFFFFD02  
#define rfbEncodingSubsampGray          0xFFFFFD03  
#define rfbEncodingSubsamp8X            0xFFFFFD04  
#define rfbEncodingSubsamp16X           0xFFFFFD05  
#define rfbEncodingFineQualityLevel0    0xFFFFFE00  
#define rfbEncodingFineQualityLevel100  0xFFFFFE64  

#define rfbEncodingTightWithoutZlib     0xFFFFFEC3  

#define rfbEncodingExtendedMouseButtons 0xFFFFFEC4  

#define rfbEncodingContinuousUpdates    0xFFFFFEC7  
#define rfbEncodingFence                0xFFFFFEC8  

#define rfbEncodingExtendedDesktopSize  0xFFFFFECC  

#define rfbEncodingGII                  0xFFFFFECF  

#define rfbEncodingQEMULEDState         0xFFFFFEFB  
#define rfbEncodingQEMUExtendedKeyEvent 0xFFFFFEFE  

#define rfbEncodingCompressLevel0       0xFFFFFF00  
#define rfbEncodingCompressLevel1       0xFFFFFF01  
#define rfbEncodingCompressLevel2       0xFFFFFF02  
#define rfbEncodingCompressLevel3       0xFFFFFF03  
#define rfbEncodingCompressLevel4       0xFFFFFF04  
#define rfbEncodingCompressLevel5       0xFFFFFF05  
#define rfbEncodingCompressLevel6       0xFFFFFF06  
#define rfbEncodingCompressLevel7       0xFFFFFF07  
#define rfbEncodingCompressLevel8       0xFFFFFF08  
#define rfbEncodingCompressLevel9       0xFFFFFF09  

#define rfbEncodingXCursor              0xFFFFFF10  
#define rfbEncodingRichCursor           0xFFFFFF11  
#define rfbEncodingPointerPos           0xFFFFFF18  

#define rfbEncodingLastRect             0xFFFFFF20  
#define rfbEncodingNewFBSize            0xFFFFFF21  

#define rfbEncodingQualityLevel0        0xFFFFFFE0  
#define rfbEncodingQualityLevel1        0xFFFFFFE1  
#define rfbEncodingQualityLevel2        0xFFFFFFE2  
#define rfbEncodingQualityLevel3        0xFFFFFFE3  
#define rfbEncodingQualityLevel4        0xFFFFFFE4  
#define rfbEncodingQualityLevel5        0xFFFFFFE5  
#define rfbEncodingQualityLevel6        0xFFFFFFE6  
#define rfbEncodingQualityLevel7        0xFFFFFFE7  
#define rfbEncodingQualityLevel8        0xFFFFFFE8  
#define rfbEncodingQualityLevel9        0xFFFFFFE9  

#define sig_rfbEncodingCompressLevel0    "COMPRLVL"
#define sig_rfbEncodingXCursor           "X11CURSR"
#define sig_rfbEncodingRichCursor        "RCHCURSR"
#define sig_rfbEncodingPointerPos        "POINTPOS"
#define sig_rfbEncodingLastRect          "LASTRECT"
#define sig_rfbEncodingNewFBSize         "NEWFBSIZ"
#define sig_rfbEncodingFineQualityLevel0 "FINEQLVL"
#define sig_rfbEncodingSubsamp1X         "SSAMPLVL"
#define sig_rfbEncodingQualityLevel0     "JPEGQLVL"
#define sig_rfbEncodingGII               "GII_____"

#define rfbFenceFlagBlockBefore 1
#define rfbFenceFlagBlockAfter 2
#define rfbFenceFlagSyncNext 4
#define rfbFenceFlagRequest 0x80000000
#define rfbFenceFlagsSupported (rfbFenceFlagBlockBefore | \
                                rfbFenceFlagBlockAfter | \
                                rfbFenceFlagSyncNext | \
                                rfbFenceFlagRequest)

typedef struct _rfbFenceMsg {
    CARD8 type;                 
    CARD8 pad[3];
    CARD32 flags;
    CARD8 length;
    
} rfbFenceMsg;

#define sz_rfbFenceMsg 9

#define rfbExtClipUTF8    0x00000001
#define rfbExtClipRTF     0x00000002
#define rfbExtClipHTML    0x00000004
#define rfbExtClipDIB     0x00000008
#define rfbExtClipFiles   0x00000010

#define rfbExtClipCaps    0x01000000
#define rfbExtClipRequest 0x02000000
#define rfbExtClipPeek    0x04000000
#define rfbExtClipNotify  0x08000000
#define rfbExtClipProvide 0x10000000

#define rfbGIIEvent 0
#define rfbGIIVersion 1
#define rfbGIIDeviceCreate 2
#define rfbGIIDeviceDestroy 3

#define rfbGIIBE 128

typedef struct _rfbFramebufferUpdateMsg {
    CARD8 type;                 
    CARD8 pad;
    CARD16 nRects;
    
} rfbFramebufferUpdateMsg;

#define sz_rfbFramebufferUpdateMsg 4

typedef struct _rfbFramebufferUpdateRectHeader {
    rfbRectangle r;
    CARD32 encoding;            
} rfbFramebufferUpdateRectHeader;

#define sz_rfbFramebufferUpdateRectHeader (sz_rfbRectangle + 4)

typedef struct _rfbCopyRect {
    CARD16 srcX;
    CARD16 srcY;
} rfbCopyRect;

#define sz_rfbCopyRect 4

typedef struct _rfbRREHeader {
    CARD32 nSubrects;
} rfbRREHeader;

#define sz_rfbRREHeader 4

typedef struct _rfbCoRRERectangle {
    CARD8 x;
    CARD8 y;
    CARD8 w;
    CARD8 h;
} rfbCoRRERectangle;

#define sz_rfbCoRRERectangle 4

#define rfbHextileRaw                   (1 << 0)
#define rfbHextileBackgroundSpecified   (1 << 1)
#define rfbHextileForegroundSpecified   (1 << 2)
#define rfbHextileAnySubrects           (1 << 3)
#define rfbHextileSubrectsColoured      (1 << 4)

#define rfbHextilePackXY(x,y) (((x) << 4) | (y))
#define rfbHextilePackWH(w,h) ((((w)-1) << 4) | ((h)-1))
#define rfbHextileExtractX(byte) ((byte) >> 4)
#define rfbHextileExtractY(byte) ((byte) & 0xf)
#define rfbHextileExtractW(byte) (((byte) >> 4) + 1)
#define rfbHextileExtractH(byte) (((byte) & 0xf) + 1)

typedef struct _rfbZlibHeader {
    CARD32 nBytes;
} rfbZlibHeader;

#define sz_rfbZlibHeader 4

#define rfbTightExplicitFilter  0x04
#define rfbTightFill            0x08
#define rfbTightJpeg            0x09
#define rfbTightNoZlib          0x0A
#define rfbTightMaxSubencoding  0x09

#define rfbTightFilterCopy      0x00
#define rfbTightFilterPalette   0x01
#define rfbTightFilterGradient  0x02

#define rfbHextileZlibRaw       (1 << 5)
#define rfbHextileZlibHex       (1 << 6)

typedef struct _rfbXCursorColors {
    CARD8 foreRed;
    CARD8 foreGreen;
    CARD8 foreBlue;
    CARD8 backRed;
    CARD8 backGreen;
    CARD8 backBlue;
} rfbXCursorColors;

#define sz_rfbXCursorColors 6

typedef struct {
    CARD32 length;
} rfbZRLEHeader;

#define sz_rfbZRLEHeader 4

#define rfbZRLETileWidth 64
#define rfbZRLETileHeight 64

#define rfbLEDScrollLock (1 << 0)
#define rfbLEDNumLock    (1 << 1)
#define rfbLEDCapsLock   (1 << 2)
#define rfbLEDUnknown    0xFFFFFFFF  

#define rfbEDSReasonServer 0
#define rfbEDSReasonClient 1
#define rfbEDSReasonOtherClient 2

#define rfbEDSResultSuccess 0
#define rfbEDSResultProhibited 1
#define rfbEDSResultNoResources 2
#define rfbEDSResultInvalid 3

typedef struct _rfbSetColourMapEntriesMsg {
    CARD8 type;                 
    CARD8 pad;
    CARD16 firstColour;
    CARD16 nColours;

} rfbSetColourMapEntriesMsg;

#define sz_rfbSetColourMapEntriesMsg 6

typedef struct _rfbBellMsg {
    CARD8 type;                 
} rfbBellMsg;

#define sz_rfbBellMsg 1

typedef struct _rfbServerCutTextMsg {
    CARD8 type;                 
    CARD8 pad1;
    CARD16 pad2;
    CARD32 length;
    
} rfbServerCutTextMsg;

#define sz_rfbServerCutTextMsg 8

typedef struct _rfbFileListDataMsg {
    CARD8 type;
    CARD8 flags;
    CARD16 numFiles;
    CARD16 dataSize;
    CARD16 compressedSize;
    
} rfbFileListDataMsg;

#define sz_rfbFileListDataMsg 8

typedef struct _rfbFileDownloadDataMsg {
    CARD8 type;
    CARD8 compressLevel;
    CARD16 realSize;
    CARD16 compressedSize;
    
} rfbFileDownloadDataMsg;

#define sz_rfbFileDownloadDataMsg 6

typedef struct _rfbFileUploadCancelMsg {
    CARD8 type;
    CARD8 unused;
    CARD16 reasonLen;
    
} rfbFileUploadCancelMsg;

#define sz_rfbFileUploadCancelMsg 4

typedef struct _rfbFileDownloadFailedMsg {
    CARD8 type;
    CARD8 unused;
    CARD16 reasonLen;
    
} rfbFileDownloadFailedMsg;

#define sz_rfbFileDownloadFailedMsg 4

typedef struct _rfbGIIServerVersionMsg {
    CARD8 type;                 
    CARD8 endianAndSubType;     
    CARD16 length;              
    CARD16 maximumVersion;      
    CARD16 minimumVersion;      
} rfbGIIServerVersionMsg;

#define sz_rfbGIIServerVersionMsg 8

typedef struct _rfbGIIDeviceCreatedMsg {
    CARD8 type;                 
    CARD8 endianAndSubType;     
    CARD16 length;              
    CARD32 deviceOrigin;
} rfbGIIDeviceCreatedMsg;

#define sz_rfbGIIDeviceCreatedMsg 8

typedef union _rfbServerToClientMsg {
    CARD8 type;
    rfbFramebufferUpdateMsg fu;
    rfbSetColourMapEntriesMsg scme;
    rfbBellMsg b;
    rfbServerCutTextMsg sct;
    rfbFileListDataMsg fld;
    rfbFileDownloadDataMsg fdd;
    rfbFileUploadCancelMsg fuc;
    rfbFileDownloadFailedMsg fdf;
    rfbFenceMsg f;
    rfbGIIServerVersionMsg giisv;
    rfbGIIDeviceCreatedMsg giidc;
} rfbServerToClientMsg;

typedef struct _rfbSetPixelFormatMsg {
    CARD8 type;                 
    CARD8 pad1;
    CARD16 pad2;
    rfbPixelFormat format;
} rfbSetPixelFormatMsg;

#define sz_rfbSetPixelFormatMsg (sz_rfbPixelFormat + 4)

typedef struct _rfbFixColourMapEntriesMsg {
    CARD8 type;                 
    CARD8 pad;
    CARD16 firstColour;
    CARD16 nColours;

} rfbFixColourMapEntriesMsg;

#define sz_rfbFixColourMapEntriesMsg 6

typedef struct _rfbSetEncodingsMsg {
    CARD8 type;                 
    CARD8 pad;
    CARD16 nEncodings;
    
} rfbSetEncodingsMsg;

#define sz_rfbSetEncodingsMsg 4

typedef struct _rfbFramebufferUpdateRequestMsg {
    CARD8 type;                 
    CARD8 incremental;
    CARD16 x;
    CARD16 y;
    CARD16 w;
    CARD16 h;
} rfbFramebufferUpdateRequestMsg;

#define sz_rfbFramebufferUpdateRequestMsg 10

typedef struct _rfbKeyEventMsg {
    CARD8 type;                 
    CARD8 down;                 
    CARD16 pad;
    CARD32 key;                 
} rfbKeyEventMsg;

#define sz_rfbKeyEventMsg 8

typedef struct _rfbPointerEventMsg {
    CARD8 type;                 
    CARD8 buttonMask;           
    CARD16 x;
    CARD16 y;
} rfbPointerEventMsg;

#define rfbButton1Mask 1
#define rfbButton2Mask 2
#define rfbButton3Mask 4
#define rfbButton4Mask 8
#define rfbButton5Mask 16

#define sz_rfbPointerEventMsg 6

typedef struct _rfbClientCutTextMsg {
    CARD8 type;                 
    CARD8 pad1;
    CARD16 pad2;
    CARD32 length;
    
} rfbClientCutTextMsg;

#define sz_rfbClientCutTextMsg 8

typedef struct _rfbFileListRequestMsg {
    CARD8 type;
    CARD8 flags;
    CARD16 dirNameSize;
    
} rfbFileListRequestMsg;

#define sz_rfbFileListRequestMsg 4

typedef struct _rfbFileDownloadRequestMsg {
    CARD8 type;
    CARD8 compressedLevel;
    CARD16 fNameSize;
    CARD32 position;
    
} rfbFileDownloadRequestMsg;

#define sz_rfbFileDownloadRequestMsg 8

typedef struct _rfbFileUploadRequestMsg {
    CARD8 type;
    CARD8 compressedLevel;
    CARD16 fNameSize;
    CARD32 position;
    
} rfbFileUploadRequestMsg;

#define sz_rfbFileUploadRequestMsg 8

typedef struct _rfbFileUploadDataMsg {
    CARD8 type;
    CARD8 compressedLevel;
    CARD16 realSize;
    CARD16 compressedSize;
    
} rfbFileUploadDataMsg;

#define sz_rfbFileUploadDataMsg 6

typedef struct _rfbFileDownloadCancelMsg {
    CARD8 type;
    CARD8 unused;
    CARD16 reasonLen;
    
} rfbFileDownloadCancelMsg;

#define sz_rfbFileDownloadCancelMsg 4

typedef struct _rfbFileUploadFailedMsg {
    CARD8 type;
    CARD8 unused;
    CARD16 reasonLen;
    
} rfbFileUploadFailedMsg;

#define sz_rfbFileUploadFailedMsg 4

typedef struct _rfbFileCreateDirRequestMsg {
    CARD8 type;
    CARD8 unused;
    CARD16 dNameLen;
    
} rfbFileCreateDirRequestMsg;

#define sz_rfbFileCreateDirRequestMsg 4

typedef struct _rfbEnableContinuousUpdatesMsg {
    CARD8 type;                 
    CARD8 enable;
    CARD16 x;
    CARD16 y;
    CARD16 w;
    CARD16 h;
} rfbEnableContinuousUpdatesMsg;

#define sz_rfbEnableContinuousUpdatesMsg 10

typedef struct _rfbScreenDesc {
    CARD32 id;
    CARD16 x;
    CARD16 y;
    CARD16 w;
    CARD16 h;
    CARD32 flags;
} rfbScreenDesc;

#define sz_rfbScreenDesc 16

typedef struct _rfbSetDesktopSizeMsg {
    CARD8 type;                 
    CARD8 pad1;
    CARD16 w;
    CARD16 h;
    CARD8 numScreens;
    CARD8 pad2;
    
} rfbSetDesktopSizeMsg;

#define sz_rfbSetDesktopSizeMsg 8

typedef struct _rfbGIIClientVersionMsg {
    CARD8 type;                 
    CARD8 endianAndSubType;     
    CARD16 length;              
    CARD16 version;
} rfbGIIClientVersionMsg;

#define sz_rfbGIIClientVersionMsg 6

#define rfbGIIUnitUnknown          0
#define rfbGIIUnitTime             1
#define rfbGIIUnitFreq             2
#define rfbGIIUnitLength           3
#define rfbGIIUnitVelocity         4
#define rfbGIIUnitAccel            5
#define rfbGIIUnitAngle            6
#define rfbGIIUnitAngularVelocity  7
#define rfbGIIUnitAngularAccel     8
#define rfbGIIUnitArea             9
#define rfbGIIUnitVolume           10
#define rfbGIIUnitMass             11
#define rfbGIIUnitForce            12
#define rfbGIIUnitPressure         13
#define rfbGIIUnitTorque           14
#define rfbGIIUnitEnergy           15
#define rfbGIIUnitPower            16
#define rfbGIIUnitTemp             17
#define rfbGIIUnitCurrent          18
#define rfbGIIUnitVoltage          19
#define rfbGIIUnitResistance       20
#define rfbGIIUnitCapacity         21
#define rfbGIIUnitInductivity      22

typedef struct _rfbGIIValuator {
    CARD32 index;
    CARD8 longName[75];         
    CARD8 shortName[5];         
    INT32 rangeMin;
    INT32 rangeCenter;
    INT32 rangeMax;
    CARD32 siUnit;              
    INT32 siAdd;
    INT32 siMul;
    INT32 siDiv;
    INT32 siShift;
} rfbGIIValuator;

#define sz_rfbGIIValuator 116

#define rfbGIIKeyPressMask         0x00000020
#define rfbGIIKeyReleaseMask       0x00000040
#define rfbGIIKeyRepeatMask        0x00000080
#define rfbGIIMoveRelativeMask     0x00000100
#define rfbGIIMoveAbsoluteMask     0x00000200
#define rfbGIIButtonPressMask      0x00000400
#define rfbGIIButtonReleaseMask    0x00000800
#define rfbGIIValuatorRelativeMask 0x00001000
#define rfbGIIValuatorAbsoluteMask 0x00002000

typedef struct _rfbGIIDeviceCreateMsg {
    CARD8 type;                 
    CARD8 endianAndSubType;     
    CARD16 length;              
    CARD8 deviceName[32];       
    CARD32 vendorID;
    CARD32 productID;
    CARD32 canGenerate;         
    CARD32 numRegisters;
    CARD32 numValuators;
    CARD32 numButtons;
    
} rfbGIIDeviceCreateMsg;

#define sz_rfbGIIDeviceCreateMsg 60

typedef struct _rfbGIIDeviceDestroyMsg {
    CARD8 type;                 
    CARD8 endianAndSubType;     
    CARD16 length;              
    CARD32 deviceOrigin;
} rfbGIIDeviceDestroyMsg;

#define sz_rfbGIIDeviceDestroyMsg 8

#define rfbGIIKeyPress         5
#define rfbGIIKeyRelease       6
#define rfbGIIKeyRepeat        7
#define rfbGIIMoveRelative     8
#define rfbGIIMoveAbsolute     9
#define rfbGIIButtonPress      10
#define rfbGIIButtonRelease    11
#define rfbGIIValuatorRelative 12
#define rfbGIIValuatorAbsolute 13

typedef struct _rfbGIIKeyEvent {
    CARD8 eventSize;            
    CARD8 eventType;            
    CARD16 pad;
    CARD32 deviceOrigin;
    CARD32 modifiers;
    CARD32 symbol;
    CARD32 label;
    CARD32 button;
} rfbGIIKeyEvent;

#define sz_rfbGIIKeyEvent 24

typedef struct _rfbGIIMoveEvent {
    CARD8 eventSize;            
    CARD8 eventType;            
    CARD16 pad;
    CARD32 deviceOrigin;
    INT32 x;
    INT32 y;
    INT32 z;
    INT32 wheel;
} rfbGIIMoveEvent;

#define sz_rfbGIIMoveEvent 24

typedef struct _rfbGIIButtonEvent {
    CARD8 eventSize;            
    CARD8 eventType;            
    CARD16 pad;
    CARD32 deviceOrigin;
    CARD32 buttonNumber;
} rfbGIIButtonEvent;

#define sz_rfbGIIButtonEvent 12

typedef struct _rfbGIIValuatorEvent {
    CARD8 eventSize;            
    CARD8 eventType;            
    CARD16 pad;
    CARD32 deviceOrigin;
    CARD32 first;
    CARD32 count;
    
} rfbGIIValuatorEvent;

#define sz_rfbGIIValuatorEvent 16

typedef struct _rfbGIIEventMsg {
    CARD8 type;                 
    CARD8 endianAndSubType;     
    CARD16 length;
    
} rfbGIIEventMsg;

#define sz_rfbGIIEventMsg 4

#define rfbQEMUExtendedKeyEvent 0

typedef struct _rfbQEMUExtendedKeyEventMsg {
    CARD8 type;                 
    CARD8 subType;              
    CARD16 down;                
    CARD32 keysym;              
    CARD32 keycode;             
} rfbQEMUExtendedKeyEventMsg;

#define sz_rfbQEMUExtendedKeyEventMsg 12

typedef union _rfbClientToServerMsg {
    CARD8 type;
    rfbSetPixelFormatMsg spf;
    rfbFixColourMapEntriesMsg fcme;
    rfbSetEncodingsMsg se;
    rfbFramebufferUpdateRequestMsg fur;
    rfbKeyEventMsg ke;
    rfbPointerEventMsg pe;
    rfbClientCutTextMsg cct;
    rfbFileListRequestMsg flr;
    rfbFileDownloadRequestMsg fdr;
    rfbFileUploadRequestMsg fupr;
    rfbFileUploadDataMsg fud;
    rfbFileDownloadCancelMsg fdc;
    rfbFileUploadFailedMsg fuf;
    rfbFileCreateDirRequestMsg fcdr;
    rfbEnableContinuousUpdatesMsg ecu;
    rfbFenceMsg f;
    rfbSetDesktopSizeMsg sds;
    rfbGIIClientVersionMsg giicv;
    rfbGIIDeviceCreateMsg giidc;
    rfbGIIDeviceDestroyMsg giidd;
    rfbGIIEventMsg giie;
    rfbQEMUExtendedKeyEventMsg qemueke;
} rfbClientToServerMsg;