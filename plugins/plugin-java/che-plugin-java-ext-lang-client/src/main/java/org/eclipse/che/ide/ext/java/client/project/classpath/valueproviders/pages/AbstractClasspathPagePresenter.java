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
package org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Abstract base implementation for all java project properties page implementations. It's simpler
 * to get started using Properties.
 *
 * @author Valeriy Svydenko
 */
public abstract class AbstractClasspathPagePresenter implements ClasspathPagePresenter {

  private String title;
  private String category;
  private SVGResource icon;

  protected DirtyStateListener delegate;

  public AbstractClasspathPagePresenter(String title, String category, SVGResource icon) {
    this.title = title;
    this.category = category;
    this.icon = icon;
  }

  @Override
  public void setUpdateDelegate(DirtyStateListener delegate) {
    this.delegate = delegate;
  }

  @Override
  public String getCategory() {
    return category;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public SVGResource getIcon() {
    return icon;
  }
}
