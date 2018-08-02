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

/**
 * JSO implementation for {@link CompareConfig}
 *
 * @author Evgen Vidolob
 */
public class CompareConfigJs extends JavaScriptObject implements CompareConfig {
  protected CompareConfigJs() {}

  @Override
  public final native void setOldFile(FileOptions oldFile) /*-{
        this.oldFile = oldFile;
    }-*/;

  @Override
  public final native void setNewFile(FileOptions newFile) /*-{
        this.newFile = newFile;
    }-*/;

  @Override
  public final native void setShowTitle(boolean showTitle) /*-{
        this.showTitle = showTitle;
    }-*/;

  @Override
  public final native void setShowLineStatus(boolean showLineStatus) /*-{
        this.showLineStatus = showLineStatus;
    }-*/;

  @Override
  public final native String toJson() /*-{
        return JSON.stringify(this);
    }-*/;
}
