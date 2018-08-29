/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2009 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jface.text;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Point;

/**
 * A text viewer connects a text widget with an {@link org.eclipse.jface.text.IDocument}. The
 * document is used as the widget's text model.
 *
 * <p>It supports the following kinds of listeners:
 *
 * <ul>
 *   <li>view port listeners to inform about changes of the viewer's view port
 *   <li>text listeners to inform about changes of the document and the subsequent viewer change
 *   <li>text input listeners to inform about changes of the viewer's input document.
 * </ul>
 *
 * A text viewer supports a set of configuration options and plug-ins defining its behavior:
 *
 * <ul>
 *   <li>undo manager
 *   <li>double click behavior
 *   <li>auto indentation
 *   <li>text hover
 * </ul>
 *
 * Installed plug-ins are not automatically activated. Plug-ins must be activated with the <code>
 * activatePlugins</code> call. Most plug-ins can be defined per content type. Content types are
 * derived from a partitioning of the text viewer's input document. In case of documents that
 * support multiple partitionings, the implementer is responsible for determining the partitioning
 * to use.
 *
 * <p>A text viewer also provides the concept of event consumption. Events handled by the viewer can
 * be filtered and processed by a dynamic event consumer. With {@link
 * org.eclipse.jface.text.ITextViewerExtension}, this mechanism has been replaced with the support
 * for {@link swt.custom.VerifyKeyListener}.
 *
 * <p>A text viewer provides several text editing functions, some of them are configurable, through
 * a text operation target interface. It also supports a presentation mode in which it only shows a
 * specified section of its document. By calling <code>setVisibleRegion</code> clients define which
 * section is visible. Clients can get access to this section by calling <code>getVisibleRegion
 * </code>. The viewer's presentation mode does not affect any client of the viewer other than text
 * listeners. With {@link org.eclipse.jface.text.ITextViewerExtension5} the visible region support
 * has been reworked. With that extension interface, text viewers are allowed to show fractions of
 * their input document. I.e. a widget selection of two visually neighboring characters is no longer
 * guaranteed to be two neighboring characters in the viewer's input document. Thus, viewers
 * implementing {@link org.eclipse.jface.text.ITextViewerExtension5} are potentially forced to
 * change the fractions of the input document that are shown when clients ask for the visible
 * region.
 *
 * <p>In order to provide backward compatibility for clients of <code>ITextViewer</code>, extension
 * interfaces are used as a means of evolution. The following extension interfaces exist:
 *
 * <ul>
 *   <li>{@link org.eclipse.jface.text.ITextViewerExtension} since version 2.0 replacing the event
 *       consumer mechanism and introducing the concept of rewrite targets and means to manage the
 *       viewer's redraw behavior
 *   <li>{@link org.eclipse.jface.text.ITextViewerExtension2}since version 2.1 adding a way to
 *       invalidate a viewer's presentation and setters for hovers.
 *   <li>{@link org.eclipse.jface.text.ITextViewerExtension3} since version 2.1 which itself was
 *       replaced by {@link org.eclipse.jface.text.ITextViewerExtension5} in version 3.0
 *   <li>{@link org.eclipse.jface.text.ITextViewerExtension4} since version 3.0 introducing focus
 *       handling for widget token keepers and the concept of text presentation listeners.
 *   <li>{@link org.eclipse.jface.text.ITextViewerExtension5} since version 3.0 extending the
 *       visible region concept with explicit handling and conversion of widget and model
 *       coordinates.
 *   <li>{@link org.eclipse.jface.text.ITextViewerExtension6} since version 3.1 extending the text
 *       viewer with the ability to detect hyperlinks and access the undo manager.
 *   <li>{@link org.eclipse.jface.text.ITextViewerExtension7} since version 3.3 extending the text
 *       viewer with the ability to install tabs to spaces conversion.
 *   <li>{@link org.eclipse.jface.text.ITextViewerExtension8} since version 3.4 extending the text
 *       viewer with the ability to print and rich hover support.
 * </ul>
 *
 * <p>Clients may implement this interface and its extension interfaces or use the standard
 * implementation {@link org.eclipse.jface.text.TextViewer}.
 *
 * @see org.eclipse.jface.text.ITextViewerExtension
 * @see org.eclipse.jface.text.ITextViewerExtension2
 * @see org.eclipse.jface.text.ITextViewerExtension3
 * @see org.eclipse.jface.text.ITextViewerExtension4
 * @see org.eclipse.jface.text.ITextViewerExtension5
 * @see org.eclipse.jface.text.ITextViewerExtension6
 * @see org.eclipse.jface.text.ITextViewerExtension7
 * @see org.eclipse.jface.text.ITextViewerExtension8
 * @see org.eclipse.jface.text.IDocument
 * @see org.eclipse.jface.text.ITextInputListener
 * @see org.eclipse.jface.text.IViewportListener
 * @see org.eclipse.jface.text.ITextListener
 * @see org.eclipse.jface.text.IEventConsumer
 */
