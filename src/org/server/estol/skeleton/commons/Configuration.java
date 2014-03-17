/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.server.estol.skeleton.commons;

/**
 * Very abstract representation of a configuration.
 * 
 * @author Tim
 * @param <K>
 * @param <V>
 */
public interface Configuration<K, V>
{
    V getValue(K key);
    void setValue(K key, V value);
}
