#include <jni.h>
#include <android/log.h>
#include <unistd.h>
#include <cstdlib>
#include <sys/time.h>
#include <time.h>


#define LOG_TAG "RAM"


#define MAX_THREADS 8
int g_RamStatus[MAX_THREADS] = {0, 0, 0, 0, 0, 0, 0, 0};
int g_RamLoops[MAX_THREADS] = {0, 0, 0, 0, 0, 0, 0, 0};


static bool TestZeros(int threadNum, uint32_t * buffer, int memoryCount) {
    int i;

    // Fill memory with 0's
    for(i = 0; i < memoryCount; i++)
        buffer[i] = 0;

    // Read back memory and check
    for(i = 0; i < memoryCount; i++)
        if(0 != buffer[i])
            return false;

    g_RamStatus[threadNum]++;
    return true;
}


static bool TestOnes(int threadNum, uint32_t * buffer, int memoryCount) {
    int i;

    // Fill memory with 0's
    for(i = 0; i < memoryCount; i++)
        buffer[i] = 0xFFFFFFFF;

    // Read back memory and check
    for(i = 0; i < memoryCount; i++)
        if(0xFFFFFFFF != buffer[i])
            return false;

    g_RamStatus[threadNum]++;
    return true;
}


static bool TestAddress(int threadNum, uint32_t * buffer, int memoryCount) {
    int i;

    for(i = 0; i < memoryCount; i++)
        buffer[i] = (uint32_t)i;

    // Read back memory and check
    for(i = 0; i < memoryCount; i++)
        if((uint32_t)i != buffer[i])
            return false;

    g_RamStatus[threadNum]++;
    return true;
}


static bool TestQuickBits(int threadNum, uint32_t * buffer, int memoryCount) {
    int i;

    // Fill memory with 5's and A's
    for(i = 0; i < memoryCount; i++)
    {
        if(i & 1)
            buffer[i] = 0x55555555;
        else
            buffer[i] = 0xAAAAAAAA;
    }

    // Read back memory and check
    for(i = 0; i < memoryCount; i++) {
        if (i & 1) {
            if (0x55555555 != buffer[i])
                return false;
        } else {
            if (0xAAAAAAAA != buffer[i])
                return false;
        }
    }
    g_RamStatus[threadNum]++;

    // Fill memory with 5's and A's
    for(i = 0; i < memoryCount; i++)
    {
        if(i & 1)
            buffer[i] = 0xAAAAAAAA;
        else
            buffer[i] = 0x55555555;
    }

    // Read back memory and check
    for(i = 0; i < memoryCount; i++) {
        if (i & 1) {
            if (0xAAAAAAAA != buffer[i])
                return false;
        } else {
            if (0x55555555 != buffer[i])
                return false;
        }
    }
    g_RamStatus[threadNum]++;


    // Fill memory with 0's and F's
    for(i = 0; i < memoryCount; i++)
    {
        if(i & 1)
            buffer[i] = 0;
        else
            buffer[i] = 0xFFFFFFFF;
    }

    // Read back memory and check
    for(i = 0; i < memoryCount; i++) {
        if (i & 1) {
            if (0 != buffer[i])
                return false;
        } else {
            if (0xFFFFFFFF != buffer[i])
                return false;
        }
    }
    g_RamStatus[threadNum]++;


    // Fill memory with 0's and F's
    for(i = 0; i < memoryCount; i++)
    {
        if(i & 1)
            buffer[i] = 0xFFFFFFFF;
        else
            buffer[i] = 0;
    }

    // Read back memory and check
    for(i = 0; i < memoryCount; i++) {
        if (i & 1) {
            if (0xFFFFFFFF != buffer[i])
                return false;
        } else {
            if (0 != buffer[i])
                return false;
        }
    }
    g_RamStatus[threadNum]++;

    return true;
}


