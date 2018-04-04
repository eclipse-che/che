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
package org.eclipse.che.ide.api.preferences;

/**
 * Abstract base implementation for all preference page implementations. It's simpler to get started
 * using Preferences.
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 */
public abstract class AbstractPreferencePagePresenter implements PreferencePagePresenter {

  public static String DEFAULT_CATEGORY = "IDE";

  protected DirtyStateListener delegate;

  private String title;

  private String category;

  /**
   * Create preference page.
   *
   * @param title
   * @param category
   */
  public AbstractPreferencePagePresenter(String title, String category) {
    this.title = title;
    this.category = category;
  }

  /**
   * Create preference page with a default category for grouping elements.
   *
   * @param title
   */
  public AbstractPreferencePagePresenter(String title) {
    this(title, DEFAULT_CATEGORY);
  }

  @Override
  public void setUpdateDelegate(DirtyStateListener delegate) {
    this.delegate = delegate;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public String getCategory() {
    return category;
  }
}
