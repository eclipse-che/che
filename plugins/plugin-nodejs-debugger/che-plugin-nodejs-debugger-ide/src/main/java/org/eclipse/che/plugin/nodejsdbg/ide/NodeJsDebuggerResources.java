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
package org.eclipse.che.plugin.nodejsdbg.ide;

import com.google.gwt.resources.client.ClientBundle;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Anatolii Bazko */
public interface NodeJsDebuggerResources extends ClientBundle {

  /** Returns the icon for NodeJs debugger configurations. */
  @Source("configuration/nodejs-debugger-configuration-type.svg")
  SVGResource nodeJsDebugConfigurationType();
}
