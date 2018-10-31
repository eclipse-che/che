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
package org.eclipse.che.ide.editor.orion.client.signature;

import org.eclipse.che.ide.editor.orion.client.OrionEditorWidget;
import org.eclipse.che.ide.editor.orion.client.jso.OrionKeyModeOverlay;

/**
 * Factory for creating Signature widget.
 *
 * @author Valeriy Svydenko
 */
public interface SignatureWidgetFactory {
  SignatureWidget create(OrionEditorWidget editor, OrionKeyModeOverlay assistMode);
}
