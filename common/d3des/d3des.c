#include "d3des.h"

static void scrunch(unsigned char *, unsigned long *);
static void unscrun(unsigned long *, unsigned char *);
static void desfunc(unsigned long *, unsigned long *);
static void cookey(unsigned long *);

static unsigned long KnL[32] = { 0UL };

static unsigned short bytebit[8]        = {
        01, 02, 04, 010, 020, 040, 0100, 0200 };

static unsigned long bigbyte[24] = {
        0x800000UL,     0x400000UL,     0x200000UL,     0x100000UL,
        0x80000UL,      0x40000UL,      0x20000UL,      0x10000UL,
        0x8000UL,       0x4000UL,       0x2000UL,       0x1000UL,
        0x800UL,        0x400UL,        0x200UL,        0x100UL,
        0x80UL,         0x40UL,         0x20UL,         0x10UL,
        0x8UL,          0x4UL,          0x2UL,          0x1UL    };

/* Use the key schedule specified in the Standard (ANSI X3.92-1981). */

static unsigned char pc1[56] = {
        56, 48, 40, 32, 24, 16,  8,      0, 57, 49, 41, 33, 25, 17,
         9,  1, 58, 50, 42, 34, 26,     18, 10,  2, 59, 51, 43, 35,
        62, 54, 46, 38, 30, 22, 14,      6, 61, 53, 45, 37, 29, 21,
        13,  5, 60, 52, 44, 36, 28,     20, 12,  4, 27, 19, 11,  3 };

static unsigned char totrot[16] = {
        1,2,4,6,8,10,12,14,15,17,19,21,23,25,27,28 };

static unsigned char pc2[48] = {
        13, 16, 10, 23,  0,  4,  2, 27, 14,  5, 20,  9,
        22, 18, 11,  3, 25,  7, 15,  6, 26, 19, 12,  1,
        40, 51, 30, 36, 46, 54, 29, 39, 50, 44, 32, 47,
        43, 48, 38, 55, 33, 52, 45, 41, 49, 35, 28, 31 };

/* Thanks to James Gillogly & Phil Karn! */
void deskey(unsigned char *key, int edf)
{
        register int i, j, l, m, n;
        unsigned char pc1m[56], pcr[56];
        unsigned long kn[32];

        for ( j = 0; j < 56; j++ ) {
                l = pc1[j];
                m = l & 07;
                pc1m[j] = (key[l >> 3] & bytebit[m]) ? 1 : 0;
                }
        for( i = 0; i < 16; i++ ) {
                if( edf == DE1 ) m = (15 - i) << 1;
                else m = i << 1;
                n = m + 1;
                kn[m] = kn[n] = 0UL;
                for( j = 0; j < 28; j++ ) {
                        l = j + totrot[i];
                        if( l < 28 ) pcr[j] = pc1m[l];
                        else pcr[j] = pc1m[l - 28];
                        }
                for( j = 28; j < 56; j++ ) {
                    l = j + totrot[i];
                    if( l < 56 ) pcr[j] = pc1m[l];
                    else pcr[j] = pc1m[l - 28];
                    }
                for( j = 0; j < 24; j++ ) {
                        if( pcr[pc2[j]] ) kn[m] |= bigbyte[j];
                        if( pcr[pc2[j+24]] ) kn[n] |= bigbyte[j];
                        }
                }
        cookey(kn);
        return;
        }

static void cookey(register unsigned long *raw1)
{
        register unsigned long *cook, *raw0;
        unsigned long dough[32];
        register int i;

        cook = dough;
        for( i = 0; i < 16; i++, raw1++ ) {
                raw0 = raw1++;
                *cook    = (*raw0 & 0x00fc0000UL) << 6;
                *cook   |= (*raw0 & 0x00000fc0UL) << 10;
                *cook   |= (*raw1 & 0x00fc0000UL) >> 10;
                *cook++ |= (*raw1 & 0x00000fc0UL) >> 6;
                *cook    = (*raw0 & 0x0003f000UL) << 12;
                *cook   |= (*raw0 & 0x0000003fUL) << 16;
                *cook   |= (*raw1 & 0x0003f000UL) >> 4;
                *cook++ |= (*raw1 & 0x0000003fUL);
                }
        usekey(dough);
        return;
        }

