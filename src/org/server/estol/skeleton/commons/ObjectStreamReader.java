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

package org.server.estol.skeleton.commons;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import org.server.estol.skeleton.debug.DebugUtilities;

/**
 *
 * @author Péter Szabó
 */
public class ObjectStreamReader
{
    private String path;
    private Object rObject = null;
    
    public ObjectStreamReader(){}
    
    public ObjectStreamReader(String path)
    {
        setPath(path);
    }
    
    public final void setPath(String path)
    {
        this.path = path;
    }
    
    public Object read()
    {
        try
        {
            FileInputStream fin = new FileInputStream(path);
            ObjectInputStream ois = new ObjectInputStream(fin);
            rObject = ois.readObject();
            ois.close();
        }
        catch (IOException | ClassNotFoundException e)
        {
            DebugUtilities.addDebugMessage("Reading from " + path + " failed!\nException occured: " + e.getMessage());
        }
        return rObject;
    }
}
