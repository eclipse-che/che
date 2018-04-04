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
package org.eclipse.che.plugin.gwt;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.eclipse.che.plugin.gwt.Utils.getFileContent;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipFile;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.Xpp3DomWriter;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Detects if Full IDE dependency with excluded plugins declared in pom.xml and modifies the IDE GWT
 * module to avoid inheriting modules of the excluded plugins.
 */
@Mojo(
  name = "process-excludes",
  defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
  requiresDependencyResolution = ResolutionScope.COMPILE
)
public class ProcessExcludesMojo extends AbstractMojo {

  public static final String FULL_IDE_ARTIFACT_ID = "che-ide-full";
  public static final String FULL_IDE_GWT_MODULE_SUFFIX = "-with-excludes";

  @Component private RepositorySystem repositorySystem;

  /** The local repository to use for the artifacts resolution. */
  @Parameter(defaultValue = "${localRepository}", required = true, readonly = true)
  private ArtifactRepository localRepository;

  /** The remote repositories to use for the artifacts resolution. */
  @Parameter(
    defaultValue = "${project.remoteArtifactRepositories}",
    required = true,
    readonly = true
  )
  private List<ArtifactRepository> remoteRepositories;

  @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
  private File outputDirectory;

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject project;

  /** Full IDE maven artifact. */
  private Artifact fullIdeArtifact;

  /** Name of the Full IDE GWT module. */
  private String fullIdeGwtModule;

