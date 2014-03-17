/**
 * "There is no system but GNU and Linux is one of its kernels!" - St. IGNUcius
 * 
 * There is an extremely high chance, that the server - e.g. this application - in
 * its current form will only produce cryptic error messages on not Unix like systems.
 * 
 * Runs on BSD, Linux, and Solaris, but never tested on Redmond's finest.
 * I strongly believe, if you can figure out the name associated with the network
 * interface you want to bind to, it will run under Windows, but currently, I have
 * no time, nor initiative to test the server application in a Microsoft environment,
 * with the Artificial Intelligence homework piling up and whatnot.
 * 
 * So long story short, if you want this to work on Windows, be warned, you just
 * sailed to the waters, marked "Here Be Dragons!"
 * 
 * Also, for windows you need appropriate applications and scripts.
 */
package org.server.estol.skeleton.core;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.server.estol.skeleton.applicationlogic.MainLogic;
import org.server.estol.skeleton.debug.DebugUtilities;


/**
 * Entry point, starts the server
 * 
 * @author estol
 */
public class Core
{
    private static final List<String> switches = new ArrayList<>();
    private static final HashMap<String, String> arguments = new HashMap<>();
    
    private static final String CONFIG_FILE
            = "# Parameters used for the server. See\n" +
              "# https://github.com/estol/org.server.estol.rexServer/wiki/Configuration\n" +
              "# for reference.\n" +
              "[network]\n" +
              "  interface = internal\n" +
              "  communication_port = 5052\n" +
              "  discovery = true\n" +
              "  broadcast_port = 4041\n" +
              "[application]\n" +
              "  root = /storage\n" +
              "  max_jobs = 10\n" +
              "  max_connections = 15\n" +
              "[log]\n" +
              "  logging = true\n" +
              "  path = logs/rex.log\n" +
              "  max_size = 1M\n" +
              "  rotate = true\n" +
              "  back = 5\n" +
              "[database]\n" +
              "  type = nodb\n" +
              "  host = localhost\n" +
              "  port = 3306\n" +
              "[debug]\n" +
              "  port = 5075\n" +
              "  ";
    
    public static List<String> getSwitches()
    {
        return switches;
    }
    
    public static HashMap<String, String> getArguments()
    {
        return arguments;
    }
    
    private static enum States
    {
        PRFIXCHK,
        NPRFIXCHK;
    }
    
    private static class Parser
    {
        List<String> switches = Core.getSwitches();
        Map<String, String> arguments = Core.getArguments();

        
        void switchParser()
        {
            for (String s : switches)
            {
                switch(s)
                {
                    case "-create_config":
                    {
                        try
                        {
                            createConfig("conf/server.conf");
                            System.out.printf("Config file was created!%n");
                            break;
                        }
                        catch (IOException ex)
                        {
                            DebugUtilities.addDebugMessage("Exception occured while creating config file: " + ex.getMessage());
                            // There is no break in the catch branch. At this point, I want the server to quit, so instead of
                            // "breaking out" of the switch, I'm gonna slip to default.
                            // Probably not "Best Practice"
                        }
                    }
                    
                    case "-create_user":
                    {
                        
                    }
                    
                    case "-nostart":
                    {
                        System.exit(0);
                    }
                    
                    default:
                    {

                    }
                }
            }
        }
        
        void argumentParser()
        {
            Iterator<Entry<String, String>> iterator = arguments.entrySet().iterator();
            while (iterator.hasNext())
            {
                switch (iterator.next().getKey())
                {
                    default:
                    {
                        break;
                    }
                }
            }
        }
        
        public void createConfig(String path) throws IOException
        {
            String[] pathElements = path.split("/");
            String firstPathElement = pathElements[0];
            String lastPathElement = pathElements[pathElements.length - 1];
            String cwd = "";
            for (String s : pathElements)
            {
                /*
                if (s.equals(firstPathElement))
                {
                    continue;
                }*/
                if (!s.equals(lastPathElement))
                {
                    cwd = (s.equals(firstPathElement)) ? s : cwd + s ;
                    File f = new File(cwd).getAbsoluteFile();
                    System.out.printf("%s%n", f.getAbsolutePath());
                    if (!f.exists())
                    {
                        System.out.printf("doesn't exist");
                        f.mkdir();
                    }
                }
            }
            File file = new File(path).getAbsoluteFile();
            file.createNewFile();
            try (PrintWriter writer = new PrintWriter(file.getAbsoluteFile()))
            {
                writer.printf("%s%n", CONFIG_FILE);
                writer.flush();
            }
        }
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            DebugUtilities.startDebugUtils();
            MainLogic.MainLogic.initialize();
        }
        else
        {
            States state = States.PRFIXCHK;
            String prevIteration = null;
            for (String arg : args)
            {
                if (state == States.PRFIXCHK)
                {
                    if (arg.startsWith("-"))
                    {
                        switches.add(arg.trim());
                    }
                    else if (arg.startsWith("--"))
                    {
                        state = States.NPRFIXCHK;
                        prevIteration = arg.trim();
                    }
                }
                else if (state == States.NPRFIXCHK)
                {
                    arguments.put(prevIteration, arg);
                }
            }
            /*
            if (switches.contains("-debug"))
            {
                
            }*/
            Parser parser = new Parser();
            parser.switchParser();
            parser.argumentParser();
            DebugUtilities.startDebugUtils();
            MainLogic.MainLogic.initialize();
        }
    }
}
