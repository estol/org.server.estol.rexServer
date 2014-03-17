package org.server.estol.skeleton.commons;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.server.estol.skeleton.debug.DebugUtilities;

/**
 *
 * @author estol
 */
public class ObjectStreamWriter implements ThreadedUtility, Runnable
{
    private String filename;
    private String dir = ".";
    private String path;
    private Object o;
    
    private boolean runFlag = false;
    
    public ObjectStreamWriter(Object o)
    {
        this.o = o;
    }
    
    public ObjectStreamWriter(Object o, String filename)
    {
        this(o);
        setFilename(filename);
    }
    
    public ObjectStreamWriter(Object o, String filename, String dir)
    {
        this(o, filename);
        setDir(dir);
    }
    
    public final void setFilename(String filename)
    {
        this.filename = filename;
        generatePath();
    }
    
    public final void setDir(String dir)
    {
        this.dir = dir;
        generatePath();
    }

    private void generatePath()
    {
        path = (".".equals(dir)) ? filename : dir + System.getProperty("file.separator") + filename;
    }
    
    @Override
    public void display() {
        // NOT USED
    }

    @Override
    public void shutdown() {
        runFlag = false;
    }

    @Override
    public boolean isRunning() {
        return runFlag;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Object writer for " + path);
        runFlag = true;
        try {
            FileOutputStream fout = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            synchronized (oos)
            {
                oos.writeObject(o);
                oos.close();
            }
            DebugUtilities.addDebugMessage(path + " was written successfully!");
        }
        catch (IOException e)
        {
            DebugUtilities.addDebugMessage("Writing to " + path + "failed!\nException occured: " + e.getMessage());
            shutdown();
        }
        shutdown();
    }
}