static bool TestWalkingOnes(int threadNum, uint32_t * buffer, int memoryCount) {
    int i;
    uint32_t value = 1;
    for(i = 0; i < memoryCount; i++)
    {
        buffer[i] = value;
        if(0x80000000 == value)
            value = 1;
        else
            value = value << 1;
    }

    value = 1;
    for(i = 0; i < memoryCount; i++)
    {
        if(value != buffer[i])
            return false;
        if(0x80000000 == value)
            value = 1;
        else
            value = value << 1;
    }
    g_RamStatus[threadNum]++;
    return true;
}


static bool TestWalkingZeros(int threadNum, uint32_t * buffer, int memoryCount) {
    int i;
    uint32_t value = 1;
    for(i = 0; i < memoryCount; i++)
    {
        buffer[i] = ~value;
        if(0x80000000 == value)
            value = 1;
        else
            value = value << 1;
    }

    value = 1;
    for(i = 0; i < memoryCount; i++)
    {
        if(~value != buffer[i])
            return false;
        if(0x80000000 == value)
            value = 1;
        else
            value = value << 1;
    }
    g_RamStatus[threadNum]++;
    return true;
}


// Single Shot == Fill all of ram before reading it back
static bool RamStressTest(int threadNum, uint32_t * buffer, int memoryCount, bool bSingleShot, uint32_t pattern0, uint32_t pattern1, uint32_t pattern2, uint32_t pattern3, uint32_t pattern4, uint32_t pattern5, uint32_t pattern6, uint32_t pattern7)
{
    uint32_t readData[8];
    int i;
    uint32_t * pCurrentAddress;

    if(bSingleShot) {
        // Write the test data to the entire RAM range
        pCurrentAddress = buffer;
        for(i = 0; i < memoryCount; i += 8) {
            pCurrentAddress[0] = pattern0;
            pCurrentAddress[1] = pattern1;
            pCurrentAddress[2] = pattern2;
            pCurrentAddress[3] = pattern3;
            pCurrentAddress[4] = pattern4;
            pCurrentAddress[5] = pattern5;
            pCurrentAddress[6] = pattern6;
            pCurrentAddress[7] = pattern7;
            pCurrentAddress += 8;
        }

        // Read back all of it and check it for errors
        pCurrentAddress = buffer;
        for(i = 0; i < memoryCount; i += 8) {
            readData[0] = pCurrentAddress[0];
            readData[1] = pCurrentAddress[1];
            readData[2] = pCurrentAddress[2];
            readData[3] = pCurrentAddress[3];
            readData[4] = pCurrentAddress[4];
            readData[5] = pCurrentAddress[5];
            readData[6] = pCurrentAddress[6];
            readData[7] = pCurrentAddress[7];

            if( readData[0] != pattern0 ||
                readData[1] != pattern1 ||
                readData[2] != pattern2 ||
                readData[3] != pattern3 ||
                readData[4] != pattern4 ||
                readData[5] != pattern5 ||
                readData[6] != pattern6 ||
                readData[7] != pattern7 ) {
                return false;
            }
            pCurrentAddress += 8;
        }
    } else {
        // Write and read the test data in blocks
        pCurrentAddress = buffer;
        for(i = 0; i < memoryCount; i += 8) {
            pCurrentAddress[0] = pattern0;
            pCurrentAddress[1] = pattern1;
            pCurrentAddress[2] = pattern2;
            pCurrentAddress[3] = pattern3;
            pCurrentAddress[4] = pattern4;
            pCurrentAddress[5] = pattern5;
            pCurrentAddress[6] = pattern6;
            pCurrentAddress[7] = pattern7;

            readData[0] = pCurrentAddress[0];
            readData[1] = pCurrentAddress[1];
            readData[2] = pCurrentAddress[2];
            readData[3] = pCurrentAddress[3];
            readData[4] = pCurrentAddress[4];
            readData[5] = pCurrentAddress[5];
            readData[6] = pCurrentAddress[6];
            readData[7] = pCurrentAddress[7];

            if( readData[0] != pattern0 ||
                readData[1] != pattern1 ||
                readData[2] != pattern2 ||
                readData[3] != pattern3 ||
                readData[4] != pattern4 ||
                readData[5] != pattern5 ||
                readData[6] != pattern6 ||
                readData[7] != pattern7 ) {
                return false;
            }

            pCurrentAddress += 8;
        }
    }

    g_RamStatus[threadNum]++;
    return true;
}


