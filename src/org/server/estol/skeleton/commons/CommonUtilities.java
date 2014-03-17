package org.server.estol.skeleton.commons;

import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;
import org.server.estol.skeleton.debug.DebugUtilities;

/**
 *
 * @author estol
 */
public class CommonUtilities
{
    /**
     * Opens the image supplied in @param path and returns it as an Image type.
     * 
     * @param path
     * @param description
     * @param callerClass
     * @return 
     */
    public static Image createImage(String path,
            String description,
            Class callerClass)
    {
        URL imageURL = callerClass.getResource(path);
        if (imageURL == null)
        {
            DebugUtilities.addDebugMessage("Resource not found: " + path);
            return null;
        }
        else
        {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }
}
