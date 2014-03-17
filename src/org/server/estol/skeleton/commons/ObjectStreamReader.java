package org.server.estol.skeleton.commons;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import org.server.estol.skeleton.debug.DebugUtilities;

/**
 *
 * @author estol
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
