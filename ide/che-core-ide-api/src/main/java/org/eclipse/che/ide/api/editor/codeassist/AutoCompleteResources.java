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
package org.eclipse.che.ide.api.editor.codeassist;

import com.google.gwt.resources.client.CssResource;
import org.eclipse.che.ide.ui.Popup;
import org.eclipse.che.ide.ui.list.SimpleList;

/** Resource that defines the appearance of autocomplete popups. */
public interface AutoCompleteResources extends SimpleList.Resources, Popup.Resources {

  @Source({"AutocompleteComponent.css", "org/eclipse/che/ide/api/ui/style.css"})
  Css autocompleteComponentCss();

  interface Css extends CssResource {

    String proposalIcon();

    String proposalLabel();

    String proposalGroup();

    String container();

    String items();
  }
}
