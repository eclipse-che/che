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
package org.eclipse.che.ide.ui.zeroclipboard;

import com.google.gwt.i18n.client.Messages;

/**
 * Tooltip messages interface for the ZeroClipboard lib.
 *
 * @author Oleksii Orel
 */
public interface ClipboardConstant extends Messages {
  /* Prompts */
  @Key("prompt.readyToCopy")
  String promptReadyToCopy();

  @Key("prompt.afterCopy")
  String promptAfterCopy();

  @Key("prompt.copyError")
  String promptCopyError();

  @Key("prompt.readyToSelect")
  String promptReadyToSelect();
}
