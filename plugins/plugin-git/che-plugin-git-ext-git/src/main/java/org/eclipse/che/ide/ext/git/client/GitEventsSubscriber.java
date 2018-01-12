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
package org.eclipse.che.ide.ext.git.client;

import org.eclipse.che.api.git.shared.FileChangedEventDto;
import org.eclipse.che.api.git.shared.RepositoryDeletedEventDto;
import org.eclipse.che.api.git.shared.RepositoryInitializedEventDto;
import org.eclipse.che.api.git.shared.StatusChangedEventDto;
import org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto;

/**
 * Describes git events.
 *
 * @author Mykola Morhun
 */
public interface GitEventsSubscriber {

  /** Invoked when a project file was changed */
  default void onFileChanged(String endpointId, FileChangedEventDto dto) {}

  // TODO change method name or behaviour. It is not git status actually.
  // For example, this even won't be fired when:
  // - edit just committed file
  // - redo changes to committed state
  // - delete a committed file
  // And maybe some others
  /** Invoked when git status of project was changed */
  default void onGitStatusChanged(String endpointId, StatusChangedEventDto dto) {}

  /** Invoked when git checkout was performed */
  default void onGitCheckout(String endpointId, GitCheckoutEventDto dto) {}

  /** Invoked when git repository was initialized inside an existing project. */
  default void onGitRepositoryInitialized(
      String endpointId, RepositoryInitializedEventDto gitRepositoryInitializedEventDto) {}

  /** Invoked when git repository was deleted inside an existing project. */
  default void onGitRepositoryDeleted(
      String endpointId, RepositoryDeletedEventDto repositoryDeletedEventDto) {}
}
