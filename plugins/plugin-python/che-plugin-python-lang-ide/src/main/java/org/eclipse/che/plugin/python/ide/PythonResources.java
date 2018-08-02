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
package org.eclipse.che.plugin.python.ide;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Valeriy Svydenko */
public interface PythonResources extends ClientBundle {
  PythonResources INSTANCE = GWT.create(PythonResources.class);

  @Source("svg/python.svg")
  SVGResource pythonFile();

  @Source("svg/python.svg")
  SVGResource category();
}
