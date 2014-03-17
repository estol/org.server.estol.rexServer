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
 * @author estol
 */
public class BroadcastWorker implements Runnable, ThreadedUtility
{
    private DatagramSocket socket = null;
    private InetAddress group;
    private int port = 4041;
    private boolean runFlag = true;
    
    private static String SERVER_ADDRESS;

    public BroadcastWorker() throws UnknownHostException, SocketException
    {
        group = InetAddress.getByName("230.0.0.1");
        socket = new DatagramSocket(port);
    }
    
    
    public static String getBroadcastingAddress()
    {
        return SERVER_ADDRESS;
    }
    
    @Override
    public void run()
    {
        try {
            Thread.currentThread().setName("Broadcast Worker");
            byte[] buffer = new byte[256];
            // CHANGE THIS BACK TO INTERNAL BEFORE YOU GO APESHIT OVER THE MYSTERIOUS FAILURES!
            NetworkInterface nif = NetworkInterface.getByName(MainLogic.MainLogic.getParser().getString("", "", "internal")); // TODO read this from ini
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
            sb.append(MainLogic.SERVERPORT);
            
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
