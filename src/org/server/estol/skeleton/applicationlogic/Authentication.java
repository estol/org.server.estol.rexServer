package org.server.estol.skeleton.applicationlogic;

import org.server.estol.skeleton.commons.ObjectStreamReader;
import org.server.estol.skeleton.commons.ObjectStreamWriter;

/**
 *
 * @author Tim
 */
public class Authentication
{
    private AuthBean userFile;
    
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
        if (userFile.checkUser(username, password))
        {
            return AuthStates.OKAY;
        }
        else
        {
            return AuthStates.ERROR;
        }
    }
    
    public AuthStates addUser(String username, String password)
    {
        if (userFile.addUser(username, password))
        {
            ObjectStreamWriter writer = new ObjectStreamWriter(userFile, "conf/users.bin");
            return AuthStates.OKAY;
        }
        else
        {
            return AuthStates.ERROR;
        }
    }
    
    public AuthStates removeUser(String username, String password)
    {
        if (userFile.removeUser(username))
        {
            ObjectStreamWriter writer = new ObjectStreamWriter(userFile, "conf/users.bin");
            return AuthStates.OKAY;
        }
        else
        {
            return AuthStates.ERROR;
        }
    }
}
