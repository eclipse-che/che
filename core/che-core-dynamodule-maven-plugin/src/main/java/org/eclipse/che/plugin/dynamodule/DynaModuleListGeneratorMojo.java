/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.dynamodule;

import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Mojo for generating Guice module that will include all {@link org.eclipse.che.inject.DynaModule}
 * annotations found.<br>
 * It allows to disable the runtime scan that is scanning all JAR files.
 *
 * @author Florent Benoit
 */
@Mojo(
  name = "build",
  defaultPhase = LifecyclePhase.PROCESS_CLASSES,
  requiresProject = true,
  requiresDependencyCollection = ResolutionScope.RUNTIME
)
public class DynaModuleListGeneratorMojo extends AbstractMojo {

  /** Project providing artifact id, version and dependencies. */
  @Parameter(defaultValue = "${project}", readonly = true)
  private MavenProject project;

  /** build directory. */
  @Parameter(defaultValue = "${project.build.directory}")
  private File targetDirectory;

  /** List pattern of files to be excluded during the scan. */
  @Parameter private String[] skipResources;

  /** Directory used to generate the code */
  private File generatedDirectory;

  /** Directory used to unpack war files */
  private File unpackedDirectory;

  /** Local Repository. */
  @Parameter(defaultValue = "${localRepository}", readonly = true, required = true)
  protected ArtifactRepository localRepository;

  /** Path to the generated file */
  private File guiceGeneratedModuleFile;

  /** Use of classpath instead of dependencies */
  private boolean useClassPath;

  /** Scan war dependencies */
  @Parameter private boolean scanWarDependencies;

  /** Scan .jar files in war dependencies */
  @Parameter(defaultValue = "true")
  private boolean scanJarInWarDependencies;

  private DynaModuleListByteCodeGenerator dynaModuleListGenerator;

  /** Repository system used to generate a repository session. */
  @Component private org.apache.maven.repository.RepositorySystem repositorySystem;

  /** The remote repositories used to get artifacts. */
  @Parameter(
    defaultValue = "${project.remoteArtifactRepositories}",
    required = true,
    readonly = true
  )
  private List<ArtifactRepository> artifactRepositories;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    long start = System.currentTimeMillis();
    generatedDirectory = new File(targetDirectory, "generated-sources/dynamodules");
    unpackedDirectory = new File(targetDirectory, "unpacked-dynamodule");
    if (!generatedDirectory.exists() && !generatedDirectory.mkdirs()) {
      throw new MojoExecutionException(
          "Unable to create a directory for writing Guice DynaModule file '"
              + generatedDirectory
              + "'.");
    }
    if (!unpackedDirectory.exists() && !unpackedDirectory.mkdirs()) {
      throw new MojoExecutionException(
          "Unable to create a directory for writing Guice unpacked files '"
              + unpackedDirectory
              + "'.");
    }

    dynaModuleListGenerator = new DynaModuleListByteCodeGenerator();
    dynaModuleListGenerator.setSkipResources(skipResources);
    dynaModuleListGenerator.setUnpackedDirectory(unpackedDirectory);
    dynaModuleListGenerator.setScanJarInWarDependencies(scanJarInWarDependencies);

    String className = LOWER_HYPHEN.to(UPPER_CAMEL, project.getArtifactId().replace(".", "-"));

    Collection<URL> urls;
    if (useClassPath) {
      urls = new ArrayList<>();
      String javaClassPath = System.getProperty("java.class.path");
      if (javaClassPath != null) {
        for (String path : javaClassPath.split(File.pathSeparator)) {
          try {
            urls.add(new File(path).toURI().toURL());
          } catch (Exception e) {
            throw new MojoExecutionException("Unable to get URL", e);
          }
        }
      }
    } else {
      urls = new ArrayList<>();
      List<String> elements;
      try {
        elements = project.getCompileClasspathElements();
      } catch (DependencyResolutionRequiredException e) {
        throw new MojoExecutionException("Unable to get classpath elements", e);
      }

      for (String element : elements) {
        try {
          urls.add(new File(element).toURI().toURL());
        } catch (MalformedURLException e) {
          throw new MojoExecutionException("Unable to create URL", e);
        }
      }
    }

