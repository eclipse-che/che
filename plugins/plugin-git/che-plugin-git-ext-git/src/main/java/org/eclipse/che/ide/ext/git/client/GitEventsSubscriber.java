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
