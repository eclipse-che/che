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
package org.eclipse.che.plugin.ceylon.ide;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author David Festal */
public interface CeylonResources extends ClientBundle {
  CeylonResources INSTANCE = GWT.create(CeylonResources.class);

  @Source("svg/ceylon.svg")
  SVGResource ceylonFile();

  @Source("svg/ceylon.svg")
  SVGResource category();

  @Source("json/highlighting.json")
  TextResource syntax();
}