public interface ITextViewer {

  /* ---------- widget --------- */

  //	/**
  //	 * Returns this viewer's SWT control, <code>null</code> if the control is disposed.
  //	 * <p>
  //	 * <em>Calling API directly on the widget can interfere with features provided
  //	 * by a text viewer. Clients who call API directly on the widget are responsible
  //	 * to resolve such conflicts on their side.</em>
  //	 * </p>
  //	 *
  //	 * @return the SWT control or <code>null</code>
  //	 */
  //	StyledText getTextWidget();

  //	/* --------- plug-ins --------- */
  //
  //	/**
  //	 * Sets this viewer's undo manager.
  //	 *
  //	 * @param undoManager the new undo manager. <code>null</code> is a valid argument.
  //	 */
  //	void setUndoManager(IUndoManager undoManager);
  //
  //	/**
  //	 * Sets this viewer's text double click strategy for the given content type.
  //	 *
  //	 * @param strategy the new double click strategy. <code>null</code> is a valid argument.
  //	 * @param contentType the type for which the strategy is registered
  //	 */
  //	void setTextDoubleClickStrategy(ITextDoubleClickStrategy strategy, String contentType);
  ////
  ////	/**
  ////	 * Sets this viewer's auto indent strategy for the given content type. If
  ////	 * the given strategy is <code>null</code> any installed strategy for the
  ////	 * content type is removed. This method has been replaced by
  ////	 * {@link ITextViewerExtension2#prependAutoEditStrategy(IAutoEditStrategy, String)} and
  ////	 * {@link ITextViewerExtension2#removeAutoEditStrategy(IAutoEditStrategy, String)}.
  ////	 * It is now equivalent to
  ////	 * <pre>
  ////	 * 		ITextViewerExtension2 extension= (ITextViewerExtension2) viewer;
  ////	 * 		extension.removeAutoEditStrategy(oldStrategy, contentType);
  ////	 * 		extension.prependAutoEditStrategy(strategy, contentType);
  ////	 * </pre>
  ////	 *
  ////	 * @param strategy the new auto indent strategy. <code>null</code> is a
  ////	 *            valid argument.
  ////	 * @param contentType the type for which the strategy is registered
  ////	 * @deprecated since 3.1, use
  ////	 *             {@link ITextViewerExtension2#prependAutoEditStrategy(IAutoEditStrategy,
  // String)} and
  ////	 *             {@link ITextViewerExtension2#removeAutoEditStrategy(IAutoEditStrategy,
  // String)} instead
  ////	 */
  ////	void setAutoIndentStrategy(IAutoIndentStrategy strategy, String contentType);
  //
  //	/**
  //	 * Sets this viewer's text hover for the given content type.
  //	 * <p>
  //	 * This method has been replaced by {@link ITextViewerExtension2#setTextHover(ITextHover,
  // String, int)}.
  //	 * It is now equivalent to
  //	 * <pre>
  //	 *    ITextViewerExtension2 extension= (ITextViewerExtension2) document;
  //	 *    extension.setTextHover(textViewerHover, contentType,
  // ITextViewerExtension2#DEFAULT_HOVER_STATE_MASK);
  //	 * </pre>
  //	 *
  //	 *
  //	 * @param textViewerHover the new hover. <code>null</code> is a valid
  //	 *            argument.
  //	 * @param contentType the type for which the hover is registered
  //	 */
  //	void setTextHover(ITextHover textViewerHover, String contentType);
  //
  //	/**
  //	 * Activates the installed plug-ins. If the plug-ins are already activated
  //	 * this call has no effect.
  //	 */
  //	void activatePlugins();
  //
  //	/**
  //	 * Resets the installed plug-ins. If plug-ins change their state or
  //	 * behavior over the course of time, this method causes them to be set
  //	 * back to their initial state and behavior. E.g., if an {@link IUndoManager}
  //	 * has been installed on this text viewer, the manager's list of remembered
  //     * text editing operations is removed.
  //	 */
  //	void resetPlugins();

