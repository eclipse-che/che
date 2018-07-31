/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.typescript.dto;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

/**
 * Mojo for generating TypeScript DTO interface + implementation for handling JSON data.
 *
 * @author Florent Benoit
 */
@Mojo(
  name = "build",
  defaultPhase = LifecyclePhase.PACKAGE,
  requiresProject = true,
  requiresDependencyCollection = ResolutionScope.RUNTIME
)
public class TypeScriptDTOGeneratorMojo extends AbstractMojo {

  /** Project providing artifact id, version and dependencies. */
  @Parameter(defaultValue = "${project}", readonly = true)
  private MavenProject project;

  /** build directory used to write the intermediate bom file. */
  @Parameter(defaultValue = "${project.build.directory}")
  private File targetDirectory;

  @Component private MavenProjectHelper projectHelper;

  /** Path to the generated typescript file */
  private File typescriptFile;

  /** Use of classpath instead of classloader */
  private boolean useClassPath;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    getLog().info("Generating TypeScript DTO");

    TypeScriptDtoGenerator typeScriptDtoGenerator = new TypeScriptDtoGenerator();

    typeScriptDtoGenerator.setUseClassPath(useClassPath);

    // define output path for the file to write with typescript definition
    String output = typeScriptDtoGenerator.execute();

    this.typescriptFile = new File(targetDirectory, project.getArtifactId() + ".ts");
    File parentDir = this.typescriptFile.getParentFile();
    if (!parentDir.exists() && !parentDir.mkdirs()) {
      throw new MojoExecutionException(
          "Unable to create a directory for writing DTO typescript file '" + parentDir + "'.");
    }

    try (Writer fileWriter =
        Files.newBufferedWriter(this.typescriptFile.toPath(), StandardCharsets.UTF_8)) {
      fileWriter.write(output);
    } catch (IOException e) {
      throw new MojoExecutionException("Cannot write DTO typescript file");
    }

    // attach this typescript file as maven artifact
    projectHelper.attachArtifact(project, "ts", typescriptFile);
  }

  /**
   * Gets the TypeScript generated file
   *
   * @return the generated file TypeScript link
   */
  public File getTypescriptFile() {
    return typescriptFile;
  }

  /**
   * Allow to configure generator to use classpath instead of classloader
   *
   * @param useClassPath true if want to use classpath loading
   */
  public void setUseClassPath(boolean useClassPath) {
    this.useClassPath = useClassPath;
  }
}