  @Override
  public void execute() throws MojoExecutionException {
    if (!init()) {
      getLog().debug(format("No dependency on '%s'. Skipping the execution", FULL_IDE_ARTIFACT_ID));
      return;
    }

    getLog().debug("Detected dependency on " + fullIdeArtifact.getArtifactId());

    try {
      Set<String> excludedModules = detectExcludedGwtModules();

      if (excludedModules.isEmpty()) {
        getLog().debug("No excluded GWT modules detected. Skipping the execution");
        return;
      }

      createFullIdeModuleWithExcludes(excludedModules);
      patchGwtModule();
    } catch (XmlPullParserException | IOException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  /**
   * Initializes mojo with the essential information about Full IDE dependency.
   *
   * @return {@code true} if there is a dependency on Full IDE in pom.xml; otherwise - {@code false}
   * @throws IllegalStateException if error occurs during getting Full IDE dependency information
   */
  private boolean init() {
    Optional<Artifact> fullIdeArtifactOpt =
        project
            .getDependencyArtifacts()
            .stream()
            .filter(artifact -> FULL_IDE_ARTIFACT_ID.equals(artifact.getArtifactId()))
            .findAny();

    if (!fullIdeArtifactOpt.isPresent()) {
      return false;
    }

    fullIdeArtifact = fullIdeArtifactOpt.get();

    try {
      fullIdeGwtModule = readGwtModuleName(fullIdeArtifact);
    } catch (IOException e) {
      throw new IllegalStateException(e.getMessage(), e);
    }

    return true;
  }

  /**
   * Detects the GWT modules that should be excluded from the IDE GWT app.
   *
   * @return detected GWT modules
   */
  private Set<String> detectExcludedGwtModules() throws IOException {
    Set<String> modules = new HashSet<>();

    ArtifactFilter dependencyFilter = fullIdeArtifact.getDependencyFilter();

    if (dependencyFilter instanceof ExcludesArtifactFilter) {
      ExcludesArtifactFilter excludesDependencyFilter = (ExcludesArtifactFilter) dependencyFilter;

      for (String pattern : excludesDependencyFilter.getPatterns()) {
        String[] split = pattern.split(":");
        String groupId = split[0];
        String artifactId = split[1];
        String version = fullIdeArtifact.getVersion();

        Artifact artifact = repositorySystem.createArtifact(groupId, artifactId, version, "jar");
        String gwtModule = readGwtModuleName(artifact);

        modules.add(gwtModule);

        getLog().info("Detected GWT module to exclude: " + gwtModule);
      }
    }

    return modules;
  }

  /** Creates copy of the Full.gwt.xml with removed '<inherits>' for the excluded GWT modules. */
  private void createFullIdeModuleWithExcludes(Set<String> modulesToExclude)
      throws XmlPullParserException, IOException {
    String fullIdeGwtModulePath = fullIdeGwtModule.replace('.', '/') + ".gwt.xml";
    String fullIdeGwtModuleContent =
        getFileContent(new ZipFile(fullIdeArtifact.getFile()), fullIdeGwtModulePath);

    InputStream in = new ByteArrayInputStream(fullIdeGwtModuleContent.getBytes(UTF_8.name()));
    Xpp3Dom module = Xpp3DomBuilder.build(in, UTF_8.name());

    for (int i = module.getChildCount() - 1; i >= 0; i--) {
      Xpp3Dom child = module.getChild(i);

      if ("inherits".equals(child.getName())) {
        String moduleName = child.getAttribute("name");

        if (modulesToExclude.contains(moduleName)) {
          module.removeChild(i);
        }
      }
    }

    String moduleRelPath =
        fullIdeGwtModulePath.replace(".gwt.xml", FULL_IDE_GWT_MODULE_SUFFIX + ".gwt.xml");

    Path modulePath = Paths.get(outputDirectory.getPath(), moduleRelPath);

    try (Writer writer = new StringWriter()) {
      XMLWriter xmlWriter = new PrettyPrintXMLWriter(writer);
      Xpp3DomWriter.write(xmlWriter, module);
      Files.write(modulePath, writer.toString().getBytes());
    }
  }

  /**
   * Patches the IDE GWT module by replacing inheritance of Full.gwt.xml by
   * Full-with-excludes.gwt.xml.
   */
  private void patchGwtModule() throws XmlPullParserException, IOException {
    String gwtModuleFileRelPath = getGwtModule().replace('.', '/') + ".gwt.xml";
    Path gwtModuleFilePath = Paths.get(outputDirectory.getPath(), gwtModuleFileRelPath);

    Xpp3Dom module = Xpp3DomBuilder.build(Files.newInputStream(gwtModuleFilePath), UTF_8.name());

    for (int i = module.getChildCount() - 1; i >= 0; i--) {
      Xpp3Dom child = module.getChild(i);

      if ("inherits".equals(child.getName())) {
        String moduleName = child.getAttribute("name");

        if (moduleName.equals(fullIdeGwtModule)) {
          child.setAttribute("name", fullIdeGwtModule + FULL_IDE_GWT_MODULE_SUFFIX);
          break;
        }
      }
    }

    try (Writer writer = new StringWriter()) {
      XMLWriter xmlWriter = new PrettyPrintXMLWriter(writer);
      Xpp3DomWriter.write(xmlWriter, module);
      Files.write(gwtModuleFilePath, writer.toString().getBytes());
    }
  }

  /** Returns GWT module name specified in the configuration of the gwt-maven-plugin. */
  private String getGwtModule() {
    Xpp3Dom configuration =
        project.getGoalConfiguration("net.ltgt.gwt.maven", "gwt-maven-plugin", null, null);

    return configuration.getChild("moduleName").getValue();
  }

  /** Reads name of GWT module from the given artifact. */
  private String readGwtModuleName(Artifact artifact) throws IOException {
    if (artifact.getFile() == null) {
      resolveArtifact(artifact);
    }

    return getFileContent(new ZipFile(artifact.getFile()), "META-INF/gwt/mainModule");
  }

  /**
   * Resolves the given artifact from a repository.
   *
   * @param artifact artifact to resolve
   * @throws IllegalStateException if unable to resolve artifact
   */
  private void resolveArtifact(Artifact artifact) {
    ArtifactResolutionRequest resolutionRequest =
        new ArtifactResolutionRequest()
            .setLocalRepository(localRepository)
            .setRemoteRepositories(remoteRepositories)
            .setArtifact(artifact);

    ArtifactResolutionResult resolutionResult = repositorySystem.resolve(resolutionRequest);

    if (!resolutionResult.isSuccess()) {
      throw new IllegalStateException("Unable to resolve artifact " + artifact.toString());
    }
  }
}
