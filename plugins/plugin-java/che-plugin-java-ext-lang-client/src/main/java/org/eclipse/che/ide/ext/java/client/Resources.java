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
package org.eclipse.che.ide.ext.java.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/** @author Evgen Vidolob */
public interface Resources extends ClientBundle {

  @Source({"Semantic.css"})
  SemanticCss css();

  interface SemanticCss extends CssResource {

    String field();

    String typeParameter();

    String deprecatedMember();

    String staticField();

    String staticFinalField();

    String staticMethodInvocation();

    String methodDeclaration();
  }
}
