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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import org.server.estol.skeleton.commons.NumericUtilities;
import org.server.estol.skeleton.commons.ThreadedUtility;

/**
 *
 * @author Péter Szabó
 */
public class BroadcastWorker implements Runnable, ThreadedUtility
{
    private DatagramSocket socket = null;
    private InetAddress group;
    private boolean runFlag = true;
    private int port;
    private MainLogic ml = MainLogic.MainLogic;
    
    private static String SERVER_ADDRESS;

    public BroadcastWorker() throws UnknownHostException, SocketException
    {
        port = ml.getParser().getInt("network", "broadcast_port", 4041);
        group = InetAddress.getByName("230.0.0.1");
        socket = new DatagramSocket(port); // TODO: bind to the interface defined in the configuration file only.
        //System.out.printf("%d - %d%n", socket.getPort(), ml.getParser().getInt("network", "broadcast_port", 4041));
    }
    
    
    public static String getBroadcastingAddress()
    {
        return SERVER_ADDRESS;
    }
    
    @Override
    public void run()
    {
        Thread.currentThread().setName("Broadcast Worker");
        try {
            byte[] buffer = new byte[256];
            // CHANGE THIS BACK TO INTERNAL BEFORE YOU GO APESHIT OVER THE MYSTERIOUS FAILURES!
            NetworkInterface nif = NetworkInterface.getByName(ml.getParser().getString("network", "interface", "internal"));
            //System.out.printf("%s\n", nif.getInetAddresses().nextElement().getHostAddress());
            StringBuilder sb = new StringBuilder();
            Enumeration<InetAddress> inetAddresses = nif.getInetAddresses();
            for (InetAddress inetAddress : Collections.list(inetAddresses))
            {
                if (inetAddress.getHostAddress().matches("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"))
                {
                    //System.out.printf("%s\n", inetAddress.getHostAddress());   TODO implements log functionality
                    sb.append(inetAddress.getHostAddress());
                }
            }
            
            sb.append(":");
            sb.append(ml.getParser().getInt("network", "communication_port", 5052));
            
            SERVER_ADDRESS = sb.toString().split(":")[0];
            
            buffer = sb.toString().getBytes();
            while (runFlag)
            {
                try
                {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
                    socket.send(packet);
                    System.out.printf("Broadcasting...\n");
                    Thread.sleep(NumericUtilities.ONE_SECOND * 5);
                }
                catch (IOException | InterruptedException ex)
                {
                    System.out.printf("%s\n", ex.getMessage());
                }
            }
            socket.close();
        } catch (SocketException ex) {
            System.out.printf("%s\n", ex.getMessage());
        }
    }

    @Override
    public void display()
    {
        // not used
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
}
