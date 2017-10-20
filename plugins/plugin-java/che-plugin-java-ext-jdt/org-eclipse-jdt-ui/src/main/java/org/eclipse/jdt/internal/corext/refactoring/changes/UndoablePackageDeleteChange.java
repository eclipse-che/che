/**
 * ***************************************************************************** Copyright (c) 2006,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.changes;

import java.util.List;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ui.ide.undo.ResourceDescription;

public class UndoablePackageDeleteChange extends DynamicValidationStateChange {

  private final List<IResource> fPackageDeletes;

  public UndoablePackageDeleteChange(String name, List<IResource> packageDeletes) {
    super(name);
    fPackageDeletes = packageDeletes;
  }

  @Override
  public Change perform(IProgressMonitor pm) throws CoreException {
    int count = fPackageDeletes.size();
    pm.beginTask("", count * 3); // $NON-NLS-1$
    ResourceDescription[] packageDeleteDescriptions =
        new ResourceDescription[fPackageDeletes.size()];
    for (int i = 0; i < fPackageDeletes.size(); i++) {
      IResource resource = fPackageDeletes.get(i);
      packageDeleteDescriptions[i] = ResourceDescription.fromResource(resource);
      pm.worked(1);
    }

    DynamicValidationStateChange result =
        (DynamicValidationStateChange) super.perform(new SubProgressMonitor(pm, count));

    for (int i = 0; i < fPackageDeletes.size(); i++) {
      IResource resource = fPackageDeletes.get(i);
      ResourceDescription resourceDescription = packageDeleteDescriptions[i];
      resourceDescription.recordStateFromHistory(resource, new SubProgressMonitor(pm, 1));
      result.add(new UndoDeleteResourceChange(resourceDescription));
    }
    return result;
  }
}
