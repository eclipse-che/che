/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.template.contentassist;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.jface.text.ITextViewer;
import org.eclipse.che.jface.text.contentassist.ICompletionProposal;
import org.eclipse.che.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.che.jface.text.contentassist.IContextInformation;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * Global state for templates. Selecting a proposal for the master template variable will cause the
 * value (and the proposals) for the slave variables to change.
 *
 * @see MultiVariable
 */
public class MultiVariableGuess {

  /** Implementation of the <code>ICompletionProposal</code> interface and extension. */
  private static class Proposal implements ICompletionProposal, ICompletionProposalExtension2 {

    /** The string to be displayed in the completion proposal popup */
    private String fDisplayString;
    /** The replacement string */
    String fReplacementString;
    /** The replacement offset */
    private int fReplacementOffset;
    /** The replacement length */
    private int fReplacementLength;
    /** The cursor position after this proposal has been applied */
    private int fCursorPosition;
    /** The image to be displayed in the completion proposal popup */
    private Image fImage;
    /** The context information of this proposal */
    private IContextInformation fContextInformation;
    /** The additional info of this proposal */
    private String fAdditionalProposalInfo;

    /**
     * Creates a new completion proposal based on the provided information. The replacement string
     * is considered being the display string too. All remaining fields are set to <code>null</code>
     * .
     *
     * @param replacementString the actual string to be inserted into the document
     * @param replacementOffset the offset of the text to be replaced
     * @param replacementLength the length of the text to be replaced
     * @param cursorPosition the position of the cursor following the insert relative to
     *     replacementOffset
     */
    public Proposal(
        String replacementString,
        int replacementOffset,
        int replacementLength,
        int cursorPosition) {
      this(
          replacementString,
          replacementOffset,
          replacementLength,
          cursorPosition,
          null,
          null,
          null,
          null);
    }

    /**
     * Creates a new completion proposal. All fields are initialized based on the provided
     * information.
     *
     * @param replacementString the actual string to be inserted into the document
     * @param replacementOffset the offset of the text to be replaced
     * @param replacementLength the length of the text to be replaced
     * @param cursorPosition the position of the cursor following the insert relative to
     *     replacementOffset
     * @param image the image to display for this proposal
     * @param displayString the string to be displayed for the proposal
     * @param contextInformation the context information associated with this proposal
     * @param additionalProposalInfo the additional information associated with this proposal
     */
    public Proposal(
        String replacementString,
        int replacementOffset,
        int replacementLength,
        int cursorPosition,
        Image image,
        String displayString,
        IContextInformation contextInformation,
        String additionalProposalInfo) {
      Assert.isNotNull(replacementString);
      Assert.isTrue(replacementOffset >= 0);
      Assert.isTrue(replacementLength >= 0);
      Assert.isTrue(cursorPosition >= 0);

      fReplacementString = replacementString;
      fReplacementOffset = replacementOffset;
      fReplacementLength = replacementLength;
      fCursorPosition = cursorPosition;
      fImage = image;
      fDisplayString = displayString;
      fContextInformation = contextInformation;
      fAdditionalProposalInfo = additionalProposalInfo;
    }

    /*
     * @see ICompletionProposal#apply(IDocument)
     */
    public void apply(IDocument document) {
      try {
        document.replace(fReplacementOffset, fReplacementLength, fReplacementString);
      } catch (BadLocationException x) {
        // ignore
      }
    }

    /*
     * @see ICompletionProposal#getSelection(IDocument)
     */
    public Point getSelection(IDocument document) {
      return new Point(fReplacementOffset + fCursorPosition, 0);
    }

    /*
     * @see ICompletionProposal#getContextInformation()
     */
    public IContextInformation getContextInformation() {
      return fContextInformation;
    }

    /*
     * @see ICompletionProposal#getImage()
     */
    public Image getImage() {
      return fImage;
    }

    /*
     * @see ICompletionProposal#getDisplayString()
     */
    public String getDisplayString() {
      if (fDisplayString != null) return fDisplayString;
      return fReplacementString;
    }

    /*
     * @see ICompletionProposal#getAdditionalProposalInfo()
     */
    public String getAdditionalProposalInfo() {
      return fAdditionalProposalInfo;
    }

