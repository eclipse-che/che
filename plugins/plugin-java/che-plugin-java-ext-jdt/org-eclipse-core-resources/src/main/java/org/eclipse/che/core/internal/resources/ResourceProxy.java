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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;

/** @author Evgen Vidolob */
public class ResourceProxy implements IResourceProxy {
  protected final Workspace workspace = (Workspace) ResourcesPlugin.getWorkspace();
  protected ResourceInfo info;
  protected IPath fullPath;

  protected IResource resource;

  @Override
  public long getModificationStamp() {
    return 0;
  }

  @Override
  public boolean isAccessible() {
    return false;
  }

  @Override
  public boolean isDerived() {
    return false;
  }

  @Override
  public boolean isLinked() {
    return false;
  }

  @Override
  public boolean isPhantom() {
    return false;
  }

  @Override
  public boolean isHidden() {
    return false;
  }

  @Override
  public boolean isTeamPrivateMember() {
    return false;
  }

  @Override
  public String getName() {
    return fullPath.lastSegment();
  }

  @Override
  public Object getSessionProperty(QualifiedName qualifiedName) {
    return null;
  }

  @Override
  public int getType() {
    return info.getType();
  }

  @Override
  public IPath requestFullPath() {
    return fullPath;
  }

  @Override
  public IResource requestResource() {
    if (resource == null) resource = workspace.newResource(requestFullPath(), info.getType());
    return resource;
  }

  protected void reset() {
    fullPath = null;
    resource = null;
  }
}
