#include <jni.h>
#include <fcntl.h>
#include <linux/fs.h>
#include <sys/ioctl.h>
#include <unistd.h>

#define A3CFG_DEVICE_NAME   "/dev/a3cfg"

// Read or write Config Key IOCTLs and structure
struct config_key_data {
    uint8_t key;
    uint16_t length;
    uint8_t data[];
};
// key    - specify which key to read, write, or erase
// length - length of the key.  For reads, set to 0 to determine the size of buffer (pData) that is
//          needed, and then call the function again.  Or, pass in a buffer that is too large and
//          length will be returned as the size of the key.
//          A length of 0 indicates there is no key.
// pData  - buffer to hold the contents of the key.  Can be NULL if just getting the length of a
//          key, or erasing.
#define A3_CFG_IOCTL_READ_KEY   _IOR('C', 1, struct config_key_data *)
#define A3_CFG_IOCTL_WRITE_KEY  _IOW('C', 2, struct config_key_data *)
#define A3_CFG_IOCTL_ERASE_KEY  _IOW('C', 3, struct config_key_data *)


extern "C" JNIEXPORT jint JNICALL
Java_com_junipersys_a3_1chamber_1test_SerialActivity_GetKeyLength(
        JNIEnv *env,
        jobject /* this */,
        jchar key) {
    (void)env;
    int hCfgDriver;
    int ret;
    config_key_data key_data;

    // IOCTLs don't seem to need write access
    // Opening as O_RDONLY instead of O_RDWR avoids kmsg printing an SELINUX permissive warning
    hCfgDriver = open(A3CFG_DEVICE_NAME, O_RDONLY);
    if(hCfgDriver < 0)
        return (jint)-1;

    key_data.key = (uint8_t)key;
    key_data.length = 0;
    ret = ioctl(hCfgDriver, A3_CFG_IOCTL_READ_KEY, &key_data);
    close(hCfgDriver);

    return ret;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_junipersys_a3_1chamber_1test_SerialActivity_ReadKey(
        JNIEnv *env,
        jobject /* this */,
        jchar key,
        jbyteArray data) {
    (void)env;
    int hCfgDriver;
    int ret;
    struct config_key_data * key_data;

    // IOCTLs don't seem to need write access
    // Opening as O_RDONLY instead of O_RDWR avoids kmsg printing an SELINUX permissive warning
    hCfgDriver = open(A3CFG_DEVICE_NAME, O_RDONLY);
    if(hCfgDriver < 0)
        return (jboolean)false;

    int len = env->GetArrayLength(data);
    if(len > 0xFFFF)
        len = 0xFFFF;   // bound to uint16

    key_data = (struct config_key_data *)(new uint8_t[offsetof(struct config_key_data, data) + len]);
    key_data->key = (uint8_t)key;
    key_data->length = (uint16_t)len;
    ret = ioctl(hCfgDriver, A3_CFG_IOCTL_READ_KEY, key_data);
    close(hCfgDriver);
    if(ret < 0) {
        delete [] key_data;
        return (jboolean) false;
    }

    // Copy the data
    jbyte * pBuf = env->GetByteArrayElements(data, NULL);
    for(int i = 0; i < ret && i < len; i++) {
        pBuf[i] = key_data->data[i];
    }
    env->ReleaseByteArrayElements(data, pBuf, JNI_COMMIT);

    delete [] key_data;
    return (jboolean)true;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_junipersys_a3_1chamber_1test_SerialActivity_WriteKey(
        JNIEnv *env,
        jobject /* this */,
        jchar key,
        jbyteArray data) {
    int hCfgDriver;
    int ret;
    struct config_key_data * key_data;

    // IOCTLs don't seem to need write access
    // Opening as O_RDONLY instead of O_RDWR avoids kmsg printing an SELINUX permissive warning
    hCfgDriver = open(A3CFG_DEVICE_NAME, O_RDONLY);
    if(hCfgDriver < 0)
        return (jboolean)false;

    int len = env->GetArrayLength(data);
    if(len > 0xFFFF)
        len = 0xFFFF;   // bound to uint16

    key_data = (struct config_key_data *)(new uint8_t[offsetof(struct config_key_data, data) + len]);
    key_data->key = (uint8_t)key;
    key_data->length = (uint16_t)len;

    // Copy the data
    jbyte * pBuf = env->GetByteArrayElements(data, NULL);
    for(int i = 0; i < len; i++) {
        key_data->data[i] = (uint8_t)pBuf[i];
    }
    env->ReleaseByteArrayElements(data, pBuf, JNI_ABORT);

    ret = ioctl(hCfgDriver, A3_CFG_IOCTL_WRITE_KEY, key_data);
    close(hCfgDriver);
    delete [] key_data;

    return (jboolean)(ret >= 0);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_junipersys_a3_1chamber_1test_SerialActivity_EraseKey(
        JNIEnv *env,
        jobject /* this */,
        jchar key) {
    (void)env;
    int hCfgDriver;
    int ret;
    struct config_key_data key_data;

    // IOCTLs don't seem to need write access
    // Opening as O_RDONLY instead of O_RDWR avoids kmsg printing an SELINUX permissive warning
    hCfgDriver = open(A3CFG_DEVICE_NAME, O_RDONLY);
    if(hCfgDriver < 0)
        return (jboolean)false;

    key_data.key = (uint8_t)key;
    key_data.length = 0;

    ret = ioctl(hCfgDriver, A3_CFG_IOCTL_ERASE_KEY, &key_data);
    close(hCfgDriver);

    return (jboolean)(ret >= 0);
}