    /*
     * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#apply(org.eclipse.jface.text.ITextViewer, char, int, int)
     */
    public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
      apply(viewer.getDocument());
    }

    /*
     * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#selected(org.eclipse.jface.text.ITextViewer, boolean)
     */
    public void selected(ITextViewer viewer, boolean smartToggle) {}

    /*
     * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#unselected(org.eclipse.jface.text.ITextViewer)
     */
    public void unselected(ITextViewer viewer) {}

    /*
     * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#validate(org.eclipse.jface.text.IDocument, int, org.eclipse.jface.text.DocumentEvent)
     */
    public boolean validate(IDocument document, int offset, DocumentEvent event) {
      try {
        String content = document.get(fReplacementOffset, fReplacementLength);
        if (content.startsWith(fReplacementString)) return true;
      } catch (BadLocationException e) {
        // ignore concurrently modified document
      }
      return false;
    }
  }

  private final Map<MultiVariable, Set<MultiVariable>> fDependencies =
      new HashMap<MultiVariable, Set<MultiVariable>>();
  private final Map<MultiVariable, MultiVariable> fBackwardDeps =
      new HashMap<MultiVariable, MultiVariable>();
  private final Map<MultiVariable, VariablePosition> fPositions =
      new HashMap<MultiVariable, VariablePosition>();

  public MultiVariableGuess() {}

  public ICompletionProposal[] getProposals(final MultiVariable variable, int offset, int length) {
    MultiVariable master = fBackwardDeps.get(variable);
    Object[] choices;
    if (master == null) choices = variable.getChoices();
    else choices = variable.getChoices(master.getCurrentChoice());

    if (choices == null) return null;

    if (fDependencies.containsKey(variable)) {
      ICompletionProposal[] ret = new ICompletionProposal[choices.length];
      for (int i = 0; i < ret.length; i++) {
        final Object choice = choices[i];
        ret[i] =
            new Proposal(variable.toString(choice), offset, length, offset + length) {
              @Override
              public void apply(IDocument document) {
                super.apply(document);
                Object oldChoice = variable.getCurrentChoice();
                variable.setCurrentChoice(choice);
                updateSlaves(variable, document, oldChoice);
              }
            };
      }

      return ret;

    } else {
      if (choices.length < 2) return null;

      ICompletionProposal[] ret = new ICompletionProposal[choices.length];
      for (int i = 0; i < ret.length; i++)
        ret[i] = new Proposal(variable.toString(choices[i]), offset, length, offset + length);

      return ret;
    }
  }

  private void updateSlaves(MultiVariable variable, IDocument document, Object oldChoice) {
    Object choice = variable.getCurrentChoice();
    if (!oldChoice.equals(choice)) {
      Set<MultiVariable> slaves = fDependencies.get(variable);
      for (Iterator<MultiVariable> it = slaves.iterator(); it.hasNext(); ) {
        MultiVariable slave = it.next();
        VariablePosition pos = fPositions.get(slave);

        Object slavesOldChoice = slave.getCurrentChoice();
        slave.setKey(choice); // resets the current choice
        try {
          document.replace(pos.getOffset(), pos.getLength(), slave.getDefaultValue());
        } catch (BadLocationException x) {
          // ignore and continue
        }
        // handle slaves recursively
        if (fDependencies.containsKey(slave)) updateSlaves(slave, document, slavesOldChoice);
      }
    }
  }

  /** @param position */
  public void addSlave(VariablePosition position) {
    fPositions.put(position.getVariable(), position);
  }

  /**
   * @param master
   * @param slave
   * @since 3.3
   */
  public void addDependency(MultiVariable master, MultiVariable slave) {
    // check for cycles and multi-slaves
    if (fBackwardDeps.containsKey(slave))
      throw new IllegalArgumentException("slave can only serve one master"); // $NON-NLS-1$
    Object parent = master;
    while (parent != null) {
      parent = fBackwardDeps.get(parent);
      if (parent == slave) throw new IllegalArgumentException("cycle detected"); // $NON-NLS-1$
    }

    Set<MultiVariable> slaves = fDependencies.get(master);
    if (slaves == null) {
      slaves = new HashSet<MultiVariable>();
      fDependencies.put(master, slaves);
    }
    fBackwardDeps.put(slave, master);
    slaves.add(slave);
  }
}
