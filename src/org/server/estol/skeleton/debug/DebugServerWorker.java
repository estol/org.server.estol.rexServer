package org.server.estol.skeleton.debug;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import org.clientserver.estol.commobject.CommunicationInterface;

/** 
 * @author Tim
 */
class DebugServerWorker implements Runnable
{
    private Socket socket;
    private boolean runFlag = true;

    public DebugServerWorker(Socket socket)
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
        Thread.currentThread().setName("DebugWorker for " + socket.getRemoteSocketAddress().toString());
        ObjectInputStream  ois = null;
        ObjectOutputStream oos = null;
        DebugInputParser DIP = DebugInputParser.DIP;
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
                //DebugInputParser parser = new DebugInputParser();
                Object object = communicationObject.getPayload();
                //CommunicationObject co = new CommunicationObject();
                if (object instanceof String)
                {
                    DIP.parseCommand(oos, (String) object);
                }
                else
                {
                    DIP.parseCommand(oos, "badInput");
                }

            }
            catch (IOException | ClassNotFoundException ex)
            {
                
            }
        }
    }
    
}
