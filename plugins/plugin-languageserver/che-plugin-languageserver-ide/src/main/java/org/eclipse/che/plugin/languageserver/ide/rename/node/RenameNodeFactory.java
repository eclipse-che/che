/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.languageserver.ide.rename.node;

import org.eclipse.che.plugin.languageserver.ide.rename.model.RenameChange;
import org.eclipse.che.plugin.languageserver.ide.rename.model.RenameFile;
import org.eclipse.che.plugin.languageserver.ide.rename.model.RenameFolder;
import org.eclipse.che.plugin.languageserver.ide.rename.model.RenameProject;

/** Factory for all rename nodes */
public interface RenameNodeFactory {

  ProjectNode create(RenameProject project);

  FolderNode create(RenameFolder folder);

  FileNode create(RenameFile file);

  ChangeNode create(RenameChange change);
}
