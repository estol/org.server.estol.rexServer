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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Connects to a mysql database.
 * 
 * In case you were wondering why is this done with an enum, instead of a class,
 * implementing the singleton anti-pattern, here are some good reasons
 * http://javarevisited.blogspot.hu/2012/07/why-enum-singleton-are-better-in-java.html
 * 
 * Joshua Bloch - Effective Java also encourages enum based singletons.
 * 
 * @author Péter Szabó
 */
public enum DatabaseConnector 
{
    DB;
    
    private Connection connection = null; // http://youtu.be/v2V2h_Bd_Gg
    private Statement statement = null;
    private ResultSet resultSet = null;
    
    public void init()
    {
        String dbuser = "rexuser", dbpass = "rexpass"; // TODO: read from config
        
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/rex", dbuser, dbpass); // TODO: read db from config
            statement = connection.createStatement();
            
        }
        catch (ClassNotFoundException | SQLException ex)
        {
            if (ex instanceof ClassNotFoundException)
            {
                System.out.printf("ClassNotFoundException occured while looking for mysql jdbc:%n%s%n", ex.getMessage());
            }
            if (ex instanceof SQLException)
            {
                System.out.printf("SQLException occured while looking for mysql jdbc:%n%s%n", ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * For running selects only.
     * 
     * @param SQL The SQL query to run.
     * @return ResultSet type, with the results of the query
     * @throws SQLException 
     */
    public ResultSet query(String SQL) throws SQLException
    {
        return statement.executeQuery(SQL);
    }
    
    /**
     * For DDL or DML type SQL statements.
     * 
     * @param SQL
     * @return
     * @throws SQLException 
     */
    public int queryDDL(String SQL) throws SQLException
    {
        return statement.executeUpdate(SQL);
    }
    
    public void terminate()
    {
        close(resultSet);
        close(statement);
        close(connection);
    }
    
    private void close(AutoCloseable c)
    {
        try
        {
            if (c != null)
            {
                c.close();
            }
        }
        catch (Exception ex)
        {
            // suppress this exception.
        }
    }
}
