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

import java.io.Serializable;
import java.util.HashMap;

/**
 * JavaBeans are reusable software components for Java.
 * Practically, they are classes that encapsulate many objects into a single object.
 * They are serializable, have a 0-argument constructor,
 * and allow access to properties using getter and setter methods. 
 * 
 * @author Péter Szabó
 */
public class AuthBean implements Serializable
{
    private final HashMap<String, String> users = new HashMap<>();
    private final HashMap<String, Boolean> admins = new HashMap<>();
    
    private static final long serialVersionUID = 38541876782L;
    
    /**
     * Adds a user to the HashMap.
     * 
     * @param username name of the user 
     * @param password password of the user
     * @param admin
     * @return true if okay, false if username exists
     */
    public boolean addUser(String username, String password, Boolean admin)
    {
        if (users.get(username) == null)
        {
            users.put(username, password);
            admins.put(username, admin);
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Checks if the user exists in this bean, and if the password is correct.
     * 
     * @param username name of the user to check
     * @param password password to compare
     * @return true if everything checks out, false if something is wrong.
     */
    public boolean userExists(String username, String password)
    {
        if (users.get(username) == null)
        {
            return false;
        }
        return users.get(username).equals(password);
    }
    
    public boolean userAdmin(String username)
    {
        if (admins.get(username) == null)
        {
            return false;
        }
        return admins.get(username);
    }
    
    /**
     * Deletes the entry associated with the username from the HashMap.
     * 
     * @param username name of the user to remove
     * @return true if the user existed, and was removed, false otherwise.
     */
    public boolean removeUser(String username)
    {
        if (users.get(username) == null)
        {
            return false;
        }
        users.remove(username);
        admins.remove(username);
        return true;
    }
    
    public boolean grantAdmin(String username)
    {
        if (admins.get(username) != null && admins.get(username) != true)
        {
            admins.put(username, Boolean.TRUE);
            return true;
        }
        return false;
    }
    
    public boolean revokeAdmin(String username)
    {
        if ("root".equals(username))
        {
            return false;
        }
        else
        {
            if (admins.get(username) != null && admins.get(username) == true)
            {
                admins.put(username, Boolean.FALSE);
                return true;
            }
            else
            {
                return false;
            }
        }
    }
    
    public boolean changePassword(String username, String oldPassword, String newPassword)
    {
        if (users.get(username) == null)
        {
            return false;
        }
        else
        {
            if (users.get(username).equals(oldPassword))
            {
                users.put(username, newPassword);
                return true;
            }
            else
            {
                return false;
            }
        }
    }
}
