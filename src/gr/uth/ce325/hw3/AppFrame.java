/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uth.ce325.hw3;

import java.awt.FlowLayout;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;

/**
 *
 * @author anton
 */
public class AppFrame extends javax.swing.JFrame {

    /**
     * Creates new form AppFrame
     */
    
    private File currDir;
    private UIFile selFile;
    private File markedFile;
    private boolean markedForCut;   //false means marked for Copy
    private static final String ICON_DIR = "icons" + File.separatorChar;
    private static final String HOME_DIR = System.getProperty("user.home");
    private final JMenuItem[] editItems;
    
    public AppFrame() {
        System.out.println("Create new window");
        initComponents();
        
        editItems = new JMenuItem[]{jMenuItemCut,
                                    jMenuItemCopy,
                                    jMenuItemPaste,
                                    jMenuItemRename,
                                    jMenuItemDelete,
                                    jMenuItemAddFav,
                                    jMenuItemProp};
        
        setMarkedFile(null);
        
        /* Initialization*/
        setCurrDir(HOME_DIR);
        
        updateFav();
    }
    
    private ImageIcon getDefIcon(List<File> icons, File fileName){
        String fileExt = "";

        int dotIndex = fileName.getName().lastIndexOf('.');
        if (dotIndex > 0) {
            fileExt = fileName.getName().substring(dotIndex+1);
        }
        if(fileName.isDirectory())
            return new ImageIcon(ICON_DIR+"folder.png");
        
        for(File i: icons){
            String iconNoExt = i.getName().substring(0, i.getName().indexOf('.'));
            if( iconNoExt.equals(fileExt) )
                    return new ImageIcon(ICON_DIR+i.getName()); 
        }
        
        return new ImageIcon(ICON_DIR+"question.png");
    }
    
