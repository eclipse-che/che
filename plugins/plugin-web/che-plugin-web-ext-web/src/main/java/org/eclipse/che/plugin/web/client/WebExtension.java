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
package org.eclipse.che.plugin.web.client;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_ASSISTANT;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_FILE_NEW;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_CONTEXT_MENU;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.plugin.web.client.css.NewCssFileAction;
import org.eclipse.che.plugin.web.client.css.NewLessFileAction;
import org.eclipse.che.plugin.web.client.html.NewHtmlFileAction;
import org.eclipse.che.plugin.web.client.html.PreviewHTMLAction;
import org.eclipse.che.plugin.web.client.html.editor.HtmlEditorProvider;
import org.eclipse.che.plugin.web.client.js.NewJavaScriptFileAction;
import org.eclipse.che.plugin.web.client.js.editor.JsEditorProvider;
import org.eclipse.che.plugin.web.client.vue.NewVueFileAction;
import org.vectomatic.dom.svg.ui.SVGImage;

/**
 * Extension add editing JavaScript, HTML, CSS css type support to the IDE Application. It provides
 * configured TextEditorView with syntax coloring and autocomplete.
 *
 * @author Nikolay Zamosenchuk
 * @author Sébastien Demanou
 */
@Singleton
@Extension(title = "Web", version = "3.1.0", description = "syntax highlighting and autocomplete.")
public class WebExtension {
  /**
   * Web Extension adds JavaScript, HTML and CSS Support to IDE Application. It provides syntax
   * highlighting for CSS, JS, HTML, Vue files and code completion features for CSS files to IDE.
   */
  @Inject
  public WebExtension(
      HtmlEditorProvider htmlEditorProvider,
      JsEditorProvider jsEditorProvider,
      EditorRegistry editorRegistry,
      WebExtensionResource resources,
      IconRegistry iconRegistry,
      @Named("JSFileType") FileType jsFile,
      @Named("HTMLFileType") FileType htmlFile,
      @Named("VueFileType") FileType vueFile,
      @Named("ES6FileType") FileType es6File,
      @Named("JSXFileType") FileType jsxFile) {
    // register new Icon for javascript project type
    iconRegistry.registerIcon(
        new Icon("JavaScript.samples.category.icon", resources.samplesCategoryJs()));

    editorRegistry.registerDefaultEditor(jsFile, jsEditorProvider);
    editorRegistry.registerDefaultEditor(es6File, jsEditorProvider);
    editorRegistry.registerDefaultEditor(jsxFile, jsEditorProvider);
    editorRegistry.registerDefaultEditor(htmlFile, htmlEditorProvider);
    editorRegistry.registerDefaultEditor(vueFile, htmlEditorProvider);
  }

  @Inject
  private void registerFileTypes(
      FileTypeRegistry fileTypeRegistry,
      @Named("CSSFileType") FileType cssFile,
      @Named("LESSFileType") FileType lessFile,
      @Named("JSFileType") FileType jsFile,
      @Named("ES6FileType") FileType es6File,
      @Named("JSXFileType") FileType jsxFile,
      @Named("TypeScript") FileType typeScriptFile,
      @Named("HTMLFileType") FileType htmlFile,
      @Named("VueFileType") FileType vueFile,
      @Named("PHPFileType") FileType phpFile) {
    fileTypeRegistry.registerFileType(cssFile);
    fileTypeRegistry.registerFileType(lessFile);
    fileTypeRegistry.registerFileType(jsFile);
    fileTypeRegistry.registerFileType(es6File);
    fileTypeRegistry.registerFileType(jsxFile);
    fileTypeRegistry.registerFileType(typeScriptFile);
    fileTypeRegistry.registerFileType(htmlFile);
    fileTypeRegistry.registerFileType(vueFile);
    fileTypeRegistry.registerFileType(phpFile);
  }

  @Inject
  private void prepareActions(
      WebExtensionResource resources,
      ActionManager actionManager,
      NewCssFileAction newCssFileAction,
      NewLessFileAction newLessFileAction,
      NewHtmlFileAction newHtmlFileAction,
      NewVueFileAction newVueFileAction,
      NewJavaScriptFileAction newJavaScriptFileAction,
      PreviewHTMLAction previewHTMLAction) {
    // register actions
    actionManager.registerAction("newCssFile", newCssFileAction);
    actionManager.registerAction("newLessFile", newLessFileAction);
    actionManager.registerAction("newHtmlFile", newHtmlFileAction);
    actionManager.registerAction("newVueFile", newVueFileAction);
    actionManager.registerAction("newJavaScriptFile", newJavaScriptFileAction);
    actionManager.registerAction("previewHTML", previewHTMLAction);

    // set icons
    newCssFileAction
        .getTemplatePresentation()
        .setImageElement(new SVGImage(resources.cssFile()).getElement());
    newLessFileAction
        .getTemplatePresentation()
        .setImageElement(new SVGImage(resources.lessFile()).getElement());
    newHtmlFileAction
        .getTemplatePresentation()
        .setImageElement(new SVGImage(resources.htmlFile()).getElement());
    newVueFileAction
        .getTemplatePresentation()
        .setImageElement(new SVGImage(resources.vueFile()).getElement());
    newJavaScriptFileAction
        .getTemplatePresentation()
        .setImageElement(new SVGImage(resources.jsFile()).getElement());

    // add actions in main menu
    DefaultActionGroup newGroup = (DefaultActionGroup) actionManager.getAction(GROUP_FILE_NEW);
    newGroup.add(newCssFileAction);
    newGroup.add(newLessFileAction);
    newGroup.add(newHtmlFileAction);
    newGroup.add(newVueFileAction);
    newGroup.add(newJavaScriptFileAction);

    // add actions in context menu
    DefaultActionGroup mainContextMenuGroup =
        (DefaultActionGroup) actionManager.getAction(GROUP_MAIN_CONTEXT_MENU);
    mainContextMenuGroup.add(previewHTMLAction);

    // add actions in Assistant main menu
    DefaultActionGroup assistantMainMenuGroup =
        (DefaultActionGroup) actionManager.getAction(GROUP_ASSISTANT);
    assistantMainMenuGroup.add(previewHTMLAction);
  }
}
