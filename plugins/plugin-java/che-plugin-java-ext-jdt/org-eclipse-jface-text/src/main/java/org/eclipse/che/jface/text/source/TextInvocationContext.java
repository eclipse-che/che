/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jface.text.source;

import org.eclipse.che.jface.text.quickassist.IQuickAssistInvocationContext;

/**
 * Text quick assist invocation context.
 *
 * <p>Clients may extend this class to add additional context information.
 *
 * @since 3.3
 */
public class TextInvocationContext implements IQuickAssistInvocationContext {

  private ISourceViewer fSourceViewer;
  private int fOffset;
  private int fLength;

  public TextInvocationContext(ISourceViewer sourceViewer, int offset, int length) {
    fSourceViewer = sourceViewer;
    fOffset = offset;
    fLength = length;
  }

  /*
   * @see org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext#getOffset()
   */
  public int getOffset() {
    return fOffset;
  }

  /*
   * @see org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext#getLength()
   */
  public int getLength() {
    return fLength;
  }

  /*
   * @see org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext#getSourceViewer()
   */
  public ISourceViewer getSourceViewer() {
    return fSourceViewer;
  }
}
