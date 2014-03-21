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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.server.estol.skeleton.debug.DebugUtilities;

/**
 *
 * @author Péter Szabó
 */
public class Authentication
{
    private DatabaseConnector db = DatabaseConnector.DB;
    private static final ArrayList<String> authenticatedUsers = new ArrayList<>();  
    
    
    
    // I know, I could have used prepared statements, but that looked like an overkill for a one table "database".
    // Also I know this is prone to sql injection, but at least has some room for improvement. Also it is 1:25 am, so fuck off.
    private static final String SQL_SELECT_USER = "SELECT `username` FROM `users` WHERE `username` = \"uname\" AND `password` = \"pword\"";
    private static final String SQL_LIST__USERS = "SELECT `username` FROM `users`";
    private static final String SQL_CREATE_USER = "INSERT INTO `users` (`username`, `password`, `admin`) VALUES (\"uname\", \"pword\", flag)";
    private static final String SQL_CHECK_ADMIN = "SELECT `username` FROM `users` WHERE `username` = \"uname\" AND `admin` = 1";
    private static final String SQL_REMOVE_USER = "DELETE FROM `users` WHERE `username` = \"uname\" AND `password` = \"pword\"";
    private static final String SQL_UPDATE_PASS = "UPDATE `users` SET `password` = \"npword\" WHERE `username` = \"uname\" AND `password` = \"pword\"";
    private static final String SQL_GRANT_ADMIN = "UPDATE `users` SET `admin` = 1 WHERE `username` = \"uname\"";
    private static final String SQL_RVOKE_ADMIN = "UPDATE `users` SET `admin` = 0 WHERE `username` = \"uname\" AND `username` <> \"root\"";
    
    
    /**
     * Reads the binary file, containing the AuthBean class.
     */
    public Authentication()
    {
        db.init();
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
        String actualSQL = SQL_SELECT_USER.replace("uname", username).replace("pword", password);
        try (ResultSet result =  db.query(actualSQL))
        {
            if (!result.next())
            {
                return AuthStates.ERROR;
            }
            else
            {
                authenticatedUsers.add(username);
                return AuthStates.AUTHENTICATED;
            }
        }
        catch (SQLException ex)
        {
            DebugUtilities.addDebugMessage("Error while running query: " + ex.getMessage());
            return AuthStates.ERROR;
        }
    }
    
    /**
     * Replaces the password of the user.
     * 
     * @param username
     * @param oldPass
     * @param newPass
     * @return 
     */
    public AuthStates changeUserPassword(String username, String oldPass, String newPass)
    {
        String actualSQL = SQL_UPDATE_PASS.replace("uname", username).replace("pword", oldPass).replace("npword", newPass);
        try
        {
            int result = db.queryDDL(actualSQL);
            if (result != 0)
            {
                return AuthStates.ERROR;
            }
            else
            {
                return AuthStates.OKAY;
            }
        }
        catch (SQLException ex)
        {
            DebugUtilities.addDebugMessage("Exception occured while creating user: " + ex.getMessage());
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
        authenticatedUsers.remove(username);
    }
    
    /**
     * 
     * @return 
     */
    public List<String> getAuthenticatedUsers()
    {
        return authenticatedUsers;
    }
    
    public List<String> getAllUsers() throws InternalError
    {
        ArrayList<String> users = new ArrayList<>();
        try (ResultSet result = db.query(SQL_LIST__USERS))
        {
            if (!result.next())
            {
                throw new InternalError("List is empty!");
            }
            else
            {
                while(result.next())
                {
                    users.add(result.getString("username"));
                }
                return users;
            }
        }
        catch (SQLException ex)
        {
            DebugUtilities.addDebugMessage("Exception occured while listing users: " + ex.getMessage());
            throw new InternalError(ex.getMessage());
        }
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
        String actualSQL = SQL_CREATE_USER.replace("uname", username)
                                          .replace("pword", password)
                                          .replace("flag", (admin) ? "1" : "0"); // a bit of ternary for easier understanding. How I will hate myself in the near future...
        try
        {
            int result = db.queryDDL(actualSQL);
            if (result != 1)
            {
                return AuthStates.ERROR;
            }
            else
            {
                return AuthStates.OKAY;
            }
        }
        catch (SQLException ex)
        {
            DebugUtilities.addDebugMessage("Exception occured while creating user: " + ex.getMessage());
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
        String actualSQL = SQL_CHECK_ADMIN.replace("uname", username);
        try (ResultSet result = db.query(actualSQL))
        {
            if (!result.next())
            {
                return AuthStates.ERROR;
            }
            else
            {
                return AuthStates.OKAY;
            }
        }
        catch (SQLException ex)
        {
            DebugUtilities.addDebugMessage("Exception occured while querying the database: " + ex.getMessage());
            return AuthStates.ERROR;
        }
    }
    
    public AuthStates grantAdmin(String username)
    {
        String actualSQL = SQL_GRANT_ADMIN.replace("uname", username);
        try
        {
            int result = db.queryDDL(actualSQL);
            if (result != 0)
            {
                return AuthStates.ERROR;
            }
            else
            {
                return AuthStates.OKAY;
            }
        }
        catch (SQLException ex)
        {
            DebugUtilities.addDebugMessage("Exception occured while granting admin: " + ex.getMessage());
            return AuthStates.ERROR;
        }
    }
    
    public AuthStates revokeAdmin(String username)
    {
        String actualSQL = SQL_RVOKE_ADMIN.replace("uname", username);
        try
        {
            int result = db.queryDDL(actualSQL);
            if (result != 0)
            {
                return AuthStates.ERROR;
            }
            else
            {
                return AuthStates.OKAY;
            }
        }
        catch (SQLException ex)
        {
            DebugUtilities.addDebugMessage("Exception occured while granting admin: " + ex.getMessage());
            return AuthStates.ERROR;
        }
    }
    
    /**
     * Deletes the user.
     * 
     * @param username
     * @param password
     * @return
     */
    public AuthStates removeUser(String username, String password)
    {
        String selectSQL = SQL_SELECT_USER.replace("uname", username).replace("pword", password);
        try (ResultSet existsResult = db.query(selectSQL))
        {
            if (!existsResult.next())
            {
                return AuthStates.ERROR;
            }
            else
            {
                String actualSQL = SQL_REMOVE_USER.replace("uname", username).replace("pword", password);
                int deleteResult = db.queryDDL(actualSQL);
                if (deleteResult != 0)
                {
                    return AuthStates.ERROR;
                }
                else
                {
                    return AuthStates.OKAY;
                }
            }
        }
        catch (SQLException ex)
        {
            DebugUtilities.addDebugMessage("Exception occured while deleting a user: " + ex.getMessage());
            return AuthStates.ERROR;
        }
    }
}
