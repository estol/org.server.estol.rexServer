/*
 * Copyright (C) 2014 Péter Szabó
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.server.estol.skeleton.debug;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import org.clientserver.estol.commobject.CommunicationInterface;

/** 
 * @author Péter Szabó
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
