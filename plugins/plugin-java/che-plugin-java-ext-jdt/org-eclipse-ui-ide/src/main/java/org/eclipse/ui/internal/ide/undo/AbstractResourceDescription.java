/**
 * ***************************************************************************** Copyright (c) 2006,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.ui.internal.ide.undo;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.ide.undo.ResourceDescription;

/**
 * Base implementation of ResourceDescription that describes the common attributes of a resource to
 * be created.
 *
 * <p>This class is not intended to be instantiated or used by clients.
 *
 * @since 3.3
 */
abstract class AbstractResourceDescription extends ResourceDescription {
  IContainer parent;

  long modificationStamp = IResource.NULL_STAMP;

  long localTimeStamp = IResource.NULL_STAMP;

  ResourceAttributes resourceAttributes;

  MarkerDescription[] markerDescriptions;

  /** Create a resource description with no initial attributes */
  protected AbstractResourceDescription() {
    super();
  }

  /**
   * Create a resource description from the specified resource.
   *
   * @param resource the resource to be described
   */
  protected AbstractResourceDescription(IResource resource) {
    super();
    parent = resource.getParent();
    if (resource.isAccessible()) {
      modificationStamp = resource.getModificationStamp();
      localTimeStamp = resource.getLocalTimeStamp();
      resourceAttributes = resource.getResourceAttributes();
      try {
        IMarker[] markers = resource.findMarkers(null, true, IResource.DEPTH_INFINITE);
        markerDescriptions = new MarkerDescription[markers.length];
        for (int i = 0; i < markers.length; i++) {
          markerDescriptions[i] = new MarkerDescription(markers[i]);
        }
      } catch (CoreException e) {
        // Eat this exception because it only occurs when the resource
        // does not exist and we have already checked this.
        // We do not want to throw exceptions on the simple constructor,
        // as no one has actually tried to do anything yet.
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.eclipse.ui.ide.undo.ResourceDescription#createResource(org.eclipse
   * .core.runtime.IProgressMonitor)
   */
  public IResource createResource(IProgressMonitor monitor) throws CoreException {
    IResource resource = createResourceHandle();
    createExistentResourceFromHandle(resource, monitor);
    restoreResourceAttributes(resource);
    return resource;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.ui.ide.undo.ResourceDescription#isValid()
   */
  public boolean isValid() {
    return parent == null || parent.exists();
  }

  /**
   * Restore any saved attributed of the specified resource. This method is called after the
   * existent resource represented by the receiver has been created.
   *
   * @param resource the newly created resource
   * @throws CoreException
   */
  protected void restoreResourceAttributes(IResource resource) throws CoreException {
    if (modificationStamp != IResource.NULL_STAMP) {
      resource.revertModificationStamp(modificationStamp);
    }
    if (localTimeStamp != IResource.NULL_STAMP) {
      resource.setLocalTimeStamp(localTimeStamp);
    }
    if (resourceAttributes != null) {
      resource.setResourceAttributes(resourceAttributes);
    }
    if (markerDescriptions != null) {
      for (int i = 0; i < markerDescriptions.length; i++) {
        if (markerDescriptions[i].resource.exists()) markerDescriptions[i].createMarker();
      }
    }
  }

  /*
   * Return the workspace.
   */
  IWorkspace getWorkspace() {
    return ResourcesPlugin.getWorkspace();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.ui.ide.undo.ResourceDescription#verifyExistence(boolean)
   */
  public boolean verifyExistence(boolean checkMembers) {
    IContainer p = parent;
    if (p == null) {
      p = getWorkspace().getRoot();
    }
    IResource handle = p.findMember(getName());
    return handle != null;
  }
}
