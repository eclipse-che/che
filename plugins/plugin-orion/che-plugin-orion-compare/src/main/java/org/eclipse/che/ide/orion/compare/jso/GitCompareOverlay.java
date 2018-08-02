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
package org.eclipse.che.ide.orion.compare.jso;

import com.google.gwt.core.client.JavaScriptObject;
import org.eclipse.che.ide.orion.compare.CompareConfig;
import org.eclipse.che.ide.orion.compare.FileOptions;

/** @author Mykola Morhun */
public class GitCompareOverlay extends JavaScriptObject {

  protected GitCompareOverlay() {}

  public static native GitCompareOverlay create(
      JavaScriptObject gitCompareJso, CompareConfig compareConfig, String parentId) /*-{
    compareConfig.parentDivId = parentId;

    var compare = new gitCompareJso(compareConfig);
    this.widget = compare.getCompareView().getWidget();

    return compare;
  }-*/;

  public final native void update(FileOptions newFile, FileOptions oldFile) /*-{
    var widget = this.getCompareView().getWidget();
    widget.options.newFile = newFile;
    widget.options.oldFile = oldFile;
    this.refresh(true);
  }-*/;

  public final native void refresh() /*-{
    var widget = this.getCompareView().getWidget();
    var editors = widget.getEditors();
    var oldContent= editors[0].getTextView().getText();
    var newContent = editors[1].getTextView().getText();

    widget.options.oldFile.Content = oldContent;
    widget.options.newFile.Content = newContent;
    widget.refresh();
  }-*/;

  public final native String getContent() /*-{
    return this.getCompareView().getWidget().getEditors()[1].getTextView().getText();
  }-*/;
}
