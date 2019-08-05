/*
 * Copyright 2018 Nils Hoffmann &lt;nils.hoffmann@isas.de&gt;.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.isas.mztab2.test.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * <p>
 * ZipResourceExtractor class.</p>
 *
 * @author nilshoffmann
 *
 */
public class ZipResourceExtractor {

    private ZipResourceExtractor() {
        
    }
    
    /**
     * <p>
     * Extract a JAR resource into the provided destination directory and return
     * that extracted resource's location as a file.</p>
     *
     * @param resourcePath a {@link java.lang.String} object.
     * @param destDir a {@link java.io.File} object.
     * @return a {@link java.io.File} object.
     */
    public static File extract(String resourcePath, File destDir) {
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        URL resourceURL = ZipResourceExtractor.class.getResource(resourcePath);
        if (resourceURL == null) {
            throw new NullPointerException(
                "Could not retrieve resource for path: " + resourcePath);
        }
        File outputFile = null;
        try (InputStream resourceInputStream = resourceURL.openStream()) {

            String outname = new File(resourceURL.getPath()).getName();
            outname = outname.replaceAll("%20", " ");
            if (resourcePath.endsWith("zip")) {
                outname = outname.substring(0, outname.lastIndexOf(
                    '.'));
                return extractZipArchive(resourceInputStream, destDir);
            } else if (resourcePath.endsWith("gz")) {
                try (InputStream in = new GZIPInputStream(
                    new BufferedInputStream(
                        resourceInputStream))) {

                    outname = outname.substring(0, outname.lastIndexOf(
                        '.'));
                    return writeTo(destDir, outname, in);
                }
            } else {
                try (InputStream in = new BufferedInputStream(
                    resourceInputStream)) {
                    return writeTo(destDir, outname, in);
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(ZipResourceExtractor.class.getName()).
                log(Level.SEVERE, null, ex);
        }

        return outputFile;
    }

    private static File writeTo(File destDir, String outname, InputStream in) throws FileNotFoundException, IOException {
        File outputFile = new File(destDir, outname);
        try (BufferedOutputStream out = new BufferedOutputStream(
            new FileOutputStream(outputFile))) {

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
        return outputFile;
    }

    /**
     * <p>
     * extractZipArchive.</p>
     *
     * @param istream a {@link java.io.InputStream} object.
     * @param outputDir a {@link java.io.File} object.
     * @return a {@link java.io.File} object.
     */
    public static File extractZipArchive(InputStream istream, File outputDir) {
        try {
            File outDir;
            try (ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(istream))) {
                ZipEntry entry;
                outDir = null;
                while ((entry = zis.getNextEntry()) != null) {
                    int size;
                    byte[] buffer = new byte[2048];
                    File outFile = new File(outputDir, entry.getName());
                    if (entry.isDirectory()) {
                        outFile.mkdirs();
                        if (outDir == null) {
                            outDir = outFile;
                        }
                    } else {
                        try (FileOutputStream fos = new FileOutputStream(outFile)) {
                            BufferedOutputStream bos = new BufferedOutputStream(
                                fos, buffer.length);
                            while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
                                bos.write(buffer, 0, size);
                            }
                            bos.flush();
                            bos.close();
                        }
                    }
                }
                if (outDir == null) {
                    outDir = outputDir;
                }
            }
            istream.close();
            return outDir;
        } catch (IOException e) {
            throw new ZipResourceExtractionException(e);
        }
    }
}
