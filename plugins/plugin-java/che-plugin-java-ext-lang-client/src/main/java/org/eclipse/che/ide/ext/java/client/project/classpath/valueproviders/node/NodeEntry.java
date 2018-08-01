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
package org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.node;

import com.google.inject.ImplementedBy;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The interface which provides methods which allow change behaviour of the widget and answer on
 * user actions(clicks).
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(NodeWidget.class)
public interface NodeEntry extends View<NodeEntry.ActionDelegate> {
  interface ActionDelegate {
    /**
     * Performs some actions when user click on entry.
     *
     * @param nodeWidget widget of the added node which was selected
     */
    void onNodeClicked(NodeWidget nodeWidget);

    /** Performs some actions when user click on remove button. */
    void onRemoveButtonClicked(NodeWidget nodeWidget);
  }
}
