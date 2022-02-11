/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uth.ce325.hw3;

import java.awt.Desktop;
import java.io.*;
import java.nio.file.*;
import static java.nio.file.StandardCopyOption.*;
import java.util.*;
import javax.swing.JOptionPane;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 *
 * @author anton
 */
public class Utilities {
    
    private static final char PATH_SEP = File.separatorChar;
    private static final int XBLENGTH = 1024;
    private static final String[] BUNITS = {"Bytes","KB","MB","GB"};
    private static final String STYPE = " type:";
    private static final String DIRTYPE = "dir";
    private static final String DIRXML = System.getProperty("user.home")+PATH_SEP+".java-file-browser";
    
    /*returns the Files of dir as a list with the following form:
    first catalogs(sorted by name), then files(sorted by name)*/
    public static List<File> getFilesFromDir(File dir){
        List<File> fileList = new ArrayList<>();
        List<File> dirList = new ArrayList<>();
        
        if(dir.isDirectory() == false){
            System.err.println(dir.getAbsolutePath()+" is not a directory.");
            return new ArrayList<File>();
        }
        
        File fileArray[] = dir.listFiles();
        if(fileArray == null){
            System.out.println("ERROR: unable to access files from "+dir);
            return dirList;
        }
        List<File> tempList = Arrays.asList(fileArray);
        Collections.sort(tempList);
        
        for(File f: tempList){
            if(f.isDirectory())
                dirList.add(f);
            else
                fileList.add(f);
        }
        
        dirList.addAll(fileList);
        return dirList;
    }
    
    /*breaks path to hierarchy catalogs*/
    public static List<String> collapsePath(String path){
        List<String> rootCatalogs = new ArrayList<>();
        int lastSlashIndex=-1;
        int temp = path.indexOf(PATH_SEP);
        
        while(temp != -1){
            String ancDir = path.substring(lastSlashIndex+1, temp);
            if(ancDir.equals("") == false)
                rootCatalogs.add(ancDir);
            lastSlashIndex = temp;
            temp = path.indexOf(PATH_SEP, lastSlashIndex+1);
        }
        
        //add last folder
        String lastDir = path.substring(lastSlashIndex+1);
        if(lastDir.equals("") == false)
            rootCatalogs.add(lastDir);
        
        return rootCatalogs;
    }
    
    /*makes the path from the beginning of pathList to selCatalog*/
    public static String buildPath(List<String> pathList, String selCatalog){
        String bPath =  "";
        String temp;
        Iterator it = pathList.iterator();
        
        while(it.hasNext()){
            temp = ""+it.next();
            bPath = bPath + temp;
            if(temp.equals(selCatalog))
                break;
            bPath = bPath +PATH_SEP;
        }
        
        return bPath;
    }
    
    public static void deleteFile(String delName){
        try{
            Files.delete(Paths.get(delName));
            System.out.println("Delete "+ delName +" succeed");
        }
        catch(DirectoryNotEmptyException e){
            List<File> fileList = getFilesFromDir(new File(delName));
            
            //delete every file/dir in the current not empty directory
            for(File fi: fileList){
                deleteFile(fi.getPath());
            }
            //now dirctory is empty, so re-attempt to delete yourself
            deleteFile(delName);
            
        }
        catch(IOException e){
            System.out.println("Delete "+ delName +" FAILED");
        }
    }
    
    public static void moveFile(AppFrame app, File source, File destination, boolean overwrite){
        Path src = FileSystems.getDefault().getPath(source.getPath());
        Path dst = FileSystems.getDefault().getPath(destination.getPath());
        
        try{
            System.out.print("Move "+src+" to "+dst);
            if(overwrite)
                Files.move(src, dst.resolve(src.getFileName()), REPLACE_EXISTING);
            else
                Files.move(src, dst.resolve(src.getFileName()));
            System.out.println(" succeed");
        }
        catch(FileAlreadyExistsException ex){
            int ans = JOptionPane.showConfirmDialog(app, 
                dst.toString()+PATH_SEP+src.getFileName().toString()+
                " already exists.\nDo you want to replace it?", 
                "Overwrite",JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ans == JOptionPane.YES_OPTION){
                System.out.println(" overwriting");
                moveFile(app, src.toFile(), dst.toFile(), true);
            }
            else
                System.out.println(" cancelled");
        }
        catch(DirectoryNotEmptyException ex){
            System.out.println(" deleting old version");
            deleteFile(dst.toString()+PATH_SEP+src.getFileName().toString());
            moveFile(app, src.toFile(), dst.toFile(), false);
        }
        catch(IOException ex){
            System.out.println(" FAILED");
        }
    }
    
