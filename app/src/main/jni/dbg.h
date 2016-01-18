#ifndef __dbg_h
#define __dbg_h

#include <stdio.h>

#ifndef NDEBUG
#define debug(M, ...)
#else
#define debug(M, ...) fprintf(stderr, "DEBUG %s (in function '%s'):%d:  " M "\n", __FILE__, __FUNCTION__, __LINE__, ##__VA_ARGS__)
#endif

#define jump_unless(A) if (!(A)) { goto error; }
#define error_unless(A, M, ...) if (!(A)) { fprintf(stderr, M "\n", ##__VA_ARGS__); goto error; }
#define jump_to_error_if(A) if (A) { goto error; }
#define jump_to_error_unless(A) if (!(A)) { goto error; }
#endif
