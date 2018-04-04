/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jdt.core.resources;

import java.io.File;
import org.eclipse.che.api.project.server.notification.ProjectCreatedEvent;
import org.eclipse.che.api.project.server.notification.ProjectItemModifiedEvent;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;

/** @author Evgen Vidolob */
public class ResourceChangedEvent implements IResourceChangeEvent {

  private ResourceDeltaImpl resourceDelta;

  public ResourceChangedEvent(File workspace, ProjectItemModifiedEvent event) {
    resourceDelta = new ResourceDeltaImpl(workspace, event);
  }

  public ResourceChangedEvent(File workspace, ProjectCreatedEvent event) {
    resourceDelta = new ResourceDeltaImpl(workspace, event);
  }

  @Override
  public IMarkerDelta[] findMarkerDeltas(String s, boolean b) {
    return new IMarkerDelta[0];
  }

  @Override
  public int getBuildKind() {
    return 0;
  }

  @Override
  public IResourceDelta getDelta() {
    return resourceDelta;
  }

  @Override
  public IResource getResource() {
    return resourceDelta.getResource();
  }

  @Override
  public Object getSource() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getType() {
    return POST_CHANGE;
  }
}
