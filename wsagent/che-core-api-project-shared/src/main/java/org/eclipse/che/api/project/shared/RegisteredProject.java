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
package org.eclipse.che.api.project.shared;

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;

/**
 * Describes current state of project
 *
 * @author gazarenkov
 */
public interface RegisteredProject extends ProjectConfig {

  /**
   * @return whether this project is synchronized with Workspace storage On the other words this
   *     project is not updated
   */
  boolean isSynced();

  /**
   * @return whether this project is detected using Project Type resolver If so it should not be
   *     persisted to Workspace storage
   */
  boolean isDetected();

  /** @return root folder or null */
  String getBaseFolder();

  /**
   * @return name-value map of persistable (as opposed to provided) attributes configured for the
   *     project
   */
  Map<String, List<String>> getPersistableAttributes();

  /**
   * Sets 'synchronized with Workspace storage' flag
   *
   * @param synced true if synced
   */
  void setSynced(boolean synced);
}
