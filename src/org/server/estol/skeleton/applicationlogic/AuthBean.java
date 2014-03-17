package org.server.estol.skeleton.applicationlogic;

import java.io.Serializable;
import java.util.HashMap;

/**
 * JavaBeans are reusable software components for Java.
 * Practically, they are classes that encapsulate many objects into a single object.
 * They are serializable, have a 0-argument constructor,
 * and allow access to properties using getter and setter methods. 
 * 
 * @author Tim
 */
public class AuthBean implements Serializable
{
    private static final HashMap<String, String> users = new HashMap<>();
    
    private static final long serialVersionUID = 38541876742L;
    
    /**
     * Adds a user to the HashMap.
     * 
     * @param username name of the user 
     * @param password password of the user
     * @return true if okay, false if username exists
     */
    public boolean addUser(String username, String password)
    {
        if (users.get(username) == null)
        {
            users.put(username, password);
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
    public boolean checkUser(String username, String password)
    {
        if (users.get(username) == null)
        {
            return false;
        }
        return users.get(username).equals(password);
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
        else
        {
            users.remove(username);
            return true;
        }
    }
}
