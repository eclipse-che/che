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
package org.eclipse.che.ide.part.widgets.partbutton;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.PartStackView.TabItem;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
public interface PartButton extends View<PartButton.ActionDelegate>, TabItem {

  @NotNull
  PartButton setTooltip(@Nullable String tooltip);

  @NotNull
  PartButton setIcon(@Nullable SVGResource resource);

  interface ActionDelegate {
    void onTabClicked(@NotNull TabItem selectedTab);
  }
}
