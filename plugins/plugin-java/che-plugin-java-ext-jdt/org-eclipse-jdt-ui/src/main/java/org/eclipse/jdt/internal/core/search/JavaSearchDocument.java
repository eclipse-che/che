/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2009 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.core.search;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchDocument;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.internal.core.search.processing.JobManager;
import org.eclipse.jdt.internal.core.util.Util;

public class JavaSearchDocument extends SearchDocument {

  private IFile file;
  protected byte[] byteContents;
  protected char[] charContents;

  public JavaSearchDocument(String documentPath, SearchParticipant participant) {
    super(documentPath, participant);
  }

  public JavaSearchDocument(
      java.util.zip.ZipEntry zipEntry,
      IPath zipFilePath,
      byte[] contents,
      SearchParticipant participant) {
    super(
        zipFilePath + IJavaSearchScope.JAR_FILE_ENTRY_SEPARATOR + zipEntry.getName(), participant);
    this.byteContents = contents;
  }

  public byte[] getByteContents() {
    if (this.byteContents != null) return this.byteContents;
    try {
      return Util.getResourceContentsAsByteArray(getFile());
    } catch (JavaModelException e) {
      if (BasicSearchEngine.VERBOSE
          || JobManager.VERBOSE) { // used during search and during indexing
        e.printStackTrace();
      }
      return null;
    }
  }

  public char[] getCharContents() {
    if (this.charContents != null) return this.charContents;
    try {
      return Util.getResourceContentsAsCharArray(getFile());
    } catch (JavaModelException e) {
      if (BasicSearchEngine.VERBOSE
          || JobManager.VERBOSE) { // used during search and during indexing
        e.printStackTrace();
      }
      return null;
    }
  }

  public String getEncoding() {
    // Return the encoding of the associated file
    IFile resource = getFile();
    if (resource != null) {
      try {
        return resource.getCharset();
      } catch (CoreException ce) {
        try {
          return ResourcesPlugin.getWorkspace().getRoot().getDefaultCharset();
        } catch (CoreException e) {
          // use no encoding
        }
      }
    }
    return null;
  }

  private IFile getFile() {
    if (this.file == null)
      this.file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(getPath()));
    return this.file;
  }

  public String toString() {
    return "SearchDocument for " + getPath(); // $NON-NLS-1$
  }
}
