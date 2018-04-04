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
package org.eclipse.che.ide.ext.java.client;

import com.google.gwt.resources.client.CssResource;

/** @author Evgen Vidolob */
public interface JavaCss extends CssResource {

  @ClassName("exo-autocomplete-fqn")
  String fqnStyle();

  @ClassName("exo-codeassistant-counter")
  String counter();

  @ClassName("overview-mark-warning")
  String overviewMarkWarning();

  @ClassName("overview-mark-error")
  String overviewMarkError();

  @ClassName("overview-mark-task")
  String overviewMarkTask();

  @ClassName("mark-element")
  String markElement();

  @ClassName("error-border")
  String errorBorder();

  @ClassName("presentable-text-container")
  String presentableTextContainer();

  @ClassName("search-match")
  String searchMatch();
}
