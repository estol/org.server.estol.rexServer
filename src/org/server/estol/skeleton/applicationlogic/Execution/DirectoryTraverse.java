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

import java.io.File;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

//import org.server.estol.skeleton.debug.DebugUtilities;

/**
 *
 * @author Péter Szabó
 */
public class DirectoryTraverse
{
    /*
   public TreeModel buildTree(String path)
    {
        root = new File(path);
        rootNode = new DefaultMutableTreeNode(root.getName());
        traverseTree(root, rootNode);
        DefaultTreeModel tree = new DefaultTreeModel(rootNode);
        return tree;
    }
     
    
    protected File root;
    protected DefaultMutableTreeNode rootNode;
    
    private ExecutorService threadPool = Executors.newFixedThreadPool(20); // disks * cores
    
    public DirectoryTraverse(String path)
    {
        root = new File(path);
        rootNode = new DefaultMutableTreeNode(root.getName());
    }
    
    private void go() throws InterruptedException
    {
        for (int i = 0; i < 500; i++)
        {
            Runnable worker = new Worker();
            threadPool.execute(worker);
        }
        threadPool.shutdown();
        threadPool.awaitTermination(NumericUtilities.ONE_SECOND * 10, TimeUnit.MILLISECONDS);
    }
    
    public TreeModel getTree()
    {
        return new DefaultTreeModel(rootNode);
    }
    
    private class Worker implements Runnable
    {
        
/**
 * 
 *        File[] contents = root.listFiles();
        if (rootNode.getLeafCount() == 1)
        {
            rootNode.removeAllChildren();
        }
        for (File f : contents)
        {
            DefaultMutableTreeNode branch = new DefaultMutableTreeNode(f.getName());
            if (f.isDirectory())
            {
                //traverseTree(f, branch);
                TRAVERSE_THREAD_POOL.execute(new DirectoryTraverse(f, branch));
                rootNode.add(branch);
            }
            else
            {
                rootNode.add(branch);
            }
            Thread.sleep(1L);
        }
 
        @Override
        public void run()
        {
            
        }
        
    }*/
    /*
    private TreeModel buildTree(String path)
    {
        ArrayList<File> fileList, directoryList = new ArrayList<>();
        
        File file = new File(path);
        if (file.isDirectory())
        {
            //directoryList.add(file);
            DefaultMutableTreeNode dir = new DefaultMutableTreeNode(file);
        }
        Iterator iterator = directoryList.iterator();
        for (int i = 0; i < directoryList.size(); i++)
        {
            File directory = directoryList.get(i);
            String[] subNode = directory.list();
        }
    }
    */
    
    public List<DefaultMutableTreeNode> getNodes(String path)
    {
        List<DefaultMutableTreeNode> nodes = new ArrayList<>();
        
        System.out.printf("%s%n", path);
        
        File directory = new File(path);
        File[] contents = directory.listFiles();
        for (File item : contents)
        {
            item = item.getAbsoluteFile();
            if (item.isDirectory())
            {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(item.getName());
                node.add(new DefaultMutableTreeNode("Placeholder..."));
                nodes.add(node);
            }
            else
            {
                nodes.add(new DefaultMutableTreeNode(item.getName()));
            }
        }
        Collections.sort(nodes, new TreeNodeComparator());
        return nodes;
    }
    
    private static class TreeNodeComparator implements Comparator<DefaultMutableTreeNode>
    {
        @Override
        public int compare(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2)
        {
            String str1 = (String)o1.getUserObject();
            String str2 = (String)o2.getUserObject();
            int result = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
            return (result != 0) ? result : str1.compareTo(str2);
        }
        
    }
}
