package org.server.estol.skeleton.applicationlogic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import org.clientserver.estol.commobject.CommunicationInterface;
import org.server.estol.skeleton.debug.DebugUtilities;

/**
 *
 * @author estol
 */
public class ServerWorker implements Runnable
{
    private Socket socket;
    
    private boolean runFlag = true;
    
    public ServerWorker(Socket socket)
    {
        this.socket = socket;
    }
    
    private void shutdown()
    {
        runFlag = false;
    }
    
    @Override
    public void run()
    {
        Thread.currentThread().setName("Worker for " + socket.getRemoteSocketAddress().toString());
        ObjectInputStream  ois = null;
        ObjectOutputStream oos = null;
        try
        {
            ois = new ObjectInputStream (socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());            
        }
        catch (IOException ex)
        {
            runFlag = false;
            System.out.printf("Failed to open io streams because the following error occured: %s\n", ex.getMessage());
            //ex.printStackTrace();
        }    
        while (runFlag)
        {
            try
            {
                CommunicationInterface communicationObject = (CommunicationInterface) ois.readObject();
                InputParser parser = InputParser.Parser;
                Object object = communicationObject.getPayload();
                if (object instanceof String)
                {
                    /*
                    System.out.printf("got a string!\n");
                    co.setPayload("got a string!");
                    oos.writeObject(co);
                            */
                    try
                    {
                        parser.parseCommand(oos, (String) object);
                    }
                    catch (InterruptedException ex)
                    {
                        DebugUtilities.addDebugMessage(ex.getMessage());
                    }
                }
                else
                {
                    /*
                    System.out.printf("got something else!\n");
                    co.setPayload("got soething else");
                    oos.writeObject(co);
                            */
                    parser.parseObject(oos, object);
                }
                //ois.close();
            }
            /**
             * Some dick at the college said you should never organize control around exceptions.
             * Well, this IOException here is not a random occurrence, this is excepted to be thrown,
             * when the client disconnects.
             * 
             * Doubt it? I challenge you to write better.
             */
            catch (IOException ex)
            {
                System.out.printf("Client disconnected gracefully!\n");
                //ex.printStackTrace();
                shutdown();
            }
            catch (ClassNotFoundException ex)
            {
                System.out.printf("Exception was thrown in thread %s:%n%s%n", Thread.currentThread().getName(), ex.getMessage());
                shutdown();
            }
        }
    }
    
}
