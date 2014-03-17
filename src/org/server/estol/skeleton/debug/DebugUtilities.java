package org.server.estol.skeleton.debug;

import java.io.OutputStream;
import java.io.PrintWriter;
import org.server.estol.skeleton.commons.NumericUtilities;
import org.server.estol.skeleton.commons.ThreadedUtility;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.headless.estol.debugobject.DebugInformationObject;
import org.headless.estol.debugobject.DebugObject;
import org.headless.estol.debugobject.MemoryInformationObject;
import org.headless.estol.debugobject.ThreadInformationObject;

/**
 * Class name says it all. 
 * 
 * TODO: revise this class to be suitable for headless operation
 * 
 * @author estol
 */
public class DebugUtilities
{
    private static Memory        m  = null;
    private static ThreadMonitor t = null;
    private static DebugConsole  d = null;
    
    private static Thread consoleThread = null;
    private static Thread monitorThread = null;
    private static Thread  memoryThread = null;
    
    //private static DebugObject debugObject = new DebugObject();
    
    /**
     * Defines the global sleep interval, all debug utilities should use this,
     * so they are in sync. 
     *
     * TODO: find a more accurate way to sleep, than Thread.sleep(). LOWPRIO
     */
    private static final long SLEEP_INTERVAL = 800L; // TODO: define a shorter and a longer interval, so the debugger can switch. LOWPRIO

    // UTILITY CLASS DEFINITIONS BEGIN
    // MEMORY CLASS DEFINITION
    /**
     * The Memory class provides information about the available, and used
     * memory.
     * 
     * TODO: somehow this shows different values than the java debugger,
     * try to find out why. LOWPRIO
     */
    private static class Memory implements Runnable, ThreadedUtility
    {
        Runtime rt;
        float totalmem, freemem, maxmem, usedmem;
        static float peakmem = 0;
        boolean runFlag = true;
        String t, f, m, u, p;
        // TODO start the thread from the instance. Make the class a singleton. HIGHPRIO
        final static int RELATION = 1024;
        final static String K = "KB", M = "MB", G = "GB";
        final static String TOTAL = "Total: ",
                             FREE = "Free: ",
                             MAX  = "Max: ",
                             USED = "Used: ",
                             PEAK = "Peak: ";
        MemoryInformationObject mio;
        
        /**
         * Starts up the Memory thread.
         */
        Memory() {
            start();
        }
        
        final void start()
        {
            rt = Runtime.getRuntime();
            runFlag = false;
            display();
        }
        
        void restart()
        {
            shutdown();
            start();
        }
        
        void getMemoryUsage()
        {
            totalmem = (float)rt.totalMemory();
            freemem  = (float)rt.freeMemory();
            maxmem   = (float)rt.maxMemory();
            usedmem  = (float)totalmem - freemem;
            peakmem  = (usedmem > peakmem) ? (float)usedmem : (float)peakmem;
            
            totalmem /= (RELATION * RELATION);
            t = TOTAL + Float.toString(NumericUtilities.roundFloat(totalmem, 2)) + M;
            freemem  /= (RELATION * RELATION);
            f = FREE  + Float.toString(NumericUtilities.roundFloat(freemem, 2))  + M;
            maxmem   /= (RELATION * RELATION);
            m = MAX   + Float.toString(NumericUtilities.roundFloat(maxmem, 2))   + M;
            usedmem  /= (RELATION * RELATION);
            u = USED  + Float.toString(NumericUtilities.roundFloat(usedmem, 2))  + M;
            peakmem  /= (RELATION * RELATION);
            p = PEAK  + Float.toString(NumericUtilities.roundFloat(peakmem, 2))  + M;

        }

        void showMemoryUsage()
        {
            mio = new MemoryInformationObject(t, f, m, u, p);
        }

        MemoryInformationObject getMio()
        {
            return mio;
        }

        @Override
        public final void display()
        {
            getMemoryUsage();
            showMemoryUsage();
        }
        
        
        @Override
        public void shutdown()
        {
            runFlag = false;
        }

        @Override
        public void run()
        {
            Thread.currentThread().setName("Memory - DEBUG THREAD");
            while (runFlag)
            {
                try
                {
                    display();
                    Thread.sleep(SLEEP_INTERVAL);
                }
                catch (InterruptedException ex)
                {
                    DebugUtilities.addDebugMessage(ex.getMessage());
                    System.out.println("asd");
                    // no more "continue;" // supressing the error... BURN THE HERETIC! KILL THE MUTANT! PURGE THE UNCLEAN!
                    restart();
                }
            }
        }
        
        @Override
        public boolean isRunning()
        {
            return runFlag;
        }
    }
    // END OF MEMORY CLASS DEFINITION
    
    // THREAD MONITOR CLASS DEFINITION
    /**
     * Monitors the number of threads spawned by the program.
     * 
     * TODO: Review this class HIGHPRIO
     */
    private static class ThreadMonitor implements Runnable, ThreadedUtility
    {
        ThreadMXBean ThreadBean = ManagementFactory.getThreadMXBean();
        /**
         * Containts the current number of threads.
         */
        int currentThreadCount      = 0;
        /**
         * Contains the peak number of threads, since the last peak reset. 
         */
        int peakThreadCount_VM      = 0;
        /**
         * Contains the peak number of threads, throughout the entire
         * lifetime of the ThreadMonitor object. This is usually the same,
         * as the programs lifetime.
         */
        int peakThreadCount_NORESET = 0;
        boolean runFlag = true;
        ThreadInformationObject tio;
        
        
        ThreadMonitor()
        {
            display();
            runFlag = true;
        }

