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