void cpkey(register unsigned long *into)
{
        register unsigned long *from, *endp;

        from = KnL, endp = &KnL[32];
        while( from < endp ) *into++ = *from++;
        return;
        }

void usekey(register unsigned long *from)
{
        register unsigned long *to, *endp;

        to = KnL, endp = &KnL[32];
        while( to < endp ) *to++ = *from++;
        return;
        }

void des(unsigned char *inblock, unsigned char *outblock)
{
        unsigned long work[2];

        scrunch(inblock, work);
        desfunc(work, KnL);
        unscrun(work, outblock);
        return;
        }

static void scrunch(register unsigned char *outof,
                    register unsigned long *into)
{
        *into    = (*outof++ & 0xffUL) << 24;
        *into   |= (*outof++ & 0xffUL) << 16;
        *into   |= (*outof++ & 0xffUL) << 8;
        *into++ |= (*outof++ & 0xffUL);
        *into    = (*outof++ & 0xffUL) << 24;
        *into   |= (*outof++ & 0xffUL) << 16;
        *into   |= (*outof++ & 0xffUL) << 8;
        *into   |= (*outof   & 0xffUL);
        return;
        }

static void unscrun(register unsigned long *outof,
                    register unsigned char *into)
{
        *into++ = (*outof >> 24) & 0xffUL;
        *into++ = (*outof >> 16) & 0xffUL;
        *into++ = (*outof >>  8) & 0xffUL;
        *into++ =  *outof++      & 0xffUL;
        *into++ = (*outof >> 24) & 0xffUL;
        *into++ = (*outof >> 16) & 0xffUL;
        *into++ = (*outof >>  8) & 0xffUL;
        *into   =  *outof        & 0xffUL;
        return;
        }

static unsigned long SP1[64] = {
        0x01010400UL, 0x00000000UL, 0x00010000UL, 0x01010404UL,
        0x01010004UL, 0x00010404UL, 0x00000004UL, 0x00010000UL,
        0x00000400UL, 0x01010400UL, 0x01010404UL, 0x00000400UL,
        0x01000404UL, 0x01010004UL, 0x01000000UL, 0x00000004UL,
        0x00000404UL, 0x01000400UL, 0x01000400UL, 0x00010400UL,
        0x00010400UL, 0x01010000UL, 0x01010000UL, 0x01000404UL,
        0x00010004UL, 0x01000004UL, 0x01000004UL, 0x00010004UL,
        0x00000000UL, 0x00000404UL, 0x00010404UL, 0x01000000UL,
        0x00010000UL, 0x01010404UL, 0x00000004UL, 0x01010000UL,
        0x01010400UL, 0x01000000UL, 0x01000000UL, 0x00000400UL,
        0x01010004UL, 0x00010000UL, 0x00010400UL, 0x01000004UL,
        0x00000400UL, 0x00000004UL, 0x01000404UL, 0x00010404UL,
        0x01010404UL, 0x00010004UL, 0x01010000UL, 0x01000404UL,
        0x01000004UL, 0x00000404UL, 0x00010404UL, 0x01010400UL,
        0x00000404UL, 0x01000400UL, 0x01000400UL, 0x00000000UL,
        0x00010004UL, 0x00010400UL, 0x00000000UL, 0x01010004UL };

static unsigned long SP2[64] = {
        0x80108020UL, 0x80008000UL, 0x00008000UL, 0x00108020UL,
        0x00100000UL, 0x00000020UL, 0x80100020UL, 0x80008020UL,
        0x80000020UL, 0x80108020UL, 0x80108000UL, 0x80000000UL,
        0x80008000UL, 0x00100000UL, 0x00000020UL, 0x80100020UL,
        0x00108000UL, 0x00100020UL, 0x80008020UL, 0x00000000UL,
        0x80000000UL, 0x00008000UL, 0x00108020UL, 0x80100000UL,
        0x00100020UL, 0x80000020UL, 0x00000000UL, 0x00108000UL,
        0x00008020UL, 0x80108000UL, 0x80100000UL, 0x00008020UL,
        0x00000000UL, 0x00108020UL, 0x80100020UL, 0x00100000UL,
        0x80008020UL, 0x80100000UL, 0x80108000UL, 0x00008000UL,
        0x80100000UL, 0x80008000UL, 0x00000020UL, 0x80108020UL,
        0x00108020UL, 0x00000020UL, 0x00008000UL, 0x80000000UL,
        0x00008020UL, 0x80108000UL, 0x00100000UL, 0x80000020UL,
        0x00100020UL, 0x80008020UL, 0x80000020UL, 0x00100020UL,
        0x00108000UL, 0x00000000UL, 0x80008000UL, 0x00008020UL,
        0x80000000UL, 0x80100020UL, 0x80108020UL, 0x00108000UL };

