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
package org.eclipse.che.ide.api.wizard;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.collections.ListHelper;

/**
 * Abstract base implementation of a {@link Wizard}.
 *
 * @author Andrey Plotnikov
 * @author Artem Zatsarynnyi
 */
public abstract class AbstractWizard<T> implements Wizard<T> {
  protected final T dataObject;
  protected final Map<String, String> context;
  protected List<WizardPage<T>> wizardPages;
  private UpdateDelegate delegate;
  private int currentPageIndex;

  /**
   * Creates new wizard with the specified {@code dataObject} which will be passed into every added
   * page.
   *
   * <p>So multiple pages have the same {@code dataObject}, and any change to the {@code dataObject}
   * made by one page is available to the other pages.
   *
   * @param dataObject data-object for wizard
   */
  @Inject
  public AbstractWizard(T dataObject) {
    this.dataObject = dataObject;
    context = new HashMap<>();
    wizardPages = new ArrayList<>();
  }

  public Map<String, String> getContext() {
    return context;
  }

  /** Returns wizard's data-object. */
  public T getDataObject() {
    return dataObject;
  }

  /**
   * Add page to wizard.
   *
   * @param page page to add
   */
  public void addPage(@NotNull WizardPage<T> page) {
    page.setUpdateDelegate(delegate);
    page.setContext(context);
    page.init(dataObject);
    wizardPages.add(page);
  }

  /**
   * Add page to wizard at the specified position.
   *
   * @param page page to be stored at the specified position
   * @param index position where the page should be inserted
   * @param replace {@code true} if the existed page should be replaced by the given one, {@code
   *     false} if a page should be inserted at the specified position
   */
  public void addPage(@NotNull WizardPage<T> page, int index, boolean replace) {
    if (index >= wizardPages.size()) {
      addPage(page);
      return;
    }

    if (replace) {
      setPage(page, index);
    } else {
      List<WizardPage<T>> before = ListHelper.slice(wizardPages, 0, index);
      WizardPage<T> currentPage = wizardPages.get(index);
      List<WizardPage<T>> after = ListHelper.slice(wizardPages, index + 1, wizardPages.size());

      wizardPages.clear();
      wizardPages.addAll(before);
      addPage(page);
      wizardPages.add(currentPage);
      wizardPages.addAll(after);
    }
  }

  private void setPage(@NotNull WizardPage<T> page, int index) {
    page.setUpdateDelegate(delegate);
    page.setContext(context);
    page.init(dataObject);
    wizardPages.set(index, page);
  }

  @Override
  public void setUpdateDelegate(@NotNull UpdateDelegate delegate) {
    this.delegate = delegate;
    for (WizardPage<T> page : wizardPages) {
      page.setUpdateDelegate(delegate);
    }
  }

  @Nullable
  @Override
  public WizardPage<T> navigateToFirst() {
    resetNavigationState();
    return navigateToNext();
  }

  /** Reset wizard's navigation state. */
  private void resetNavigationState() {
    currentPageIndex = -1;
  }

  @Nullable
  @Override
  public WizardPage<T> navigateToNext() {
    return getNextPage();
  }

  /** Returns next page that may be shown. */
  @Nullable
  private WizardPage<T> getNextPage() {
    while (++currentPageIndex < wizardPages.size()) {
      WizardPage<T> page = wizardPages.get(currentPageIndex);
      if (!page.canSkip()) {
        return page;
      }
    }
    return null;
  }

  @Nullable
  @Override
  public WizardPage<T> navigateToPrevious() {
    while (--currentPageIndex >= 0) {
      final WizardPage<T> page = wizardPages.get(currentPageIndex);
      if (!page.canSkip()) {
        return page;
      }
    }
    return null;
  }

  @Override
  public boolean hasNext() {
    for (int i = currentPageIndex + 1; i < wizardPages.size(); i++) {
      WizardPage<T> page = wizardPages.get(i);
      if (!page.canSkip()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean hasPrevious() {
    for (int i = currentPageIndex - 1; i >= 0; i--) {
      WizardPage<T> page = wizardPages.get(i);
      if (!page.canSkip()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean canComplete() {
    for (WizardPage<T> page : wizardPages) {
      if (!page.isCompleted()) {
        return false;
      }
    }
    return true;
  }
}
