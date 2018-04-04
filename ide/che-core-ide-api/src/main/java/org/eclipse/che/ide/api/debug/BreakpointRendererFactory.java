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
package org.eclipse.che.ide.api.debug;

import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.gutter.Gutter;
import org.eclipse.che.ide.api.editor.texteditor.LineStyler;

/** Factory for {@link BreakpointRenderer} instances. */
public interface BreakpointRendererFactory {

  /**
   * Creates an instance of {@link BreakpointRenderer} that uses both a gutter and a line styler.
   *
   * @param hasGutter the gutter manager
   * @param lineStyler the line style manager
   * @param document the document
   * @return a {@link BreakpointRenderer}
   */
  BreakpointRenderer create(
      final Gutter hasGutter, final LineStyler lineStyler, final Document document);
}
