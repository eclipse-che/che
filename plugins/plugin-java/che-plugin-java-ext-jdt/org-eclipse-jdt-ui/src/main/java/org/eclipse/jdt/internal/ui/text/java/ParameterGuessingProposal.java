/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Andrew McCullough - initial API and implementation IBM Corporation - general
 * improvement and bug fixes, partial reimplementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.java;

import static org.eclipse.che.plugin.java.server.dto.DtoServerImpls.LinkedDataImpl;
import static org.eclipse.che.plugin.java.server.dto.DtoServerImpls.LinkedModeModelImpl;
import static org.eclipse.che.plugin.java.server.dto.DtoServerImpls.LinkedPositionGroupImpl;
import static org.eclipse.che.plugin.java.server.dto.DtoServerImpls.RegionImpl;

import org.eclipse.che.jdt.javaeditor.HasLinkedModel;
import org.eclipse.che.jface.text.contentassist.ICompletionProposal;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.template.java.SignatureUtil;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.InclusivePositionUpdater;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.swt.graphics.Point;

/**
 * This is a {@link org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal} which includes
 * templates that represent the best guess completion for each parameter of a method.
 */
public class ParameterGuessingProposal extends JavaMethodCompletionProposal
    implements HasLinkedModel {

  private LinkedModeModelImpl linkedModel;

  /**
   * Creates a {@link ParameterGuessingProposal} or <code>null</code> if the core context isn't
   * available or extended.
   *
   * @param proposal the original completion proposal
   * @param context the currrent context
   * @param fillBestGuess if set, the best guess will be filled in
   * @return a proposal or <code>null</code>
   */
  public static ParameterGuessingProposal createProposal(
      CompletionProposal proposal,
      JavaContentAssistInvocationContext context,
      boolean fillBestGuess) {
    CompletionContext coreContext = context.getCoreContext();
    if (coreContext != null && coreContext.isExtended()) {
      return new ParameterGuessingProposal(proposal, context, coreContext, fillBestGuess);
    }
    return null;
  }

  /** Tells whether this class is in debug mode. */
  private static final boolean DEBUG =
      "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.jdt.ui/debug/ResultCollector"));
  // $NON-NLS-1$//$NON-NLS-2$

  private ICompletionProposal[][] fChoices; // initialized by guessParameters()
  private Position[] fPositions; // initialized by guessParameters()

  private IRegion fSelectedRegion; // initialized by apply()
  private IPositionUpdater fUpdater;

  private final boolean fFillBestGuess;

  private final CompletionContext fCoreContext;

  public ParameterGuessingProposal(
      CompletionProposal proposal,
      JavaContentAssistInvocationContext context,
      CompletionContext coreContext,
      boolean fillBestGuess) {
    super(proposal, context);
    fCoreContext = coreContext;
    fFillBestGuess = fillBestGuess;
  }

  private IJavaElement getEnclosingElement() {
    return fCoreContext.getEnclosingElement();
  }

  private IJavaElement[][] getAssignableElements() {
    char[] signature = SignatureUtil.fix83600(getProposal().getSignature());
    char[][] types = Signature.getParameterTypes(signature);

    IJavaElement[][] assignableElements = new IJavaElement[types.length][];
    for (int i = 0; i < types.length; i++) {
      assignableElements[i] = fCoreContext.getVisibleElements(new String(types[i]));
    }
    return assignableElements;
  }

  /*
   * @see ICompletionProposalExtension#apply(IDocument, char)
   */
  @Override
  public void apply(final IDocument document, char trigger, int offset) {
    super.apply(document, trigger, offset);

    int baseOffset = getReplacementOffset();
    String replacement = getReplacementString();

    if (fPositions != null && getTextViewer() != null) {

      LinkedModeModelImpl model = new LinkedModeModelImpl();

      for (int i = 0; i < fPositions.length; i++) {
        LinkedPositionGroupImpl group = new LinkedPositionGroupImpl();
        int positionOffset = fPositions[i].getOffset();
        int positionLength = fPositions[i].getLength();

        if (fChoices[i].length < 2) {
          RegionImpl region = new RegionImpl();
          region.setOffset(positionOffset);
          region.setLength(positionLength);
          //                        group.addPositions(new LinkedPosition(document, positionOffset,
          // positionLength, LinkedPositionGroup.NO_STOP));
          group.addPositions(region);
        } else {
          //                        ensurePositionCategoryInstalled(document, model);
          //                        document.addPosition(getCategory(), fPositions[i]);
          RegionImpl region = new RegionImpl();
          region.setOffset(positionOffset);
          region.setLength(positionLength);
          //                        group.addPositions(
          //                                new ProposalPosition(document, positionOffset,
          // positionLength, LinkedPositionGroup.NO_STOP, fChoices[i]));
          group.addPositions(region);
          LinkedDataImpl data = new LinkedDataImpl();
          for (ICompletionProposal proposal : fChoices[i]) {
            data.addValues(proposal.getDisplayString());
          }
          group.setData(data);
        }
        model.addGroups(group);
      }
      model.setEscapePosition(baseOffset + replacement.length());
      this.linkedModel = model;

      //                model.forceInstall();
      //                JavaEditor editor = getJavaEditor();
      //                if (editor != null) {
      //                    model.addLinkingListener(new EditorHighlightingSynchronizer(editor));
      //                }

      //                LinkedModeUI ui = new EditorLinkedModeUI(model, getTextViewer());
      //                ui.setExitPosition(getTextViewer(), baseOffset + replacement.length(), 0,
      // Integer.MAX_VALUE);
      //                // exit character can be either ')' or ';'
      //                final char exitChar = replacement.charAt(replacement.length() - 1);
      //                ui.setExitPolicy(new ExitPolicy(exitChar, document) {
      //                    @Override
      //                    public ExitFlags doExit(LinkedModeModel model2, VerifyEvent event, int
      // offset2, int length) {
      //                        if (event.character == ',') {
      //                            for (int i = 0; i < fPositions.length - 1; i++) { // not for the
      // last one
      //                                Position position = fPositions[i];
      //                                if (position.offset <= offset2 && offset2 + length <=
      // position.offset + position.length) {
      //                                    try {
      //                                        ITypedRegion partition = TextUtilities
      //                                                .getPartition(document,
      // IJavaPartitions.JAVA_PARTITIONING, offset2 + length, false);
      //                                        if
      // (IDocument.DEFAULT_CONTENT_TYPE.equals(partition.getType())
      //                                            || offset2 + length == partition.getOffset() +
      // partition.getLength()) {
      //                                            event.character = '\t';
      //                                            event.keyCode = SWT.TAB;
      //                                            return null;
      //                                        }
      //                                    } catch (BadLocationException e) {
      //                                        // continue; not serious enough to log
      //                                    }
      //                                }
      //                            }
      //                        } else if (event.character == ')' && exitChar != ')') {
      //                            // exit from link mode when user is in the last ')' position.
      //                            Position position = fPositions[fPositions.length - 1];
      //                            if (position.offset <= offset2 && offset2 + length <=
      // position.offset + position.length) {
      //								return new ExitFlags(ILinkedModeListener.UPDATE_CARET, false);
      //							}
      //						}
      //						return super.doExit(model2, event, offset2, length);
      //					}
      //				});
      //				ui.setCyclingMode(LinkedModeUI.CYCLE_WHEN_NO_PARENT);
      //				ui.setDoContextInfo(true);
      //				ui.enter();
      fSelectedRegion = new Region(baseOffset + replacement.length(), 0);

    } else {
      fSelectedRegion = new Region(baseOffset + replacement.length(), 0);
    }
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal#needsLinkedMode()
   */
  @Override
  protected boolean needsLinkedMode() {
    return false; // we handle it ourselves
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal#computeReplacementString()
   */
  @Override
  protected String computeReplacementString() {

    if (!hasParameters() || !hasArgumentList()) return super.computeReplacementString();

    long millis = DEBUG ? System.currentTimeMillis() : 0;
    String replacement;
    try {
      replacement = computeGuessingCompletion();
    } catch (JavaModelException x) {
      fPositions = null;
      fChoices = null;
      JavaPlugin.log(x);
      openErrorDialog(x);
      return super.computeReplacementString();
    }
    if (DEBUG)
      System.err.println(
          "Parameter Guessing: " + (System.currentTimeMillis() - millis)); // $NON-NLS-1$

    return replacement;
  }

  /**
   * Creates the completion string. Offsets and Lengths are set to the offsets and lengths of the
   * parameters.
   *
   * @return the completion string
   * @throws org.eclipse.jdt.core.JavaModelException if parameter guessing failed
   */
  private String computeGuessingCompletion() throws JavaModelException {

    StringBuffer buffer = new StringBuffer();
    appendMethodNameReplacement(buffer);

    FormatterPrefs prefs = getFormatterPrefs();

    setCursorPosition(buffer.length());

    if (prefs.afterOpeningParen) buffer.append(SPACE);

    char[][] parameterNames = fProposal.findParameterNames(null);

    fChoices = guessParameters(parameterNames);
    int count = fChoices.length;
    int replacementOffset = getReplacementOffset();

    for (int i = 0; i < count; i++) {
      if (i != 0) {
        if (prefs.beforeComma) buffer.append(SPACE);
        buffer.append(COMMA);
        if (prefs.afterComma) buffer.append(SPACE);
      }

      ICompletionProposal proposal = fChoices[i][0];
      String argument = proposal.getDisplayString();

      Position position = fPositions[i];
      position.setOffset(replacementOffset + buffer.length());
      position.setLength(argument.length());

      if (proposal
          instanceof
          JavaCompletionProposal) // handle the "unknown" case where we only insert a proposal.
      ((JavaCompletionProposal) proposal).setReplacementOffset(replacementOffset + buffer.length());
      buffer.append(argument);
    }

    if (prefs.beforeClosingParen) buffer.append(SPACE);

    buffer.append(RPAREN);

    if (canAutomaticallyAppendSemicolon()) buffer.append(SEMICOLON);

    return buffer.toString();
  }

  //	/**
  //	 * Returns the currently active java editor, or <code>null</code> if it
  //	 * cannot be determined.
  //	 *
  //	 * @return  the currently active java editor, or <code>null</code>
  //	 */
  //	private JavaEditor getJavaEditor() {
  //		IEditorPart part= JavaPlugin.getActivePage().getActiveEditor();
  //		if (part instanceof JavaEditor)
  //			return (JavaEditor) part;
  //		else
  //			return null;
  //	}

  private ICompletionProposal[][] guessParameters(char[][] parameterNames)
      throws JavaModelException {
    // find matches in reverse order.  Do this because people tend to declare the variable meant for
    // the last
    // parameter last.  That is, local variables for the last parameter in the method completion are
    // more
    // likely to be closer to the point of code completion. As an example consider a "delegation"
    // completion:
    //
    // 		public void myMethod(int param1, int param2, int param3) {
    // 			someOtherObject.yourMethod(param1, param2, param3);
    //		}
    //
    // The other consideration is giving preference to variables that have not previously been used
    // in this
    // code completion (which avoids "someOtherObject.yourMethod(param1, param1, param1)";

    int count = parameterNames.length;
    fPositions = new Position[count];
    fChoices = new ICompletionProposal[count][];

    String[] parameterTypes = getParameterTypes();
    ParameterGuesser guesser = new ParameterGuesser(getEnclosingElement());
    IJavaElement[][] assignableElements = getAssignableElements();

    for (int i = count - 1; i >= 0; i--) {
      String paramName = new String(parameterNames[i]);
      Position position = new Position(0, 0);

      boolean isLastParameter = i == count - 1;
      ICompletionProposal[] argumentProposals =
          guesser.parameterProposals(
              parameterTypes[i],
              paramName,
              position,
              assignableElements[i],
              fFillBestGuess,
              isLastParameter);
      if (argumentProposals.length == 0) {
        JavaCompletionProposal proposal =
            new JavaCompletionProposal(paramName, 0, paramName.length(), null, paramName, 0);
        if (isLastParameter) proposal.setTriggerCharacters(new char[] {','});
        argumentProposals = new ICompletionProposal[] {proposal};
      }

      fPositions[i] = position;
      fChoices[i] = argumentProposals;
    }

    return fChoices;
  }

  private String[] getParameterTypes() {
    char[] signature = SignatureUtil.fix83600(fProposal.getSignature());
    char[][] types = Signature.getParameterTypes(signature);

    String[] ret = new String[types.length];
    for (int i = 0; i < types.length; i++) {
      ret[i] = new String(Signature.toCharArray(types[i]));
    }
    return ret;
  }

  /*
   * @see ICompletionProposal#getSelection(IDocument)
   */
  @Override
  public Point getSelection(IDocument document) {
    if (fSelectedRegion == null) return new Point(getReplacementOffset(), 0);

    return new Point(fSelectedRegion.getOffset(), fSelectedRegion.getLength());
  }

  private void openErrorDialog(Exception e) {
    //		Shell shell= getTextViewer().getTextWidget().getShell();
    //		MessageDialog.openError(shell, JavaTextMessages.ParameterGuessingProposal_error_msg,
    // e.getMessage());
    JavaPlugin.log(e);
  }

  private void ensurePositionCategoryInstalled(final IDocument document, LinkedModeModel model) {
    if (!document.containsPositionCategory(getCategory())) {
      document.addPositionCategory(getCategory());
      fUpdater = new InclusivePositionUpdater(getCategory());
      document.addPositionUpdater(fUpdater);

      model.addLinkingListener(
          new ILinkedModeListener() {

            /*
             * @see org.eclipse.jface.text.link.ILinkedModeListener#left(org.eclipse.jface.text.link.LinkedModeModel, int)
             */
            public void left(LinkedModeModel environment, int flags) {
              ensurePositionCategoryRemoved(document);
            }

            public void suspend(LinkedModeModel environment) {}

            public void resume(LinkedModeModel environment, int flags) {}
          });
    }
  }

  private void ensurePositionCategoryRemoved(IDocument document) {
    if (document.containsPositionCategory(getCategory())) {
      try {
        document.removePositionCategory(getCategory());
      } catch (BadPositionCategoryException e) {
        // ignore
      }
      document.removePositionUpdater(fUpdater);
    }
  }

  private String getCategory() {
    return "ParameterGuessingProposal_" + toString(); // $NON-NLS-1$
  }

  @Override
  public org.eclipse.che.ide.ext.java.shared.dto.LinkedModeModel getLinkedModel() {
    return linkedModel;
  }
}
