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

import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;
import org.server.estol.skeleton.debug.DebugUtilities;

/**
 *
 * @author Péter Szabó
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
