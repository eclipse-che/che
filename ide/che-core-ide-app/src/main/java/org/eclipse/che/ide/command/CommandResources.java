/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.command;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Client bundle for Command related resources.
 *
 * @author Artem Zatsarynnyi
 */
public interface CommandResources extends ClientBundle {

    /** Resource is used as CSS constant's value for setting 'background-image' property. */
    @DataResource.MimeType("image/svg+xml")
    @Source("magnifier.svg")
    DataResource magnifier();

    @Source("explorer/explorer-part.svg")
    SVGResource explorerPart();

    @Source("explorer/add-command-button.svg")
    SVGResource addCommand();

    @Source("explorer/duplicate-command-button.svg")
    SVGResource duplicateCommand();

    @Source("explorer/remove-command-button.svg")
    SVGResource removeCommand();

    @Source("editor/execute.svg")
    SVGResource execute();

    @Source("editor/iconCollapsed.png")
    ImageResource iconCollapsed();

    @Source("editor/iconExpanded.png")
    ImageResource iconExpanded();

    @Source({"explorer/styles.css", "org/eclipse/che/ide/api/ui/style.css"})
    ExplorerCSS commandsExplorerCss();

    @Source({"palette/styles.css", "org/eclipse/che/ide/api/ui/style.css"})
    PaletteCSS commandsPaletteCss();

    @Source({"toolbar/processes/styles.css", "org/eclipse/che/ide/api/ui/style.css"})
    ToolbarCSS commandToolbarCss();

    interface ExplorerCSS extends CssResource {
        String commandGoalNode();

        String commandNode();

        String commandNodeText();

        String commandNodeButtonsPanel();
    }

    interface PaletteCSS extends CssResource {
        String filterField();
    }

    interface ToolbarCSS extends CssResource {
        String toolbarButton();

        String runButton();

        String debugButton();

        String processesListLabel();

        String processesList();

        String processesListItemNameLabel();

        String processesListItemActionButton();
    }
}
