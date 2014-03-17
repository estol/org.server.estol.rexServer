package org.server.estol.skeleton.debug;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple tcp server that serves java beans that contain debug objects.
 * 
 * @author Tim
 */
public enum DebugServer implements Runnable
{
    Server;

    public static final int PORT = 5075;
    
    private ServerSocket socket;
    private static boolean runFlag = true;
    private ThreadGroup workers;
    private ExecutorService executor = Executors.newFixedThreadPool(5); // TODO: read this from ini 
    
    public void initialize(ThreadGroup debugWorkers)
    {
        try
        {
            socket = new ServerSocket(PORT);
            socket.setReuseAddress(true); // dealing with time wait
            workers = debugWorkers;
            runFlag = true;
        }
        catch (IOException ex)
        {
            if (ex instanceof BindException)
            {
                System.out.printf("This is a bind exception!%n");
            }
            
            System.out.printf("%s\n", ex.getMessage());
        }
    }
    
    public boolean isRunning()
    {
        return runFlag;
    }
    
    public void shutdown()
    {
        /*
        try
        {*/
            runFlag = false;
            /*new Socket("127.0.0.1", PORT).getInputStream().read();
        }
        catch (IOException ex)
        {
            System.out.printf("Shutdown failed: %s%n", ex.getMessage());
        }*/
    }
    
    @Override
    public void run()
    {
        Thread.currentThread().setName("Debug Server");
        while (runFlag)
        {
            try
            {
                Socket sock = socket.accept();
                DebugServerWorker worker = new DebugServerWorker(sock);
                executor.execute(worker);
            }
            catch (IOException ex)
            {
                System.out.printf("%s%n", ex.getMessage());
            }
        }
        try
        {
            socket.close();
            System.out.printf("%b%n", socket.isBound());
        }
        catch (IOException ex)
        {
            System.out.printf("%s%n", ex.getMessage());
        }
    }
}
