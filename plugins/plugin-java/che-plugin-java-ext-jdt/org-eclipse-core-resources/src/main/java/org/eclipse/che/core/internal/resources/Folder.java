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
package org.eclipse.che.core.internal.resources;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/** @author Evgen Vidolob */
public class Folder extends Container implements IFolder {

  protected Folder(IPath path, Workspace workspace) {
    super(path, workspace);
  }

  @Override
  public void create(boolean force, boolean local, IProgressMonitor monitor) throws CoreException {
    // funnel all operations to central method
    create((force ? IResource.FORCE : IResource.NONE), local, monitor);
  }

  @Override
  public void create(int updateFlags, boolean local, IProgressMonitor monitor)
      throws CoreException {
    workspace.createResource(this, 0);
  }

  @Override
  public String getDefaultCharset(boolean b) throws CoreException {
    return "UTF-8";
  }

  @Override
  public int getType() {
    return FOLDER;
  }
}
