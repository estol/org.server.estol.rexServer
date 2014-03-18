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

import org.server.estol.skeleton.applicationlogic.Execution.DirectoryTraverse;
import org.server.estol.skeleton.applicationlogic.Execution.ExecutionEngine;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.clientserver.estol.commobject.CommunicationInterface;
import org.clientserver.estol.commobject.CommunicationObject;
import org.clientserver.estol.commobject.CommunicationObjectObjectPayload;
import org.server.estol.skeleton.debug.DebugServer;
import org.server.estol.skeleton.debug.DebugUtilities;
import org.server.estol.skeleton.system.exceptions.ProcessNotDoneException;

/**
 *
 * @author Péter Szabó
 */
public enum  InputParser
{
    Parser;
    private DebugServer ds = DebugServer.Server;
    private Thread dsThread = null;
    private static int jobNumber = 0;
    private static final HashMap<Integer, ExecutionEngine> jobs = new HashMap();
    private static final HashMap<Integer, String> userJobs = new HashMap();
    private MainLogic ml = MainLogic.MainLogic;
    
    
    private class jobWatcher implements Runnable
    {
        @Override
        public void run()
        {
            Thread.currentThread().setName("jobWatcher");
            Iterator<Entry<Integer, ExecutionEngine>> iterator = jobs.entrySet().iterator();
            while (iterator.hasNext())
            {
                try
                {
                    ExecutionEngine ee = iterator.next().getValue();
                    int job = iterator.next().getKey();
                    try
                    {
                        if (ee.getReturnValue() == 0)
                        {
                            // TODO implement a notification for the user, that the process is done, and looks okay
                            System.out.printf("Job done!%n");
                            iterator.remove();
                        }
                        else
                        {
                            // TODO implement a notification for the user, that the process is done, and looks erroneous
                            System.out.printf("Job failed!%n");
                            iterator.remove();
                        }
                    }
                    catch (ProcessNotDoneException ex)
                    {
                        // process is not done yet.
                    }
                    Thread.sleep(100L);
                }
                catch (InterruptedException ex)
                {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    
    public void parseCommand(ObjectOutputStream oos, String payload) throws InterruptedException
    {
        DebugUtilities.addDebugMessage("recieved command \"" + payload + "\"");
        switch (payload)
        {
            case "BeginDebug":
            {
                StringBuilder response = new StringBuilder();
                response.append(BroadcastWorker.getBroadcastingAddress());
                response.append(":");
                response.append(DebugServer.PORT);
                if (dsThread == null || !dsThread.isAlive())
                {
                    ds.initialize(ml.workers);
                    dsThread = new Thread(ds);
                    dsThread.start();
                }
                respond(oos, packPayload(response.toString()));
                break;
            }
            case "EndDebug":
            {
                ds.shutdown();
                respond(oos, packPayload("EndDebug recieved, debug server will shut down!"));
                break;
            }
            case "ping":
            {
                respond(oos, packPayload("pong"));
                break;
            }
            case "GetRoot":
            {
                respond(oos, packPayload(ml.getRoot()));
                break;
            }
            case "auth":
            {
                respond(oos, packPayload("hash"));
                break;
            }
            default:
            {
                if(payload.startsWith("ls"))
                {
                    String[] slices = payload.split(":");
                    if ("|root|".equals(slices[1]))
                    {
                        slices[1] = ml.getRoot();
                    }
                    respond(oos, packPayload(new DirectoryTraverse().getNodes(slices[1])));
                }
                else if (payload.startsWith("logout"))
                {
                    new Authentication().logout(payload.split(":")[1]);
                }
                else if ((!jobs.isEmpty() && null != payload && !payload.contentEquals("\u0000")) && jobs.containsKey(Integer.parseInt(payload.split(":")[1])))
                {
                    String[] slices = payload.split(":");
                    ExecutionEngine ee = jobs.get(Integer.parseInt(slices[1]));
                    String input = slices[0];
                    ee.setInput(input);
                    respond(oos, packPayload(ee.getOutput()));
                }
                else if (payload.startsWith("outputof")) // outputof:[jobnumber]
                {
                    String[] slices = payload.split(":");
                    ExecutionEngine ee = jobs.get(Integer.parseInt(slices[1]));
                    respond(oos, packPayload(ee.getOutput()));
                }
                else if (payload.startsWith("authAs"))
                {
                    String[] userNpass = payload.split(":")[1].split("-");
                    Authentication auth = new Authentication();
                    if (auth.authenticateUser(userNpass[0], userNpass[1]) == AuthStates.OKAY)
                    {
                        if (auth.checkAdmin(userNpass[0]) == AuthStates.OKAY)
                        {
                            respond(oos, packPayload("admin"));
                        }
                        else
                        {
                            respond(oos, packPayload("ok"));
                        }
                    }
                    else
                    {
                        respond(oos, packPayload("Auth failed"));
                    }
                }
                else if (payload.startsWith("run")) // run:[username]|script|parameter|parameter|parameter
                {
                    String[] slices = payload.split(":", 2)[1].split("::");
                    if ("wget".equals(slices[1])) // TODO: check the list of available scripts.
                    {
                        ArrayList<String> command = new ArrayList<>();
                        String[] parameters = new String[slices.length - 1];
                        for (int i = 1; i < slices.length; i++)
                        {
                            parameters[i - 1] = slices[i];
                        }
                        //command.addAll(ds);

                        ExecutionEngine ee = new ExecutionEngine(Arrays.asList(parameters));
                        new Thread(ee).start();
                        respond(oos, packPayload("probably downloading."));
                    }
                    else
                    {
                        respond(oos, packPayload("Not yet implemented!"));
                    }
                }
                else if (payload.startsWith("admin")) // admin:[username]-action-user-{passw}-{flag}
                {
                    String[] slices = payload.split(":")[1].split("-");
                    if (new Authentication().checkAdmin(slices[0]) == AuthStates.OKAY)
                    {
                        respond(oos, packPayload("Not yet implemented!"));
                    }
                    else
                    {
                        respond(oos, packPayload("You are not an administrator!"));
                    }
                }
                else
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Invalid input ");
                    sb.append(payload);
                    sb.append(".");
                    respond(oos, packPayload(sb.toString()));
                }
            }
        }
    }
    

    public void parseObject(ObjectOutputStream oos, Object payload)
    {
        respond(oos, packPayload("Object parsing is not yet implemented!"));
    }
    
    private CommunicationInterface packPayload(Object payload)
    {
        CommunicationObjectObjectPayload coop = new CommunicationObjectObjectPayload();
        coop.setPayload(payload);
        return coop;
    }
    
    private CommunicationInterface packPayload(String payload)
    {
        CommunicationObject co = new CommunicationObject();
        co.setPayload(payload);
        return co;
    }
    
    private void respond(ObjectOutputStream oos, CommunicationInterface response)
    {
        try
        {
            oos.writeObject(response);
        }
        catch (IOException ex)
        {
            System.out.printf("Exception was thrown while responding to input: %s\n", ex.getMessage());
        }
    }
}
