/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.editor.filetype;

import java.util.List;
import org.eclipse.che.ide.api.resources.VirtualFile;

/**
 * An interface for a file identification service.
 *
 * @author "Mickaël Leduque"
 */
public interface FileTypeIdentifier {

  /**
   * Returns a list of possible content types for the file.
   *
   * @param file the file to identify
   * @return a list of content type or null if identification failed
   */
  List<String> identifyType(VirtualFile file);
}
