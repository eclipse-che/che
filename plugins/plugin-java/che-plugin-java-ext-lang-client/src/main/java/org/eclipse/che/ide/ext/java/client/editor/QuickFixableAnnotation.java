/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.client.editor;

/**
 * Allows an annotation to tell whether there are quick fixes for it and to cache that state.
 *
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id:
 */
public interface QuickFixableAnnotation {

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
   */
  boolean isQuickFixable();
}
