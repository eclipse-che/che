/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.util;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;

public class Changes {

  public static IFile[] getModifiedFiles(Change[] changes) {
    List<IFile> result = new ArrayList<IFile>();
    getModifiedFiles(result, changes);
    return result.toArray(new IFile[result.size()]);
  }

  private static void getModifiedFiles(List<IFile> result, Change[] changes) {
    for (int i = 0; i < changes.length; i++) {
      Change change = changes[i];
      Object modifiedElement = change.getModifiedElement();
      if (modifiedElement instanceof IAdaptable) {
        IFile file = (IFile) ((IAdaptable) modifiedElement).getAdapter(IFile.class);
        if (file != null) result.add(file);
      }
      if (change instanceof CompositeChange) {
        getModifiedFiles(result, ((CompositeChange) change).getChildren());
      }
    }
  }
}
