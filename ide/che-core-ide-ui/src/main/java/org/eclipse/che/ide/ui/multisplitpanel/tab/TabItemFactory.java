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
package org.eclipse.che.ide.ui.multisplitpanel.tab;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Factory for {@link Tab} instances.
 *
 * @author Artem Zatsarynnyi
 */
public interface TabItemFactory {

  /** Create new {@link Tab} instance with the given title text and icon. */
  Tab createTabItem(String title, SVGResource icon, boolean closable);
}
