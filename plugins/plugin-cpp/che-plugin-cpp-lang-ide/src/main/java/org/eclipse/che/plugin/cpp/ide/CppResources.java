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
package org.eclipse.che.plugin.cpp.ide;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Vitalii Parfonov */
public interface CppResources extends ClientBundle {
  CppResources INSTANCE = GWT.create(CppResources.class);

  @Source("svg/c_file.svg")
  SVGResource cFile();

  @Source("svg/cpp_file.svg")
  SVGResource cppFile();

  @Source("svg/c_header_file.svg")
  SVGResource cHeaderFile();

  @Source("svg/category.svg")
  SVGResource category();
}
