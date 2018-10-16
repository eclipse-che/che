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
package org.eclipse.che.api.languageserver;

import static org.eclipse.lsp4j.FileChangeType.Deleted;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.notification.EventService;

/**
 * Notifies registered language servers when file deleted
 *
 * @author Dmytro Kulieshov
 * @see <a
 *     href="https://microsoft.github.io/language-server-protocol/specification#workspace_didChangeWatchedFiles">DidChangeWatchedFiles
 *     Notification</a> section of LSP specification
 */
@Singleton
class LanguageServerDeleteFileWatcher extends LanguageServerAbstractFileWatcher {
  @Inject
  LanguageServerDeleteFileWatcher(EventService eventService, RegistryContainer registries) {
    super(eventService, registries, Deleted);
  }
}
