/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.nls.changes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.resource.DeleteResourceChange;
import org.eclipse.ltk.core.refactoring.resource.ResourceChange;

public class CreateFileChange extends ResourceChange {

  private String fChangeName;

  private IPath fPath;
  private String fSource;
  private String fEncoding;
  private boolean fExplicitEncoding;
  private long fStampToRestore;

  public CreateFileChange(IPath path, String source, String encoding) {
    this(path, source, encoding, IResource.NULL_STAMP);
  }

  public CreateFileChange(IPath path, String source, String encoding, long stampToRestore) {
    Assert.isNotNull(path, "path"); // $NON-NLS-1$
    Assert.isNotNull(source, "source"); // $NON-NLS-1$
    fPath = path;
    fSource = source;
    fEncoding = encoding;
    fExplicitEncoding = fEncoding != null;
    fStampToRestore = stampToRestore;
  }

  /*
  private CreateFileChange(IPath path, String source, String encoding, long stampToRestore, boolean explicit) {
  	Assert.isNotNull(path, "path"); //$NON-NLS-1$
  	Assert.isNotNull(source, "source"); //$NON-NLS-1$
  	Assert.isNotNull(encoding, "encoding"); //$NON-NLS-1$
  	fPath= path;
  	fSource= source;
  	fEncoding= encoding;
  	fStampToRestore= stampToRestore;
  	fExplicitEncoding= explicit;
  }
  */

  protected void setEncoding(String encoding, boolean explicit) {
    Assert.isNotNull(encoding, "encoding"); // $NON-NLS-1$
    fEncoding = encoding;
    fExplicitEncoding = explicit;
  }

  @Override
  public String getName() {
    if (fChangeName == null)
      return Messages.format(
          NLSChangesMessages.createFile_Create_file, BasicElementLabels.getPathLabel(fPath, false));
    else return fChangeName;
  }

  public void setName(String name) {
    fChangeName = name;
  }

  protected void setSource(String source) {
    fSource = source;
  }

  protected String getSource() {
    return fSource;
  }

  protected void setPath(IPath path) {
    fPath = path;
  }

  protected IPath getPath() {
    return fPath;
  }

  @Override
  protected IResource getModifiedResource() {
    return ResourcesPlugin.getWorkspace().getRoot().getFile(fPath);
  }

  @Override
  public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
    RefactoringStatus result = new RefactoringStatus();
    IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(fPath);

    URI location = file.getLocationURI();
    if (location == null) {
      result.addFatalError(
          Messages.format(
              NLSChangesMessages.CreateFileChange_error_unknownLocation,
              BasicElementLabels.getPathLabel(file.getFullPath(), false)));
      return result;
    }

    //		IFileInfo jFile= EFS.getStore(location).fetchInfo();
    //		if (jFile.exists()) {
    //			result.addFatalError(Messages.format(
    //					NLSChangesMessages.CreateFileChange_error_exists,
    //					BasicElementLabels.getPathLabel(file.getFullPath(), false)));
    //			return result;
    //		}
    return result;
  }

  @Override
  public Change perform(IProgressMonitor pm) throws CoreException, OperationCanceledException {

    InputStream is = null;
    try {
      pm.beginTask(NLSChangesMessages.createFile_creating_resource, 3);

      initializeEncoding();
      IFile file = getOldFile(new SubProgressMonitor(pm, 1));
      /*
      if (file.exists()) {
      	CompositeChange composite= new CompositeChange(getName());
      	composite.add(new DeleteFileChange(file));
      	composite.add(new CreateFileChange(fPath, fSource, fEncoding, fStampToRestore, fExplicitEncoding));
      	pm.worked(1);
      	return composite.perform(new SubProgressMonitor(pm, 1));
      } else { */
      try {
        is = new ByteArrayInputStream(fSource.getBytes(fEncoding));
        file.create(is, false, new SubProgressMonitor(pm, 1));
        if (fStampToRestore != IResource.NULL_STAMP) {
          file.revertModificationStamp(fStampToRestore);
        }
        if (fExplicitEncoding) {
          file.setCharset(fEncoding, new SubProgressMonitor(pm, 1));
        } else {
          pm.worked(1);
        }
        return new DeleteResourceChange(file.getFullPath(), true);
      } catch (UnsupportedEncodingException e) {
        throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
      }
    } finally {
      try {
        if (is != null) is.close();
      } catch (IOException ioe) {
        throw new JavaModelException(ioe, IJavaModelStatusConstants.IO_EXCEPTION);
      } finally {
        pm.done();
      }
    }
  }

  protected IFile getOldFile(IProgressMonitor pm) throws OperationCanceledException {
    pm.beginTask("", 1); // $NON-NLS-1$
    try {
      return ResourcesPlugin.getWorkspace().getRoot().getFile(fPath);
    } finally {
      pm.done();
    }
  }

  private void initializeEncoding() {
    if (fEncoding == null) {
      fExplicitEncoding = false;
      IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(fPath);
      if (file != null) {
        try {
          if (file.exists()) {
            fEncoding = file.getCharset(false);
            if (fEncoding == null) {
              fEncoding = file.getCharset(true);
            } else {
              fExplicitEncoding = true;
            }
          } else {
            IContentType contentType =
                Platform.getContentTypeManager().findContentTypeFor(file.getName());
            if (contentType != null) fEncoding = contentType.getDefaultCharset();
            if (fEncoding == null) fEncoding = file.getCharset(true);
          }
        } catch (CoreException e) {
          fEncoding = ResourcesPlugin.getEncoding();
          fExplicitEncoding = true;
        }
      } else {
        fEncoding = ResourcesPlugin.getEncoding();
        fExplicitEncoding = true;
      }
    }
    Assert.isNotNull(fEncoding);
  }
}