  /* ---------- listeners ------------- */

  //	/**
  //	 * Adds the given view port listener to this viewer. If the listener is already registered with
  //	 * this viewer, this call has no effect.
  //	 *
  //	 * @param listener the listener to be added
  //	 */
  //	void addViewportListener(IViewportListener listener);
  //
  //	/**
  //	 * Removes the given listener from this viewer's set of view port listeners.
  //	 * If the listener is not registered with this viewer, this call has
  //	 * no effect.
  //	 *
  //	 * @param listener the listener to be removed
  //	 */
  //	void removeViewportListener(IViewportListener listener);
  //
  //	/**
  //	 * Adds a text listener to this viewer. If the listener is already registered
  //	 * with this viewer, this call has no effect.
  //	 *
  //	 * @param listener the listener to be added
  //	 */
  //	void addTextListener(ITextListener listener);
  //
  //	/**
  //	 * Removes the given listener from this viewer's set of text listeners.
  //	 * If the listener is not registered with this viewer, this call has
  //	 * no effect.
  //	 *
  //	 * @param listener the listener to be removed
  //	 */
  //	void removeTextListener(ITextListener listener);
  //
  //	/**
  //	 * Adds a text input listener to this viewer. If the listener is already registered
  //	 * with this viewer, this call has no effect.
  //	 *
  //	 * @param listener the listener to be added
  //	 */
  //	void addTextInputListener(ITextInputListener listener);
  //
  //	/**
  //	 * Removes the given listener from this viewer's set of text input listeners.
  //	 * If the listener is not registered with this viewer, this call has
  //	 * no effect.
  //	 *
  //	 * @param listener the listener to be removed
  //	 */
  //	void removeTextInputListener(ITextInputListener listener);

  /* -------------- model manipulation ------------- */

  /**
   * Sets the given document as the text viewer's model and updates the presentation accordingly. An
   * appropriate <code>TextEvent</code> is issued. This text event does not carry a related document
   * event.
   *
   * @param document the viewer's new input document <code>null</code> if none
   */
  void setDocument(IDocument document);

  /**
   * Returns the text viewer's input document.
   *
   * @return the viewer's input document or <code>null</code> if none
   */
  IDocument getDocument();

  /* -------------- event handling ----------------- */

  //	/**
  //	 * Registers an event consumer with this viewer. This method has been
  //	 * replaces with the {@link org.eclipse.swt.custom.VerifyKeyListener}
  //	 * management methods in {@link ITextViewerExtension}.
  //	 *
  //	 * @param consumer the viewer's event consumer. <code>null</code> is a
  //	 *            valid argument.
  //	 */
  //	void setEventConsumer(IEventConsumer consumer);
  //
  //	/**
  //	 * Sets the editable state.
  //	 *
  //	 * @param editable the editable state
  //	 */
  //	void setEditable(boolean editable);
  //
  //	/**
  //	 * Returns whether the shown text can be manipulated.
  //	 *
  //	 * @return the viewer's editable state
  //	 */
  //	boolean isEditable();
  //

