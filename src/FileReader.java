
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author JAM {javajoe@programmer.net}
 * @since Mar 13, 2019
 */
public class FileReader {
    
    public FileReader() {
//        for (int x = 0; x < 500; x++) {
//            System.out.println(x + " : " + (char)x);
//        }
//        if (true) return;
        File f = new File("/home/default/FREUNDSH.WPT");
        try {
            FileInputStream fin = new FileInputStream(f);
            long size = 0;
            StringBuilder sb = new StringBuilder();
            while(fin.available() > 0) {
                int i = fin.read();
                if (i == 13) {
                    if (sb.length() > 10) {
                        System.out.println(sb.toString().substring(1));
                    }
                    sb = new StringBuilder();
                } else if (i > 30 && i < 127) {
                    sb.append((char)i);
                }
                
                //System.out.println((char)i);
                if (i == 13) size++;
            }
            fin.close();
            System.out.println("size = " + size);
        } catch (Exception ex) {
            Logger.getLogger(FileReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public static void main(String[] args) {
        new FileReader();
    }

}
