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
package org.eclipse.che.ide.ui.dialogs;

/**
 * Callback called when the user clicks on "Cancel" in the confirmation/input dialog.
 *
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
public interface CancelCallback {

  /** Action called when the user clicks on Cancel. */
  void cancelled();
}
