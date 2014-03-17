package org.server.estol.skeleton.system.exceptions;

/**
 *
 * @author estol
 */
public class ThreadNotAliveException extends Exception
{
    public ThreadNotAliveException() {
        super();
    }
    
    public ThreadNotAliveException(String msg) {
        super(msg);
    }
}
