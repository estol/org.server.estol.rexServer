/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.server.estol.skeleton.core;

import java.io.Serializable;

/**
 *
 * @author estol
 */
public class dumpObject implements Serializable
{
    private static final long serialVersionUID = 8525574231L;

    String threadName;
    String errorMessage;

    public dumpObject(Thread t, Throwable e)
    {
        threadName   = t.getName();
        errorMessage = e.getMessage();
    }

    public String getThreadName()
    {
        return threadName;
    }
    
    public String getErrorMessage()
    {
        return errorMessage;
    }
}
    
