/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.core;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LinkElement;
import com.google.inject.Singleton;

/**
 * Injects Font Awesome icons.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class FontAwesomeInjector {

  public FontAwesomeInjector() {
    LinkElement link = Document.get().createLinkElement();
    link.setRel("stylesheet");
    link.setHref(GWT.getModuleBaseForStaticFiles() + "font-awesome-4.5.0/css/font-awesome.min.css");

    Document.get().getHead().appendChild(link);
  }
}
