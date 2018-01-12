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
package org.eclipse.che.plugin.maven.server.core.classpath;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.io.File;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.maven.data.MavenArtifact;
import org.eclipse.che.maven.data.MavenArtifactKey;
import org.eclipse.che.maven.server.MavenTerminal;
import org.eclipse.che.plugin.maven.server.MavenServerWrapper;
import org.eclipse.che.plugin.maven.server.MavenWrapperManager;
import org.eclipse.che.plugin.maven.server.core.MavenClasspathContainer;
import org.eclipse.che.plugin.maven.server.core.MavenProgressNotifier;
import org.eclipse.che.plugin.maven.server.core.MavenProjectManager;
import org.eclipse.che.plugin.maven.server.core.project.MavenProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Evgen Vidolob */
@Singleton
public class ClasspathManager {
  public static final String GROUP_ID_ATTRIBUTE = "maven.groupId";
  public static final String ARTIFACT_ID_ATTRIBUTE = "maven.artifactId";
  public static final String VERSION_ATTRIBUTE = "maven.version";
  public static final String CLASSIFIER_ATTRIBUTE = "maven.classifier";
  public static final String PACKAGING_ATTRIBUTE = "maven.packaging";
  public static final String SCOPE_ATTRIBUTE = "maven.scope";

  private static final String SOURCES = "sources";

  private static final Logger LOG = LoggerFactory.getLogger(ClasspathManager.class);
  private final String workspacePath;
  private final MavenWrapperManager wrapperManager;
  private final MavenProjectManager projectManager;
  private final MavenTerminal terminal;
  private final MavenProgressNotifier notifier;
  private File localRepository;

  @Inject
  public ClasspathManager(
      @Named("che.user.workspaces.storage") String workspacePath,
      MavenWrapperManager wrapperManager,
      MavenProjectManager projectManager,
      MavenTerminal terminal,
      MavenProgressNotifier notifier) {

    this.workspacePath = workspacePath;
    this.wrapperManager = wrapperManager;
    this.projectManager = projectManager;
    this.terminal = terminal;
    this.notifier = notifier;
  }

  /**
   * Lazy init of the local repository.
   *
   * @return the local repository
   */
  public synchronized File getLocalRepository() {
    if (localRepository == null) {

      MavenServerWrapper mavenServer =
          wrapperManager.getMavenServer(MavenWrapperManager.ServerType.DOWNLOAD);
      try {
        localRepository = mavenServer.getLocalRepository();
      } catch (RuntimeException e) {
        // We can got this exception if maven not install in system
        // This is temporary solution will be fix more accurate in
        // https://jira.codenvycorp.com/browse/CHE-1120
        LOG.warn("Maven server not started looks like you don't have Maven in your path");
      } finally {
        wrapperManager.release(mavenServer);
      }
    }

    return localRepository;
  }

  public void updateClasspath(MavenProject mavenProject) {
    IJavaProject javaProject = JavaCore.create(mavenProject.getProject());
    if (javaProject != null) {
      IClasspathEntry[] entries = getClasspath(mavenProject);
      MavenClasspathContainer container = new MavenClasspathContainer(entries);
      try {
        JavaCore.setClasspathContainer(
            new Path(MavenClasspathContainer.CONTAINER_ID),
            new IJavaProject[] {javaProject},
            new IClasspathContainer[] {container},
            new NullProgressMonitor());
      } catch (JavaModelException e) {
        LOG.error(e.getMessage(), e);
      }
    }
  }

  private IClasspathEntry[] getClasspath(MavenProject mavenProject) {
    ClasspathHelper helper = new ClasspathHelper(true);

    List<MavenArtifact> dependencies = mavenProject.getDependencies();
    for (MavenArtifact dependency : dependencies) {

      File file = dependency.getFile();
      if (file == null) {
        continue;
      }

      ClasspathEntryHelper entry;
      if (file.getPath().endsWith("pom.xml")) {
        String path = file.getParentFile().getPath();
        entry = helper.addProjectEntry(new Path(path.substring(workspacePath.length())));
      } else {
        entry = helper.addLibraryEntry(new Path(file.getPath()));
      }
      if (entry != null) {
        MavenArtifactKey artifactKey =
            new MavenArtifactKey(
                dependency.getGroupId(),
                dependency.getArtifactId(),
                dependency.getVersion(),
                dependency.getExtension(),
                dependency.getClassifier());
        entry.setArtifactKey(artifactKey);
        attachSources(entry);
      }
    }
    return helper.getEntries();
  }