  /* ----------- visible region support ------------- */
  //
  //	/**
  //	 * Sets the given document as this viewer's model and
  //	 * exposes the specified region. An appropriate
  //	 * <code>TextEvent</code> is issued. The text event does not carry a
  //	 * related document event. This method is a convenience method for
  //	 * <code>setDocument(document);setVisibleRegion(offset, length)</code>.
  //	 *
  //	 * @param document the new input document or <code>null</code> if none
  //	 * @param modelRangeOffset the offset of the model range
  //	 * @param modelRangeLength the length of the model range
  //	 */
  //	void setDocument(IDocument document, int modelRangeOffset, int modelRangeLength);
  //
  //	/**
  //	 * Defines and sets the region of this viewer's document which will be
  //	 * visible in the presentation. Every character inside the specified region
  //	 * is supposed to be visible in the viewer's widget after that call.
  //	 *
  //	 * @param offset the offset of the visible region
  //	 * @param length the length of the visible region
  //	 */
  //	void setVisibleRegion(int offset, int length);
  //
  //	/**
  //	 * Resets the region of this viewer's document which is visible in the presentation.
  //	 * Afterwards, the whole input document is visible.
  //	 */
  //	void resetVisibleRegion();

  /**
   * Returns the current visible region of this viewer's document. The result may differ from the
   * argument passed to <code>setVisibleRegion</code> if the document has been modified since then.
   * The visible region is supposed to be a consecutive region in viewer's input document and every
   * character inside that region is supposed to visible in the viewer's widget.
   *
   * <p>Viewers implementing {@link ITextViewerExtension5} may be forced to change the fractions of
   * the input document that are shown, in order to fulfill this contract.
   *
   * @return this viewer's current visible region
   */
  IRegion getVisibleRegion();

  //	/**
  //	 * Returns whether a given range overlaps with the visible region of this
  //	 * viewer's document.
  //	 * <p>
  //	 * Viewers implementing {@link ITextViewerExtension5}may be forced to
  //	 * change the fractions of the input document that are shown in order to
  //	 * fulfill this request. This is because the overlap is supposed to be
  //	 * without gaps.
  //	 *
  //	 * @param offset the offset
  //	 * @param length the length
  //	 * @return <code>true</code> if the specified range overlaps with the
  //	 *         visible region
  //	 */
  //	boolean overlapsWithVisibleRegion(int offset, int length);
  //
  //
  //
  //	/* ------------- presentation manipulation ----------- */
  //
  //	/**
  //	 * Applies the color information encoded in the given text presentation.
  //	 * <code>controlRedraw</code> tells this viewer whether it should take care of
  //	 * redraw management or not. If, e.g., this call is one in a sequence of multiple
  //	 * presentation calls, it is more appropriate to explicitly control redrawing at the
  //	 * beginning and the end of the sequence.
  //	 *
  //	 * @param presentation the presentation to be applied to this viewer
  //	 * @param controlRedraw indicates whether this viewer should manage redraws
  //	 */
  //	void changeTextPresentation(TextPresentation presentation, boolean controlRedraw);

  /**
   * Marks the currently applied text presentation as invalid. It is the viewer's responsibility to
   * take any action it can to repair the text presentation.
   *
   * <p>See {@link ITextViewerExtension2#invalidateTextPresentation(int, int)} for a way to
   * invalidate specific regions rather than the presentation as a whole.
   *
   * @since 2.0
   */
  void invalidateTextPresentation();

