/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2010 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jface.text.source;

import org.eclipse.che.jface.text.ITextViewer;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * In addition to the text viewer functionality a source viewer supports:
 *
 * <ul>
 *   <li>visual annotations based on an annotation model
 *   <li>visual range indication
 *   <li>management of text viewer add-ons
 *   <li>explicit configuration
 * </ul>
 *
 * It is assumed that range indication and visual annotations are shown inside the same presentation
 * area. There are no assumptions about whether this area is different from the viewer's text
 * widget.
 *
 * <p>As the visibility of visual annotations can dynamically be changed, it is assumed that the
 * annotation presentation area can dynamically be hidden if it is different from the text widget.
 *
 * <p>In order to provide backward compatibility for clients of <code>ISourceViewer</code>,
 * extension interfaces are used as a means of evolution. The following extension interfaces exist:
 *
 * <ul>
 *   <li>{link org.eclipse.jface.text.source.ISourceViewerExtension} since version 2.1 introducing
 *       the concept of an annotation overview.
 *   <li>{link org.eclipse.jface.text.source.ISourceViewerExtension2} since version 3.0 allowing
 *       source viewers to roll back a previously performed configuration and allows access to the
 *       viewer's visual annotation model.
 *   <li>{link org.eclipse.jface.text.source.ISourceViewerExtension3} since version 3.2 introducing
 *       the concept of a quick assist assistant and providing access to the quick assist invocation
 *       context as well as the current annotation hover.
 *   <li>{link org.eclipse.jface.text.source.ISourceViewerExtension4} since version 3.4 introducing
 *       API to access a minimal set of content assistant APIs.
 * </ul>
 *
 * <p>Clients may implement this interface and its extension interfaces or use the default
 * implementation provided by {link org.eclipse.jface.text.source.SourceViewer}. see
 * org.eclipse.jface.text.source.ISourceViewerExtension see
 * org.eclipse.jface.text.source.ISourceViewerExtension2 see
 * org.eclipse.jface.text.source.ISourceViewerExtension3 see
 * org.eclipse.jface.text.source.ISourceViewerExtension4
 */
public interface ISourceViewer extends ITextViewer {

  //	/**
  //	 * Text operation code for requesting content assist to show completion
  //	 * proposals for the current insert position.
  //	 */
  //	int CONTENTASSIST_PROPOSALS= ITextOperationTarget.STRIP_PREFIX + 1;
  //
  //	/**
  //	 * Text operation code for requesting content assist to show
  //	 * the content information for the current insert position.
  //	 */
  //	int CONTENTASSIST_CONTEXT_INFORMATION=	ITextOperationTarget.STRIP_PREFIX + 2;
  //
  //	/**
  //	 * Text operation code for formatting the selected text or complete document
  //	 * of this viewer if the selection is empty.
  //	 */
  //	int FORMAT= ITextOperationTarget.STRIP_PREFIX + 3;
  //
  //	/**
  //	 * Text operation code for requesting information at the current insertion position.
  //	 * @since 2.0
  //	 */
  //	int INFORMATION= ITextOperationTarget.STRIP_PREFIX + 4;
  //
  //	/*
  //	 * XXX: Cannot continue numbering due to operation codes used in ProjectionViewer
  //	 */
  //
  //	/**
  //	 * Text operation code for requesting quick assist. This will normally
  //	 * show quick assist and quick fix proposals for the current position.
  //	 * @since 3.2
  //	 */
  //	int QUICK_ASSIST= ITextOperationTarget.STRIP_PREFIX + 10;

  /*
   * XXX: Next free number is HyperlinkManager.OPEN_HYPERLINK + 1
   */

  //	/**
  //	 * Configures the source viewer using the given configuration. Prior to 3.0 this
  //	 * method can only be called once. Since 3.0 this method can be called again
  //	 * after a call to {@link ISourceViewerExtension2#unconfigure()}.
  //	 *
  //	 * @param configuration the source viewer configuration to be used
  //	 */
  //	void configure(SourceViewerConfiguration configuration);
  //
  //	/**
  //	 * Sets the annotation hover of this source viewer. The annotation hover
  //	 * provides the information to be displayed in a hover popup window
  //	 * if requested over the annotation presentation area. The annotation
  //	 * hover is assumed to be line oriented.
  //	 *
  //	 * @param annotationHover the hover to be used, <code>null</code> is a valid argument
  //	 */
  //	void setAnnotationHover(IAnnotationHover annotationHover);

  /**
   * Sets the given document as this viewer's text model and the given annotation model as the model
   * for this viewer's visual annotations. The presentation is accordingly updated. An appropriate
   * <code>TextEvent</code> is issued. This text event does not carry a related document event.
   *
   * @param document the viewer's new input document
   * @param annotationModel the model for the viewer's visual annotations
   * @see ITextViewer#setDocument(IDocument)
   */
  void setDocument(IDocument document, IAnnotationModel annotationModel);

  /**
   * Sets the given document as this viewer's text model and the given annotation model as the model
   * for this viewer's visual annotations. The presentation is accordingly updated whereby only the
   * specified region is exposed. An appropriate <code>TextEvent</code> is issued. The text event
   * does not carry a related document event. This method is a convenience method for <code>
   * setDocument(document, annotationModel);setVisibleRegion(offset, length)</code>.
   *
   * @param document the new input document
   * @param annotationModel the model of the viewer's visual annotations
   * @param modelRangeOffset the offset of the model range
   * @param modelRangeLength the length of the model range
   *     <p>see ITextViewer#setDocument(IDocument, int, int)
   */
  void setDocument(
      IDocument document,
      IAnnotationModel annotationModel,
      int modelRangeOffset,
      int modelRangeLength);

  /**
   * Returns this viewer's annotation model. Use {link
   * ISourceViewerExtension2#getVisualAnnotationModel()}in order to get access to the viewer's
   * visual annotation model.
   *
   * @return this viewer's annotation model
   */
  IAnnotationModel getAnnotationModel();

  /**
   * Sets the annotation used by this viewer as range indicator. The range covered by this
   * annotation is referred to as range indication.
   *
   * @param rangeIndicator the annotation to be used as this viewer's range indicator
   */
  void setRangeIndicator(Annotation rangeIndicator);

  /**
   * Sets the viewers's range indication to the specified range. It is indicated whether the cursor
   * should also be moved to the beginning of the specified range.
   *
   * @param offset the offset of the range
   * @param length the length of the range
   * @param moveCursor indicates whether the cursor should be moved to the given offset
   */
  void setRangeIndication(int offset, int length, boolean moveCursor);

  //	/**
  //	 * Returns the viewer's range indication.
  //	 *
  //	 * @return the viewer's range indication.
  //	 */
  //	IRegion getRangeIndication();
  //
  //	/**
  //	 * Removes the viewer's range indication. There is no visible range indication
  //	 * after this method completed.
  //	 */
  //	void removeRangeIndication();
  //
  //	/**
  //	 * Controls the visibility of annotations and in the case of separate
  //	 * presentation areas of text and annotations, the visibility of the
  //	 * annotation's presentation area.<p>
  //	 * By default, annotations and their presentation area are visible.
  //	 *
  //	 * @param show indicates the visibility of annotations
  //	 */
  //	void showAnnotations(boolean show);
}
