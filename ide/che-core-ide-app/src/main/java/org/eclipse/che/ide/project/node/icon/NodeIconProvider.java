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
package org.eclipse.che.ide.project.node.icon;

import org.eclipse.che.ide.api.resources.Resource;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Provides mechanism for resolving icon, based on file extension.
 *
 * @author Vlad Zhukovskiy
 */
public interface NodeIconProvider {
  /**
   * Resolve icon based on given {@code resource}.
   *
   * @param resource the resource to resolve icon
   * @return icon or null if icons for this extension doesn't exist
   */
  SVGResource getIcon(Resource resource);
}
