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
package org.eclipse.che.ide.api.editor.gutter;

public final class Gutters {

  private Gutters() {}

  /** Logical identifer for the breakpoints gutter. */
  public static final String BREAKPOINTS_GUTTER = "breakpoints";

  /** Logical identifer for the vcs change markers gutter. */
  public static final String VCS_CHANGE_MARKERS_GUTTER = "vcsChangeMarkersGutter";

  /** Logical identifer for the line number gutter. */
  public static final String LINE_NUMBERS_GUTTER = "lineNumbers";

  /** Logical identifer for the annotations gutter. */
  public static final String ANNOTATION_GUTTER = "annotation";
}
