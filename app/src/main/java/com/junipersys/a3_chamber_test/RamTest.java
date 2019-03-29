package com.junipersys.a3_chamber_test;

import android.view.View;

public class RamTest {
    final int numThreads = 4;
    final int memorySize = 64*1024*1024;
    final int numLoops = 250;

    public void runRamTest(){
        switch(numThreads)
        {
            case 4:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startRamTest(3, memorySize, numLoops);
                    }
                }).start();
                // Fall through ...
            case 3:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startRamTest(2, memorySize, numLoops);
                    }
                }).start();
                // Fall through ...
            case 2:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startRamTest(1, memorySize, numLoops);
                    }
                }).start();
                // Fall through ...
            case 1:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startRamTest(0, memorySize, numLoops);
                    }
                }).start();
                // Fall through ...
                break;
        }
        // Start another thread that just monitors the status of the test threads
        new Thread(new Runnable() {
            @Override
            public void run() {

                int status0 = 0;
                int status1 = 0;
                int status2 = 0;
                int status3 = 0;
                int loops0 = 0;
                int loops1 = 0;
                int loops2 = 0;
                int loops3 = 0;
                // Mark the threads we never started as being complete
                switch (numThreads) {
                    case 1:
                        status1 = 100;
                        // Fall through
                    case 2:
                        status2 = 100;
                        // Fall through
                    case 3:
                        status3 = 100;
                        // Fall through
                }

                while (status0 >= 0 &&
                        status1 >= 0 &&
                        status2 >= 0 &&
                        status3 >= 0 &&
                        (status0 < 100 ||
                                status1 < 100 ||
                                status2 < 100 ||
                                status3 < 100)) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    switch (numThreads) {
                        case 4:
                            status3 = getRamStatus(3);
                            loops3 = getRamLoops(3);
                            // fall through
                        case 3:
                            status2 = getRamStatus(2);
                            loops2 = getRamLoops(2);
                            // fall through
                        case 2:
                            status1 = getRamStatus(1);
                            loops1 = getRamLoops(1);
                            // fall through
                        case 1:
                            status0 = getRamStatus(0);
                            loops0 = getRamLoops(0);
                            break;
                    }

                    final String s0 = "      0,   " + loops0 + "(" + 100 * (float) loops0 / numLoops + "%),   " + status0;
                    final String s1 = "      1,   " + loops1 + "(" + 100 * (float) loops1 / numLoops + "%),   " + status1;
                    final String s2 = "      2,   " + loops2 + "(" + 100 * (float) loops2 / numLoops + "%),   " + status2;
                    final String s3 = "      3,   " + loops3 + "(" + 100 * (float) loops3 / numLoops + "%),   " + status3;
                }
            }
        }).start();
    }



    public native void startRamTest(int threadNum, int memorySize, int numLoops);
    public native int getRamStatus(int threadNum);
    public native int getRamLoops(int threadNum);

}
