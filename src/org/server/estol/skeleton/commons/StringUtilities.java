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

import java.awt.event.KeyEvent;
import java.util.Arrays;
import org.server.estol.skeleton.system.exceptions.OutOfBoundariesException;


/**
 *
 * @author Péter Szabó
 */
public class StringUtilities
{
    public static final int INDEX_NOT_FOUND = -1;
    public static final String EMPTY = "";

    /**
     * Returns the Levenshtein distance between two Strings.
     * 
     * derived from
     * http://www.merriampark.com/ldjava.htm
     * 
     * @param s first string, should not be null
     * @param t second string, should not be null
     * @return  integer, the distance
     * @throws IllegalArgumentException if a parameter is null
     */
    public static int getLevenshteinDistance (CharSequence s, CharSequence t)
            throws IllegalArgumentException {
        if (s == null || t == null) {
            throw new IllegalArgumentException("Null value passed!");
        }

        int n = s.length();
        int m = t.length();

        if (n == 0) {
            return m;
        } else
        if (m == 0) {
            return n;
        }

        // if n is longer than m, we should swap them,
        // to consume less memory in the loop
        if (n > m) {
            CharSequence temp = s;
            s = t;
            t = temp;
            // avoiding a useless function call, and a useless loop
            // also, XOR is accelerated via CPU instruction sets
            n = n ^ m;
            m = n ^ m;
            n = n ^ m;
        }

        int[] p = new int[n + 1]; // horizontal cost array, previous
        int[] d = new int[n + 1]; // horizontal cost array, current
        int[] _temp;

        int i; // index iterator of s
        int j; // index iterator of t

        char t_j; // j-th character of t

        int distance; // cost

        for (i = 0; i <= n; i++) {
            p[i] = i;
        }

        for (j = 1; j<=m; j++) {
            t_j = t.charAt(j - 1);
            d[0] = j;
            
            for (i = 1; i <= n; i++) {
                distance = s.charAt(i - 1) == t_j ? 0 : 1;
                d[i] = Math.min(Math.min(d[i - 1] + 1,
                        p[i] + 1), p[i - 1] + distance);
            }
            _temp = p;
            p = d;
            d = _temp;
        }

        return p[n];
    }

    /**
     * Returns the Levenshtein distance between two Strings if it's
     * less than or equal to a given threshold.
     * 
     * A bit modified version of the one above.
     * Uses some ideas from Algorithms on Strings, Trees and Sequences
     * by Dan Gusfield
     * @param s first string should not be null
     * @param t second string should not be null
     * @param threshold target threshold, must not be negative
     * @return integer, the distance
     * @throws IllegalArgumentException 
     */
    public static int getLevenshteinDistance(CharSequence s, CharSequence t, int threshold)
            throws IllegalArgumentException, OutOfBoundariesException {
        if (s == null | t == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }
        if (threshold < 0) {
            throw new IllegalArgumentException("Threshold must not be negative");
        }

        /**
         * This implementation has a little trick in it.
         * With that, the complexity of the algorithm could be reduced, to be better suitable
         * for the specific needs of the software.
         * 
         * If the distance is unbound, the distance takes O(mn) time to compute. With a bound
         * of k, that time may be reduced to O(km).
         * 
         * This comes with some other perks, 
         */
        
        int n = s.length();
        int m = t.length();
        
        if (n == 0) {
            if (m <= threshold) {
                return m;
            } else {
                throw new OutOfBoundariesException
                        ("First parameter was empty, second was longer than the threshold.");
            }
        } else
        if (m == 0) {
            if (n <= threshold) {
                return n;
            } else {
                throw new OutOfBoundariesException
                        ("Second parameter was empty, first was longer than the threshold.");
            }
        }
        
        if (n > m) {
            CharSequence temp = s;
            s = t;
            t = temp;
            // avoiding a useless function call, and a useless loop
            // also, XOR is accelerated via CPU instruction sets
            n = n ^ m;
            m = n ^ m;
            n = n ^ m;
        }
        
        int[] p = new int[n + 1]; // horizontal cost array, of the previous iteration
        int[] d = new int[n + 1]; // horizontal cost array, of the current iteration
        int[] _temp;
        
        int boundary = Math.min(n, threshold) + 1;
        for (int i = 0; i < boundary; i++) {
            p[i] = i;
        }
        
        Arrays.fill(p, boundary, p.length, Integer.MAX_VALUE);
        Arrays.fill(d, Integer.MAX_VALUE);
        
        for (int j = 1; j <= m; j++) {
            char t_j = t.charAt(j - 1);
            d[0] = j;
            
            int min = Math.max(1, j - threshold);
            int max = Math.min(n, j + threshold);
            
            if (min > max) {
                return -1;
            }
            
            if (min > 1) {
                d[min - 1] = Integer.MAX_VALUE;
            }
            
            for (int i = min; i <= max; i++) {
                if (s.charAt(i - 1) == t_j) {
                    d[i] = p[i - 1];
                } else {
                    d[i] = 1 + Math.min(Math.min(d[i - 1], p[i]), p[i - 1]);
                }
            }
            _temp = p;
            p = d;
            d = _temp;
        }
        if (p[n] <= threshold) {
            return p[n];
        } else {
            throw new OutOfBoundariesException
                    ("Distance is larger than threshold!");
        }
    }

