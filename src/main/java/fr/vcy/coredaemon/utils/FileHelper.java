package fr.vcy.coredaemon.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author vchoury
 */
public class FileHelper {
    
    private FileHelper() { }
    
    /**
     * Deplace le fichier source dans le repertoire destination
     * Si un fichier du meme nom existe, rajoute un suffixe
     * Si le repertoire parent du fichier source est vide, essaye de le supprimer
     */
    public static File move(File sourceFile, File destinationDir) throws IOException {
        return move(sourceFile, destinationDir, sourceFile.getName(), true);
    }
            
    public static File move(File sourceFile, File destinationDir, String destinationFilename, boolean deleteSourceDirIfEmpty) throws IOException {
        File destinationFile = new File(destinationDir, destinationFilename);
        int i = 1;
        while (destinationFile.exists()) {
            destinationFile = new File(destinationFile.getPath() + "-" + i++);
        }
        FileUtils.moveFile(sourceFile, destinationFile);
        if (deleteSourceDirIfEmpty && sourceFile.getParentFile().list().length == 0){
            FileUtils.deleteQuietly(sourceFile.getParentFile());
        }
        return destinationFile;
    }

    /**
     * Deplace le fichier source dans le repertoire destination
     * Si un fichier du meme nom existe, rajoute un suffixe
     */
    public static File copy(File sourceFile, File destinationDir) throws IOException {
        return copy(sourceFile, destinationDir, sourceFile.getName());
    }
            
    public static File copy(File sourceFile, File destinationDir, String destinationFilename) throws IOException {
        File destinationFile = new File(destinationDir, destinationFilename);
        int i = 1;
        while (destinationFile.exists()) {
            destinationFile = new File(destinationFile.getPath() + "-" + i++);
        }
        FileUtils.copyFile(sourceFile, destinationFile);
        return destinationFile;
    }

    /**
     * Zip un répertoire complet.
     *
     * @param dirFile Path du contenu de repertoire a zipper. le repertoire lui-même ne sera pas dans le zip.
     * @param zipDest path complet du zip a creer.
     * @throws IOException
     */
    public static void zip(File dirFile, File zipDest) throws IOException {
        zip(dirFile.listFiles(), zipDest);
    }
    
    /**
     * Zip un tableau de fichiers
     */
    public static void zip(File[] filesSrc, File zipDest) throws IOException {
        FileOutputStream fOut = null;
        BufferedOutputStream bOut = null;
        ZipOutputStream zOut = null;
        try {
            fOut = new FileOutputStream(zipDest);
            bOut = new BufferedOutputStream(fOut);
            zOut = new ZipOutputStream(bOut);
            zOut.setMethod(ZipOutputStream.DEFLATED);
            zOut.setLevel(1); // Taux de compression au plus rapide
            for (File f : filesSrc) {
                ZipEntry zEntry = new ZipEntry(f.getName());
                try {
                    zOut.putNextEntry(zEntry);
                    FileUtils.copyFile(f, zOut);
                } finally {
                    zOut.closeEntry();
                }
            }
        } finally {
            IOUtils.closeQuietly(zOut);
            IOUtils.closeQuietly(bOut);
            IOUtils.closeQuietly(fOut);
        }
    }
    
    /**
     * Unzip les fichiers du zip dans le répertoire cible
     *
     * @param zipFile
     * @param repCible
     * @throws IOException
     */
    public static void unzip(File zipFile, File repCible) throws IOException {
        ZipFile zip = new ZipFile(zipFile);
        try {
            Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();
            while(entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                FileUtils.copyInputStreamToFile(zip.getInputStream(entry), new File(repCible, entry.getName()));
            }
        } finally {
            zip.close();
        }
    }
    
}
