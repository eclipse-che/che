/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction.proposals;

import static org.eclipse.che.plugin.java.server.dto.DtoServerImpls.LinkedModeModelImpl;
import static org.eclipse.che.plugin.java.server.dto.DtoServerImpls.LinkedPositionGroupImpl;
import static org.eclipse.che.plugin.java.server.dto.DtoServerImpls.RegionImpl;

import java.util.Arrays;
import java.util.Comparator;
import org.eclipse.che.jdt.javaeditor.HasLinkedModel;
import org.eclipse.che.jface.text.ITextViewer;
import org.eclipse.che.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.che.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.che.jface.text.contentassist.IContextInformation;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.corext.dom.LinkedNodeFinder;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.jdt.internal.ui.text.correction.IProposalRelevance;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.correction.ICommandAccess;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/** A template proposal. */
public class LinkedNamesAssistProposal
    implements IJavaCompletionProposal,
        ICompletionProposalExtension2,
        ICompletionProposalExtension6,
        ICommandAccess,
        HasLinkedModel {

  //	/**
  //	 * An exit policy that skips Backspace and Delete at the beginning and at the end
  //	 * of a linked position, respectively.
  //	 *
  //	 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=183925 .
  //	 */
  //	public static class DeleteBlockingExitPolicy implements IExitPolicy {
  //		private IDocument fDocument;
  //
  //		public DeleteBlockingExitPolicy(IDocument document) {
  //			fDocument = document;
  //		}
  //
  //		public ExitFlags doExit(LinkedModeModel model, VerifyEvent event, int offset, int length) {
  //			if (length == 0 && (event.character == SWT.BS || event.character == SWT.DEL)) {
  //				LinkedPosition position = model.findPosition(new LinkedPosition(fDocument, offset, 0,
  // LinkedPositionGroup.NO_STOP));
  //				if (position != null) {
  //					if (event.character == SWT.BS) {
  //						if (offset - 1 < position.getOffset()) {
  //							//skip backspace at beginning of linked position
  //							event.doit = false;
  //						}
  //					} else /* event.character == SWT.DEL */ {
  //						if (offset + 1 > position.getOffset() + position.getLength()) {
  //							//skip delete at end of linked position
  //							event.doit = false;
  //						}
  //					}
  //				}
  //			}
  //
  //			return null; // don't change behavior
  //		}
  //	}

  public static final String ASSIST_ID =
      "org.eclipse.jdt.ui.correction.renameInFile.assist"; // $NON-NLS-1$

  private SimpleName fNode;
  private IInvocationContext fContext;
  private String fLabel;
  private String fValueSuggestion;
  private int fRelevance;
  private LinkedModeModelImpl linkedModel;

  public LinkedNamesAssistProposal(IInvocationContext context, SimpleName node) {
    this(CorrectionMessages.LinkedNamesAssistProposal_description, context, node, null);
  }

  public LinkedNamesAssistProposal(
      String label, IInvocationContext context, SimpleName node, String valueSuggestion) {
    fLabel = label;
    fNode = node;
    fContext = context;
    fValueSuggestion = valueSuggestion;
    fRelevance = IProposalRelevance.LINKED_NAMES_ASSIST;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#apply(org.eclipse.jface.text.ITextViewer, char, int, int)
   */
  public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
    try {
      Point seletion = viewer.getSelectedRange();

      // get full ast
      CompilationUnit root =
          SharedASTProvider.getAST(fContext.getCompilationUnit(), SharedASTProvider.WAIT_YES, null);

      ASTNode nameNode = NodeFinder.perform(root, fNode.getStartPosition(), fNode.getLength());
      final int pos = fNode.getStartPosition();

      ASTNode[] sameNodes;
      if (nameNode instanceof SimpleName) {
        sameNodes = LinkedNodeFinder.findByNode(root, (SimpleName) nameNode);
      } else {
        sameNodes = new ASTNode[] {nameNode};
      }

      // sort for iteration order, starting with the node @ offset
      Arrays.sort(
          sameNodes,
          new Comparator<ASTNode>() {

            public int compare(ASTNode o1, ASTNode o2) {
              return rank(o1) - rank(o2);
            }

            /**
             * Returns the absolute rank of an <code>ASTNode</code>. Nodes preceding <code>offset
             * </code> are ranked last.
             *
             * @param node the node to compute the rank for
             * @return the rank of the node with respect to the invocation offset
             */
            private int rank(ASTNode node) {
              int relativeRank = node.getStartPosition() + node.getLength() - pos;
              if (relativeRank < 0) return Integer.MAX_VALUE + relativeRank;
              else return relativeRank;
            }
          });

      IDocument document = viewer.getDocument();
      LinkedPositionGroupImpl group = new LinkedPositionGroupImpl();
      for (int i = 0; i < sameNodes.length; i++) {
        ASTNode elem = sameNodes[i];
        RegionImpl region = new RegionImpl();
        region.setOffset(elem.getStartPosition());
        region.setLength(elem.getLength());
        group.addPositions(region);
        //				group.addPosition(new LinkedPosition(document, elem.getStartPosition(),
        // elem.getLength(), i));
      }

      LinkedModeModelImpl model = new LinkedModeModelImpl();
      model.addGroups(group);
      //			model.forceInstall();
      model.setEscapePosition(offset);
      this.linkedModel = model;
      if (fContext instanceof AssistContext) {
        //				IEditorPart editor = ((AssistContext)fContext).getEditor();
        //				if (editor instanceof JavaEditor) {
        //					model.addLinkingListener(new EditorHighlightingSynchronizer((JavaEditor)editor));
        //				}
      }

      //			LinkedModeUI ui = new EditorLinkedModeUI(model, viewer);
      //			ui.setExitPolicy(new DeleteBlockingExitPolicy(document));
      //			ui.setExitPosition(viewer, offset, 0, LinkedPositionGroup.NO_STOP);
      //			ui.enter();

      if (fValueSuggestion != null) {
        document.replace(nameNode.getStartPosition(), nameNode.getLength(), fValueSuggestion);
        //				IRegion selectedRegion = ui.getSelectedRegion();
        //				seletion = new Point(selectedRegion.getOffset(), fValueSuggestion.length());
      }

      //			viewer.setSelectedRange(seletion.x, seletion.y); // by default full word is selected,
      // restore original selection

    } catch (BadLocationException e) {
      JavaPlugin.log(e);
    }
  }

  /*
   * @see ICompletionProposal#apply(IDocument)
   */
  public void apply(IDocument document) {
    // can't do anything
  }

  /*
   * @see ICompletionProposal#getSelection(IDocument)
   */
  public Point getSelection(IDocument document) {
    return null;
  }

  /*
   * @see ICompletionProposal#getAdditionalProposalInfo()
   */
  public String getAdditionalProposalInfo() {
    return CorrectionMessages.LinkedNamesAssistProposal_proposalinfo;
  }

  /*
   * @see ICompletionProposal#getDisplayString()
   */
  public String getDisplayString() {
    //		String shortCutString = CorrectionCommandHandler.getShortCutString(getCommandId());
    //		if (shortCutString != null) {
    //			return Messages.format(CorrectionMessages.ChangeCorrectionProposal_name_with_shortcut, new
    // String[]{fLabel, shortCutString});
    //		}
    return fLabel;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension6#getStyledDisplayString()
   */
  public StyledString getStyledDisplayString() {
    StyledString str = new StyledString(fLabel);

    //		String shortCutString = CorrectionCommandHandler.getShortCutString(getCommandId());
    //		if (shortCutString != null) {
    //			String decorated= Messages
    //					.format(CorrectionMessages.ChangeCorrectionProposal_name_with_shortcut, new
    // String[]{fLabel, shortCutString});
    //			return StyledCellLabelProvider.styleDecoratedString(decorated,
    // StyledString.QUALIFIER_STYLER, str);
    //		}
    return str;
  }

  /*
   * @see ICompletionProposal#getImage()
   */
  public Image getImage() {
    return JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_LINKED_RENAME);
  }

  /*
   * @see ICompletionProposal#getContextInformation()
   */
  public IContextInformation getContextInformation() {
    return null;
  }

  /*
   * @see IJavaCompletionProposal#getRelevance()
   */
  public int getRelevance() {
    return fRelevance;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#selected(org.eclipse.jface.text.ITextViewer, boolean)
   */
  public void selected(ITextViewer textViewer, boolean smartToggle) {}

  /* (non-Javadoc)
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#unselected(org.eclipse.jface.text.ITextViewer)
   */
  public void unselected(ITextViewer textViewer) {}

  /* (non-Javadoc)
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#validate(org.eclipse.jface.text.IDocument, int, org
   * .eclipse.jface.text.DocumentEvent)
   */
  public boolean validate(IDocument document, int offset, DocumentEvent event) {
    return false;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.ui.text.correction.IShortcutProposal#getProposalId()
   */
  public String getCommandId() {
    return ASSIST_ID;
  }

  public void setRelevance(int relevance) {
    fRelevance = relevance;
  }

  @Override
  public org.eclipse.che.ide.ext.java.shared.dto.LinkedModeModel getLinkedModel() {
    return linkedModel;
  }
}
