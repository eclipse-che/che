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
package org.eclipse.che.ide.ui.toolbar;

/** @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a> */
public interface CloseMenuHandler {

  /** Implement closing of all opened popups when user will click outside of all of them */
  void onCloseMenu();
}
