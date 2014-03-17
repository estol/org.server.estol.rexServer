/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.server.estol.skeleton.applicationlogic;

import java.io.IOException;
import java.net.ServerSocket;

/**
 *
 * @author estol
 */
public enum Server implements Runnable
{
    Server;
    
    private ServerSocket socket;
    private boolean runFlag = true;
    private ThreadGroup workers;
    private MainLogic ml = MainLogic.MainLogic;
    
    public void initialize(ThreadGroup workers)
    {
        try
        {
            socket = new ServerSocket(ml.getParser().getInt("network", "communication_port", 5052)); // TODO: bind to the interface only defined in the configuration file.
            this.workers = workers;
        }
        catch (IOException ex)
        {
            System.out.printf("%s\n", ex.getMessage());
        }
    }
    
    public void shutdown()
    {
        runFlag = false;
    }
    
    @Override
    public void run()
    {
        while(runFlag)
        {
            try
            {
                new Thread(workers, new ServerWorker(socket.accept())).start();
                System.out.printf("Currently %d worker is running.\n", MainLogic.MainLogic.workers.activeCount());
            }
            catch (IOException ex)
            {
                System.out.printf("%s\n", ex.getMessage());
            }
        }
    }
}
