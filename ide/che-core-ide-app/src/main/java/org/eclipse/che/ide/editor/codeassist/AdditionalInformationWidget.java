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
package org.eclipse.che.ide.editor.codeassist;

import elemental.dom.Element;
import org.eclipse.che.ide.ui.popup.PopupResources;
import org.eclipse.che.ide.ui.popup.PopupWidget;

public class AdditionalInformationWidget extends PopupWidget<Element> {

  public AdditionalInformationWidget(PopupResources popupResources) {
    super(popupResources, "Proposals:");
  }

  @Override
  public String getEmptyMessage() {
    return "No information available";
  }

  @Override
  public Element createItem(final Element itemModel) {
    return itemModel;
  }
}
