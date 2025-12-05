#define snprintf(str, n, format, ...) \
  _snprintf_s(str, n, _TRUNCATE, format, __VA_ARGS__)

#define SPRINTF(str, format, ...) \
  snprintf(str, _countof(str), format, __VA_ARGS__)

#define STRNCAT(dst, src, n) \
  strncat_s(dst, n, src, _TRUNCATE)

#define STRCAT(dst, src) \
  STRNCAT(dst, src, _countof(dst))

#define STRNCPY(dst, src, n) \
  strncpy_s(dst, n, src, _TRUNCATE)

#define STRCPY(dst, src) \
  STRNCPY(dst, src, _countof(dst))

extern "C" char *strsep(char **stringp, const char *delim);
