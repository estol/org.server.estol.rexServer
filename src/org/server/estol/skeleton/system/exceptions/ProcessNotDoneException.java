/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.server.estol.skeleton.system.exceptions;

/**
 *
 * @author Tim
 */
public class ProcessNotDoneException extends Exception
{
    public ProcessNotDoneException()
    {
        super();
    }
    
    public ProcessNotDoneException(String msg)
    {
        super(msg);
    }
}