static unsigned long SP3[64] = {
        0x00000208UL, 0x08020200UL, 0x00000000UL, 0x08020008UL,
        0x08000200UL, 0x00000000UL, 0x00020208UL, 0x08000200UL,
        0x00020008UL, 0x08000008UL, 0x08000008UL, 0x00020000UL,
        0x08020208UL, 0x00020008UL, 0x08020000UL, 0x00000208UL,
        0x08000000UL, 0x00000008UL, 0x08020200UL, 0x00000200UL,
        0x00020200UL, 0x08020000UL, 0x08020008UL, 0x00020208UL,
        0x08000208UL, 0x00020200UL, 0x00020000UL, 0x08000208UL,
        0x00000008UL, 0x08020208UL, 0x00000200UL, 0x08000000UL,
        0x08020200UL, 0x08000000UL, 0x00020008UL, 0x00000208UL,
        0x00020000UL, 0x08020200UL, 0x08000200UL, 0x00000000UL,
        0x00000200UL, 0x00020008UL, 0x08020208UL, 0x08000200UL,
        0x08000008UL, 0x00000200UL, 0x00000000UL, 0x08020008UL,
        0x08000208UL, 0x00020000UL, 0x08000000UL, 0x08020208UL,
        0x00000008UL, 0x00020208UL, 0x00020200UL, 0x08000008UL,
        0x08020000UL, 0x08000208UL, 0x00000208UL, 0x08020000UL,
        0x00020208UL, 0x00000008UL, 0x08020008UL, 0x00020200UL };

static unsigned long SP4[64] = {
        0x00802001UL, 0x00002081UL, 0x00002081UL, 0x00000080UL,
        0x00802080UL, 0x00800081UL, 0x00800001UL, 0x00002001UL,
        0x00000000UL, 0x00802000UL, 0x00802000UL, 0x00802081UL,
        0x00000081UL, 0x00000000UL, 0x00800080UL, 0x00800001UL,
        0x00000001UL, 0x00002000UL, 0x00800000UL, 0x00802001UL,
        0x00000080UL, 0x00800000UL, 0x00002001UL, 0x00002080UL,
        0x00800081UL, 0x00000001UL, 0x00002080UL, 0x00800080UL,
        0x00002000UL, 0x00802080UL, 0x00802081UL, 0x00000081UL,
        0x00800080UL, 0x00800001UL, 0x00802000UL, 0x00802081UL,
        0x00000081UL, 0x00000000UL, 0x00000000UL, 0x00802000UL,
        0x00002080UL, 0x00800080UL, 0x00800081UL, 0x00000001UL,
        0x00802001UL, 0x00002081UL, 0x00002081UL, 0x00000080UL,
        0x00802081UL, 0x00000081UL, 0x00000001UL, 0x00002000UL,
        0x00800001UL, 0x00002001UL, 0x00802080UL, 0x00800081UL,
        0x00002001UL, 0x00002080UL, 0x00800000UL, 0x00802001UL,
        0x00000080UL, 0x00800000UL, 0x00002000UL, 0x00802080UL };

