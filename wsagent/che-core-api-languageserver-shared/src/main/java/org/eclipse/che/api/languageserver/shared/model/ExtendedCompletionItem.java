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
package org.eclipse.che.api.languageserver.shared.model;

import org.eclipse.lsp4j.CompletionItem;

/**
 * Extended version of lsp4j {@link CompletionItem} for communication with the IDE.
 *
 * @author Thomas Mäder
 */
public class ExtendedCompletionItem {
  private String languageServerId;
  private CompletionItem item;

  public CompletionItem getItem() {
    return item;
  }

  public void setItem(CompletionItem item) {
    this.item = item;
  }

  public String getLanguageServerId() {
    return languageServerId;
  }

  public void setLanguageServerId(String languageServerId) {
    this.languageServerId = languageServerId;
  }
}
