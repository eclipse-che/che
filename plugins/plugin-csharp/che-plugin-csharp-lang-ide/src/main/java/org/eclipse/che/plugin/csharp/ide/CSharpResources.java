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
package org.eclipse.che.plugin.csharp.ide;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Anatolii Bazko */
public interface CSharpResources extends ClientBundle {
  CSharpResources INSTANCE = GWT.create(CSharpResources.class);

  @Source("svg/csharp_file.svg")
  SVGResource csharpFile();

  @Source("svg/category.svg")
  SVGResource category();
}
