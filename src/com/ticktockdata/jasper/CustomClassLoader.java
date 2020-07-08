package com.ticktockdata.jasper;

/*
 * Copyright (C) 2018 Joseph A Miller
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


import java.net.URL;


/**
 * This code obtained from 
 * {@link https://coderanch.com/t/384068/java/Adding-JAR-file-Classpath-Runtime}
 * <br>Was not able to get it to work as desired.
 * @author JAM {javajoe@programmer.net}
 * @since Nov 10, 2018
 */
public class CustomClassLoader extends java.net.URLClassLoader {
    
    
    public CustomClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }
    
    
    public CustomClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }
    
    /**
     * @param urls, to carryforward the existing classpath.
     */
    public CustomClassLoader(URL[] urls) {
        super(urls);
    }
    
    
     
    /**
     * add ckasspath to the loader.
     */
    public void addNewURL(URL url) {
        this.addURL(url);
    }
  
}