        void getCounts()
        {
            currentThreadCount      = ThreadBean.getThreadCount();
            peakThreadCount_VM      = ThreadBean.getPeakThreadCount();
            peakThreadCount_NORESET = (peakThreadCount_VM > peakThreadCount_NORESET) ? peakThreadCount_VM : peakThreadCount_NORESET;
        }
        
        void showCounts()
        {
            tio = new ThreadInformationObject(currentThreadCount, peakThreadCount_VM, peakThreadCount_NORESET);
        }

        ThreadInformationObject getTio()
        {
            return tio;
        }
        
        @Override
        public final void display()
        {
            getCounts();
            showCounts();
        }

        @Override
        public void shutdown()
        {
            runFlag = false;
        }
        
        @Override
        public boolean isRunning()
        {
            return runFlag;
        }

        @Override
        public void run()
        {
            Thread.currentThread().setName("Thread monitor - DEBUG THREAD");
            while (runFlag)
            {
                try
                {
                    display();
                    Thread.sleep(SLEEP_INTERVAL);
                }
                catch (InterruptedException ex)
                {
                    DebugUtilities.addDebugMessage(ex.getMessage());
                }
            }
        }
    }
    // END OF THREAD MONITOR CLASS DEFINITION
    
    // DEBUG CONSOLE CLASS DEFINITION
    private static class DebugConsole implements Runnable, ThreadedUtility
    {
        boolean runFlag = true;
        static boolean Running = false;
        static volatile TreeMap<String, String> messages = new TreeMap();
        static final DateFormat df = new SimpleDateFormat("MM/dd HH:mm:ss");
        DebugInformationObject dio;

        DebugConsole()
        {
            addMessage("Debug console started.");
        }

        @Override
        public boolean isRunning()
        {
            return Running;
        }

        final synchronized void addMessage(String msg)
        {
                messages.put(df.format(new Date()), msg);
        }

        @Override
        public void display()
        {
            dio = new DebugInformationObject(messages);
        }
        
        DebugInformationObject getDio()
        {
            return dio;
        }

        @Override
        public void shutdown() {
            runFlag = false;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("DebugConsole thread");
            Running = true;
            while (runFlag)
            {
                try
                {
                    display();
                    Thread.sleep(SLEEP_INTERVAL);
                }
                catch (InterruptedException ex)
                {
                    System.err.printf("Interrupted Exception caught: %s\n", ex.getMessage());
                }
            }
            // should not see this...
            addMessage("Debug console stoped.");
            Running = false;
        }
    }
    // END OF DEBUG CONSOLE CLASS DEFINITION
    // END OF UTILITY CLASS DEFINITIONS
    
    // DEBUG UTIL FUNCTIONS
    /**
     * called once to startup all the debug utilities
     */
    public static void startDebugUtils()
    {
        startConsole();
        startMonitor();
        startMemory();
    }

    /**
     * called by startDebugUtils to start the debug console
     */
    private static void startConsole()
    {
        if (d == null)
        {
            d = new DebugConsole();
        }
        if (consoleThread == null || !consoleThread.isAlive())
        {
            consoleThread = new Thread(d);
            consoleThread.start();
        }        
    }
    
    /**
     * called by startDebugUtils to start the thread monitor
     */
    private static void startMonitor()
    {
        if (t == null)
        {
            t = new ThreadMonitor();
        }
        if (monitorThread == null || !monitorThread.isAlive())
        {
            monitorThread = new Thread(t);
            monitorThread.start();
        }
    }
    
    /**
     * called by startDebugUtils to start the memory monitor
     */
    private static void startMemory()
    {
        if (m == null)
        {
            m = new Memory();
        }
        if (memoryThread  == null ||  !memoryThread.isAlive())
        {
            memoryThread  = new Thread(m);
            memoryThread.start();
        }
    }
    
    /**
     * adds a debug message
     * @param msg 
     */
    public static void addDebugMessage(String msg)
    {
        if (d == null)
        {
            startDebugUtils();
        }
        d.addMessage(msg);
    }

    public static void dumpDebugMessages(OutputStream output)
    {
        PrintWriter out = new PrintWriter(output);
        Iterator<Entry<String, String>> iterator = d.getDio().getDebugMessages().entrySet().iterator();
        while (iterator.hasNext())
        {
            out.printf("%s - %s%n", iterator.next().getKey(), iterator.next().getValue());
        }
        out.flush();
        out.close();
    }
    
    /**
     * should be called on a clean exit on server side
     */
    public static void stopDebugUtils()
    {
        d.shutdown();
        t.shutdown();
        m.shutdown();
    }
    
    public static DebugObject getDebugObject()
    {
        return new DebugObject(m.getMio(), t.getTio(), d.getDio());
    }
    
    // END OF DEBUG UTIL FUNCTIONS
    
    
    public static String getCallerMethodName() {
        final StackTraceElement[] ste = new Throwable().getStackTrace();
        return ste[1].getMethodName();
    }

    static String getCallerMethod() {
        final StackTraceElement[] ste = new Throwable().getStackTrace();
        return ste[2].getMethodName();
    }
    
    public static String getCallerClassName() {
        final StackTraceElement[] ste = new Throwable().getStackTrace();
        return ste[1].getClassName();
    }
    
    static String getCallerClass() {
        final StackTraceElement[] ste = new Throwable().getStackTrace();
        return ste[2].getClassName();
    }
    
    public static void invalidParameter() {
        DebugUtilities.addDebugMessage("Invalid parameter passed to method " + getCallerMethod() + " in class\n" + getCallerClass());
    }
}
