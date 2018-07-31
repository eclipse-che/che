/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.editor.orion.client.jso;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import java.util.List;
import org.eclipse.che.ide.api.editor.link.LinkedModelData;
import org.eclipse.che.ide.api.editor.link.LinkedModelGroup;
import org.eclipse.che.ide.api.editor.text.Position;

/** @author Evgen Vidolob */
public class OrionLinkedModelGroupOverlay extends JavaScriptObject implements LinkedModelGroup {

  protected OrionLinkedModelGroupOverlay() {}

  public final native void setData(OrionLinkedModelDataOverlay dat) /*-{
        this.data = dat;
    }-*/;

  public final native void setPositions(JsArray<OrionLinkedModelPositionOverlay> pos) /*-{
        this.positions = pos;
    }-*/;

  @Override
  public final void setData(LinkedModelData data) {
    if (data instanceof OrionLinkedModelDataOverlay) {
      setData(((OrionLinkedModelDataOverlay) data));
    } else {
      throw new IllegalArgumentException(
          "This implementation supports only OrionLinkedModelDataOverlay data");
    }
  }

  @Override
  public final void setPositions(List<Position> positions) {
    JsArray<OrionLinkedModelPositionOverlay> arr = JavaScriptObject.createArray().cast();
    for (Position position : positions) {
      OrionLinkedModelPositionOverlay pos = JavaScriptObject.createObject().cast();
      pos.setLength(position.getLength());
      pos.setOffset(position.getOffset());
      arr.push(pos);
    }
    setPositions(arr);
  }

  public static native OrionLinkedModelGroupOverlay create() /*-{
        return {};
    }-*/;
}
