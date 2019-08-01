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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

/**
 * <p>
 * ExtractClassPathFiles class.</p>
 *
 * @author Nils Hoffmann
 *
 */
public class ExtractClassPathFiles extends ExternalResource {

    private final TemporaryFolder tf;
    private final ClassPathFile[] classPathFiles;
    private final List<File> files = new LinkedList<>();
    private File baseFolder;

    /**
     * <p>
     * Constructor for ExtractClassPathFiles.</p>
     *
     * @param classPathFiles an array of {@link ClassPathFile} objects to extract.
     */
    public ExtractClassPathFiles(ClassPathFile... classPathFiles) {
        this(new TemporaryFolder(), classPathFiles);
    }
    
    /**
     * <p>
     * Constructor for ExtractClassPathFiles.</p>
     *
     * @param tf a {@link org.junit.rules.TemporaryFolder} object.
     * @param classPathFiles an array of {@link ClassPathFile} objects to extract.
     */
    public ExtractClassPathFiles(TemporaryFolder tf, ClassPathFile... classPathFiles) {
        this.tf = tf;
        this.classPathFiles = classPathFiles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void before() throws Throwable {
        try {
            this.tf.create();
        } catch (IOException ex) {
            throw ex;
        }
        baseFolder = tf.getRoot();
        int i = 0;
        for (ClassPathFile resource : classPathFiles) {
            File file = ZipResourceExtractor.extract(
                resource.resourcePath(), baseFolder);
            files.add(file);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void after() {
        for (File f : files) {
            if (f != null && f.exists()) {
                try {
                    Files.deleteIfExists(f.toPath());
                } catch (IOException ioex) {
                    Logger.getLogger(ExtractClassPathFiles.class.getName()).
                        log(Level.SEVERE,
                            "Caught an IOException while trying to delete file " + f.
                                getAbsolutePath(), ioex);
                }
            }
        }
        tf.delete();
    }

    /**
     * <p>
     * Getter for the field <code>files</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<File> getFiles() {
        return this.files;
    }

    /**
     * <p>
     * getBaseDir.</p>
     *
     * @return a {@link java.io.File} object.
     */
    public File getBaseDir() {
        return baseFolder;
    }
}
