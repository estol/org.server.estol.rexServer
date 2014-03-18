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


package org.server.estol.skeleton.applicationlogic.Execution;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import org.server.estol.skeleton.commons.NumericUtilities;
import org.server.estol.skeleton.debug.DebugUtilities;
import org.server.estol.skeleton.system.exceptions.ProcessDoneException;
import org.server.estol.skeleton.system.exceptions.ProcessNotDoneException;

/**
 * The problem which it solves is pretty easy:
 * Run a program, and get the output of the program. If any input is required, prompt the user
 * for some input.
 * 
 * This version of ExecutionEngine is written to make use of the ProcessBuilder class from java.lang.ProcessBuilder.
 * 
 * This class could run any program, that could be started from a command line interface, which is on the server's
 * native Unix like platform is any program. Probably works on proprietary systems as well, as long as they define
 * the correct parameters, and a JVM is available.
 * 
 * A basic use case would be the following:
 * 
 * 1. Other object creates an instance of the ExecutionEngine class, through one of the constructors, or via java.lang.reflect
 * 2. Instantiating object creates an instance of the java.lang.Thread class, with the ExecutionEngine instance passed as the
 *    runnable, and start the thread.
 *    Generally considered the lazy way - therefor the best way - to ignore the new instance of the Thread class, and just spawn
 *    the new thread on the fly. e.g: new Thread(runnableObject).start();
 *    Since the payload of these threads happen to be in a heterogeneous fashion, there are virtually no resources, that could be
 *    blocked by any of the ExecutionEngine objects, therefor there should be no reason to keep a close leash on these threads.
 *    Trust me, the Runnable has been tested to the extremes to take care of any problems occurring while executing.
 * 3. ??? - http://youtu.be/tO5sxLapAts
 * 4. The thread will eventually stop, and a return code of the process could be retrieved.
 * 
 * While the process is running, this Runnable will be blocked by Process.waitFor().
 * Input and output is being handled by separate threads:
 * Reader and Writer respectively.
 * - Reader is a utility thread, running while the process is running.
 * - Writer is a worker thread, spawning a thread every time an input is given.
 * 
 * TODO:
 *   - Figure out a way to send a notification to the client if the process is waiting for an input
 * 
 * @author Péter Szabó
 */
public class ExecutionEngine implements Runnable
{
    private static final int instanceCount = 0;
    
    // VARIABLES FOR SPAWNING A PROCESS
    private String cmd;
    private String[] arguments;
    private String[] switches;
    private String command;
    private ProcessBuilder builder;
    private Map<String, String> environment;
    private Process process;
    private boolean _process_complete = false;
    // VARIABLES FOR APPLICATION
    private String workingDir;
    private int returnValue = -1;
    // VARIABLES FOR HANDLING REALTIME I/O
    private Thread reader;
    private Thread writer;
    private Reader readerObject;
    private BufferedReader outputReader;
    private BufferedReader errorReader;
    private PrintWriter input;
    
    /**
     * This is an internal class.
     * Might not be a best practice, but this class represents and object, only required in this class.
     * This class is a runnable, therefor this will run in a thread.
     */
    private static class Reader implements Runnable
    {
        long sleepTime = NumericUtilities.ONE_SECOND / 4L; // I only want this thread to run once every 250 miliseconds. As far as Thread.sleep is accurate
        boolean runFlag = true; // defines a boolean variable, which will mark the end of the word.
        StringBuilder output = new StringBuilder(); // A string builder object which will build our string. Why not a threadsafe string buffer? The same object shouldn't run twice, concurrently with itself.
        BufferedReader stdout; // the buffered reader that listens on the standard output of the process.
        BufferedReader stderr; // the buffered reader that listens on the standard error output of the process.
        
        /**
         * empty constructor for the heretic
         */
        Reader() {}
        
        /**
         * initializer function called by the constructor.
         * 
         * @param out - The BufferedReader associated with stdout
         * @param err - The BufferedReader associated with stderr
         */
        final void init(BufferedReader out, BufferedReader err)
        {
            stdout = out;
            stderr = err;
        }
        
        /**
         * @see init {@link  init}
         * 
         * @param out
         * @param err 
         */
        Reader(BufferedReader out, BufferedReader err)
        {
            init(out, err);
        }
        
        /**
         * Returns the output of the process.
         * 
         * @return - the output of the process as a string.
         */
        String getOutput()
        {
            return output.toString();
        }
        
        /**
         * Appends the input from the {@see @link Writer} to the output, just like an interactive teleprinter.
         * 
         * @param input - the input
         */
        void addInput(String input)
        {
            output.append(input);
        }
        
