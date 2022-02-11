/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uth.ce325.hw3;

import java.awt.Color;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.nio.file.*;

/**
 *
 * @author anton
 */
public class UIFile {
    private AppFrame app;   //"parent" app Frame
    private File file;
    private ImageIcon icon;
    private JLabel label;
    private JPanel panel;
    private boolean selected;
    private static int INIT_RGB;
    private static final int SEL_RGB = 0x0080c0c0;   //R=128, G=192, B=192
    
    public UIFile(AppFrame app, File file, ImageIcon imgIcon){
        this.app = app;
        this.file = file;
        selected = false;
        icon = imgIcon;
        
        label = new JLabel(file.getName(), icon, JLabel.LEFT);
        label.setHorizontalTextPosition(JLabel.CENTER);
        label.setVerticalTextPosition(JLabel.BOTTOM);
        
        INIT_RGB = label.getBackground().getRGB();
        
        panel = new JPanel();
        panel.add(label);
        panel.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                //if UIFile is double-clicked (left click)
                if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1){
                    openUIFile();
                }
                //else if UIFile is single-clicked
                else if(e.getClickCount() == 1){
                    //left or right click
                    if(!isSelected()){
                        System.out.println("Select "+getFile());
                        setThisAsSelFile();
                    }
                    //if right-clicked
                    if(e.getButton() == MouseEvent.BUTTON3){
                        app.getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                    }
                }

            }
        });
        
    }
    
    private void setThisAsSelFile(){
        app.setSelFile(this);
    }
    
    public JPanel getPanel(){
        return panel;
    }
    
    public void setSelected(boolean sel){
        selected = sel;
        if(sel)
            panel.setBackground(new Color(SEL_RGB));   
        else
            panel.setBackground(new Color(INIT_RGB));
    }
    
    public boolean isSelected(){
        return selected;
    }
    
    public File getFile(){
        return file;
    }
    
    public void openUIFile(){
        Path path = FileSystems.getDefault().getPath(file.getName());
        
        if(file.isDirectory()){
            //System.out.println("Go to "+getFile());
            app.setCurrDir(getFile().getPath());
        }
        else if(Files.isExecutable(path)){
            System.out.println("Run "+getFile());
            Utilities.runExecFile(file);
        }
        else{
            if(Utilities.openFileWithDefApp(file))
                System.out.println("Open "+getFile());
            else
                System.err.println("Failed to open "+getFile());
            
        }
    }
    
    @Override
    public String toString(){
        return getFile().getName();
    }
}
