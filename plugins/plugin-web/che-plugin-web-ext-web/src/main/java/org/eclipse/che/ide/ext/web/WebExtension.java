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
package org.eclipse.che.ide.ext.web;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_FILE_NEW;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.ext.web.css.NewCssFileAction;
import org.eclipse.che.ide.ext.web.css.NewLessFileAction;
import org.eclipse.che.ide.ext.web.js.editor.JsEditorProvider;
import org.eclipse.che.ide.ext.web.html.NewHtmlFileAction;
import org.eclipse.che.ide.ext.web.html.editor.HtmlEditorProvider;
import org.eclipse.che.ide.ext.web.js.NewJavaScriptFileAction;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Extension add editing JavaScript, HTML, CSS css type support to the IDE Application.
 * It provides configured TextEditorView with syntax coloring and autocomplete.
 *
 * @author Nikolay Zamosenchuk
 */
@Singleton
@Extension(title = "Web", version = "3.0.0", description = "syntax highlighting and autocomplete.")
public class WebExtension {
    /**
     * Web Extension adds JavaScript, HTML and CSS Support to IDE Application.
     * It provides syntax highlighting for CSS, JS, HTML files and code completion features for CSS files to IDE.
     */
    @Inject
    public WebExtension(HtmlEditorProvider htmlEditorProvider,
                        JsEditorProvider jsEditorProvider,
                        EditorRegistry editorRegistry,
                        WebExtensionResource resources,
                        IconRegistry iconRegistry,
                        @Named("JSFileType") FileType jsFile,
                        @Named("HTMLFileType") FileType htmlFile) {
        // register new Icon for javascript project type
        iconRegistry.registerIcon(new Icon("JavaScript.samples.category.icon", resources.samplesCategoryJs()));

        editorRegistry.registerDefaultEditor(jsFile, jsEditorProvider);
        editorRegistry.registerDefaultEditor(htmlFile, htmlEditorProvider);
    }

    @Inject
    private void registerFileTypes(FileTypeRegistry fileTypeRegistry,
                                   @Named("CSSFileType") FileType cssFile,
                                   @Named("LESSFileType") FileType lessFile,
                                   @Named("JSFileType") FileType jsFile,
                                   @Named("HTMLFileType") FileType htmlFile,
                                   @Named("PHPFileType") FileType phpFile) {
        fileTypeRegistry.registerFileType(cssFile);
        fileTypeRegistry.registerFileType(lessFile);
        fileTypeRegistry.registerFileType(jsFile);
        fileTypeRegistry.registerFileType(htmlFile);
        fileTypeRegistry.registerFileType(phpFile);
    }

    @Inject
    private void prepareActions(WebLocalizationConstant constant,
                                WebExtensionResource resources,
                                ActionManager actionManager,
                                NewCssFileAction newCssFileAction,
                                NewLessFileAction newLessFileAction,
                                NewHtmlFileAction newHtmlFileAction,
                                NewJavaScriptFileAction newJavaScriptFileAction) {
        // register actions
        actionManager.registerAction(constant.newCssFileActionId(), newCssFileAction);
        actionManager.registerAction(constant.newLessFileActionId(), newLessFileAction);
        actionManager.registerAction(constant.newHtmlFileActionId(), newHtmlFileAction);
        actionManager.registerAction(constant.newJavaScriptFileActionId(), newJavaScriptFileAction);

        // set icons
        newCssFileAction.getTemplatePresentation().setSVGResource(resources.cssFile());
        newLessFileAction.getTemplatePresentation().setSVGResource(resources.lessFile());
        newHtmlFileAction.getTemplatePresentation().setSVGResource(resources.htmlFile());
        newJavaScriptFileAction.getTemplatePresentation().setSVGResource(resources.jsFile());

        // add actions in main menu
        DefaultActionGroup newGroup = (DefaultActionGroup)actionManager.getAction(GROUP_FILE_NEW);
        newGroup.add(newCssFileAction);
        newGroup.add(newLessFileAction);
        newGroup.add(newHtmlFileAction);
        newGroup.add(newJavaScriptFileAction);
    }
}
