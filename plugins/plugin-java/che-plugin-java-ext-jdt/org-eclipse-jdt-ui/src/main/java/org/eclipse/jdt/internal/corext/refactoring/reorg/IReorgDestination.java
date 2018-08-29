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

/** {@link ReorgDestinationFactory} can create concrete instances */
public interface IReorgDestination {
  /**
   * Constant describing the position of the cursor relative to the target object. This means the
   * mouse is positioned directly on the target.
   *
   * @see #getCurrentLocation()
   */
  public static final int LOCATION_ON = 3;

  /**
   * Constant describing the position of the cursor relative to the target object. This means the
   * mouse is positioned slightly before the target.
   *
   * @see #getCurrentLocation()
   */
  public static final int LOCATION_BEFORE = 1;

  /**
   * Constant describing the position of the cursor relative to the target object. This means the
   * mouse is positioned slightly after the target.
   *
   * @see #getCurrentLocation()
   */
  public static final int LOCATION_AFTER = 2;
  //    public static final int LOCATION_BEFORE = JdtViewerDropAdapter.LOCATION_BEFORE;
  //    public static final int LOCATION_AFTER = JdtViewerDropAdapter.LOCATION_AFTER;
  //    public static final int LOCATION_ON = JdtViewerDropAdapter.LOCATION_ON;

  public Object getDestination();

  public int getLocation();
}
