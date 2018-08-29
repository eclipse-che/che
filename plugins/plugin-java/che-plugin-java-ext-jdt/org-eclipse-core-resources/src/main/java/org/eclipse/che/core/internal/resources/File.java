/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.core.internal.resources;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import org.eclipse.che.core.internal.utils.Policy;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.osgi.util.NLS;

/** @author Evgen Vidolob */
public class File extends Resource implements IFile {

  protected File(IPath path, Workspace workspace) {
    super(path, workspace);
  }

  @Override
  public void appendContents(
      InputStream content, boolean force, boolean keepHistory, IProgressMonitor monitor)
      throws CoreException {
    // funnel all operations to central method
    int updateFlags = force ? IResource.FORCE : IResource.NONE;
    updateFlags |= keepHistory ? IResource.KEEP_HISTORY : IResource.NONE;
    appendContents(content, updateFlags, monitor);
  }

  @Override
  public void appendContents(InputStream content, int updateFlags, IProgressMonitor monitor)
      throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void create(InputStream content, boolean force, IProgressMonitor monitor)
      throws CoreException {
    // funnel all operations to central method
    create(content, (force ? IResource.FORCE : IResource.NONE), monitor);
  }

  @Override
  public void create(InputStream content, int updateFlags, IProgressMonitor monitor)
      throws CoreException {
    workspace.createResource(this, updateFlags);
    internalSetContents(content);
  }

  protected void internalSetContents(InputStream content) {
    workspace.setFileContent(this, content);
  }

  @Override
  public String getCharset() throws CoreException {
    return getCharset(true);
  }

  @Override
  public String getCharset(boolean checkImplicit) throws CoreException {
    return "UTF-8";
  }

  @Override
  public String getCharsetFor(Reader contents) throws CoreException {
    return "UTF-8";
  }

  @Override
  public IContentDescription getContentDescription() throws CoreException {
    //        throw new UnsupportedOperationException();
    return null;
  }

  @Override
  public InputStream getContents() throws CoreException {
    return getContents(true);
  }

  @Override
  public InputStream getContents(boolean force) throws CoreException {
    try {
      return new FileInputStream(workspace.getFile(path));
    } catch (FileNotFoundException e) {
      throw new CoreException(
          new Status(
              0,
              ResourcesPlugin.getPluginId(),
              IStatus.ERROR,
              "Error while getting content of file:" + getFullPath().toOSString(),
              e));
    }
  }

  @Override
  public int getEncoding() throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public IFileState[] getHistory(IProgressMonitor iProgressMonitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setCharset(String s) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setCharset(String s, IProgressMonitor iProgressMonitor) throws CoreException {
    //        throw new UnsupportedOperationException();
  }

  @Override
  public void setContents(
      InputStream content, boolean force, boolean keepHistory, IProgressMonitor monitor)
      throws CoreException {
    // funnel all operations to central method
    int updateFlags = force ? IResource.FORCE : IResource.NONE;
    updateFlags |= keepHistory ? IResource.KEEP_HISTORY : IResource.NONE;
    setContents(content, updateFlags, monitor);
  }

  @Override
  public void setContents(
      IFileState source, boolean force, boolean keepHistory, IProgressMonitor monitor)
      throws CoreException {
    // funnel all operations to central method
    int updateFlags = force ? IResource.FORCE : IResource.NONE;
    updateFlags |= keepHistory ? IResource.KEEP_HISTORY : IResource.NONE;
    setContents(source.getContents(), updateFlags, monitor);
  }

  @Override
  public void setContents(InputStream content, int updateFlags, IProgressMonitor monitor)
      throws CoreException {
    monitor = Policy.monitorFor(monitor);
    try {
      String message = NLS.bind(Messages.resources_settingContents, getFullPath());
      monitor.beginTask(message, Policy.totalWork);
      //            if (workspace.shouldValidate)
      //                workspace.validateSave(this);
      final ISchedulingRule rule = workspace.getRuleFactory().modifyRule(this);
      try {
        workspace.prepareOperation(rule, monitor);
        ResourceInfo info = getResourceInfo(false, false);
        //                checkAccessible(getFlags(info));
        workspace.beginOperation(true);
        //                IFileInfo fileInfo = getStore().fetchInfo();
        internalSetContents(
            content, updateFlags, false, Policy.subMonitorFor(monitor, Policy.opWork));
      } catch (OperationCanceledException e) {
        workspace.getWorkManager().operationCanceled();
        throw e;
      } finally {
        workspace.endOperation(rule, true, Policy.subMonitorFor(monitor, Policy.endOpWork));
      }
    } finally {
      monitor.done();
      FileUtil.safeClose(content);
    }
  }

  protected void internalSetContents(
      InputStream content, int updateFlags, boolean append, IProgressMonitor monitor)
      throws CoreException {
    if (content == null) content = new ByteArrayInputStream(new byte[0]);
    workspace.write(this, content, updateFlags, append, monitor);

    //        workspace.getAliasManager().updateAliases(this, getStore(), IResource.DEPTH_ZERO,
    // monitor);
  }

  @Override
  public void setContents(IFileState content, int updateFlags, IProgressMonitor monitor)
      throws CoreException {
    setContents(content.getContents(), updateFlags, monitor);
  }

  @Override
  public int getType() {
    return FILE;
  }
}
