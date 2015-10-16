/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.maven.plugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.Archiver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

/**
 * Mojo used when providing the che-maven packaging.
 * Defines a way to embed dependencies (extensions), machines recipes or templates
 * @author Florent Benoit
 */
@Mojo(name = "build",
      defaultPhase = LifecyclePhase.PACKAGE,
      requiresProject = true,
      requiresDependencyCollection = ResolutionScope.RUNTIME)
public class CheMojo extends AbstractMojo {

    /**
     * Extensions folder where to store all dependencies or current artifact being build (if java sources)
     */
    private static String EXTENSIONS_DIRECTORY = "extensions";

    /**
     * All machines found in src/main/che/machines folder will be embedded in this machines folder of zip assembly
     */
    private static String MACHINES_DIRECTORY = "machines";

    /**
     * All templates found in src/main/che/templates folder will be embedded in this templates folder of zip assembly
     */
    private static String TEMPLATES_DIRECTORY = "templates";


    /**
     * Component allowing to create final zip file
     */
    @Component(hint = "zip")
    private Archiver archiver;

    /**
     * Project providing artifact id, version and dependencies
     */
    @Component
    private MavenProject project;

    /**
     * Path to the java source folder of the project (my or may not exist)
     */
    @Parameter(defaultValue = "${project.build.sourceDirectory}")
    private File srcJavaFolder;

    /**
     * Parent folder for all che related sources (machines, templates, etc)
     */
    @Parameter(defaultValue = "${basedir}/src/main/che")
    private File cheFolder;

    /**
     * Destination file to the YML plugin descriptor of the zip assembly
     */
    @Parameter(defaultValue = "${project.build.directory}/che-plugin.yml")
    private File ymlFile;

    /**
     * if project contains java source folder, then there will have a generated jar file
     */
    @Parameter(defaultValue = "${project.build.directory}/${project.artifactId}-${project.version}.jar")
    private File compiledJarFile;

    /**
     * Destination file of the produced zip assembly
     */
    @Parameter(defaultValue = "${project.build.directory}/${project.artifactId}-${project.version}.zip")
    private File destFile;


    /**
     * Entry point of the Che mojo
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        // Add dependency artifacts if it is an assembly (no java code)
        // -----------------------------------------------------------------
        if (!srcJavaFolder.exists()) {
            Set<Artifact> artifacts = project.getDependencyArtifacts();
            if (artifacts != null) {
                artifacts.stream().forEach(artifact ->
                                                   archiver.addFile(artifact.getFile(),
                                                                    EXTENSIONS_DIRECTORY + "/" + artifact.getFile().getName())
                                          );
            }
        } else {
            // java code, needs to add the jar produced by the compiler
            archiver.addFile(compiledJarFile, EXTENSIONS_DIRECTORY + "/" + compiledJarFile.getName());
        }


        // machines
        File machinesFolder = new File(cheFolder, MACHINES_DIRECTORY);
        if (machinesFolder.exists()) {
            try {
                Files.walkFileTree(machinesFolder.toPath(), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                        if (!attrs.isDirectory()) {
                            String entryName = path.toFile().getAbsolutePath().substring(machinesFolder.getAbsolutePath().length());
                            archiver.addFile(path.toFile(), MACHINES_DIRECTORY + entryName);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                throw new MojoExecutionException("Cannot add machines", e);
            }
        }

        // templates
        File templatesFolder = new File(cheFolder, TEMPLATES_DIRECTORY);
        if (templatesFolder.exists()) {
            try {
                Files.walkFileTree(templatesFolder.toPath(), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                        if (!attrs.isDirectory()) {
                            String entryName = path.toFile().getAbsolutePath().substring(templatesFolder.getAbsolutePath().length());
                            archiver.addFile(path.toFile(), TEMPLATES_DIRECTORY + entryName);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                throw new MojoExecutionException("Cannot add machines", e);
            }
        }

        // customize plugin metadata
        // -----------------------------------------------------------------
        if (!ymlFile.getParentFile().exists()) {
            if (!ymlFile.getParentFile().mkdirs()) {
                throw new MojoExecutionException("Unable to create parent directory '" + ymlFile.getParentFile() + "'.");
            }
        }
        try (Writer w = new OutputStreamWriter(new FileOutputStream(ymlFile), "UTF-8");
             PrintWriter writer = new PrintWriter(w)) {
            writer.write("name: " + project.getArtifactId() + "\n");
            writer.write("version: " + project.getVersion() + "\n");
            archiver.addFile(ymlFile, ymlFile.getName());
        } catch (IOException ioe) {
            throw new MojoExecutionException("Unable to write plugin metadata", ioe);
        }

        // Create the archive
        try {
            archiver.setDestFile(destFile);
            archiver.createArchive();
            project.getArtifact().setFile(destFile);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot create plugin", e);
        }
    }

}
