/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.command.editor.page.text;

import static org.eclipse.che.ide.api.editor.EditorPartPresenter.PROP_DIRTY;
import static org.eclipse.che.ide.api.editor.EditorPartPresenter.PROP_INPUT;

import com.google.gwt.user.client.ui.IsWidget;
import org.eclipse.che.ide.api.editor.OpenEditorCallbackImpl;
import org.eclipse.che.ide.api.editor.defaulteditor.EditorBuilder;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.editorconfig.TextEditorConfiguration;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.resources.SyntheticFile;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.command.editor.page.AbstractCommandEditorPage;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage;
import org.eclipse.che.ide.macro.chooser.MacroChooser;

/**
 * Abstract {@link CommandEditorPage} which allows to edit a command's property with a text editor
 * that provides autocompletion for macros names.
 *
 * @author Artem Zatsarynnyi
 */
public abstract class AbstractPageWithTextEditor extends AbstractCommandEditorPage
    implements PageWithTextEditorView.ActionDelegate {

  private final PageWithTextEditorView view;
  private final FileTypeRegistry fileTypeRegistry;
  private final MacroChooser macroChooser;
  private final TextEditorConfiguration editorConfiguration;

  private TextEditor editor;

  /** Initial value of the edited command's property. */
  private String initialValue;

  protected AbstractPageWithTextEditor(
      PageWithTextEditorView view,
      EditorBuilder editorBuilder,
      FileTypeRegistry fileTypeRegistry,
      MacroChooser macroChooser,
      String title,
      TextEditorConfiguration editorConfiguration) {
    super("");

    this.view = view;
    this.fileTypeRegistry = fileTypeRegistry;
    this.macroChooser = macroChooser;
    this.editorConfiguration = editorConfiguration;

    view.setDelegate(this);
    view.setHeight(getHeight());
    view.setEditorTitle(title);

    initializeEditor(editorBuilder);
  }

  private void initializeEditor(EditorBuilder editorBuilder) {
    editor = editorBuilder.buildEditor();
    editor.initialize(editorConfiguration);
    editor.activate();

    editor.addPropertyListener(
        (source, propId) -> {
          switch (propId) {
            case PROP_INPUT:
              editor.go(view.getEditorContainer());

              editor.getEditorWidget().setAnnotationRulerVisible(false);
              editor.getEditorWidget().setFoldingRulerVisible(false);
              editor.getEditorWidget().setZoomRulerVisible(false);
              editor.getEditorWidget().setOverviewRulerVisible(false);
              editor.getView().setInfoPanelVisible(false);

              break;
            case PROP_DIRTY:
              updateCommandPropertyValue(editor.getDocument().getContents());
              notifyDirtyStateChanged();

              break;
            default:
          }
        });
  }

  @Override
  public IsWidget getView() {
    return view;
  }

  @Override
  protected void initialize() {
    initialValue = getCommandPropertyValue();

    Document document = editor.getDocument();
    if (document != null) {
      document.replace(0, document.getContentsCharCount(), initialValue);
    } else {
      VirtualFile file = new SyntheticFile(editedCommand.getName() + getType(), initialValue);
      editor.init(
          new EditorInputImpl(fileTypeRegistry.getFileTypeByFile(file), file),
          new OpenEditorCallbackImpl());
    }
  }

  @Override
  public boolean isDirty() {
    if (editedCommand == null) {
      return false;
    }

    return !initialValue.equals(getCommandPropertyValue());
  }

  /** Returns the current value of the edited command's property. */
  protected abstract String getCommandPropertyValue();

  /**
   * Updates the value of the edited command's property.
   *
   * @param newValue new value of the edited command's property
   */
  protected abstract void updateCommandPropertyValue(String newValue);

  /** Returns height of the page in pixels. Default height is 150 px. */
  protected int getHeight() {
    return 150;
  }

  /**
   * Returns type of the edited content. Type must be specified as file's extension e.g.: .sh, .css.
   * Default type is text/plain.
   */
  protected String getType() {
    return "";
  }

  @Override
  public void onExploreMacros() {
    macroChooser.show(
        macro -> {
          Document document = editor.getDocument();
          document.replace(document.getCursorOffset(), 0, macro.getName());
        });
  }
}
