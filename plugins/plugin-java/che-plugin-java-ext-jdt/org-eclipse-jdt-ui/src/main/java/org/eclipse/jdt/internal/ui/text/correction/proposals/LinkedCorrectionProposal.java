/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2012 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction.proposals;

import static org.eclipse.che.plugin.java.server.dto.DtoServerImpls.LinkedDataImpl;
import static org.eclipse.che.plugin.java.server.dto.DtoServerImpls.LinkedModeModelImpl;
import static org.eclipse.che.plugin.java.server.dto.DtoServerImpls.LinkedPositionGroupImpl;
import static org.eclipse.che.plugin.java.server.dto.DtoServerImpls.RegionImpl;

import java.util.Iterator;
import org.eclipse.che.ide.ext.java.shared.dto.LinkedModeModel;
import org.eclipse.che.jdt.javaeditor.HasLinkedModel;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalModel;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;

/**
 * A proposal for quick fixes and quick assists that works on a AST rewriter and enters the linked
 * mode when the proposal is set up. Either a rewriter is directly passed in the constructor or
 * method {@link #getRewrite()} is overridden to provide the AST rewriter that is evaluated to the
 * document when the proposal is applied.
 *
 * @since 3.0
 */
public class LinkedCorrectionProposal extends ASTRewriteCorrectionProposal
    implements HasLinkedModel {

  private LinkedProposalModel fLinkedProposalModel;
  private LinkedModeModelImpl linkedModel;

  /**
   * Constructs a linked correction proposal.
   *
   * @param name The display name of the proposal.
   * @param cu The compilation unit that is modified.
   * @param rewrite The AST rewrite that is invoked when the proposal is applied <code>null</code>
   *     can be passed if {@link #getRewrite()} is overridden.
   * @param relevance The relevance of this proposal.
   * @param image The image that is displayed for this proposal or <code>null</code> if no image is
   *     desired.
   */
  public LinkedCorrectionProposal(
      String name, ICompilationUnit cu, ASTRewrite rewrite, int relevance, Image image) {
    super(name, cu, rewrite, relevance, image);
    fLinkedProposalModel = null;
  }

  protected LinkedProposalModel getLinkedProposalModel() {
    if (fLinkedProposalModel == null) {
      fLinkedProposalModel = new LinkedProposalModel();
    }
    return fLinkedProposalModel;
  }

  public void setLinkedProposalModel(LinkedProposalModel model) {
    fLinkedProposalModel = model;
  }

  /**
   * Adds a linked position to be shown when the proposal is applied. All positions with the same
   * group id are linked.
   *
   * @param position The position to add.
   * @param isFirst If set, the proposal is jumped to first.
   * @param groupID The id of the group the proposal belongs to. All proposals in the same group are
   *     linked.
   */
  public void addLinkedPosition(ITrackedNodePosition position, boolean isFirst, String groupID) {
    getLinkedProposalModel().getPositionGroup(groupID, true).addPosition(position, isFirst);
  }

  /**
   * Adds a linked position to be shown when the proposal is applied. All positions with the same
   * group id are linked.
   *
   * @param position The position to add.
   * @param sequenceRank The sequence rank, see TODO.
   * @param groupID The id of the group the proposal belongs to. All proposals in the same group are
   *     linked.
   */
  public void addLinkedPosition(ITrackedNodePosition position, int sequenceRank, String groupID) {
    getLinkedProposalModel().getPositionGroup(groupID, true).addPosition(position, sequenceRank);
  }

  /**
   * Sets the end position of the linked mode to the end of the passed range.
   *
   * @param position The position that describes the end position of the linked mode.
   */
  public void setEndPosition(ITrackedNodePosition position) {
    getLinkedProposalModel().setEndPosition(position);
  }

  /**
   * Adds a linked position proposal to the group with the given id.
   *
   * @param groupID The id of the group that should present the proposal
   * @param proposal The string to propose.
   * @param image The image to show for the position proposal or <code>null</code> if no image is
   *     desired.
   */
  public void addLinkedPositionProposal(String groupID, String proposal, Image image) {
    getLinkedProposalModel().getPositionGroup(groupID, true).addProposal(proposal, image, 10);
  }

  /**
   * Adds a linked position proposal to the group with the given id.
   *
   * @param groupID The id of the group that should present the proposal
   * @param displayString The name of the proposal
   * @param proposal The string to insert.
   * @param image The image to show for the position proposal or <code>null</code> if no image is
   *     desired.
   * @deprecated use {@link #addLinkedPositionProposal(String, String, Image)} instead
   */
  public void addLinkedPositionProposal(
      String groupID, String displayString, String proposal, Image image) {
    addLinkedPositionProposal(groupID, proposal, image);
  }

  /**
   * Adds a linked position proposal to the group with the given id.
   *
   * @param groupID The id of the group that should present the proposal
   * @param type The binding to use as type name proposal.
   */
  public void addLinkedPositionProposal(String groupID, ITypeBinding type) {
    getLinkedProposalModel()
        .getPositionGroup(groupID, true)
        .addProposal(type, getCompilationUnit(), 10);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.ui.text.correction.ChangeCorrectionProposal#performChange(org.eclipse.jface.text.IDocument, org.eclipse.ui.IEditorPart)
   */
  @Override
  protected void performChange(/*IEditorPart part,*/ IDocument document) throws CoreException {
    //		try {
    super.performChange(/*part,*/ document);
    //			if (part == null) {
    //				return;
    //			}
    //
    if (fLinkedProposalModel != null) {
      if (fLinkedProposalModel.hasLinkedPositions()) {
        // enter linked mode
        //					ITextViewer viewer= ((JavaEditor) part).getViewer();
        //					new LinkedProposalModelPresenter().enterLinkedMode(viewer, part, didOpenEditor(),
        // fLinkedProposalModel);
        boolean added = false;
        LinkedModeModelImpl model = new LinkedModeModelImpl();
        Iterator<LinkedProposalPositionGroup> iterator =
            fLinkedProposalModel.getPositionGroupIterator();
        while (iterator.hasNext()) {
          LinkedProposalPositionGroup curr = iterator.next();

          LinkedPositionGroupImpl group = new LinkedPositionGroupImpl();

          LinkedProposalPositionGroup.PositionInformation[] positions = curr.getPositions();
          if (positions.length > 0) {
            LinkedProposalPositionGroup.Proposal[] linkedModeProposals = curr.getProposals();
            if (linkedModeProposals.length <= 1) {
              for (int i = 0; i < positions.length; i++) {
                LinkedProposalPositionGroup.PositionInformation pos = positions[i];
                if (pos.getOffset() != -1) {
                  RegionImpl position = new RegionImpl();
                  position.setOffset(pos.getOffset());
                  position.setLength(pos.getLength());
                  //                                        group.addPositions(
                  //                                                new LinkedPosition(document,
                  // pos.getOffset(), pos.getLength(), pos.getSequenceRank()));
                  group.addPositions(position);
                }
              }
            } else {
              //                                LinkedPositionProposalImpl[] proposalImpls= new
              // LinkedPositionProposalImpl[linkedModeProposals.length];
              LinkedDataImpl data = new LinkedDataImpl();
              for (int i = 0; i < linkedModeProposals.length; i++) {
                //                                    proposalImpls[i] = new
                // LinkedPositionProposalImpl(linkedModeProposals[i], model);
                data.addValues(linkedModeProposals[i].getDisplayString());
              }
              group.setData(data);
              for (int i = 0; i < positions.length; i++) {
                LinkedProposalPositionGroup.PositionInformation pos = positions[i];
                if (pos.getOffset() != -1) {
                  //                                        group.addPosition(
                  //                                                new ProposalPosition(document,
                  // pos.getOffset(), pos.getLength(), pos.getSequenceRank(),
                  //
                  // proposalImpls));
                  RegionImpl position = new RegionImpl();
                  position.setOffset(pos.getOffset());
                  position.setLength(pos.getLength());
                  group.addPositions(position);
                }
              }
            }
            model.addGroups(group);
            added = true;
          }
        }
        if (added) {
          LinkedProposalPositionGroup.PositionInformation endPosition =
              fLinkedProposalModel.getEndPosition();
          if (endPosition != null && endPosition.getOffset() != -1) {
            model.setEscapePosition(endPosition.getOffset() + endPosition.getLength());
          }
          this.linkedModel = model;
        }
      }
      //				else if (part instanceof ITextEditor) {
      //					LinkedProposalPositionGroup.PositionInformation endPosition=
      // fLinkedProposalModel.getEndPosition();
      //					if (endPosition != null) {
      //						// select a result
      //						int pos= endPosition.getOffset() + endPosition.getLength();
      //						((ITextEditor) part).selectAndReveal(pos, 0);
      //					}
      //				}
    }
    //		} catch (BadLocationException e) {
    //			throw new CoreException(JavaUIStatus.createError(IStatus.ERROR, e));
    //		}
  }

  @Override
  public LinkedModeModel getLinkedModel() {
    return linkedModel;
  }
}
