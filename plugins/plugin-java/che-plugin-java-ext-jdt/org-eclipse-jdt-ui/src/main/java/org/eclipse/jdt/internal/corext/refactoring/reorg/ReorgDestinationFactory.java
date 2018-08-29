/**
 * ***************************************************************************** Copyright (c) 2007,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.reorg;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IJavaElement;

public class ReorgDestinationFactory {

  private static class Destination implements IReorgDestination {

    private final Object fDestination;
    private final int fLocation;

    public Destination(Object destination, int location) {
      Assert.isNotNull(destination);
      Assert.isLegal(
          location == LOCATION_AFTER || location == LOCATION_BEFORE || location == LOCATION_ON);

      fDestination = destination;
      fLocation = location;
    }

    /** {@inheritDoc} */
    public Object getDestination() {
      return fDestination;
    }

    /** {@inheritDoc} */
    public int getLocation() {
      return fLocation;
    }
  }

  static final class ResourceDestination extends Destination {

    private ResourceDestination(IResource destination, int location) {
      super(destination, location);
    }

    public IResource getResource() {
      return (IResource) getDestination();
    }
  }

  static final class JavaElementDestination extends Destination {

    private JavaElementDestination(IJavaElement destination, int location) {
      super(destination, location);
    }

    public IJavaElement getJavaElement() {
      return (IJavaElement) getDestination();
    }
  }

  /**
   * Wrap the given object into a destination
   *
   * @param destination the object to wrap
   * @return a reorg destination if possible reorg destination or <b>null</b> otherwise
   */
  public static IReorgDestination createDestination(Object destination) {
    return createDestination(destination, IReorgDestination.LOCATION_ON);
  }

  public static IReorgDestination createDestination(Object destination, int location) {
    if (destination instanceof IJavaElement) {
      return new JavaElementDestination((IJavaElement) destination, location);
    }
    if (destination instanceof IResource) {
      return new ResourceDestination((IResource) destination, location);
    }

    return null;
  }
}
