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

import java.awt.event.MouseEvent;
import javax.swing.JTextPane;
import org.server.estol.skeleton.debug.DebugUtilities;

/**
 *
 * @author Péter Szabó
 */
public class SelectionListener implements Runnable, ThreadedUtility
{
    private boolean runFlag = true, enabled = true, mouseState = false;
    private JTextPane pane;
    private final static long interval = 250;
    private static int c = 0;

    public SelectionListener(JTextPane p)
    {
        c++;
        pane = p;
    }

    @Override
    public void display()
    { 
        // this function has no display REVIEW THRADED UTILITY INTERFACE ASAP!
    }
    
    @Override
    public void shutdown()
    {
        runFlag = false;
    }
    
    public void disable()
    {
        enabled = false;
    }
    
    public void enable()
    {
        enabled = true;
    }
    
    public boolean isEnabled()
    {
        return enabled;
    }

    @Override
    public boolean isRunning()
    {
        return runFlag;
    }

    public synchronized void getMouseState(MouseEvent e)
    {
        mouseState = (e.getButton() == MouseEvent.BUTTON1) ? true : false;
    }
    
    @Override
    public void run()
    {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        while (runFlag)
        {
            if (enabled)
            {
                Thread.currentThread().setName("Selection Listener");
                try
                {
                    Thread.sleep(interval);
                    if (pane.getSelectedText() != null)
                    {
                        pane.copy();
                        // FIXME track mouse button state, and only call pane.select(0, 0) when the mouse button is up
                        // fixed 10.01 2013
                        if (!mouseState)
                        {
                            pane.select(0, 0);
                        }
                    }
                }
                catch (InterruptedException ex)
                {
                    DebugUtilities.addDebugMessage(ex.getMessage());
                }
            }
            else
            {
                try
                {
                    Thread.sleep(interval);
                }
                catch (InterruptedException ex)
                {
                    DebugUtilities.addDebugMessage(ex.getMessage());
                }
            }
        }
    }
}
