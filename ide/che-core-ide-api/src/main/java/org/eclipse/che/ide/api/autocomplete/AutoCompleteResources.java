/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.autocomplete;

import com.google.gwt.resources.client.CssResource;

import org.eclipse.che.ide.ui.Popup;
import org.eclipse.che.ide.ui.list.SimpleList;

/**
 * Resource that defines the appearance of autocomplete popups.
 */
public interface AutoCompleteResources extends SimpleList.Resources, Popup.Resources {

    @Source({"AutocompleteComponent.css", "org/eclipse/che/ide/api/ui/style.css"})
    Css autocompleteComponentCss();

    interface Css extends CssResource {

        String proposalIcon();

        String proposalLabel();

        String proposalGroup();

        String container();

        String items();

        String noborder();
    }
}
