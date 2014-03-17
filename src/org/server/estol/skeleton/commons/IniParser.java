/*
 * Copyright (C) 2014 Tim
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses INI files.
 * Ignores sections, preceded by other than whitespace characters.
 * Ignores key, preceded by other than whitespace characters.
 * Doesn't ignore value followed by characters other than whitespace characters.
 * 
 * To state the obvious, this class DOESN'T create INI files.
 * 
 * A BAD example, or test case, what not to do:
 * ----------- INI FILE -----------
 * # key1 is unavalable because [section] is preceded by characters other than whitespace
 * key1 = value
 * # section1 and all the key inside will be available
 * [section1]
 *     key2 = value2 # this will show up, because value could contain whitespace
 * [section2] # this section shouldn't be accessable either
 *     key3 = value3 # this will be appended to section1 as section2 is unmatched
 * # key4 is part of section1, the last matched section in the file
 * key4=value4
 * ----------- INI FILE -----------
 * 
 * A GOOD example:
 * ----------- INI FILE -----------
 * # This is some comment explaining the section
 * # following this comment.
 * # as long as the first word is not in square brackets, followed by only whitespace
 * # or the first word is not followed by optional whitespaces, equal sign
 * # the line will be ignored.
 * # Since this file is being parsed line by line, there are no multiline comments.
 * [section]
 *   key = value
 * [section9]
 * # Although there is nothing stopping you from using the same key name in several sections
 * # it is considered extremely stupid, so don't do it.
 *   key = value
 * ----------- INI FILE -----------
 * 
 * DISCLAIMER: if you test this class, and copy the above ini files, with the preceding
 * asterisks, I'd suggest you shouldn't proliferate.
 * 
 * @author Tim
 */
public class IniParser
{
    // Why did we learn advanced calculus, but nothing about regular expressions?
    // One of these will be pretty useful slaving from 9 to 5 at any firm...
    /**
     * Whitespace zero or more times, followed by a square bracket, back-referencing
     * to the whitespace selector, to make sure the square bracket was the first character after
     * the whitespace - avoiding the sections mentioned in comments, then allowing any
     * character sequence between the two square brackets, followed by zero or more whitespace character,
     * followed by a line end character.
     */
    private Pattern _section  = Pattern.compile("\\s*\\[([^]]*)\\]\\s*");
    /**
     * Whitespace zero or more times, followed by a capturing group, which defines a capturing set,
     * which is at least one character long, and the first character after the preceding whitespace,
     * followed by any whitespace or followed by an equals sign,
     * followed by a capturing group, which defines a capturing set, which is at least zero character
     * long, or more, followed by an arbitrary amount of whitespace, followed by a line end character.
     */
    private Pattern _keyValue = Pattern.compile("\\s*([^=]*)=(.*)");
    
    private HashMap<String, Map<String, String>> _entries = new HashMap();
    
    /**
     * Creates an instance of the IniParser class.
     * 
     * @param path - the path on the filesystem to the ini file
     * @throws IOException  - thrown if the file is not readable, doesn't exist etc.
     */
    public IniParser(String path) throws IOException
    {
        load(path);
    }
    
    /**
     * Reads a file defined in the parameter.
     * If you want to switch ini files in runtime, it is strongly suggested, to create
     * a new instance of the parser.
     * 
     * @param path - the path on the filesystem to the ini file
     * @throws IOException  - thrown if the file is not readable, doesn't exist etc.
     */
    public final void load(String path) throws IOException
    {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) // try with resources, closes the BufferedReader when the block exits.
        {
            String line; // each line of the ini file
            String section = null; // defines the section e.g. [section]
            while((line = br.readLine()) != null)
            {
                Matcher m = _section.matcher(line); // We try to match each line with the _section regexp, if it matches, we set the section variable to the matched value.
                if (m.matches())
                {
                    section = m.group(1).trim();
                    /**
                     * Maybe a skip is available the above line sets the section variable, therefor it is not null
                     * and the else if's condition is true, but the matcher won't match.
                     * One less matcher call per section.
                     */
                }
                else if (section != null) // if we are already in a section we are looking for key - value pairs.
                {
                    m = _keyValue.matcher(line); // set the matcher to the _keyValue regexp
                    if (m.matches()) // if we match a line, we try to parse it for key and value, otherwise ignore it, hence the no else clause.
                    {
                        String key   = m.group(1).trim(); // trimming the matched key.
                        String value = m.group(2).trim(); // trimming the matched value.
                        Map<String, String> kv = _entries.get(section); // putting it in a map.
                        if (kv == null) // if we got a non null value above
                        {
                            _entries.put(section, kv = new HashMap<>());
                        }
                        kv.put(key, value);
                    }
                }
            }
        }
    }
    
    /**
     * Looks for a value of a key in a section.
     * 
     * @param section  the section where we are looking for the value
     * @param key  the key of the value we are looking for
     * @param defaultValue  returned if the key has no value
     * @return  the value associated with the specified key in the specified section
     */
    public String getString(String section, String key, String defaultValue)
    {
        Map<String, String> kv = _entries.get(section);
        if (kv == null)
        {
            return defaultValue;
        }
        return kv.get(key);
    }
    
    /**
     * Looks for a value of a key in a section.
     * 
     * @param section  the section where we are looking for the value
     * @param key  the key of the value we are looking for
     * @param defaultValue  returned if the key has no value
     * @return  the value associated with the specified key in the specified section
     */
    public int getInt(String section, String key, int defaultValue)
    {
        Map<String, String> kv = _entries.get(section);
        if (kv == null)
        {
            return defaultValue;
        }
        return Integer.parseInt(kv.get(key));
    }
    
    /**
     * Looks for a value of a key in a section.
     * 
     * @param section  the section where we are looking for the value
     * @param key  the key of the value we are looking for
     * @param defaultValue  returned if the key has no value
     * @return  the value associated with the specified key in the specified section
     */
    public boolean getBoolean(String section, String key, boolean defaultValue)
    {
        Map<String, String> kv = _entries.get(section);
        if(kv == null)
        {
            return defaultValue;
        }
        return Boolean.parseBoolean(kv.get(key));
    }
}
