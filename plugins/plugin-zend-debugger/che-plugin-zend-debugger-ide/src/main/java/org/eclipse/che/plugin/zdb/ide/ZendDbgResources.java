/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.zdb.ide;

import com.google.gwt.resources.client.ClientBundle;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Zend debugger SVG resources.
 *
 * @author Bartlomiej Laczkowski
 */
public interface ZendDbgResources extends ClientBundle {

  /** Returns the icon for PHP debug configuration type. */
  @Source("configuration/zend-dbg-configuration-type.svg")
  SVGResource zendDbgConfigurationType();
}
