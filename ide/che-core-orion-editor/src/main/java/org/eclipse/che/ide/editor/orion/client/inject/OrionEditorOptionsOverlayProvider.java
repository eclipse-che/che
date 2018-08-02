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
package org.eclipse.che.ide.editor.orion.client.inject;

import com.google.inject.Provider;
import org.eclipse.che.ide.editor.orion.client.jso.OrionEditorOptionsOverlay;

/** @author Alexander Andrienko */
public class OrionEditorOptionsOverlayProvider implements Provider<OrionEditorOptionsOverlay> {
  @Override
  public OrionEditorOptionsOverlay get() {
    return OrionEditorOptionsOverlay.createObject().cast();
  }
}
