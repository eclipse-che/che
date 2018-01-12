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

import org.eclipse.core.resources.FileInfoMatcherDescription;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceFilterDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/** @author Evgen Vidolob */
public abstract class Container extends Resource implements IContainer {
  protected Container(IPath path, Workspace workspace) {
    super(path, workspace);
  }

  @Override
  public IResourceFilterDescription createFilter(
      int type,
      FileInfoMatcherDescription matcherDescription,
      int updateFlags,
      IProgressMonitor monitor)
      throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean exists(IPath childPath) {
    return workspace.getResourceInfo(getFullPath().append(childPath)) != null;
  }

  @Override
  public IResource findMember(String memberPath, boolean phantom) {
    IPath childPath = getFullPath().append(memberPath);
    ResourceInfo info = workspace.getResourceInfo(childPath);
    return info == null ? null : workspace.newResource(childPath, info.getType());
  }

  @Override
  public IResource findMember(String memberPath) {
    return findMember(memberPath, false);
  }

  @Override
  public IResource findMember(IPath childPath) {
    return findMember(childPath, false);
  }

  @Override
  public IResource findMember(IPath childPath, boolean phantom) {
    childPath = getFullPath().append(childPath);
    ResourceInfo info = workspace.getResourceInfo(childPath);
    return (info == null) ? null : workspace.newResource(childPath, info.getType());
  }

  @Override
  public IResourceFilterDescription[] getFilters() throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public IFile getFile(IPath childPath) {
    return (IFile) workspace.newResource(getFullPath().append(childPath), FILE);
  }

  @Override
  public IFolder getFolder(IPath childPath) {
    return (IFolder) workspace.newResource(getFullPath().append(childPath), FOLDER);
  }

  @Override
  public IResource[] members() throws CoreException {
    // forward to central method
    return members(IResource.NONE);
  }

  @Override
  public IResource[] members(boolean phantom) throws CoreException {
    // forward to central method
    return members(phantom ? INCLUDE_PHANTOMS : IResource.NONE);
  }

  @Override
  public IResource[] members(int memberFlags) throws CoreException {
    return workspace.getChildren(path);
  }

  @Override
  public String getDefaultCharset() throws CoreException {
    return getDefaultCharset(true);
  }

  @Override
  public IFile[] findDeletedMembersWithHistory(int depth, IProgressMonitor monitor)
      throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setDefaultCharset(String charset) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setDefaultCharset(String newCharset, IProgressMonitor monitor) throws CoreException {
    //        throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see IFolder#getFolder(String) and IProject#getFolder(String)
   */
  public IFolder getFolder(String name) {
    return (IFolder) workspace.newResource(getFullPath().append(name), FOLDER);
  }

  /* (non-Javadoc)
   * @see IFolder#getFile(String) and IProject#getFile(String)
   */
  public IFile getFile(String name) {
    return (IFile) workspace.newResource(getFullPath().append(name), FILE);
  }
}
