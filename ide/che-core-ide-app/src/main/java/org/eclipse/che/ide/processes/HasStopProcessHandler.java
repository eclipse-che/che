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
package org.eclipse.che.ide.processes;

/**
 * An object that implements this interface provides registration for {@link StopProcessHandler}
 * instances.
 *
 * @author Vlad Zhukovskyi
 * @see StopProcessHandler
 * @since 5.11.0
 */
public interface HasStopProcessHandler {

  /**
   * Adds a {@link StopProcessHandler} handler.
   *
   * @param handler the stop process handler
   */
  void addStopProcessHandler(StopProcessHandler handler);
}
