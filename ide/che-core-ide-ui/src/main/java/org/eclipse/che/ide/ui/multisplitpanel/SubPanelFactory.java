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
package org.eclipse.che.ide.ui.multisplitpanel;

import org.eclipse.che.commons.annotation.Nullable;

/**
 * Factory for {@link SubPanel}.
 *
 * @author Artem Zatsarynnyi
 */
public interface SubPanelFactory {

  /** Create new instance of {@link SubPanel}. */
  SubPanel newPanel();

  /** For internal use only. Not intended to be used by client code. */
  SubPanel newPanel(@Nullable SubPanel parentPanel);
}
