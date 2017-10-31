/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
