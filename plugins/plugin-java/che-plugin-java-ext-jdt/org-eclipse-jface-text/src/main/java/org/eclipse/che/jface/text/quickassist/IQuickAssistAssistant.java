/**
 * ***************************************************************************** Copyright (c) 2006,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jface.text.quickassist;

import org.eclipse.che.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.Annotation;

/**
 * An <code>IQuickAssistAssistant</code> provides support for quick fixes and quick assists. The
 * quick assist assistant is a {@link ISourceViewer} add-on. Its purpose is to propose, display, and
 * insert quick assists and quick fixes available at the current source viewer's quick assist
 * invocation context.
 *
 * <p>The quick assist assistant can be configured with a {@link IQuickAssistProcessor} which
 * provides the possible quick assist and quick fix completions. In order to provide backward
 * compatibility for clients of <code>IQuickAssistAssistant</code>, extension interfaces are used to
 * provide a means of evolution. The following extension interfaces exist:
 *
 * <ul>
 *   <li>{@link IQuickAssistAssistantExtension} since version 3.4 introducing the following
 *       function:
 *       <ul>
 *         <li>allows to get a handler for the given command identifier
 *         <li>allows to enable support for colored labels in the proposal popup
 *       </ul>
 *       <p>The interface can be implemented by clients. By default, clients use
 *       {QuickAssistAssistant} as the standard implementer of this interface.
 *
 * @see ISourceViewer
 * @see IQuickAssistProcessor
 * @see IQuickAssistAssistantExtension
 * @since 3.2
 */
public interface IQuickAssistAssistant {

  /**
   * Installs quick assist support on the given source viewer.
   *
   * <p><strong>Note:</strong> This quick assist assistant will only be able to query the invocation
   * context if <code>sourceViewer</code> also implements {ISourceViewerExtension3}.
   *
   * @param sourceViewer the source viewer on which quick assist will work
   */
  void install(ISourceViewer sourceViewer);

  //	/**
  //	 * Sets the information control creator for the additional information control.
  //	 *
  //	 * @param creator the information control creator for the additional information control
  //	 */
  //	void setInformationControlCreator(IInformationControlCreator creator);

  /** Uninstalls quick assist support from the source viewer it has previously be installed on. */
  void uninstall();

  /**
   * Shows all possible quick fixes and quick assists at the viewer's cursor position.
   *
   * @return an optional error message if no proposals can be computed
   */
  String showPossibleQuickAssists();

  /**
   * Registers a given quick assist processor for a particular content type. If there is already a
   * processor registered, the new processor is registered instead of the old one.
   *
   * @param processor the quick assist processor to register, or <code>null</code> to remove an
   *     existing one
   */
  void setQuickAssistProcessor(IQuickAssistProcessor processor);

  /**
   * Returns the quick assist processor to be used for the given content type.
   *
   * @return the quick assist processor or <code>null</code> if none exists
   */
  IQuickAssistProcessor getQuickAssistProcessor();

  /**
   * Tells whether this assistant has a fix for the given annotation.
   *
   * <p><strong>Note:</strong> This test must be fast and optimistic i.e. it is OK to return <code>
   * true</code> even though there might be no quick fix.
   *
   * @param annotation the annotation
   * @return <code>true</code> if the assistant has a fix for the given annotation
   */
  boolean canFix(Annotation annotation);

  /**
   * Tells whether this assistant has assists for the given invocation context.
   *
   * @param invocationContext the invocation context
   * @return <code>true</code> if the assistant has a fix for the given annotation
   */
  boolean canAssist(IQuickAssistInvocationContext invocationContext);
  //
  //	/**
  //	 * Sets the proposal selector's background color.
  //	 * <p>
  //	 * <strong>Note:</strong> As of 3.4, you should only call this
  //	 * method if you want to override the {@link JFacePreferences#CONTENT_ASSIST_BACKGROUND_COLOR}.
  //	 * </p>
  //	 *
  //	 * @param background the background color
  //	 */
  //	void setProposalSelectorBackground(Color background);
  //
  //	/**
  //	 * Sets the proposal's foreground color.
  //	 * <p>
  //	 * <strong>Note:</strong> As of 3.4, you should only call this
  //	 * method if you want to override the {@link JFacePreferences#CONTENT_ASSIST_FOREGROUND_COLOR}.
  //	 * </p>
  //	 *
  //	 * @param foreground the foreground color
  //	 */
  //	void setProposalSelectorForeground(Color foreground);
  //
  //	/**
  //	 * Adds a completion listener that will be informed before proposals are computed.
  //	 *
  //	 * @param listener the listener
  //	 */
  //	void addCompletionListener(ICompletionListener listener);
  //
  //	/**
  //	 * Removes a completion listener.
  //	 *
  //	 * @param listener the listener to remove
  //	 */
  //	void removeCompletionListener(ICompletionListener listener);

  /**
   * Enables displaying a status line below the proposal popup. The default is not to show the
   * status line. The contents of the status line may be set via {@link #setStatusMessage(String)}.
   *
   * @param show <code>true</code> to show a message line, <code>false</code> to not show one.
   */
  public void setStatusLineVisible(boolean show);

  /**
   * Sets the caption message displayed at the bottom of the completion proposal popup.
   *
   * @param message the message
   */
  public void setStatusMessage(String message);
}
