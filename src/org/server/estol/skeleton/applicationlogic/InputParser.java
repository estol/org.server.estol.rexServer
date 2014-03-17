package org.server.estol.skeleton.applicationlogic;

import org.server.estol.skeleton.applicationlogic.Execution.DirectoryTraverse;
import org.server.estol.skeleton.applicationlogic.Execution.ExecutionEngine;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import org.clientserver.estol.commobject.CommunicationInterface;
import org.clientserver.estol.commobject.CommunicationObject;
import org.clientserver.estol.commobject.CommunicationObjectObjectPayload;
import org.server.estol.skeleton.debug.DebugServer;
import org.server.estol.skeleton.debug.DebugUtilities;

/**
 *
 * @author Tim
 */
public enum  InputParser
{
    Parser;
    private DebugServer ds = DebugServer.Server;
    private Thread dsThread = null;
    private static int jobNumber = 0;
    private static final HashMap<Integer, ExecutionEngine> jobs = new HashMap();
    private MainLogic ml = MainLogic.MainLogic;
    
    
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
                else if ((!jobs.isEmpty() && null != payload && !payload.contentEquals("\u0000")) && jobs.containsKey(Integer.parseInt(payload.split(":")[1])))
                {
                    String[] slices = payload.split(":");
                    ExecutionEngine ee = jobs.get(Integer.parseInt(slices[1]));
                    String input = slices[0];
                    ee.setInput(input);
                    respond(oos, packPayload(ee.getOutput() + "\nReturn value: "));
                }
                else if (payload.startsWith("authAs"))
                {
                    System.out.printf("%s%n", payload);
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
