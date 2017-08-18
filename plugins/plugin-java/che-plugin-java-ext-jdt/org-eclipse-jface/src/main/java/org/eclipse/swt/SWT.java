/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2015 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.swt;

/**
 * This class provides access to a small number of SWT system-wide methods, and in addition defines
 * the public constants provided by SWT.
 *
 * <p>By defining constants like UP and DOWN in a single class, SWT can share common names and
 * concepts at the same time minimizing the number of classes, names and constants for the
 * application programmer.
 *
 * <p>Note that some of the constants provided by this class represent optional, appearance related
 * aspects of widgets which are available either only on some window systems, or for a differing set
 * of widgets on each window system. These constants are marked as <em>HINT</em>s. The set of
 * widgets which support a particular <em>HINT</em> may change from release to release, although we
 * typically will not withdraw support for a <em>HINT</em> once it is made available.
 *
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public class SWT {
  /**
   * Keyboard and/or mouse event mask indicating that the CTRL key was pushed on the keyboard when
   * the event was generated (value is 1&lt;&lt;18).
   */
  public static final int CTRL = 1 << 18;
}
