/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jdt.javaeditor;

import org.eclipse.che.jface.text.ITextViewer;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Point;

/** @author Evgen Vidolob */
public class TextViewer implements ITextViewer {

  private IDocument document;

  private Point point;

  public TextViewer(IDocument document, Point point) {
    this.document = document;
    this.point = point;
  }

  @Override
  public void setDocument(IDocument document) {}

  @Override
  public IDocument getDocument() {
    return document;
  }

  @Override
  public IRegion getVisibleRegion() {
    return null;
  }

  @Override
  public void invalidateTextPresentation() {}

  @Override
  public Point getSelectedRange() {
    return point;
  }
}