    public static void copyFile(AppFrame app, File source, File destination, boolean overwrite){
        Path src = FileSystems.getDefault().getPath(source.getPath());
        Path dst = FileSystems.getDefault().getPath(destination.getPath());
        
        try{
            System.out.print("Copy "+src+" to "+dst);
            if(overwrite)
                Files.copy(src, dst.resolve(src.getFileName()), COPY_ATTRIBUTES,REPLACE_EXISTING);
            else
                Files.copy(src, dst.resolve(src.getFileName()), COPY_ATTRIBUTES);
            System.out.println(" succeed");
            //if source file is a directory, then every file in it should be copied too
            if(source.isDirectory()){
                List<File> fileList = getFilesFromDir(source);
            
                //copy every file/dir in the directory that has been just copied (source)
                for(File fi: fileList){
                    copyFile(app, fi, new File(dst.toString()+PATH_SEP+src.getFileName().toString()), false);
                }
            }
        }
        catch(FileAlreadyExistsException ex){
            int ans = JOptionPane.showConfirmDialog(app, 
                dst.toString()+PATH_SEP+src.getFileName().toString()+
                " already exists.\nDo you want to replace it?", 
                "Overwrite",JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ans == JOptionPane.YES_OPTION){
                System.out.println(" overwriting");
                copyFile(app, src.toFile(), dst.toFile(), true);
            }
            else
                System.out.println(" cancelled");
        }
        catch(DirectoryNotEmptyException ex){
            System.out.println(" deleting old version");
            deleteFile(dst.toString()+PATH_SEP+src.getFileName().toString());
            copyFile(app, src.toFile(), dst.toFile(), false);
        }
        catch(IOException ex){
            System.out.println(" FAILED");
        }
    }
    
    public static String getFileSize(File file){
        long fileSizeInBytes;
        float fileSize;
        int i=0;
        
        fileSizeInBytes = getSizeInBytes(file);
        fileSize = fileSizeInBytes;
        while(fileSize>XBLENGTH && i+1<BUNITS.length){
            fileSize = fileSize / XBLENGTH;
            i++;
        }
        
        if(i==0)
            return (int)fileSize+" "+BUNITS[i];
        else
            return (int)fileSize+" "+BUNITS[i]+" ("+fileSizeInBytes+" "+BUNITS[0]+")";
    }
    
    public static long getSizeInBytes(File file){
        long size = 0;
        
        if(file.exists() == false)
            return -1;
        
        if(file.isDirectory()){
            List<File> files = getFilesFromDir(file);
            for(File f: files){
                size += getSizeInBytes(f);
            }
        }
        else
            size = file.length();
        
        return size;
    }
    
    public static List<File> searchFile(String TFInput, File dir){
        String type;
        String keyword;

        int index = TFInput.lastIndexOf(STYPE);
        //if there is type:<type> specifier
        if(index>-1){
            type = TFInput.substring(index+STYPE.length()).toLowerCase();
            keyword = TFInput.substring(0, index).toLowerCase();
            System.out.println("Search for \""+keyword+"\" with type \""+type+"\" in "+ dir);
        }
        else{
            type = "";  //if no type is specified
            keyword = TFInput.toLowerCase();
            System.out.println("Search for \""+keyword+"\" in "+ dir);
        }
        
        return searchFileInDir(keyword, type, dir); 
    }
    
