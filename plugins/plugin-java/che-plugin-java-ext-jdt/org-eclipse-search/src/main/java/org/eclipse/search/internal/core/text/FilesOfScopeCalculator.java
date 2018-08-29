/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.search.internal.core.text;

import java.util.ArrayList;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.search.core.text.TextSearchScope;

public class FilesOfScopeCalculator implements IResourceProxyVisitor {

  private final TextSearchScope fScope;
  private final MultiStatus fStatus;
  private ArrayList fFiles;

  public FilesOfScopeCalculator(TextSearchScope scope, MultiStatus status) {
    fScope = scope;
    fStatus = status;
  }

  public boolean visit(IResourceProxy proxy) {
    boolean inScope = fScope.contains(proxy);

    if (inScope && proxy.getType() == IResource.FILE) {
      fFiles.add(proxy.requestResource());
    }
    return inScope;
  }

  public IFile[] process() {
    fFiles = new ArrayList();
    try {
      IResource[] roots = fScope.getRoots();
      for (int i = 0; i < roots.length; i++) {
        try {
          IResource resource = roots[i];
          if (resource.isAccessible()) {
            resource.accept(this, 0);
          }
        } catch (CoreException ex) {
          // report and ignore
          fStatus.add(ex.getStatus());
        }
      }
      return (IFile[]) fFiles.toArray(new IFile[fFiles.size()]);
    } finally {
      fFiles = null;
    }
  }
}
