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
package org.eclipse.che.ide.editor.orion.client;

import org.eclipse.che.api.promises.client.js.JsPromise;
import org.eclipse.che.ide.editor.orion.client.jso.OrionHoverContextOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionHoverOverlay;

/** @author Evgen Vidolob */
public interface OrionHoverHandler {
  JsPromise<OrionHoverOverlay> computeHover(OrionHoverContextOverlay context);
}