static unsigned long SP5[64] = {
        0x00000100UL, 0x02080100UL, 0x02080000UL, 0x42000100UL,
        0x00080000UL, 0x00000100UL, 0x40000000UL, 0x02080000UL,
        0x40080100UL, 0x00080000UL, 0x02000100UL, 0x40080100UL,
        0x42000100UL, 0x42080000UL, 0x00080100UL, 0x40000000UL,
        0x02000000UL, 0x40080000UL, 0x40080000UL, 0x00000000UL,
        0x40000100UL, 0x42080100UL, 0x42080100UL, 0x02000100UL,
        0x42080000UL, 0x40000100UL, 0x00000000UL, 0x42000000UL,
        0x02080100UL, 0x02000000UL, 0x42000000UL, 0x00080100UL,
        0x00080000UL, 0x42000100UL, 0x00000100UL, 0x02000000UL,
        0x40000000UL, 0x02080000UL, 0x42000100UL, 0x40080100UL,
        0x02000100UL, 0x40000000UL, 0x42080000UL, 0x02080100UL,
        0x40080100UL, 0x00000100UL, 0x02000000UL, 0x42080000UL,
        0x42080100UL, 0x00080100UL, 0x42000000UL, 0x42080100UL,
        0x02080000UL, 0x00000000UL, 0x40080000UL, 0x42000000UL,
        0x00080100UL, 0x02000100UL, 0x40000100UL, 0x00080000UL,
        0x00000000UL, 0x40080000UL, 0x02080100UL, 0x40000100UL };

static unsigned long SP6[64] = {
        0x20000010UL, 0x20400000UL, 0x00004000UL, 0x20404010UL,
        0x20400000UL, 0x00000010UL, 0x20404010UL, 0x00400000UL,
        0x20004000UL, 0x00404010UL, 0x00400000UL, 0x20000010UL,
        0x00400010UL, 0x20004000UL, 0x20000000UL, 0x00004010UL,
        0x00000000UL, 0x00400010UL, 0x20004010UL, 0x00004000UL,
        0x00404000UL, 0x20004010UL, 0x00000010UL, 0x20400010UL,
        0x20400010UL, 0x00000000UL, 0x00404010UL, 0x20404000UL,
        0x00004010UL, 0x00404000UL, 0x20404000UL, 0x20000000UL,
        0x20004000UL, 0x00000010UL, 0x20400010UL, 0x00404000UL,
        0x20404010UL, 0x00400000UL, 0x00004010UL, 0x20000010UL,
        0x00400000UL, 0x20004000UL, 0x20000000UL, 0x00004010UL,
        0x20000010UL, 0x20404010UL, 0x00404000UL, 0x20400000UL,
        0x00404010UL, 0x20404000UL, 0x00000000UL, 0x20400010UL,
        0x00000010UL, 0x00004000UL, 0x20400000UL, 0x00404010UL,
        0x00004000UL, 0x00400010UL, 0x20004010UL, 0x00000000UL,
        0x20404000UL, 0x20000000UL, 0x00400010UL, 0x20004010UL };

static unsigned long SP7[64] = {
        0x00200000UL, 0x04200002UL, 0x04000802UL, 0x00000000UL,
        0x00000800UL, 0x04000802UL, 0x00200802UL, 0x04200800UL,
        0x04200802UL, 0x00200000UL, 0x00000000UL, 0x04000002UL,
        0x00000002UL, 0x04000000UL, 0x04200002UL, 0x00000802UL,
        0x04000800UL, 0x00200802UL, 0x00200002UL, 0x04000800UL,
        0x04000002UL, 0x04200000UL, 0x04200800UL, 0x00200002UL,
        0x04200000UL, 0x00000800UL, 0x00000802UL, 0x04200802UL,
        0x00200800UL, 0x00000002UL, 0x04000000UL, 0x00200800UL,
        0x04000000UL, 0x00200800UL, 0x00200000UL, 0x04000802UL,
        0x04000802UL, 0x04200002UL, 0x04200002UL, 0x00000002UL,
        0x00200002UL, 0x04000000UL, 0x04000800UL, 0x00200000UL,
        0x04200800UL, 0x00000802UL, 0x00200802UL, 0x04200800UL,
        0x00000802UL, 0x04000002UL, 0x04200802UL, 0x04200000UL,
        0x00200800UL, 0x00000000UL, 0x00000002UL, 0x04200802UL,
        0x00000000UL, 0x00200802UL, 0x04200000UL, 0x00000800UL,
        0x04000002UL, 0x04000800UL, 0x00000800UL, 0x00200002UL };

