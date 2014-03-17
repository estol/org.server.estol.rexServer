package org.server.estol.skeleton.commons;

/**
 *
 * @author Tim
 * @param <S>
 * @param <K>
 * @param <V>
 */
public interface IniConfiguration<S, K, V> extends Configuration
{
    void setSection(S section);
    K getKeys();
    V getValues();
}