    public static String difference(String str1, String str2) {
        if (str1 == null) {
            return str2;
        }
        if (str2 == null) {
            return str1;
        }
        int at = indexOfDifference(str1, str2);
        if (at == INDEX_NOT_FOUND) {
            return EMPTY;
        }
        return str2.substring(at);
    }

    public static int indexOfDifference(CharSequence cs1, CharSequence cs2) {
        if (cs1 == cs2) {
            return INDEX_NOT_FOUND;
        }
        if (cs1 == null || cs2 == null) {
            return 0;
        }
        int i;
        for (i = 0; i < cs1.length() && i < cs2.length(); ++i) {
            if (cs1.charAt(i) != cs2.charAt(i)) {
                break;
            }
        }
        if (i < cs1.length() || i < cs2.length()) {
            return i;
        }
        return INDEX_NOT_FOUND;
    }

    public static int indexOfDifference(CharSequence... css) {
        if (css == null || css.length <= 1) {
            return INDEX_NOT_FOUND;
        }
        boolean anyStringNull = false;
        boolean allStringsNull = true;
        int arrayLen = css.length;
        int shortestStrLen = Integer.MAX_VALUE;
        int longestStrLen = 0;
        
        for (int i = 0; i < arrayLen; i++) {
            if (css[i] == null) {
                anyStringNull = true;
                shortestStrLen = 0;
            } else {
                allStringsNull = false;
                shortestStrLen = Math.min(css[i].length(), shortestStrLen);
                longestStrLen  = Math.max(css[i].length(), longestStrLen);
            }
        }
        if (allStringsNull || longestStrLen == 0 && !anyStringNull) {
            return INDEX_NOT_FOUND;
        }
        if (shortestStrLen == 0) {
            return 0;
        }
        
        int firstDiff = -1;
        for (int stringPos = 0; stringPos < shortestStrLen; stringPos++) {
            char comparisonChar = css[0].charAt(stringPos);
            for (int arrayPos = 1; arrayPos < arrayLen; arrayPos++) {
                if (css[arrayPos].charAt(stringPos) != comparisonChar) {
                    firstDiff = stringPos;
                }
            }
            if (firstDiff != -1) {
                break;
            }
        }
        
        if (firstDiff == -1 && shortestStrLen != longestStrLen) {
            return shortestStrLen;
        }
        return firstDiff;
    }
    
    public static String appendChar(String s, char c) 
            throws IllegalArgumentException {
        if (s == null
                || ("".equals(s) && c == KeyEvent.VK_BACK_SPACE)
                || (c == '\uFEFF' || c == '\u0000')) {
            throw new IllegalArgumentException();
        }
        StringBuilder sb = new StringBuilder();
        if (c != KeyEvent.VK_BACK_SPACE) {
            sb.append(s);
            sb.append(c);
        }
        if (c == KeyEvent.VK_BACK_SPACE) {
            s = s.substring(0, s.length()-1);
            sb.append(s);
        }
        return sb.toString();
    }
}
