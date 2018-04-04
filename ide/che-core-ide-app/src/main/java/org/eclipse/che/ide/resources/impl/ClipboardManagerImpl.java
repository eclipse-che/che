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
package org.eclipse.che.ide.resources.impl;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.ide.api.resources.Resource.PROJECT;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.modification.ClipboardManager;
import org.eclipse.che.ide.api.resources.modification.CopyProvider;
import org.eclipse.che.ide.api.resources.modification.CutProvider;
import org.eclipse.che.ide.api.resources.modification.PasteProvider;

/**
 * Default implementation of {@link ClipboardManager}.
 *
 * @author Vlad Zhukovskiy
 * @see ClipboardManager
 * @since 4.4.0
 */
@Singleton
public class ClipboardManagerImpl implements ClipboardManager {

  private final CopyPasteManager copyPasteManager;

  private CombinedProvider defProvider;

  @Inject
  public ClipboardManagerImpl(CopyPasteManager copyPasteManager) {
    this.copyPasteManager = copyPasteManager;

    defProvider = new CombinedProvider();
  }

  /** {@inheritDoc} */
  @Override
  public CutProvider getCutProvider() {
    return defProvider;
  }

  /** {@inheritDoc} */
  @Override
  public CopyProvider getCopyProvider() {
    return defProvider;
  }

  /** {@inheritDoc} */
  @Override
  public PasteProvider getPasteProvider() {
    return defProvider;
  }

  private class CombinedProvider implements CutProvider, CopyProvider, PasteProvider {
    /** {@inheritDoc} */
    @Override
    public boolean isCopyEnable(AppContext appContext) {
      final Resource[] resources = appContext.getResources();

      // check general state
      return !(resources == null || resources.length == 0);
    }

    /** {@inheritDoc} */
    @Override
    public void performCopy(AppContext appContext) {
      final Resource[] resources = appContext.getResources();

      // check general state
      checkState(resources != null && resources.length > 0);

      copyPasteManager.setResources(resources, false);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCutEnable(AppContext appContext) {
      final Resource[] resources = appContext.getResources();

      // check general state
      if (resources == null || resources.length == 0) {
        return false;
      }

      for (Resource resource : resources) {
        // if there is at least one root project
        if (resource.getResourceType() == PROJECT && resource.getLocation().segmentCount() == 1) {
          return false;
        }
      }

      return true;
    }

    /** {@inheritDoc} */
    @Override
    public void performCut(AppContext appContext) {
      final Resource[] resources = appContext.getResources();

      // check general state
      checkState(resources != null && resources.length > 0);

      for (Resource resource : resources) {
        // if there is at least one root project
        if (resource.getResourceType() == PROJECT && resource.getLocation().segmentCount() == 1) {
          throw new IllegalStateException(
              "Project '" + resource.getLocation() + "' can not be cut");
        }
      }

      copyPasteManager.setResources(resources, true);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPastePossible(AppContext appContext) {
      final Resource[] resources = appContext.getResources();

      // check general state
      if (resources == null || resources.length > 1) {
        return false;
      }

      final Resource destination = resources[0];

      // destination should be a container type
      if (destination instanceof Container) {
        return true;
      }

      final Resource[] items = copyPasteManager.getResources();

      // check prepared items
      if (items == null || items.length == 0) {
        return false;
      }

      return false;
    }

    /** {@inheritDoc} */
    @Override
    public void performPaste(AppContext appContext) {
      final Resource[] resources = appContext.getResources();

      // check general state
      checkState(resources != null && resources.length == 1 && resources[0] instanceof Container);

      final Resource[] items = copyPasteManager.getResources();

      // check prepared items
      if (items == null || items.length == 0) {
        throw new IllegalStateException("Nothing to process");
      }

      copyPasteManager.paste(resources[0].getLocation());
    }
  }
}