  private void attachSources(ClasspathEntryHelper entry) {

    MavenArtifactKey artifactKey = entry.getArtifactKey();
    if (artifactKey != null) {
      File artifact =
          MavenLocalRepositoryUtil.getFileForArtifact(
              getLocalRepository(),
              artifactKey.getGroupId(),
              artifactKey.getArtifactId(),
              artifactKey.getVersion(),
              SOURCES,
              artifactKey.getPackaging());
      if (artifact.exists()) {
        entry.setSourcePath(new Path(artifact.getAbsolutePath()));
      }
    }
  }

  public boolean downloadSources(String projectPath, String fqn) {
    IJavaProject javaProject =
        JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectPath);
    try {
      IType type = javaProject.findType(fqn);
      if (type != null && type.isBinary()) {
        IClassFile classFile = type.getClassFile();
        if (classFile.getSourceRange() == null) {
          IJavaElement element = classFile;
          while (element.getParent() != null) {
            element = element.getParent();
            if (element instanceof IPackageFragmentRoot) {
              IPackageFragmentRoot root = (IPackageFragmentRoot) element;

              if (root.getSourceAttachmentPath() == null) {
                return downloadSources(root);
              }
            }
          }
        }
      }

    } catch (JavaModelException e) {
      LOG.error(e.getMessage(), e);
    }
    return false;
  }

  private boolean downloadSources(IPackageFragmentRoot fragmentRoot) throws JavaModelException {
    fragmentRoot.getAdapter(MavenArtifactKey.class);
    IClasspathEntry classpathEntry = fragmentRoot.getResolvedClasspathEntry();
    MavenArtifactKey artifactKey = getArtifactKey(classpathEntry);
    if (artifactKey != null) {
      MavenServerWrapper mavenServer =
          wrapperManager.getMavenServer(MavenWrapperManager.ServerType.DOWNLOAD);

      try {
        mavenServer.customize(
            projectManager.copyWorkspaceCache(), terminal, notifier, false, false);

        MavenArtifactKey sourceKey =
            new MavenArtifactKey(
                artifactKey.getGroupId(),
                artifactKey.getArtifactId(),
                artifactKey.getVersion(),
                artifactKey.getPackaging(),
                SOURCES);
        MavenArtifact mavenArtifact =
            mavenServer.resolveArtifact(sourceKey, Collections.emptyList());
        if (mavenArtifact.isResolved()) {
          updateClasspath(
              projectManager.findMavenProject(fragmentRoot.getJavaProject().getProject()));
        }
        return mavenArtifact.isResolved();
      } finally {
        wrapperManager.release(mavenServer);
      }
    }
    return false;
  }

  private MavenArtifactKey getArtifactKey(IClasspathEntry classpathEntry) {
    IClasspathAttribute[] attributes = classpathEntry.getExtraAttributes();
    String groupId = null;
    String artifactId = null;
    String version = null;
    String packaging = null;
    String classifier = null;
    for (IClasspathAttribute attribute : attributes) {
      if (ClasspathManager.GROUP_ID_ATTRIBUTE.equals(attribute.getName())) {
        groupId = attribute.getValue();
      } else if (ClasspathManager.ARTIFACT_ID_ATTRIBUTE.equals(attribute.getName())) {
        artifactId = attribute.getValue();
      } else if (ClasspathManager.VERSION_ATTRIBUTE.equals(attribute.getName())) {
        version = attribute.getValue();
      } else if (ClasspathManager.PACKAGING_ATTRIBUTE.equals(attribute.getName())) {
        packaging = attribute.getValue();
      } else if (ClasspathManager.CLASSIFIER_ATTRIBUTE.equals(attribute.getName())) {
        classifier = attribute.getValue();
      }
    }

    if (groupId != null && artifactId != null && version != null) {
      return new MavenArtifactKey(groupId, artifactId, version, packaging, classifier);
    }
    return null;
  }
}
