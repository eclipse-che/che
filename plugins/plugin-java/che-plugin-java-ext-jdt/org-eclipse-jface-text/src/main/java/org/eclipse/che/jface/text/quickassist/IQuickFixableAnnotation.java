/**
 * ***************************************************************************** Copyright (c) 2006,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jface.text.quickassist;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.text.source.Annotation;

/**
 * Allows an annotation to tell whether there are quick fixes for it and to cache that state.
 *
 * <p>Caching the state is important to improve overall performance as calling {@link
 * IQuickAssistAssistant#canFix(Annotation)} can be expensive.
 *
 * <p>This interface can be implemented by clients.
 *
 * @since 3.2
 */
public interface IQuickFixableAnnotation {

  /**
   * Sets whether there are quick fixes available for this annotation.
   *
   * @param state <code>true</code> if there are quick fixes available, false otherwise
   */
  void setQuickFixable(boolean state);

  /**
   * Tells whether the quick fixable state has been set.
   *
   * <p>Normally this means {@link #setQuickFixable(boolean)} has been called at least once but it
   * can also be hard-coded, e.g. always return <code>true</code>.
   *
   * @return <code>true</code> if the state has been set
   */
  boolean isQuickFixableStateSet();

  /**
   * Tells whether there are quick fixes for this annotation.
   *
   * <p><strong>Note:</strong> This method must only be called if {@link #isQuickFixableStateSet()}
   * returns <code>true</code>.
   *
   * @return <code>true</code> if this annotation offers quick fixes
   * @throws AssertionFailedException if called when {@link #isQuickFixableStateSet()} is <code>
   *     false</code>
   */
  boolean isQuickFixable() throws AssertionFailedException;
}
