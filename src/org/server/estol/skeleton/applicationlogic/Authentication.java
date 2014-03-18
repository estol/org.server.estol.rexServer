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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.server.estol.skeleton.commons.ObjectStreamReader;
import org.server.estol.skeleton.commons.ObjectStreamWriter;

/**
 *
 * @author Péter Szabó
 */
public class Authentication
{
    private AuthBean userFile;
    private static final HashMap<String, AuthStates> authenticatedUsers = new HashMap();
    
    /**
     * Reads the binary file, containing the AuthBean class.
     */
    public Authentication()
    {
        ObjectStreamReader reader = new ObjectStreamReader();
        reader.setPath("conf/users.bin");
        userFile = (AuthBean) reader.read();
    }
    
    /**
     * Checks if the username, and password exists, and match in the AuthBean.
     * 
     * @param username
     * @param password
     * @return 
     */
    public AuthStates authenticateUser(String username, String password)
    {
        if (userFile.userExists(username, password) && authenticatedUsers.get(username) == null)
        {
            authenticatedUsers.put(username, AuthStates.AUTHENTICATED);
            return AuthStates.OKAY;
        }
        else
        {
            return AuthStates.ERROR;
        }
    }
    
    /**
     * Removes the user from the list of authenticated users.
     * 
     * @param username the name of the user to de-authenticate.
     */
    public void logout(String username)
    {
        if (authenticatedUsers.get(username) != null)
        {
            authenticatedUsers.remove(username);
        }
    }
    
    /**
     * 
     * @return 
     */
    public List<String> getAuthenticatedUsers()
    {
        ArrayList<String> users = new ArrayList<>();
        users.addAll(authenticatedUsers.keySet());
        return users;
    }
    
    /**
     *
     * @param username
     * @param password
     * @param admin
     * @return
     */
    public AuthStates addUser(String username, String password, Boolean admin)
    {
        if (userFile.addUser(username, password, admin))
        {
            ObjectStreamWriter writer = new ObjectStreamWriter(userFile, "conf/users.bin");
            new Thread(writer).start();
            return AuthStates.OKAY;
        }
        else
        {
            return AuthStates.ERROR;
        }
    }
    
    /**
     * Checks if the username is listed as an administrator.
     * 
     * @param username
     * @return 
     */
    public AuthStates checkAdmin(String username)
    {
        if (userFile.userAdmin(username))
        {
            return AuthStates.OKAY;
        }
        else
        {
            return AuthStates.ERROR;
        }
    }
    
    /**
     *
     * @param username
     * @param password
     * @return
     */
    public AuthStates removeUser(String username, String password)
    {
        if (userFile.removeUser(username))
        {
            ObjectStreamWriter writer = new ObjectStreamWriter(userFile, "conf/users.bin");
            new Thread(writer).start();
            return AuthStates.OKAY;
        }
        else
        {
            return AuthStates.ERROR;
        }
    }
}
