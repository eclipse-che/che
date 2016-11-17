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
package org.eclipse.che.ide;

import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.NotStrict;
import com.google.gwt.resources.client.TextResource;

import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.menu.MenuResources;
import org.eclipse.che.ide.notification.NotificationResources;
import org.eclipse.che.ide.projecttype.wizard.ProjectWizardResources;
import org.eclipse.che.ide.ui.DialogBoxResources;
import org.eclipse.che.ide.ui.buttonLoader.ButtonLoaderResources;
import org.eclipse.che.ide.ui.cellview.CellTableResources;
import org.eclipse.che.ide.ui.cellview.CellTreeResources;
import org.eclipse.che.ide.ui.cellview.DataGridResources;
import org.eclipse.che.ide.ui.list.CategoriesList;
import org.eclipse.che.ide.ui.list.SimpleList;
import org.eclipse.che.ide.ui.tree.Tree;
import org.eclipse.che.ide.ui.zeroclipboard.ZeroClipboardResources;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Interface for resources, e.g., css, images, text files, etc.
 *
 * @author Codenvy crowd
 */
public interface Resources extends Tree.Resources,
                                   PartStackUIResources,
                                   SimpleList.Resources,
                                   MenuResources,
                                   DialogBoxResources,
                                   ZeroClipboardResources,
                                   NotificationResources,
                                   DataGridResources,
                                   CellTableResources,
                                   CellTreeResources,
                                   CategoriesList.Resources,
                                   ButtonLoaderResources,
                                   ProjectWizardResources {

    @Source({"Core.css", "org/eclipse/che/ide/ui/constants.css", "org/eclipse/che/ide/api/ui/style.css"})
    @NotStrict
    CoreCss coreCss();

    @Source("workspace/recipe.svg")
    SVGResource recipe();

    @Source("actions/newProject.svg")
    SVGResource newProject();

    @Source("actions/showHiddenFiles.svg")
    SVGResource showHiddenFiles();

    @Source("texteditor/multi-file-icon.svg")
    SVGResource multiFileIcon();

    @Source("xml/xml.svg")
    SVGResource xmlFile();

    @Source("console/clear.svg")
    SVGResource clear();

    @Source("actions/about.svg")
    SVGResource about();

    @Source("actions/find.svg")
    SVGResource find();

    @Source("actions/find-actions.svg")
    SVGResource findActions();

    @Source("actions/undo.svg")
    SVGResource undo();

    @Source("actions/redo.svg")
    SVGResource redo();

    @Source("actions/project-configuration.svg")
    SVGResource projectConfiguration();

    @Source("actions/delete.svg")
    SVGResource delete();

    @Source("actions/cut.svg")
    SVGResource cut();

    @Source("actions/copy.svg")
    SVGResource copy();

    @Source("actions/paste.svg")
    SVGResource paste();

    @Source("actions/new-resource.svg")
    SVGResource newResource();

    @Source("actions/navigate-to-file.svg")
    SVGResource navigateToFile();

    @Source("actions/save.svg")
    SVGResource save();

    @Source("actions/preferences.svg")
    SVGResource preferences();

    @Source("actions/rename.svg")
    SVGResource rename();

    @Source("actions/format.svg")
    SVGResource format();

    @Source("actions/import.svg")
    SVGResource importProject();

    @Source("actions/importProjectFromLocation.svg")
    SVGResource importProjectFromLocation();

    @Source("actions/importGroup.svg")
    SVGResource importProjectGroup();

    @Source("actions/upload-file.svg")
    SVGResource uploadFile();

    @Source("actions/zip-folder.svg")
    SVGResource downloadZip();

    @Source("actions/refresh.svg")
    SVGResource refresh();

    @Source("defaulticons/file.svg")
    SVGResource defaultFile();

    @Source("defaulticons/default.svg")
    SVGResource defaultIcon();

    @Source("defaulticons/folder.svg")
    SVGResource defaultFolder();

    @Source("defaulticons/project.svg")
    SVGResource defaultProject();

    @Source("defaulticons/projectFolder.svg")
    SVGResource projectFolder();

    @Source("defaulticons/image-icon.svg")
    SVGResource defaultImage();

    @Source("defaulticons/md.svg")
    SVGResource mdFile();

    @Source("defaulticons/json.svg")
    SVGResource jsonFile();

    @Source("part/project-explorer-part-icon.svg")
    SVGResource projectExplorerPartIcon();

    @Source("part/events-part-icon.svg")
    SVGResource eventsPartIcon();

    @Source("part/output-part-icon.svg")
    SVGResource outputPartIcon();

    @Source("hotkeys/print_template.html")
    TextResource printTemplate();

    @Source("actions/evaluate.svg")
    SVGResource compile();

    @Source("part/che-logo.svg")
    SVGResource cheLogo();

    /** Interface for css resources. */
    interface CoreCss extends CssResource {
        String editorPaneMenuDelimiter();

        String simpleListContainer();

        String mainText();

        // wizard's styles
        String mainFont();

        String mainBoldFont();

        String defaultFont();

        String warningFont();

        String errorFont();

        String greyFontColor();

        String cursorPointer();

        String line();

        String editorFullScreen();

        String createWsTagsPopup();

        String tagsPanel();
    }
}
