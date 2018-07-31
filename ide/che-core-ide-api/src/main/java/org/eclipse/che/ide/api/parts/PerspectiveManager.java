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
package org.eclipse.che.ide.api.parts;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * The class stores current perspective type. Contains listeners which do some actions when type is
 * changed. By default PROJECT perspective type is set.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class PerspectiveManager {

  private final List<PerspectiveTypeListener> listeners;
  private final Map<String, Perspective> perspectives;

  private String currentPerspectiveId;

  @Inject
  public PerspectiveManager(
      Map<String, Perspective> perspectives,
      @Named("defaultPerspectiveId") String defaultPerspectiveId) {
    this.perspectives = perspectives;
    listeners = new ArrayList<>();

    currentPerspectiveId = defaultPerspectiveId;
  }

  /**
   * Returns current active perspective. The method can return null, if current perspective isn't
   * found.
   */
  @Nullable
  public Perspective getActivePerspective() {
    return perspectives.get(currentPerspectiveId);
  }

  /**
   * Changes perspective type and notifies listeners.
   *
   * @param perspectiveId type which need set
   */
  public void setPerspectiveId(@NotNull String perspectiveId) {
    Perspective currentPerspective = perspectives.get(currentPerspectiveId);

    currentPerspective.storeState();

    currentPerspectiveId = perspectiveId;

    for (PerspectiveTypeListener container : listeners) {
      container.onPerspectiveChanged();
    }
  }

  /** Returns current perspective type. */
  @NotNull
  public String getPerspectiveId() {
    return currentPerspectiveId;
  }

  /**
   * Adds listeners which will react on changing of perspective type.
   *
   * @param listener listener which need add
   */
  public void addListener(@NotNull PerspectiveTypeListener listener) {
    listeners.add(listener);
  }

  /** @return current map of all perspectives */
  public Map<String, Perspective> getPerspectives() {
    return new HashMap<>(perspectives);
  }

  /**
   * The interface which must be implemented by all elements who need react on perspective changing.
   */
  public interface PerspectiveTypeListener {
    /** Performs some action when perspective was changed. */
    void onPerspectiveChanged();
  }
}
