package org.server.estol.skeleton.core;

import java.lang.Thread.UncaughtExceptionHandler;
import org.server.estol.skeleton.commons.ObjectStreamWriter;

/**
 *
 * @author estol
 */
public class GlobalExceptionHandler implements UncaughtExceptionHandler
{
    /**
     * TODO decide if the program is in working state after the exception, and try
     * to continue if it is.
     * 
     * @param t
     * @param e 
     */
    @Override
    public void uncaughtException(Thread t, Throwable e)
    {
        long epoch = System.currentTimeMillis() / 1000L;
        String dumpFileName = Long.toString(epoch) + ".dumpobject";
        new Thread(new ObjectStreamWriter(new dumpObject(t, e), dumpFileName)).start();
        System.exit(1);
    }
}
