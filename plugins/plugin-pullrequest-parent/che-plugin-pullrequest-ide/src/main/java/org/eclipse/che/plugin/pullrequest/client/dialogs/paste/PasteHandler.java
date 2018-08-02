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
package org.eclipse.che.plugin.pullrequest.client.dialogs.paste;

import com.google.gwt.event.shared.EventHandler;

/** {@link EventHandler} for {@link PasteEvent}s. */
public interface PasteHandler extends EventHandler {
  void onPaste(PasteEvent event);
}
