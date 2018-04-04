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
