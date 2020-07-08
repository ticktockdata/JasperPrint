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

package com.ticktockdata.jasperserver;

import static com.ticktockdata.jasperserver.ServerManager.LOGGER;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * A socket used to communicate between command line input and running ServerSocket.
 * @author JAM {javajoe@programmer.net}
 * @since Oct 04, 2018
 */
public class Client extends Socket {

    private BufferedReader input = null;
    private PrintWriter output = null;
    
    
    public Client(String host, int port) throws IOException {
        super(host, port);
    }
    
    
    public BufferedReader getReader() {
        if (input == null && !isClosed()) {
            try {
                input = new  BufferedReader(new InputStreamReader(new BufferedInputStream(this.getInputStream())));
            } catch (Exception ex) {
                LOGGER.warn("InputStream is not available: " + ex.toString());
                input = null;
            }
        }
        return input;
    }
    
    
    /**
     * gets a PrintWriter from the OutputStream, which is buffered by a
     * BufferedOutputStream
     * @return null if the output stream isn't available
     */
    public PrintWriter getWriter() {
        if (output == null && !isClosed()) {
            try {
                output = new PrintWriter(new BufferedOutputStream(this.getOutputStream()));
            } catch (Exception ex) {
                LOGGER.warn("OutputStream is not available: " + ex.toString());
                output = null;
            }
        }
        return output;
    }
    
    
    /**
     * Use this to test if there is input available to be read from the
     * Input Stream.
     * @return 
     */
    public boolean hasInput() {
        try {
            return getReader().ready();
        } catch (Exception ex) {
            LOGGER.error("Error checking input.ready()", ex);
            return false;
        }
    }
    
    
    public int read() throws IOException {
        return getReader().read();
    }
    
    /**
     * Reads line from input stream.  Note that this blocks until input 
     * available!  Use hasInput() to check if input is available.
     * @return String that was read, or null if error.
     */
    public String readLine() {
        try {
            return getReader().readLine();
        } catch (Exception ex) {
            LOGGER.error("Failed to read line from InputStream", ex);
            return null;
        }
    }
    
    
    /**
     * Writes text to output stream, but does not terminate or flush
     * @param text 
     */
    public void print(String text) {
        getWriter().print(text);
    }
    
    
    /**
     * Writes a string to the output stream.  Terminates with line ending
     * and flushes!
     * @param text 
     */
    public void println(String text) {
        getWriter().println(text);
        output.flush();
    }
    
    
    public void println() {
        getWriter().println();
        output.flush();
    }
    
    public void write(int c) {
        getWriter().write(c);
        output.flush();
    }
    
    
    public void writeObject(Object obj) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(getOutputStream()));
            out.writeObject(obj);
            out.flush();
        } catch (Exception ex) {
            LOGGER.error("Failed to write Object " + obj, ex);
        }
        
    }
    
    @Override
    public synchronized void close() throws IOException {
        // set variables null
        input = null;
        output = null;
        super.close();
    }
    
    
    
    
}
