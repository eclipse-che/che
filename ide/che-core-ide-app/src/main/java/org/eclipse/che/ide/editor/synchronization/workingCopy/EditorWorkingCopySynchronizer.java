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
package org.eclipse.che.ide.editor.synchronization.workingCopy;

import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcPromise;
import org.eclipse.che.ide.api.editor.reconciler.DirtyRegion;

/**
 * The synchronizer of content for opened files with working copies on server side.
 *
 * @author Roman Nikitenko
 */
public interface EditorWorkingCopySynchronizer {
  /**
   * Sends the text change of editor content to sync its working copy on server side.
   *
   * @param filePath path to the file which content is needed to sync
   * @param projectPath the path to the project which contains the file to sync
   * @param dirtyRegion describes a document range which has been changed
   */
  JsonRpcPromise<Boolean> synchronize(String filePath, String projectPath, DirtyRegion dirtyRegion);
}
