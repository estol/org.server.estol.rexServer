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

package org.server.estol.skeleton.applicationlogic;

import java.io.IOException;
import java.net.ServerSocket;

/**
 *
 * @author Péter Szabó
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