extern "C" JNIEXPORT void JNICALL
Java_com_junipersys_a3_1chamber_1test_RamTest_startRamTest(
        JNIEnv * /*env*/,
        jobject /* this */,
        jint threadNum,
        jint memorySize,
        jint numLoops) {

    if(threadNum >= MAX_THREADS || threadNum < 0)
        return;

    uint32_t * buffer = nullptr;
    int memoryCount = memorySize / sizeof(uint32_t);
    try {
        buffer = new uint32_t[memoryCount];
    } catch (...) {
        g_RamStatus[threadNum] = -1;
        return;
    }

    for(g_RamLoops[threadNum] = 0; g_RamLoops[threadNum] < numLoops; g_RamLoops[threadNum]++) {
        // Indicate we have allocated space
        g_RamStatus[threadNum] = 1;

        if (!TestZeros(threadNum, buffer, memoryCount)) {
            g_RamStatus[threadNum] = -2;
            break;
        }
        if (!TestOnes(threadNum, buffer, memoryCount)) {
            g_RamStatus[threadNum] = -3;
            break;
        }
        if (!TestAddress(threadNum, buffer, memoryCount)) {
            g_RamStatus[threadNum] = -4;
            break;
        }
        if (!TestQuickBits(threadNum, buffer, memoryCount)) {
            g_RamStatus[threadNum] = -5;
            break;
        }
        if (!TestWalkingOnes(threadNum, buffer, memoryCount)) {
            g_RamStatus[threadNum] = -6;
            break;
        }
        if (!TestWalkingZeros(threadNum, buffer, memoryCount)) {
            g_RamStatus[threadNum] = -7;
            break;
        }

        if (!RamStressTest(threadNum, buffer, memoryCount, true, 0, 0, 0xFFFFFFFF, 0xFFFFFFFF, 0, 0, 0xFFFFFFFF,
                           0xFFFFFFFF)) {
            g_RamStatus[threadNum] = -8;
            break;
        }
        if (!RamStressTest(threadNum, buffer, memoryCount, true, 0xFFFFFFFF, 0xFFFFFFFF, 0, 0, 0xFFFFFFFF,
                           0xFFFFFFFF, 0, 0)) {
            g_RamStatus[threadNum] = -9;
            break;
        }
        if (!RamStressTest(threadNum, buffer, memoryCount, true, 0, 0xFFFFFFFF, 0, 0xFFFFFFFF, 0, 0xFFFFFFFF, 0,
                           0xFFFFFFFF)) {
            g_RamStatus[threadNum] = -10;
            break;
        }
        if (!RamStressTest(threadNum, buffer, memoryCount, true, 0xFFFFFFFF, 0, 0xFFFFFFFF, 0, 0xFFFFFFFF, 0,
                           0xFFFFFFFF, 0)) {
            g_RamStatus[threadNum] = -11;
            break;
        }
        if (!RamStressTest(threadNum, buffer, memoryCount, true, 0x55555555, 0x55555555, 0xAAAAAAAA, 0xAAAAAAAA,
                           0x55555555, 0x55555555, 0xAAAAAAAA, 0xAAAAAAAA)) {
            g_RamStatus[threadNum] = -12;
            break;
        }
        if (!RamStressTest(threadNum, buffer, memoryCount, true, 0xAAAAAAAA, 0xAAAAAAAA, 0x55555555, 0x55555555,
                           0xAAAAAAAA, 0xAAAAAAAA, 0x55555555, 0x55555555)) {
            g_RamStatus[threadNum] = -13;
            break;
        }
        if (!RamStressTest(threadNum, buffer, memoryCount, true, 0x55555555, 0xAAAAAAAA, 0x55555555, 0xAAAAAAAA,
                           0x55555555, 0xAAAAAAAA, 0x55555555, 0xAAAAAAAA)) {
            g_RamStatus[threadNum] = -14;
            break;
        }
        if (!RamStressTest(threadNum, buffer, memoryCount, true, 0xAAAAAAAA, 0x55555555, 0xAAAAAAAA, 0x55555555,
                           0xAAAAAAAA, 0x55555555, 0xAAAAAAAA, 0x55555555)) {
            g_RamStatus[threadNum] = -15;
            break;
        }

        // The following hard-coded random looking tests are ones that have failed on a rev03 board
        // when the RAM driver strengths were set lower (40 ohm)
        if (!RamStressTest(threadNum, buffer, memoryCount, true, 0x4F3F58BB, 0x0F057388, 0x175344D6, 0x7AC02A31,
                           0x788C0726, 0x7206FA35, 0x61991C03, 0x1A5825AE)) {
            g_RamStatus[threadNum] = -16;
            break;
        }

        if (!RamStressTest(threadNum, buffer, memoryCount, true, 0x13F33EBB, 0x05160844, 0x647C7CA7, 0x62CA545E,
                           0x4A0BBBB5, 0x759AEB84, 0x306784A8, 0x568F3BB1)) {
            g_RamStatus[threadNum] = -17;
            break;
        }

        if (!RamStressTest(threadNum, buffer, memoryCount, true, 0x574F6AD7, 0x5A43D12D, 0x607DCDA9, 0x372DD4B6,
                           0x2C62F31D, 0x79585232, 0x259662C5, 0x20A8C821)) {
            g_RamStatus[threadNum] = -18;
            break;
        }

        if (!RamStressTest(threadNum, buffer, memoryCount, true, 0x3D5AE3FB, 0x26A4D773, 0x684E5BAB, 0x0D27BF9B,
                           0x214825B8, 0x08F9F903, 0x1B9CE5D3, 0x7782B195)) {
            g_RamStatus[threadNum] = -19;
            break;
        }

        if (!RamStressTest(threadNum, buffer, memoryCount, true, 0x700955DD, 0x1C478274, 0x6279CE44, 0x7DD6B77B,
                           0x06D8FA1D, 0x0A420E67, 0x1FEBBFD7, 0x1946BF32)) {
            g_RamStatus[threadNum] = -20;
            break;
        }

        if (!RamStressTest(threadNum, buffer, memoryCount, true, 0x378A2F3B, 0x55B88A16, 0x3A7BECAE, 0x79DCB8ED,
                           0x1C5D64CB, 0x429E1353, 0x5BB4EADC, 0x04462236)) {
            g_RamStatus[threadNum] = -21;
            break;
        }

        if (!RamStressTest(threadNum, buffer, memoryCount, true, 0x27E0DA67, 0x0AB4DCAE, 0x4F53412F, 0x60AE19F9,
                           0x64776FF6, 0x624B5CF1, 0x052CA8DA, 0x659FF640)) {
            g_RamStatus[threadNum] = -22;
            break;
        }

        if (!RamStressTest(threadNum, buffer, memoryCount, true, 0x715B75FB, 0x42C4F032, 0x30B36009, 0x1732C5D8,
                           0x0FB1333A, 0x502B04AD, 0x325D1C36, 0x1CAF227A)) {
            g_RamStatus[threadNum] = -23;
            break;
        }

        if (!RamStressTest(threadNum, buffer, memoryCount, true, 0x10C090EC, 0x7E5B3E58, 0x7AAF9E3C, 0x0D179A9A,
                           0x1A100ED3, 0x7EF0821C, 0x29D683A2, 0x0A06AF2E)) {
            g_RamStatus[threadNum] = -24;
            break;
        }

        if (!RamStressTest(threadNum, buffer, memoryCount, true, 0x02D449E7, 0x5C722844, 0x3CC55B62, 0x41EBF8E2,
                           0x3053D85B, 0x56B8C9BD, 0x2985E146, 0x4044490F)) {
            g_RamStatus[threadNum] = -25;
            break;
        }

        if (!RamStressTest(threadNum, buffer, memoryCount, true, 0x58D9B45B, 0x6ACD8547, 0x1021E2D3, 0x69D2A9DB,
                           0x58880654, 0x4AA79971, 0x2E765731, 0x4FDA6E71)) {
            g_RamStatus[threadNum] = -26;
            break;
        }

        if (!RamStressTest(threadNum, buffer, memoryCount, true, 0x2DB64C7B, 0x50088F2D, 0x4007CDEB, 0x36044D9A,
                           0x52245979, 0x3D109A29, 0x0B6CA5AD, 0x46874F74)) {
            g_RamStatus[threadNum] = -27;
            break;
        }

        if (!RamStressTest(threadNum, buffer, memoryCount, true, 0x600288EF, 0x469D3B9B, 0x66B48119, 0x224D10D1,
                           0x58711BBF, 0x189FDF2C, 0x6B621B06, 0x7664A97A)) {
            g_RamStatus[threadNum] = -28;
            break;
        }

        if (!RamStressTest(threadNum, buffer, memoryCount, true, 0x6841DC7B, 0x308502E0, 0x69611211, 0x3D2EF47A,
                           0x77F227D8, 0x38670C94, 0x5C29E723, 0x184EA713)) {
            g_RamStatus[threadNum] = -29;
            __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "Error RAM stress test %d", g_RamStatus[threadNum]);
            break;
        }

        struct timespec res;
        clock_gettime(CLOCK_REALTIME, &res);
        srand((unsigned int) res.tv_nsec);

        for (int k = 0; k < 50; k++) {
            uint32_t data[8];
            data[0] = (uint32_t) rand();
            data[1] = (uint32_t) rand();
            data[2] = (uint32_t) rand();
            data[3] = (uint32_t) rand();
            data[4] = (uint32_t) rand();
            data[5] = (uint32_t) rand();
            data[6] = (uint32_t) rand();
            data[7] = (uint32_t) rand();

            if (!RamStressTest(threadNum, buffer, memoryCount, true, data[0], data[1], data[2], data[3],
                               data[4], data[5], data[6], data[7])) {
                g_RamStatus[threadNum] = -50 - k;
                __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,
                                    "Error RAM stress test 0x%08X, 0x%08X, 0x%08X, 0x%08X, 0x%08X, 0x%08X, 0x%08X, 0x%08X",
                                    data[0], data[1], data[2], data[3], data[4], data[5], data[6],
                                    data[7]);
                goto done;
            }
        }
    }

    if(g_RamStatus[threadNum] < 0)
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "Error RAM stress test %d", g_RamStatus[threadNum]);
    else
        g_RamStatus[threadNum] = 100;
done:
    delete [] buffer;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_junipersys_a3_1chamber_1test_RamTest_getRamStatus(
        JNIEnv * /*env*/,
        jobject /* this */,
        jint threadNum) {
    if(0 <= threadNum && threadNum < MAX_THREADS)
        return g_RamStatus[threadNum];
    return -1;
}


extern "C" JNIEXPORT jint JNICALL
Java_com_junipersys_a3_1chamber_1test_RamTest_getRamLoops(
        JNIEnv * /*env*/,
        jobject /* this */,
        jint threadNum) {
    if(0 <= threadNum && threadNum < MAX_THREADS)
        return g_RamLoops[threadNum];
    return -1;
}