  //	/**
  //	 * Applies the given color as text foreground color to this viewer's
  //	 * selection.
  //	 *
  //	 * @param color the color to be applied
  //	 */
  //	void setTextColor(Color color);
  //
  //	/**
  //	 * Applies the given color as text foreground color to the specified section
  //	 * of this viewer. <code>controlRedraw</code> tells this viewer whether it
  //	 * should take care of redraw management or not.
  //	 *
  //	 * @param color the color to be applied
  //	 * @param offset the offset of the range to be changed
  //	 * @param length the length of the range to be changed
  //	 * @param controlRedraw indicates whether this viewer should manage redraws
  //	 */
  //	void setTextColor(Color color, int offset, int length, boolean controlRedraw);
  //
  //
  //	/* --------- target handling and configuration ------------ */
  //
  //	/**
  //	 * Returns the text operation target of this viewer.
  //	 *
  //	 * @return the text operation target of this viewer
  //	 */
  //	ITextOperationTarget getTextOperationTarget();
  //
  //	/**
  //	 * Returns the find/replace operation target of this viewer.
  //	 *
  //	 * @return the find/replace operation target of this viewer
  //	 */
  //	IFindReplaceTarget getFindReplaceTarget();
  //
  //	/**
  //	 * Sets the strings that are used as prefixes when lines of the given content type
  //	 * are prefixed using the prefix text operation. The prefixes are considered equivalent.
  //	 * Inserting a prefix always inserts the defaultPrefixes[0].
  //	 * Removing a prefix removes all of the specified prefixes.
  //	 *
  //	 * @param defaultPrefixes the prefixes to be used
  //	 * @param contentType the content type for which the prefixes are specified
  //	 * @since 2.0
  //	 */
  //	void setDefaultPrefixes(String[] defaultPrefixes, String contentType);
  //
  //	/**
  //	 * Sets the strings that are used as prefixes when lines of the given content type
  //	 * are shifted using the shift text operation. The prefixes are considered equivalent.
  //	 * Thus "\t" and "    " can both be used as prefix characters.
  //	 * Shift right always inserts the indentPrefixes[0].
  //	 * Shift left removes all of the specified prefixes.
  //	 *
  //	 * @param indentPrefixes the prefixes to be used
  //	 * @param contentType the content type for which the prefixes are specified
  //	 */
  //	void setIndentPrefixes(String[] indentPrefixes, String contentType);
  //
  //
  //
  //	/* --------- selection handling -------------- */
  //
  //	/**
  //	 * Sets the selection to the specified range.
  //	 *
  //	 * @param offset the offset of the selection range
  //	 * @param length the length of the selection range. A negative length places
  //	 *            the caret at the visual start of the selection.
  //	 */
  //	void setSelectedRange(int offset, int length);

  /**
   * Returns the range of the current selection in coordinates of this viewer's document.
   *
   * @return a <code>Point</code> with x as the offset and y as the length of the current selection
   */
  Point getSelectedRange();

  //	/**
  //	 * Returns a selection provider dedicated to this viewer. Subsequent
  //	 * calls to this method return always the same selection provider.
  //	 *
  //	 * @return this viewer's selection provider
  //	 */
  //	ISelectionProvider getSelectionProvider();
  //
  //
  //	/* ------------- appearance manipulation --------------- */
  //
  //	/**
  //	 * Ensures that the given range is visible.
  //	 *
  //	 * @param offset the offset of the range to be revealed
  //	 * @param length the length of the range to be revealed
  //	 */
  //	void revealRange(int offset, int length);
  //
  //	/**
  //	 * Scrolls the widget so that the given index is the line
  //	 * with the smallest line number of all visible lines.
  //	 *
  //	 * @param index the line which should become the top most line
  //	 */
  //	void setTopIndex(int index);
  //
  //	/**
  //	 * Returns the visible line with the smallest line number.
  //	 *
  //	 * @return the number of the top most visible line
  //	 */
  //	int getTopIndex();
  //
  //	/**
  //	 * Returns the document offset of the upper left corner of this viewer's view port.
  //	 *
  //	 * @return the upper left corner offset
  //	 */
  //	int getTopIndexStartOffset();
  //
  //	/**
  //	 * Returns the visible line with the highest line number.
  //	 *
  //	 * @return the number of the bottom most line
  //	 */
  //	int getBottomIndex();
  //
  //	/**
  //	 * Returns the document offset of the lower right
  //	 * corner of this viewer's view port. This is the visible character
  //	 * with the highest character position. If the content of this viewer
  //	 * is shorter, the position of the last character of the content is returned.
  //	 *
  //	 * @return the lower right corner offset
  //	 */
  //	int getBottomIndexEndOffset();
  //
  //	/**
  //	 * Returns the vertical offset of the first visible line.
  //	 *
  //	 * @return the vertical offset of the first visible line
  //	 */
  //	int getTopInset();
}