        /**
         * flips the run flag, causing the thread to exit it's loop and stop execution.
         */
        void shutdown()
        {
            runFlag = false;
        }
        
        /**
         * Runs every 250 milliseconds. Appends the output to the StringBuilder.
         * 
         * exits when run flag is flipped.
         */
        @Override
        public void run()
        {
            while (runFlag)
            {
                try
                {
                    String line;
                    while((line = stdout.readLine()) != null)
                    {
                        output.append(line);
                        output.append("\n");
                    }
                    
                    while((line = stderr.readLine()) != null)
                    {
                        output.append(line);
                        output.append("\n");
                    }
                    Thread.sleep(sleepTime);
                }
                catch (IOException | InterruptedException ex)
                {
                    DebugUtilities.addDebugMessage("Error occured while reading output buffers: " + ex.getMessage());
                }
            }
        }
    }

    private static class Writer implements Runnable
    {
        String input; // the string we will write with the PrintWriter
        PrintWriter stdin; // the PrintWriter instance that handles the input
        
        /**
         * Creates an instance of this class.
         * 
         * @param in - the PrintWriter instance associated with stdin.
         */
        Writer(PrintWriter in)
        {
            stdin = in;
        }
        
        /**
         * The string we want to pipe in to the process gets passed to this method, and it sets
         * the content as an instance variable
         * 
         * @param input - The string to be written to the stdin associated with the process
         */
        void setInput(String input)
        {
            this.input = input.trim();
        }

        /**
         * Invokes the printf method of the PrintWriter and flushes the stream.
         * The Thread then exits.
         */
        @Override
        public void run()
        {
            Thread.currentThread().setName("Writer for " + input);
            stdin.printf("%s%n", input);
            stdin.flush();
        }
        
    }
    
    public ExecutionEngine(String cmd, String[] switchs, String[] args, String workingDir)
    {
        this.cmd = cmd;
        arguments = args;
        switches = switchs;
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append(this.cmd);
        if (switches != null)
        {
            for (String sw : switches)
            {
                if (1 == sw.length() && (!sw.startsWith("-") || !sw.startsWith(" -")))
                {
                    commandBuilder.append(" -");
                }
                else if (!" ".equals(sw) || !"-".equals(sw))
                {
                    commandBuilder.append(" --");
                }
                commandBuilder.append(sw);
            }
        }
        if (arguments != null)
        {
            for (String argument : arguments)
            {
                commandBuilder.append(" ");
                commandBuilder.append(argument);
            }
        }
        command = commandBuilder.toString();
        this.workingDir = workingDir;
        
        builder = new ProcessBuilder(command);
        environment = builder.environment();
        builder.directory(new File(this.workingDir));
    }
    
    public ExecutionEngine(String cmd, String workingDir)
    {
        this.cmd = cmd;
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append(this.cmd);

        command = commandBuilder.toString();
        this.workingDir = workingDir;
        
        builder = new ProcessBuilder(command);
        environment = builder.environment();
        builder.directory(new File(this.workingDir));
    }
    
    public ExecutionEngine(List<String> cmd)
    {
        /*
        this.cmd = cmd;
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append(this.cmd);

        command = commandBuilder.toString();
        */
        builder = new ProcessBuilder(cmd);
        environment = builder.environment();
        builder.directory(new File("/storage")); // TODO: read this from ini
    }

    public int getReturnValue() throws ProcessNotDoneException
    {
        if (!_process_complete)
        {
            throw new ProcessNotDoneException("");
        }
        else
        {
            return returnValue;
        }
    }
    
    public String getOutput() throws InterruptedException
    {
        Thread.sleep(250L);
        return readerObject.getOutput();
    }
    
    public void setInput(String input)
    {
        Writer writerObject = new Writer(this.input);
        writerObject.setInput(input);
        readerObject.addInput(input + "\n");
        writer = new Thread(writerObject);
        writer.start();
    }
    
    public void killChildProcess() throws ProcessDoneException
    {
        if (!_process_complete)
        {
            process.destroy();
        }
        else
        {
            throw new ProcessDoneException("The process already exited.");
        }
    }
    
    @Override
    public void run()
    {
        try
        {
            Thread.currentThread().setName("Executor " + instanceCount);
            process = builder.start();
            
            outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorReader  = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            input = new PrintWriter(new OutputStreamWriter(process.getOutputStream()));
            
            readerObject = new Reader(outputReader, errorReader);
            reader = new Thread(readerObject);
            reader.start();
            process.waitFor();
            _process_complete = true;
            returnValue = process.exitValue();
            readerObject.shutdown();
            
        }
        catch (IOException | InterruptedException ex)
        {
            
        }
    }
    
}
