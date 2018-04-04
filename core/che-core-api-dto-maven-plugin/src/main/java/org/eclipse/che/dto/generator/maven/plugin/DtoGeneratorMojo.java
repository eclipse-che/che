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
package org.eclipse.che.dto.generator.maven.plugin;

import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.eclipse.che.dto.generator.DtoGenerator;

/** Mojo to run {@link org.eclipse.che.dto.generator.DtoGenerator}. */
@Mojo(name = "generate", requiresDependencyResolution = ResolutionScope.COMPILE)
public class DtoGeneratorMojo extends AbstractMojo {
  @Parameter(property = "outputDir", required = true)
  private String outputDirectory;

  @Parameter(property = "dtoPackages", required = true)
  private String[] dtoPackages;

  @Parameter(property = "genClassName", required = true)
  private String genClassName;

  @Parameter(property = "impl", required = true)
  private String impl;

  /** A flag to disable generation of the DTOs. */
  @Parameter(property = "che.dto.skip", defaultValue = "false")
  private boolean skip;

  @Override
  public void execute() throws MojoExecutionException {
    if (skip) {
      getLog().info("Skipping the execution");
      return;
    }

    DtoGenerator dtoGenerator = new DtoGenerator();
    dtoGenerator.setPackageBase(outputDirectory);
    String genFileName = genClassName.replace('.', File.separatorChar) + ".java";
    dtoGenerator.setGenFileName(
        outputDirectory.endsWith("/")
            ? (outputDirectory + genFileName)
            : (outputDirectory + File.separatorChar + genFileName));
    dtoGenerator.setImpl(impl);
    dtoGenerator.setDtoPackages(dtoPackages);
    dtoGenerator.generate();
  }
}
