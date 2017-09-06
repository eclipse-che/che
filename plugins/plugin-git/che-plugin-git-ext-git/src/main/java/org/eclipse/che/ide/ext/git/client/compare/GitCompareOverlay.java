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
package org.eclipse.che.ide.ext.git.client.compare;

import com.google.gwt.core.client.JavaScriptObject;
import org.eclipse.che.ide.orion.compare.CompareConfig;

/** @author Mykola Morhun */
public class GitCompareOverlay extends JavaScriptObject {

  protected GitCompareOverlay() {}

  public static native GitCompareOverlay create(
      JavaScriptObject gitCompareJso, CompareConfig compareConfig) /*-{
    compareConfig.parentDivId = "gwt-debug-compareParentDiv";

    var compare = new gitCompareJso(compareConfig);
    this.widget = compare.getCompareView().getWidget();

    return compare;
  }-*/;

  public final native void setConfig(CompareConfig compareConfig) /*-{
    this.options = compareConfig;
    this.@org.eclipse.che.ide.ext.git.client.compare.GitCompareOverlay::refresh()();
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
