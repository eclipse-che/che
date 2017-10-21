/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.part;

import com.google.inject.ImplementedBy;

/**
 * This interface give ability part stack manipulate visibility an size in container.
 *
 * @author Evgen Vidolob
 */
@ImplementedBy(WorkBenchPartControllerImpl.class)
public interface WorkBenchPartController {

  /**
   * Get part stack size.
   *
   * @return the size
   */
  double getSize();

  /**
   * Set part stack size.
   *
   * @param size size which need set
   */
  void setSize(double size);

  /** Maximizes part stack. */
  void maximize();

  /**
   * Sets the minimum allowable size for the part.
   *
   * <p>The splitter cannot be dragged to a position that would make the part smaller than this
   * size.
   *
   * @param minSize the minimum size for the part
   */
  void setMinSize(int minSize);

  /**
   * Show/hide part stack.
   *
   * @param hidden <code>true</code> hides part, <code>false</code> display part
   */
  void setHidden(boolean hidden);

  /**
   * Return hidden state.
   *
   * @return the hidden state
   */
  boolean isHidden();
}
