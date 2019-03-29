package com.junipersys.a3_chamber_test;

public class Constants {

    //Platforms
    public static final int UNKOWN_PLATFORM = -1;
    public static final int ARCHER_3 = 0;
    public static final int ALLEGRO_3 = 1;

    //Unit bases in Configurator
    public static final int A3_BASE_UNIT = 3;
    public static final int A3_GEO_UNIT = 9;
    public static final int A3_BASE_TACTICAL_UNIT = 13;
    public static final int A3_GEO_TACTICAL_UNIT = 16;

    //Results
    public static final int PASSED = 1;
    public static final int FAILED = 0;

    //Features
    public static final int PRESENT = 1;
    public static final int NOT_PRESENT = 0;

    //Dialog Results
    public static final int DLG_RESULT_YES = 1;
    public static final int DLG_RESULT_NO = 0;

    //Exp Pod Constants
    public static final int NO_EXP_POD = 0;
    public static final int BARCODE_POD = 1;
    public static final int CELLULAR_POD = 2;

    //ExpPod ID's and Mask
    public static final int EXPANSION_ID_MASK = 0xF0;
    public static final int BARCODE_EXPANSION_ID =  0x20;
    public static final int CELLULAR_EXPANSION_ID = 0x60;



    //Cell Type
    public static final int NO_CELL = 0;
    public static final int EU_CELL = 1;
    public static final int US_CELL = 2;
    public static final int AU_CELL = 3;

    //Wifi
    public static final int NO_WIFI = 0;
    public static final int INTL_WIFI = 1;
    public static final int US_WIFI = 2;

    //BT
    public static final int BT_DISABLED = 0;
    public static final int BT_ENABLED = 1;

    //Tactical mode
    public static final int NON_TACTICAL_MODE = 0;
    public static final int TACTICAL_MODE = 1;

    //Archer 3 IO Modules
    public static final int IOMODULE_9Pin = 0;
    public static final int IOMODULE_DOCK = 1;


    //SYSFS Constants
    public static final String IOID_SYSFS = "/sys/devices/soc0/a3_info/io_id";
    public static final String PCB_ID_SYSFS = "/sys/devices/soc0/a3_info/pcb_id";
    public static final String PCBA_ID_SYSFS = "/sys/devices/soc0/a3_info/pcba_id";
    public static final String DOCK_DETECT_SYSFS = "/sys/devices/soc0/a3_info/dock_detect";
    public static final String IS_AR3_SYSFS = "/sys/devices/soc0/a3_info/is_ar3";
    public static final String EXP_ID_SYSFS = "/sys/devices/soc0/a3_expansion/exp_id";
    public static final String BLUE_LED_SYSFS = "sys/class/leds/a3_leds_blue/brightness";
    public static final String GREEN_LED_SYSFS = "/sys/class/leds/a3_leds_green/brightness";
    public static final String KEYBOARD_BKLIGHT_SYSFS = "/sys/class/leds/a3_leds_kbdbkl/brightness";
    public static final String BACKLIGHT_SYSFS = "/sys/class/backlight/a3-backlight/brightness";
    public static final String ATMEL_TS_VERSION_SYSFS = "/sys/devices/soc0/soc/2100000.aips-bus/21a8000.i2c/i2c-2/2-004a/config_version";
    public static final String ATMEGA_VERSION_SYSFS = "/sys/kernel/debug/atmega/version";
    public static final String WIFI_MACADDR_SYSFS = "/sys/class/net/wlan0/address";

    //Error Codes
    public static final int NO_ERRORS = 0;
    public static final int QD_Error_Exception = 1;
    public static final int QD_Error_Data_Not_Saved = 2;
    public static final int QD_Error_ParentID_Not_Set =3;
    public static final int QD_Error_Checking_FC = 4;
    public static final int QD_Error_ItemID = 5;
    public static final int QD_Error_SN_ON_WO = 6;
    public static final int QD_Error_Post_Test_Passed = 7;
    public static final int QD_Error_WO_Count = 8;
    public static final int MAX_Error_Order_Info = 9;
    public static final int MAX_Error_SpecialInstructions = 10;
    public static final int MAX_Error_ConfigString = 11;
    public static final int MAX_Error_SuperMarketPN = 12;
    public static final int MAX_Error_PN_AssignedToSN = 13;


    //Quality Database Config ID Constants - These ID's are from the ConfigurationOptions Table in Quality
    public static final int MODEL_NUMBER_ID = 1;
    public static final int OS_PLATFORM_ID = 2;
    public static final int OS_VERSION_ID = 3;
    public static final int BOOT_VERSION_ID = 4;
    public static final int BOARD_REVISION_ID = 6;
    public static final int BT_ADDRESS_ID = 14;
    public static final int WIFI_REGION_ID = 15;
    public static final int CAMERA_ID = 16;
    public static final int GPS_ID = 17;
    public static final int CELL_ID = 18;
    public static final int PCBA_SERIAL_ID = 22;
    public static final int CELL_IMEI_ID = 23;
    public static final int EXP_POD_ID = 24;
    public static final int HAZLOC_TYPE_ID = 28;
    public static final int WORK_ORDER_NUM_ID = 29;
    public static final int SALES_ORDER_NUM_ID = 30;
    public static final int SPECIAL_NOTES_ID = 31;
    public static final int SPECIAL_NOTES_EXT_ID = 32;
    public static final int KEYPAD_TYPE_ID = 34;
    public static final int IOMODULE_TYPE_ID = 35;
    public static final int SDCARD_ID_ID = 36;
    public static final int SDCARD_SIZE_ID = 37;
    public static final int WIFI_MACADDR_ID = 38;
    public static final int PKG_MODEL_NUMBER_ID = 39;
    public static final int TACTICAL_MODE_ID = 40;
    public static final int KEYBOARD_VERIFICATION_ID = 47;
    public static final int ATMega_Ver_ID = 54;
}
