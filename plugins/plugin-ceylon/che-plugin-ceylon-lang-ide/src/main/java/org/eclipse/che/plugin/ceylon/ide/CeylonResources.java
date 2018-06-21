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
