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
package org.eclipse.che.ide.rest;

/**
 * Loader which display progress information of specific long live process.
 *
 * @author Vlad Zhukovskyi
 */
public interface AsyncRequestLoader {

  /** Show loader. */
  void show();

  /**
   * Show loader with specific message.
   *
   * @param message loader message
   */
  void show(String message);

  /** Hide loader. */
  void hide();

  /**
   * Set message to current loader.
   *
   * @param message loader message
   */
  void setMessage(String message);
}