static unsigned long SP8[64] = {
        0x10001040UL, 0x00001000UL, 0x00040000UL, 0x10041040UL,
        0x10000000UL, 0x10001040UL, 0x00000040UL, 0x10000000UL,
        0x00040040UL, 0x10040000UL, 0x10041040UL, 0x00041000UL,
        0x10041000UL, 0x00041040UL, 0x00001000UL, 0x00000040UL,
        0x10040000UL, 0x10000040UL, 0x10001000UL, 0x00001040UL,
        0x00041000UL, 0x00040040UL, 0x10040040UL, 0x10041000UL,
        0x00001040UL, 0x00000000UL, 0x00000000UL, 0x10040040UL,
        0x10000040UL, 0x10001000UL, 0x00041040UL, 0x00040000UL,
        0x00041040UL, 0x00040000UL, 0x10041000UL, 0x00001000UL,
        0x00000040UL, 0x10040040UL, 0x00001000UL, 0x00041040UL,
        0x10001000UL, 0x00000040UL, 0x10000040UL, 0x10040000UL,
        0x10040040UL, 0x10000000UL, 0x00040000UL, 0x10001040UL,
        0x00000000UL, 0x10041040UL, 0x00040040UL, 0x10000040UL,
        0x10040000UL, 0x10001000UL, 0x10001040UL, 0x00000000UL,
        0x10041040UL, 0x00041000UL, 0x00041000UL, 0x00001040UL,
        0x00001040UL, 0x00040040UL, 0x10000000UL, 0x10041000UL };

static void desfunc(register unsigned long *block,
                    register unsigned long *keys)
{
        register unsigned long fval, work, right, leftt;
        register int round;

        leftt = block[0];
        right = block[1];
        work = ((leftt >> 4) ^ right) & 0x0f0f0f0fUL;
        right ^= work;
        leftt ^= (work << 4);
        work = ((leftt >> 16) ^ right) & 0x0000ffffUL;
        right ^= work;
        leftt ^= (work << 16);
        work = ((right >> 2) ^ leftt) & 0x33333333UL;
        leftt ^= work;
        right ^= (work << 2);
        work = ((right >> 8) ^ leftt) & 0x00ff00ffUL;
        leftt ^= work;
        right ^= (work << 8);
        right = ((right << 1) | ((right >> 31) & 1UL)) & 0xffffffffUL;
        work = (leftt ^ right) & 0xaaaaaaaaUL;
        leftt ^= work;
        right ^= work;
        leftt = ((leftt << 1) | ((leftt >> 31) & 1UL)) & 0xffffffffUL;

        for( round = 0; round < 8; round++ ) {
                work  = (right << 28) | (right >> 4);
                work ^= *keys++;
                fval  = SP7[ work                & 0x3fUL];
                fval |= SP5[(work >>  8) & 0x3fUL];
                fval |= SP3[(work >> 16) & 0x3fUL];
                fval |= SP1[(work >> 24) & 0x3fUL];
                work  = right ^ *keys++;
                fval |= SP8[ work                & 0x3fUL];
                fval |= SP6[(work >>  8) & 0x3fUL];
                fval |= SP4[(work >> 16) & 0x3fUL];
                fval |= SP2[(work >> 24) & 0x3fUL];
                leftt ^= fval;
                work  = (leftt << 28) | (leftt >> 4);
                work ^= *keys++;
                fval  = SP7[ work                & 0x3fUL];
                fval |= SP5[(work >>  8) & 0x3fUL];
                fval |= SP3[(work >> 16) & 0x3fUL];
                fval |= SP1[(work >> 24) & 0x3fUL];
                work  = leftt ^ *keys++;
                fval |= SP8[ work                & 0x3fUL];
                fval |= SP6[(work >>  8) & 0x3fUL];
                fval |= SP4[(work >> 16) & 0x3fUL];
                fval |= SP2[(work >> 24) & 0x3fUL];
                right ^= fval;
                }

        right = (right << 31) | (right >> 1);
        work = (leftt ^ right) & 0xaaaaaaaaUL;
        leftt ^= work;
        right ^= work;
        leftt = (leftt << 31) | (leftt >> 1);
        work = ((leftt >> 8) ^ right) & 0x00ff00ffUL;
        right ^= work;
        leftt ^= (work << 8);
        work = ((leftt >> 2) ^ right) & 0x33333333UL;
        right ^= work;
        leftt ^= (work << 2);
        work = ((right >> 16) ^ leftt) & 0x0000ffffUL;
        leftt ^= work;
        right ^= (work << 16);
        work = ((right >> 4) ^ leftt) & 0x0f0f0f0fUL;
        leftt ^= work;
        right ^= (work << 4);
        *block++ = right;
        *block = leftt;
        return;
        }