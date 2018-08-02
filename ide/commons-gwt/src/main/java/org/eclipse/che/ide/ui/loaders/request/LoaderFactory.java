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
package org.eclipse.che.ide.ui.loaders.request;

/**
 * Loader factory.
 *
 * @author Vlad Zhukovskiy
 */
public interface LoaderFactory {
  /**
   * Create new loader with default message.
   *
   * @return new loader
   */
  MessageLoader newLoader();

  /**
   * Create new loader with initial message.
   *
   * @param message initial message
   * @return new loader with initial message
   */
  MessageLoader newLoader(String message);
}
