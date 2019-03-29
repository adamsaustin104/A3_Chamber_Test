package com.junipersys.a3_chamber_test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.LongAdder;

public class CpuTest {
    private int c_runs = 250;

    public void runCpuTest(){
        int counter = 0;
        while(counter < c_runs){
            testCPU();
            counter++;
        }
    }

    public void testCPU() {
        int numThreads = 3;
        LongAdder counter = new LongAdder();

        List<CalculationThread> runningCalcs = new ArrayList<>();
        List<Thread> runningThreads = new ArrayList<>();

        for(int i = 0; i < numThreads; i++){
            CalculationThread r = new CalculationThread(counter);
            Thread t = new Thread(r);
            runningCalcs.add(r);
            runningThreads.add(t);
            t.start();
        }

        for (int i = 0; i < 15; i++) {
            counter.reset();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }

        for (int i = 0; i < runningCalcs.size(); i++)
        {
            runningCalcs.get(i).stop();
            try {
                runningThreads.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
class CalculationThread implements Runnable
{
    private final Random rng;
    private final LongAdder calculationsPerformed;
    private boolean stopped;
    private double store;

    public CalculationThread(LongAdder calculationsPerformed)
    {
        this.calculationsPerformed = calculationsPerformed;
        this.stopped = false;
        this.rng = new Random();
        this.store = 1;
    }

    public void stop()
    {
        this.stopped = true;
    }

    @Override
    public void run()
    {
        while (! this.stopped)
        {
            double r = this.rng.nextFloat();
            double v = Math.sin(Math.cos(Math.sin(Math.cos(r))));
            this.store *= v;
            this.calculationsPerformed.add(1);
        }
    }
}