    // do we have extra wars ?
    if (scanWarDependencies) {
      for (Artifact dependencyArtifact :
          this.project
              .getDependencyArtifacts()
              .stream()
              .filter(dependency -> "war".equals(dependency.getType()))
              .collect(Collectors.toList())) {
        Artifact toResolveArtifact =
            repositorySystem.createArtifact(
                dependencyArtifact.getGroupId(),
                dependencyArtifact.getArtifactId(),
                dependencyArtifact.getVersion(),
                dependencyArtifact.getScope(),
                dependencyArtifact.getType());

        ArtifactResolutionRequest artifactResolutionRequest = new ArtifactResolutionRequest();
        artifactResolutionRequest.setArtifact(toResolveArtifact);
        artifactResolutionRequest
            .setLocalRepository(localRepository)
            .setRemoteRepositories(artifactRepositories);

        ArtifactResolutionResult resolutionResult;
        resolutionResult = this.repositorySystem.resolve(artifactResolutionRequest);

        // The file should exists, but we never know.
        File file = resolutionResult.getArtifacts().stream().findFirst().get().getFile();
        if (file != null && file.exists()) {
          try {
            urls.add(file.toURI().toURL());
          } catch (MalformedURLException e) {
            throw new MojoExecutionException("Unable to get URL from file " + file, e);
          }
        }
      }
    }

    dynaModuleListGenerator.setUrls(urls);
    dynaModuleListGenerator.setClassName(className);

    // define output path for the file to write
    byte[] byteCodeToGenerate = dynaModuleListGenerator.execute();

    String fullClassName = "org.eclipse.che.dynamodule." + className;

    this.guiceGeneratedModuleFile =
        new File(
            targetDirectory,
            "classes" + File.separator + fullClassName.replace(".", File.separator) + ".class");
    File parentDir = this.guiceGeneratedModuleFile.getParentFile();
    if (!parentDir.exists() && !parentDir.mkdirs()) {
      throw new MojoExecutionException(
          "Unable to create a directory for writing Guice DynaModule file '" + parentDir + "'.");
    }

    try {
      Files.write(guiceGeneratedModuleFile.toPath(), byteCodeToGenerate);
    } catch (IOException e) {
      throw new MojoExecutionException(
          "Unable to generate class for writing Guice DynaModule file '" + parentDir + "'.");
    }

    // And now, generates the ServiceLoader
    File serviceLoaderModuleFile =
        new File(
            targetDirectory,
            "classes"
                + File.separator
                + "META-INF"
                + File.separator
                + "services"
                + File.separator
                + "org.eclipse.che.inject.ModuleFinder");
    File parentServiceLoaderDir = serviceLoaderModuleFile.getParentFile();
    if (!parentServiceLoaderDir.exists() && !parentServiceLoaderDir.mkdirs()) {
      throw new MojoExecutionException(
          "Unable to create a directory for writing Guice ServiceLoader ModuleFinder file '"
              + parentServiceLoaderDir
              + "'.");
    }

    try (Writer fileWriter =
        Files.newBufferedWriter(serviceLoaderModuleFile.toPath(), StandardCharsets.UTF_8)) {
      fileWriter.write(fullClassName);
    } catch (IOException e) {
      throw new MojoExecutionException("Cannot write Guice ServiceLoader DynaModule file", e);
    }

    long end = System.currentTimeMillis();

    getLog().debug("Generating Guice DynaModule file in " + (end - start) + " ms.");
  }

  /**
   * Gets the DynaModule generated file
   *
   * @return the generated file DynaModule link
   */
  public File getGuiceGeneratedModuleFile() {
    return guiceGeneratedModuleFile;
  }

  public DynaModuleListByteCodeGenerator getDynaModuleListGenerator() {
    return dynaModuleListGenerator;
  }
}
