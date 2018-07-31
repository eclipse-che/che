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
package org.eclipse.che.core.internal.resources;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.che.api.project.shared.RegisteredProject;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentTypeMatcher;

/** @author Evgen Vidolob */
public class Project extends Container implements IProject {

  protected Project(IPath path, Workspace workspace) {
    super(path, workspace);
  }

  @Override
  public void build(int i, String s, Map<String, String> map, IProgressMonitor iProgressMonitor)
      throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void build(int i, IProgressMonitor iProgressMonitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void build(
      IBuildConfiguration iBuildConfiguration, int i, IProgressMonitor iProgressMonitor)
      throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close(IProgressMonitor iProgressMonitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void create(IProjectDescription description, IProgressMonitor monitor)
      throws CoreException {
    create(description, IResource.NONE, monitor);
  }

  @Override
  public void create(IProgressMonitor monitor) throws CoreException {
    create(null, monitor);
  }

  @Override
  public void create(IProjectDescription description, int updateFlags, IProgressMonitor monitor)
      throws CoreException {
    workspace.createResource(this, updateFlags);
  }

  @Override
  public IBuildConfiguration getActiveBuildConfig() throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public IBuildConfiguration getBuildConfig(String s) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public IBuildConfiguration[] getBuildConfigs() throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public IContentTypeMatcher getContentTypeMatcher() throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getName() {
    return path.toOSString().substring(1);
  }

  @Override
  public IProjectDescription getDescription() throws CoreException {
    return new IProjectDescription() {
      @Override
      public IBuildConfiguration[] getBuildConfigReferences(String s) {
        return new IBuildConfiguration[0];
      }

      @Override
      public ICommand[] getBuildSpec() {
        return new ICommand[0];
      }

      @Override
      public String getComment() {
        return null;
      }

      @Override
      public IProject[] getDynamicReferences() {
        return new IProject[0];
      }

      @Override
      public IPath getLocation() {
        return null;
      }

      @Override
      public URI getLocationURI() {
        return null;
      }

      @Override
      public String getName() {
        return null;
      }

      @Override
      public String[] getNatureIds() {
        Optional<RegisteredProject> project = workspace.getProjectRegistry().get(path.toString());
        if (!project.isPresent()) {
          ResourcesPlugin.log(
              new Status(IStatus.ERROR, "resource", "Can't find project: " + path.toOSString()));
          return new String[0];
        }
        Map<String, List<String>> attributes = project.get().getAttributes();
        String language = "";
        if (attributes.containsKey("language")) {
          language = attributes.get("language").get(0);
        }
        return "java".equals(language)
            ? new String[] {"org.eclipse.jdt.core.javanature"}
            : new String[] {language};
      }

      @Override
      public IProject[] getReferencedProjects() {
        return new IProject[0];
      }

      @Override
      public boolean hasNature(String s) {
        String[] natureIds = getNatureIds();
        for (String id : natureIds) {
          if (s.equals(id)) {
            return true;
          }
        }
        return false;
      }

      @Override
      public ICommand newCommand() {
        return null;
      }

      @Override
      public void setActiveBuildConfig(String s) {}

      @Override
      public void setBuildConfigs(String[] strings) {}

      @Override
      public void setBuildConfigReferences(String s, IBuildConfiguration[] iBuildConfigurations) {}

      @Override
      public void setBuildSpec(ICommand[] iCommands) {}

      @Override
      public void setComment(String s) {}

      @Override
      public void setDynamicReferences(IProject[] iProjects) {}

      @Override
      public void setLocation(IPath iPath) {}

      @Override
      public void setLocationURI(URI uri) {}

      @Override
      public void setName(String s) {}

      @Override
      public void setNatureIds(String[] strings) {}

      @Override
      public void setReferencedProjects(IProject[] iProjects) {}
    };
  }

  @Override
  public IProjectNature getNature(String s) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public IPath getPluginWorkingLocation(IPluginDescriptor iPluginDescriptor) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IPath getWorkingLocation(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IProject[] getReferencedProjects() throws CoreException {
    // TODO need to use Project API to solve this
    return new IProject[0];
  }

  @Override
  public IProject[] getReferencingProjects() {
    //        throw new UnsupportedOperationException();
    // TODO need to use Project API to solve this
    return new IProject[0];
  }

  @Override
  public IBuildConfiguration[] getReferencedBuildConfigs(String s, boolean b) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasBuildConfig(String s) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasNature(String s) throws CoreException {
    return getDescription().hasNature(s);
  }

  @Override
  public boolean isNatureEnabled(String s) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isOpen() {
    //        throw new UnsupportedOperationException();
    return true;
  }

  @Override
  public void loadSnapshot(int i, URI uri, IProgressMonitor iProgressMonitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void move(
      IProjectDescription iProjectDescription, boolean b, IProgressMonitor iProgressMonitor)
      throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void open(int updateFlags, IProgressMonitor monitor) throws CoreException {
    //        throw new UnsupportedOperationException();
    // TODO
  }

  @Override
  public void open(IProgressMonitor monitor) throws CoreException {
    open(IResource.NONE, monitor);
  }

  @Override
  public void saveSnapshot(int i, URI uri, IProgressMonitor iProgressMonitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setDescription(IProjectDescription description, IProgressMonitor monitor)
      throws CoreException {
    // funnel all operations to central method
    setDescription(description, IResource.KEEP_HISTORY, monitor);
  }

  @Override
  public void setDescription(
      IProjectDescription iProjectDescription, int i, IProgressMonitor iProgressMonitor)
      throws CoreException {
    // ignore
  }

  @Override
  public String getDefaultCharset(boolean b) throws CoreException {
    return "UTF-8";
  }

  @Override
  public int getType() {
    return PROJECT;
  }

  @Override
  public IContainer getParent() {
    return workspace.getRoot();
  }
}
