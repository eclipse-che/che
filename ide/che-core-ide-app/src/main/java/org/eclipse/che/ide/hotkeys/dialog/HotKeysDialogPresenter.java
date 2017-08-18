/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.hotkeys.dialog;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.hotkeys.HasHotKeyItems;
import org.eclipse.che.ide.api.editor.hotkeys.HotKeyItem;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.Scheme;
import org.eclipse.che.ide.collections.js.JsoArray;
import org.eclipse.che.ide.util.StringUtils;
import org.eclipse.che.ide.util.input.CharCodeWithModifiers;
import org.eclipse.che.ide.util.input.KeyMapUtil;

/**
 * The class provides displaying list hotKeys for IDE and editor
 *
 * @author Alexander Andrienko
 * @author Artem Zatsarynnyi
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 */
@Singleton
public class HotKeysDialogPresenter implements HotKeysDialogView.ActionDelegate {

  private static final String EDITOR_KEYBINDINGS = "Editor";
  private static final String IDE_KEYBINDINGS = "IDE";

  private final HotKeysDialogView view;
  private final KeyBindingAgent keyBindingAgent;
  private final ActionManager actionManager;
  private final EditorAgent editorAgent;
  private final Resources resources;

  private List<HotKeyItem> ideHotKey;
  private List<HotKeyItem> editorHotKeys;
  private String selectedSchemeId;

  private Map<String, List<HotKeyItem>> categories;

  @Inject
  public HotKeysDialogPresenter(
      HotKeysDialogView view,
      KeyBindingAgent keyBindingAgent,
      ActionManager actionManager,
      EditorAgent editorAgent,
      Resources resources) {
    this.view = view;
    this.keyBindingAgent = keyBindingAgent;
    this.actionManager = actionManager;
    this.editorAgent = editorAgent;
    this.resources = resources;

    categories = new HashMap<>();
    view.setDelegate(this);
  }

  @Override
  public void showHotKeys() {
    showHotKeys(keyBindingAgent.getActive().getSchemeId());
  }

  public void showHotKeys(String scheme) {
    selectedSchemeId = keyBindingAgent.getScheme(scheme).getSchemeId();

    ideHotKey = getIDEHotKey();
    editorHotKeys = getEditorHotKey();

    categories.clear();
    if (!ideHotKey.isEmpty()) {
      categories.put(IDE_KEYBINDINGS, ideHotKey);
    }
    if (!editorHotKeys.isEmpty()) {
      categories.put(EDITOR_KEYBINDINGS, editorHotKeys);
    }

    view.setData(categories);
    view.setSchemes(selectedSchemeId, keyBindingAgent.getSchemes());
    view.renderKeybindings();
    view.show();
  }

  private List<HotKeyItem> getIDEHotKey() {
    List<HotKeyItem> ideHotKeys = new ArrayList<>();
    Scheme scheme = keyBindingAgent.getScheme(selectedSchemeId);

    for (String actionId : actionManager.getActionIds("")) {
      boolean isGlobal = false;
      CharCodeWithModifiers activeCharCodeWithModifiers = scheme.getKeyBinding(actionId);
      if (activeCharCodeWithModifiers == null) {
        activeCharCodeWithModifiers = keyBindingAgent.getKeyBinding(actionId);
        isGlobal = true;
      }
      if (activeCharCodeWithModifiers != null) {
        String hotKey = KeyMapUtil.getShortcutText(activeCharCodeWithModifiers);
        String description =
            actionManager.getAction(actionId).getTemplatePresentation().getDescription();
        if (description != null && !description.isEmpty()) {
          ideHotKeys.add(new HotKeyItem(description, hotKey, isGlobal));
        }
      }
    }

    return ideHotKeys;
  }

  private List<HotKeyItem> getEditorHotKey() {
    EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
    if (activeEditor instanceof HasHotKeyItems) {
      return ((HasHotKeyItems) activeEditor).getHotKeys();
    }
    return Collections.emptyList();
  }

  @Override
  public void onSaveClicked() {
    keyBindingAgent.setActive(selectedSchemeId);
    view.hide();
  }

  @Override
  public void onCloseClicked() {
    view.hide();
  }

  @Override
  public void onPrintClicked() {
    final JsoArray<Node> nodesArray = JsoArray.create();
    for (Map.Entry<String, List<HotKeyItem>> entry : categories.entrySet()) {
      nodesArray.add(wrapCategory(entry.getKey()));
      for (HotKeyItem hotKeyItem : entry.getValue()) {
        if (hotKeyItem.getActionDescription() != null) {
          nodesArray.add(
              wrapHotKey(
                  hotKeyItem.getHotKey(),
                  hotKeyItem.getActionDescription(),
                  hotKeyItem.isGlobal()));
        }
      }
    }

    openWindowForPrinting(resources.printTemplate().getText(), nodesArray);
  }

  private static Element wrapCategory(String text) {
    final DivElement div = Document.get().createDivElement();
    div.setClassName("divCategory");
    div.setInnerText(text);
    return div;
  }

  private static Element wrapHotKey(String hotKey, String description, boolean global) {
    final DivElement containerDiv = Document.get().createDivElement();
    final DivElement hotKeyDiv = Document.get().createDivElement();
    final DivElement descriptionDiv = Document.get().createDivElement();

    hotKeyDiv.setInnerText(hotKey);
    descriptionDiv.setInnerText(description);

    containerDiv.setClassName("divRow");
    hotKeyDiv.setClassName("divCell");
    hotKeyDiv.addClassName("hotKey");
    descriptionDiv.setClassName("divCell");
    descriptionDiv.addClassName("description");

    containerDiv.appendChild(hotKeyDiv);
    containerDiv.appendChild(descriptionDiv);

    return containerDiv;
  }

  private static native void openWindowForPrinting(String htmlTemplate, JsoArray<Node> nodes) /*-{
        var printWindow = $wnd.open("about:blank", "", "width=650,height=800");
        printWindow.document.write(htmlTemplate);
        var container = printWindow.document.getElementById("key-bindings-container");
        for (var node in nodes) {
            container.appendChild(nodes[node]);
        }
        printWindow.document.close(); // necessary for IE >= 10
        printWindow.focus(); // necessary for IE >= 10
        printWindow.print();
        printWindow.close();
    }-*/;

  @Override
  public void onFilterValueChanged(String filteredText) {
    categories.clear();

    List<HotKeyItem> ideFilteredHotKey = filterCategory(ideHotKey, filteredText);
    if (!ideFilteredHotKey.isEmpty()) {
      categories.put(IDE_KEYBINDINGS, ideFilteredHotKey);
    }

    List<HotKeyItem> editorFilteredHotKeys = filterCategory(editorHotKeys, filteredText);
    if (!editorFilteredHotKeys.isEmpty()) {
      categories.put(EDITOR_KEYBINDINGS, editorFilteredHotKeys);
    }

    view.setData(categories);
    view.renderKeybindings();
  }

  @Override
  public void onSchemeSelectionChanged() {
    showHotKeys(view.getSelectedScheme());
  }

  private List<HotKeyItem> filterCategory(List<HotKeyItem> hotKeyItems, String expectedText) {
    List<HotKeyItem> result = new ArrayList<>();
    for (HotKeyItem hotKeyItem : hotKeyItems) {
      String description = hotKeyItem.getActionDescription();
      String keyBindings = hotKeyItem.getHotKey();
      boolean isFound =
          description != null
              && (StringUtils.containsIgnoreCase(description, expectedText)
                  || StringUtils.containsIgnoreCase(keyBindings, expectedText));
      if (isFound) {
        result.add(hotKeyItem);
      }
    }
    return result;
  }
}
