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
import java.io.ObjectOutputStream;
import org.clientserver.estol.commobject.CommunicationInterface;
import org.clientserver.estol.commobject.CommunicationObject;
import org.clientserver.estol.commobject.CommunicationObjectObjectPayload;
import org.headless.estol.debugobject.DebugObject;

/**
 * Singleton workaround, this way I don't have to rely on the jvm and be affraid of
 * reflection.
 * 
 * These kind of things make me want to cry havoc, and just write the whole application
 * in c.
 * 
 * 
 * @author Péter Szabó
 */
public enum DebugInputParser
{
    DIP;
    
    public void parseCommand(ObjectOutputStream oos, String payload)
    {
        switch (payload)
        {
            case "full":
            {
                DebugObject debugObject = DebugUtilities.getDebugObject();
                respond(oos, packPayload(debugObject));
                break;
            }   
            case "badInput":
            default:
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Invalid input ");
                sb.append(payload);
                sb.append(".");
                respond(oos, packPayload(sb.toString()));
                break;
            }
        }
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
            System.out.printf("Exception was thrown while responding to debug input: %s\n", ex.getMessage());
        }
    }
    
    public void parseObject(ObjectOutputStream oos, Object payload)
    {
        respond(oos, packPayload("Object parsing is not yet implemented!"));
    }
}