    //typ is optional, should be "" (empty String) if it is not used
    private static List<File> searchFileInDir(String key, String typ, File dir){
        List<File> res = new ArrayList<>();
        List<File> files = getFilesFromDir(dir);
        boolean isRes;
        
        for(File f: files){
            String fileName = f.getName().toLowerCase();
            String fileExt = "";
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
               fileExt = fileName.substring(dotIndex+1);
               //fileName = fileName.substring(0, dotIndex);
            }
            
            isRes = (typ.equals(""))?
                    fileName.contains(key) : 
                    fileName.contains(key) && fileExt.equals(typ);
            if(isRes)
                res.add(f);
            if(f.isDirectory()){
                //special case with type dir
                if(fileName.contains(key) && typ.equals(DIRTYPE))
                    res.add(f);
                
                //search inside f directory
                res.addAll(searchFileInDir(key, typ, f));
            }
        }
        
        return res;
    }
    
    public static void runExecFile(File file){
        try {
            Runtime.getRuntime().exec(file.getPath(), null, file.getParentFile());
        } catch (IOException e) {
            System.err.println("IOException while running "+file);
        }
    }
    
    public static boolean openFileWithDefApp(File file) {
        if (!Desktop.isDesktopSupported()) {
            return false;
        }

        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.OPEN)) {
          return false;
        }

        try {
          desktop.open(file);
        } catch (IOException e) {
          System.err.println("IOException occured trying to open: "+file.getName());
          return false;
        }

        return true;
    }
    
    //writes the list of favourite directories in the predefined XML file
    public static void writeXMLFavFile(List<String> favList){
        try{
            File xmlFolder = new File(DIRXML);
            if(xmlFolder.exists() == false){
                if(xmlFolder.mkdir())
                    System.out.println("Create "+xmlFolder);
                else{
                    System.out.println("Create "+xmlFolder+" FAILED");
                    return;
                }
            }
            
            File xmlFile = new File(DIRXML+PATH_SEP+"properties.xml");
            if(xmlFile.exists() == false){
                xmlFile.createNewFile();
                System.out.println("Create "+xmlFile);
            }
            
            DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();
            
            //create root element
            Element root = doc.createElement("favourites");
            doc.appendChild(root);
            
            //create dir-elements
            for(int i=0; i<favList.size(); i++){
                Element e = doc.createElement("dir");
                e.setAttribute("path", favList.get(i));
                root.appendChild(e);
            }
            
            //create xml file
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource dom = new DOMSource(doc);
            StreamResult strRes = new StreamResult(xmlFile);
            transformer.transform(dom, strRes);
        }
        catch(ParserConfigurationException ex){
            System.out.println("ParserConfigurationException occured while writing XML file");
        }
        catch(TransformerConfigurationException ex){
            System.out.println("TransformerConfigurationException occured while writing XML file");
        }
        catch(TransformerException ex){
            System.out.println("TransformerException occured while writing XML file");
        }
        catch(IOException ex){
            System.out.println("IOException occured while writing XML file");
        }
    }
    
    /*returns the list of favourite directories in the predefined XML file
    if XML file does not exist, an empty list is returned*/
    public static List<String> readXMLFavFile(){
        List<String> res = new ArrayList<>();
        
        try{
            File xmlFile = new File(DIRXML+PATH_SEP+"properties.xml");
            if(xmlFile.exists() == false){
                //System.out.println(xmlFile+" does not exist");
                return res;
            }
            
            DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            
            NodeList favList = doc.getElementsByTagName("dir");
            for(int i=0; i<favList.getLength(); i++){
                Node n = favList.item(i);
                if(n.getNodeType() == Node.ELEMENT_NODE){
                    Element dir = (Element) n;
                    String path = dir.getAttribute("path");
                    res.add(path);
                }
            }
            
        }
        catch(ParserConfigurationException ex){
            System.out.println("ParserConfigurationException occured while reading XML file");
        }
        catch(SAXException ex){
            System.out.println("SAXException occured while reading XML file");
        }
        catch(IOException ex){
            System.out.println("IOException occured while reading XML file");
        }
        
        return res;
    }
    
    public static void addXMLFav(String fav){
        List<String> favList = readXMLFavFile();
        //prevent double entries
        if(favList.contains(fav) == false){
            favList.add(fav);
            writeXMLFavFile(favList);
        }
    }
    
    public static void delXMLFav(String fav){
        List<String> favList = readXMLFavFile();
        favList.remove(fav);
        writeXMLFavFile(favList);
    }
}