    private void updateFav(){
        List<String> favList = Utilities.readXMLFavFile();
        JPanel favPanel = new JPanel();
        favPanel.setLayout(new BoxLayout(favPanel, BoxLayout.Y_AXIS));
        jScrollPaneFav.setViewportView(favPanel);
        
        //add home dir as favourite (default)
        favList.add(0, HOME_DIR);
        
        favPanel.add(new JLabel("Favourites:"));
        
        for(String s: favList){
            JLabel tempLabel = new JLabel(new File(s).getName());   //only the name will be shown, not the whole path
            tempLabel.addMouseListener(new MouseAdapter(){
                @Override
                public void mouseClicked(MouseEvent e){
                    //if left button clicked
                    if(e.getButton() == MouseEvent.BUTTON1){
                        setCurrDir(s);
                    }
                    //if right button clicked
                    else if(e.getButton() == MouseEvent.BUTTON3){
                        getDelFavPopupMenu(s).show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            });
            favPanel.add(tempLabel);
        }
    }
    
    private void updateUIFiles(){
        JPanel currDirPanel = new JPanel();
        currDirPanel.setLayout(new WrapLayout(FlowLayout.LEFT));
        jScrollPaneCurrDir.setViewportView(currDirPanel);
        
        List<File> fileList = Utilities.getFilesFromDir(currDir);
        List<File> icons = Utilities.getFilesFromDir(new File(ICON_DIR));
        
        for(File f: fileList){
            UIFile uif = new UIFile(this, f, getDefIcon(icons, f));
            currDirPanel.add(uif.getPanel());
        }
    }
    
    private void updateBreadButtons(){
        List<String> breadcrumbCatalogs = Utilities.collapsePath(currDir.getPath());
        Iterator it = breadcrumbCatalogs.iterator();
        
        javax.swing.JPanel breadPanel = new javax.swing.JPanel();
        breadPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        jScrollPaneBread.setViewportView(breadPanel);
        
        boolean isFirst = true; //flag, the first catalog does not have Label '>' behind
        while(it.hasNext()){
            javax.swing.JButton tempBut= new javax.swing.JButton();
            tempBut.setText(it.next().toString());
            tempBut.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    String newPath = currDir.getPath().substring(0, currDir.getPath().indexOf(tempBut.getText())+tempBut.getText().length());
                    //String newPath = Utilities.buildPath(breadcrumbCatalogs, tempBut.getText());
                    setCurrDir(newPath);
                }
            });
            tempBut.setEnabled(it.hasNext());   //last dir is disabled
            
            if(!isFirst)
                breadPanel.add(new javax.swing.JLabel(">"));
            
            breadPanel.add(tempBut);
            
            isFirst = false;
        }
    }
    
    private void refreshUI(){
        setCurrDir(currDir.getPath());
    }
    private void setMarkedFile(File f){
        markedFile = f;
        if(f == null)
            jMenuItemPaste.setEnabled(false);
        else
            jMenuItemPaste.setEnabled(true);
    }
    
    private void setCurrDirDebugAbsolute(String debugPath){
        if(new File(debugPath).isAbsolute() == false){
            System.out.println("FAILED to change directory, "+debugPath+" is not an absolute path\n-----");
        }
        else{
            System.out.println("Proceeding with "+debugPath+" , which is an absolute path\n-----");
            setCurrDir(debugPath);
        }
    }
    public void setCurrDir(String newPath){
        if(new File(newPath).isDirectory() == false){
            System.out.println("FAILED to change directory, "+newPath+" is not a directory");
            return;
        }
        if(new File(newPath).isAbsolute() == false){
            System.out.println("-----\nWARNING, "+newPath+" is not an absolute path");
            
            String debugPath = newPath+File.separatorChar;
            System.out.println("Trying "+debugPath);
            
            setCurrDirDebugAbsolute(debugPath);
            return;
        }
        
        setSelFile(null);
        currDir = new File(newPath);
        System.out.println("Current directory is "+currDir.getPath());        
        updateBreadButtons();
        updateUIFiles();
    }
    
    public File getCurrDir(){
        return currDir;
    }
    public UIFile getSelFile(){
        return selFile;
    }
    public void setSelFile(UIFile uif){
        //diselect previously selected UIFile (if any)
        if(selFile != null) 
            selFile.setSelected(false);
        
        selFile = uif;
        //select UIFile uif
        if(uif == null)
            jMenuEdit.setEnabled(false);
        else{
            jMenuEdit.setEnabled(true);
            uif.setSelected(true);
        }
    }
    public JPopupMenu getPopupMenu(){
        JPopupMenu pop = new JPopupMenu();
        for(JMenuItem it:editItems){
            pop.add(it);
        }
        updatePasteText();
        return pop;
    }
    private void updatePasteText(){
        if(selFile == null)
            return;
        if(selFile.getFile().isDirectory())
            jMenuItemPaste.setText("Paste Into");
        else
            jMenuItemPaste.setText("Paste Here");
    }
    private JPopupMenu getDelFavPopupMenu(String delFav){
        JPopupMenu pop = new JPopupMenu();
        
        JMenuItem jMenuItemDelFav = new JMenuItem("Remove Favourite");
        jMenuItemDelFav.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                System.out.println("Remove "+delFav+" from Favourites");
                Utilities.delXMLFav(delFav);
                updateFav();
            }
        });
        
        //cannot delete home directory from favourites
        if(delFav.equals(HOME_DIR))
            jMenuItemDelFav.setEnabled(false);
        
        pop.add(jMenuItemDelFav);
        
        return pop;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPaneFav = new javax.swing.JScrollPane();
        jTextFieldSearch = new javax.swing.JTextField();
        jButtonSearch = new javax.swing.JButton();
        jScrollPaneBread = new javax.swing.JScrollPane();
        jScrollPaneCurrDir = new javax.swing.JScrollPane();
        jMenuBar = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemNWindow = new javax.swing.JMenuItem();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenuEdit = new javax.swing.JMenu();
        jMenuItemCut = new javax.swing.JMenuItem();
        jMenuItemCopy = new javax.swing.JMenuItem();
        jMenuItemPaste = new javax.swing.JMenuItem();
        jMenuItemRename = new javax.swing.JMenuItem();
        jMenuItemDelete = new javax.swing.JMenuItem();
        jMenuItemAddFav = new javax.swing.JMenuItem();
        jMenuItemProp = new javax.swing.JMenuItem();
        jMenuSearch = new javax.swing.JMenu();
        jMenuItemSearch = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Alpaca File Browser");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jScrollPaneFav.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jTextFieldSearch.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jTextFieldSearch.setEnabled(false);

        jButtonSearch.setText("Search");
        jButtonSearch.setEnabled(false);
        jButtonSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSearchActionPerformed(evt);
            }
        });

        jScrollPaneBread.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        jScrollPaneCurrDir.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jMenuFile.setText("File");

        jMenuItemNWindow.setText("New Window");
        jMenuItemNWindow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemNWindowActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemNWindow);

        jMenuItemExit.setText("Exit");
        jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExit);

        jMenuBar.add(jMenuFile);

        jMenuEdit.setText("Edit");
        jMenuEdit.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                jMenuEditMenuSelected(evt);
            }
        });

        jMenuItemCut.setText("Cut");
        jMenuItemCut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCutActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemCut);

        jMenuItemCopy.setText("Copy");
        jMenuItemCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCopyActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemCopy);

        jMenuItemPaste.setText("Paste");
        jMenuItemPaste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPasteActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemPaste);

        jMenuItemRename.setText("Rename");
        jMenuItemRename.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRenameActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemRename);

        jMenuItemDelete.setText("Delete");
        jMenuItemDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemDeleteActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemDelete);

        jMenuItemAddFav.setText("Add to Favourites");
        jMenuItemAddFav.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddFavActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemAddFav);

        jMenuItemProp.setText("Properties");
        jMenuItemProp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPropActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemProp);

        jMenuBar.add(jMenuEdit);

        jMenuSearch.setText("Search");

        jMenuItemSearch.setText("Toggle");
        jMenuItemSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchToggleHandler(evt);
            }
        });
        jMenuSearch.add(jMenuItemSearch);

        jMenuBar.add(jMenuSearch);

        setJMenuBar(jMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPaneFav, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextFieldSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 613, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonSearch))
                    .addComponent(jScrollPaneBread)
                    .addComponent(jScrollPaneCurrDir))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPaneFav, javax.swing.GroupLayout.DEFAULT_SIZE, 578, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonSearch))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneBread, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneCurrDir))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void searchToggleHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchToggleHandler
        boolean en = jTextFieldSearch.isEnabled();
        jTextFieldSearch.setEnabled(!en);
        jButtonSearch.setEnabled(!en);
        System.out.println("Set Search enable to "+!en);
    }//GEN-LAST:event_searchToggleHandler

    private void jButtonSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSearchActionPerformed
        //clear selected file
        setSelFile(null);

        //create new Panes
        JPanel sResPanel = new JPanel();
        sResPanel.setLayout(new BoxLayout(sResPanel, BoxLayout.Y_AXIS));
        jScrollPaneCurrDir.setViewportView(sResPanel);
        JPanel sBCPanel = new JPanel();
        sBCPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        jScrollPaneBread.setViewportView(sBCPanel);
        
        //get results
        List<File> sResults = Utilities.searchFile(jTextFieldSearch.getText(), currDir);
        
        //add UI List of results in CurrDir Pane (sResPanel)
        for(File f: sResults){
            JLabel tempLabel = new JLabel(f.getPath());
            tempLabel.addMouseListener(new MouseAdapter(){
                @Override
                public void mouseClicked(MouseEvent e){
                    //if left clicked
                    if(e.getButton() == MouseEvent.BUTTON1){
                        if(f.isDirectory()){
                            setCurrDir(f.getPath());
                        }
                        else{
                            if(Utilities.openFileWithDefApp(f))
                                System.out.println("  Open "+f);
                            else
                                System.err.println("  Failed to open "+f);
                        }
                    }
                }
            });
            sResPanel.add(tempLabel);
        }
        //add Res Label and Exit Search Button in Bread Pane (sBCPanel)
        JLabel numResLabel = new JLabel();
        if(sResults.isEmpty())
            numResLabel.setText("  No results found ");
        else
            numResLabel.setText("  Found "+sResults.size()+" result(s) ");
        sBCPanel.add(numResLabel);
        
        JButton exitSearchB = new JButton("Exit Search View");
        exitSearchB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                refreshUI();
            }
        });
        sBCPanel.add(exitSearchB);
        
        System.out.println(numResLabel.getText());
    }//GEN-LAST:event_jButtonSearchActionPerformed

    private void jMenuItemNWindowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemNWindowActionPerformed
        // TODO add your handling code here:
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AppFrame().setVisible(true);
            }
        });
    }//GEN-LAST:event_jMenuItemNWindowActionPerformed

    private void jMenuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExitActionPerformed
        // TODO add your handling code here:
        System.out.println("Exit window");
        this.setVisible(false);
        //System.exit(0);
    }//GEN-LAST:event_jMenuItemExitActionPerformed

    private void jMenuItemCutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCutActionPerformed
        // TODO add your handling code here:
        System.out.println("Cut "+selFile);
        setMarkedFile(selFile.getFile());
        markedForCut = true;
    }//GEN-LAST:event_jMenuItemCutActionPerformed

    private void jMenuItemRenameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRenameActionPerformed

        System.out.print("Rename "+selFile);
        String newName = (String)JOptionPane.showInputDialog(this, "Enter new file name:", "Rename", JOptionPane.QUESTION_MESSAGE, null, null, selFile.toString());
        if(newName == null)
            System.out.println(" cancelled");
        else{
            System.out.print(" to "+newName);
            Path source = FileSystems.getDefault().getPath(currDir.getPath(),selFile.toString());
            try {
                Files.move(source, source.resolveSibling(newName));
                System.out.println(" succeed");
            } catch (FileAlreadyExistsException ex) {
                System.out.println(" FAILED because "+newName+" already exists");
            } catch (IOException ex) {
                System.out.println(" FAILED");
            }
            
            refreshUI();
        }
    }//GEN-LAST:event_jMenuItemRenameActionPerformed

    private void jMenuItemDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemDeleteActionPerformed
        // TODO add your handling code here:
        int ans = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete "+selFile.toString()+" ?\nThis action cannot be undone.", 
                "Delete",JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ans == JOptionPane.YES_OPTION){
            Utilities.deleteFile(selFile.getFile().getPath());
            refreshUI();
        }
        else
            System.out.println("Delete "+ selFile +" cancelled");
        
            
    }//GEN-LAST:event_jMenuItemDeleteActionPerformed

    private void jMenuItemPropActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPropActionPerformed
        // TODO add your handling code here:
        System.out.println("Properties of "+selFile);
        JOptionPane op = new JOptionPane(
                "Name: "+selFile+"\n"
               +"Path: "+selFile.getFile().getPath()+"\n"
               +"Space: "+Utilities.getFileSize(selFile.getFile())+"\n"
               +"Click OK to apply permissions.", 
                JOptionPane.INFORMATION_MESSAGE);
        op.setVisible(true);
        op.add(new JLabel("Permissions"));
        op.add(new JSeparator());
        
        JCheckBox rd = new JCheckBox("Read");
        rd.setSelected(selFile.getFile().canRead());
        op.add(rd);
        JCheckBox wr = new JCheckBox("Write");
        wr.setSelected(selFile.getFile().canWrite());
        op.add(wr);
        JCheckBox ex = new JCheckBox("Execute");
        ex.setSelected(selFile.getFile().canExecute());
        op.add(ex);
        
        JDialog d = op.createDialog("Properties");
        d.setVisible(true);
        d.dispose();
        
        Object selectedValue = op.getValue();
        
        //if OK was clicked
        if(selectedValue instanceof Integer){
            if( (Integer)selectedValue == JOptionPane.OK_OPTION){
                System.out.print("  Set permission Read to "+rd.isSelected());
                if( selFile.getFile().setReadable(rd.isSelected()) )
                    System.out.println(" succeed");
                else
                    System.out.println(" FAILED");
                
                System.out.print("  Set permission Write to "+wr.isSelected());
                if( selFile.getFile().setWritable(wr.isSelected()) )
                    System.out.println(" succeed");
                else
                    System.out.println(" FAILED");
                
                System.out.print("  Set permission Execute to "+ex.isSelected());
                if( selFile.getFile().setExecutable(ex.isSelected()) )
                    System.out.println(" succeed");
                else
                    System.out.println(" FAILED");
                
            }
        }
        else
            System.out.println("  No change on permissions");
    }//GEN-LAST:event_jMenuItemPropActionPerformed

    private void jMenuItemCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCopyActionPerformed
        // TODO add your handling code here:
        System.out.println("Copy "+selFile);
        setMarkedFile(selFile.getFile());
        markedForCut = false;
    }//GEN-LAST:event_jMenuItemCopyActionPerformed

    private void jMenuItemPasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPasteActionPerformed
        //set as destination the selected directory or the current directory
        File dest = (selFile.getFile().isDirectory())     ? 
                    new File(selFile.getFile().getPath()) : 
                    currDir;
        
        if(markedForCut){
            Utilities.moveFile(this, markedFile, dest, false);
        }
        else{
            Utilities.copyFile(this, markedFile, dest, false);
        }
        
        setMarkedFile(null);
        refreshUI();
    }//GEN-LAST:event_jMenuItemPasteActionPerformed

    private void jMenuEditMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_jMenuEditMenuSelected
        // TODO add your handling code here:
        for(JMenuItem it:editItems){
            jMenuEdit.add(it);
        }
        updatePasteText();
    }//GEN-LAST:event_jMenuEditMenuSelected

    private void jMenuItemAddFavActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddFavActionPerformed
        String favDir = (selFile.getFile().isDirectory())   ? 
                        selFile.getFile().getPath()         : 
                        currDir.getPath();
        
        System.out.println("Add "+favDir+" to Favourites");
        
        //home directory is always in favourites, should not be added again
        if(favDir.equals(HOME_DIR))
            return;
        
        Utilities.addXMLFav(favDir);
        updateFav();
    }//GEN-LAST:event_jMenuItemAddFavActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AppFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AppFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AppFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AppFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AppFrame().setVisible(true);
            }
        });
        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonSearch;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenu jMenuEdit;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenuItem jMenuItemAddFav;
    private javax.swing.JMenuItem jMenuItemCopy;
    private javax.swing.JMenuItem jMenuItemCut;
    private javax.swing.JMenuItem jMenuItemDelete;
    private javax.swing.JMenuItem jMenuItemExit;
    private javax.swing.JMenuItem jMenuItemNWindow;
    private javax.swing.JMenuItem jMenuItemPaste;
    private javax.swing.JMenuItem jMenuItemProp;
    private javax.swing.JMenuItem jMenuItemRename;
    private javax.swing.JMenuItem jMenuItemSearch;
    private javax.swing.JMenu jMenuSearch;
    private javax.swing.JScrollPane jScrollPaneBread;
    private javax.swing.JScrollPane jScrollPaneCurrDir;
    private javax.swing.JScrollPane jScrollPaneFav;
    private javax.swing.JTextField jTextFieldSearch;
    // End of variables declaration//GEN-END:variables
}
