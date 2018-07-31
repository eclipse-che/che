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

import javax.validation.constraints.NotNull;

/**
 * Handler for the processing of click on 'Preview SSH' button
 *
 * @author Anna Shumilova
 * @author Vlad Zhukovskyi
 */
public interface PreviewSshClickHandler {

  /**
   * Will be called when user clicks 'Preview SSH' button
   *
   * @param machineId id of machine in which ssh keys are located
   */
  void onPreviewSshClick(@NotNull String machineId);
}
