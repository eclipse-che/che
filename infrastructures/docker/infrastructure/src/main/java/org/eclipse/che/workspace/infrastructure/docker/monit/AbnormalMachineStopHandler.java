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
package org.eclipse.che.workspace.infrastructure.docker.monit;

/**
 * Handles unexpected stop of a Docker machine.
 *
 * @author Alexander Garagatyi
 */
public interface AbnormalMachineStopHandler {
  /**
   * Accepts machine stop error message and handles unexpected stop of a machine.
   *
   * @param error
   */
  void handle(String error);
}
