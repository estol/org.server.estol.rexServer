package org.server.estol.skeleton.applicationlogic;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.server.estol.skeleton.commons.IniParser;
import org.server.estol.skeleton.debug.DebugUtilities;


/**
 *
 * @author estol
 */
public enum MainLogic
{
    MainLogic;
    
    private ThreadGroup servers;
    protected ThreadGroup workers;
    
    private Thread BroadcastThread;
    private Thread ServerThread;
    
    private String root;
    
    /**
     * We don't need the ini parser to sit in the memory for long,
     * so let's just get rid of it as soon as the finalizer can.
     * 
     * @see http://docs.oracle.com/javase/7/docs/api/java/lang/ref/WeakReference.html
     */
    private WeakReference<IniParser> parser;
    
    public static final int SERVERPORT = 5052; // TODO read this from ini
    
    /**
     * Starts the broadcasting of UDP datagrams, so the clients can discover
     * the server.
     * 
     * TODO: remove printStackTrace() from catch
     */
    @SuppressWarnings("CallToThreadDumpStack")
    private void startBroadcasting()
    {
        try
        {
            BroadcastThread = new Thread(servers, new BroadcastWorker());
            BroadcastThread.setPriority(Thread.MAX_PRIORITY);
            BroadcastThread.start();
        }
        catch (UnknownHostException | SocketException ex)
        {
            System.out.printf("UDP Broadcast failed to start: %s", ex.getMessage());
            ex.printStackTrace();
        }
        
    }
    
    public IniParser getParser()
    {
        return parser.get();
    }
    
    /**
     * Starts the server.
     */
    private void startServer()
    {
        Server.Server.initialize(workers);
        ServerThread = new Thread(servers, Server.Server);
        ServerThread.setPriority(Thread.MAX_PRIORITY);
        ServerThread.start();
    }
    
    public String getRoot()
    {
        return root;
    }
    
    /**
     * initializes the application logic
     */
    public void initialize()
    {
        if (new File("conf/server.conf").exists())
        {
            try
            {
                servers = new ThreadGroup("servers");
                workers = new ThreadGroup("workers");

                parser = new WeakReference<>(new IniParser("conf/server.conf"));

                servers.setMaxPriority(Thread.MAX_PRIORITY - 3);
                servers.setDaemon(false);

                workers.setMaxPriority(Thread.MAX_PRIORITY);
                workers.setDaemon(false);

                root = getParser().getString("application", "root", "/");

                startBroadcasting();
                startServer();
            }
            catch (IOException ex)
            {
                DebugUtilities.addDebugMessage(ex.getMessage());
            }
        }
        else
        {
            try
            {
                File conf = new File ("conf/server.conf");
                conf.createNewFile();
                PrintWriter writer = new PrintWriter(conf);
                
                
            }
            catch (IOException ex)
            {
                DebugUtilities.addDebugMessage("Error occured while creating config: " + ex.getMessage());
                DebugUtilities.dumpDebugMessages(System.out);
            }
        }
    }
}
