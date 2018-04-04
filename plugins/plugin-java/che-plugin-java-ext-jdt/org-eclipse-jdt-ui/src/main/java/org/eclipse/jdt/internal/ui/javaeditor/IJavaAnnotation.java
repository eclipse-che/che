/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.javaeditor;

import java.util.Iterator;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.compiler.CategorizedProblem;

/**
 * Interface of annotations representing markers and problems.
 *
 * @see org.eclipse.core.resources.IMarker
 * @see org.eclipse.jdt.core.compiler.IProblem
 */
public interface IJavaAnnotation {

  /**
   * Returns the type of the annotation.
   *
   * @return the type of the annotation
   * @see org.eclipse.jface.text.source.Annotation#getType()
   */
  String getType();

  /**
   * Returns whether this annotation is persistent.
   *
   * @return <code>true</code> if this annotation is persistent, <code>false</code> otherwise
   * @see org.eclipse.jface.text.source.Annotation#isPersistent()
   */
  boolean isPersistent();

  /**
   * Returns whether this annotation is marked as deleted.
   *
   * @return <code>true</code> if annotation is marked as deleted, <code>false</code> otherwise
   * @see org.eclipse.jface.text.source.Annotation#isMarkedDeleted()
   */
  boolean isMarkedDeleted();

  /**
   * Returns the text associated with this annotation.
   *
   * @return the text associated with this annotation or <code>null</code>
   * @see org.eclipse.jface.text.source.Annotation#getText()
   */
  String getText();

  /**
   * Returns whether this annotation is overlaid.
   *
   * @return <code>true</code> if overlaid
   */
  boolean hasOverlay();

  /**
   * Returns the overlay of this annotation.
   *
   * @return the annotation's overlay
   * @since 3.0
   */
  IJavaAnnotation getOverlay();

  /**
   * Returns an iterator for iterating over the annotation which are overlaid by this annotation.
   *
   * @return an iterator over the overlaid annotations
   */
  Iterator<IJavaAnnotation> getOverlaidIterator();

  /**
   * Adds the given annotation to the list of annotations which are overlaid by this annotations.
   *
   * @param annotation the problem annotation
   */
  void addOverlaid(IJavaAnnotation annotation);

  /**
   * Removes the given annotation from the list of annotations which are overlaid by this
   * annotation.
   *
   * @param annotation the problem annotation
   */
  void removeOverlaid(IJavaAnnotation annotation);

  /**
   * Tells whether this annotation is a problem annotation.
   *
   * @return <code>true</code> if it is a problem annotation
   */
  boolean isProblem();

  /**
   * Returns the compilation unit corresponding to the document on which the annotation.
   *
   * @return the compilation unit or <code>null</code> if no corresponding compilation unit exists
   */
  ICompilationUnit getCompilationUnit();

  /**
   * Returns the problem arguments or <code>null</code> if no problem arguments can be evaluated.
   *
   * @return returns the problem arguments or <code>null</code> if no problem arguments can be
   *     evaluated.
   */
  String[] getArguments();

  /**
   * Returns the problem id or <code>-1</code> if no problem id can be evaluated.
   *
   * @return returns the problem id or <code>-1</code>
   */
  int getId();

  /**
   * Returns the marker type associated to this problem or <code>null<code> if no marker type
   * can be evaluated. See also {@link CategorizedProblem#getMarkerType()}.
   *
   * @return the type of the marker which would be associated to the problem or
   * <code>null<code> if no marker type can be evaluated.
   */
  String getMarkerType();
}
