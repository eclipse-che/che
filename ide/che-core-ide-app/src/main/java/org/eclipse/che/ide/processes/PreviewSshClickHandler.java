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
