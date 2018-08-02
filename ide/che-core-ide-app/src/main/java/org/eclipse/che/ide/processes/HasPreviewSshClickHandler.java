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
package org.eclipse.che.ide.processes;

/**
 * An object that implements this interface provides registration for {@link PreviewSshClickHandler}
 * instances.
 *
 * @author Vlad Zhukovskyi
 * @see PreviewSshClickHandler
 * @since 5.11.0
 */
public interface HasPreviewSshClickHandler {

  /**
   * Adds a {@link PreviewSshClickHandler} handler.
   *
   * @param handler the preview ssh click handler
   */
  void addPreviewSshClickHandler(PreviewSshClickHandler handler);
}
