/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.swt.graphics;

/**
 * Instances of this class represent places on the (x, y) coordinate plane.
 *
 * <p>The coordinate space for rectangles and points is considered to have increasing values
 * downward and to the right from its origin making this the normal, computer graphics oriented
 * notion of (x, y) coordinates rather than the strict mathematical one.
 *
 * <p>The hashCode() method in this class uses the values of the public fields to compute the hash
 * value. When storing instances of the class in hashed collections, do not modify these fields
 * after the object has been inserted.
 *
 * <p>Application code does <em>not</em> need to explicitly release the resources managed by each
 * instance when those instances are no longer required, and thus no <code>dispose()</code> method
 * is provided.
 *
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public final class Point /*implements SerializableCompatibility*/ {

  /** the x coordinate of the point */
  public int x;

  /** the y coordinate of the point */
  public int y;

  static final long serialVersionUID = 3257002163938146354L;

  /**
   * Constructs a new point with the given x and y coordinates.
   *
   * @param x the x coordinate of the new point
   * @param y the y coordinate of the new point
   */
  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Compares the argument to the receiver, and returns true if they represent the <em>same</em>
   * object using a class specific comparison.
   *
   * @param object the object to compare with this object
   * @return <code>true</code> if the object is the same as this object and <code>false</code>
   *     otherwise
   * @see #hashCode()
   */
  @Override
  public boolean equals(Object object) {
    if (object == this) return true;
    if (!(object instanceof Point)) return false;
    Point p = (Point) object;
    return (p.x == this.x) && (p.y == this.y);
  }

  /**
   * Returns an integer hash code for the receiver. Any two objects that return <code>true</code>
   * when passed to <code>equals</code> must return the same value for this method.
   *
   * @return the receiver's hash
   * @see #equals(Object)
   */
  @Override
  public int hashCode() {
    return x ^ y;
  }

  /**
   * Returns a string containing a concise, human-readable description of the receiver.
   *
   * @return a string representation of the point
   */
  @Override
  public String toString() {
    return "Point {" + x + ", " + y + "}"; // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
}
