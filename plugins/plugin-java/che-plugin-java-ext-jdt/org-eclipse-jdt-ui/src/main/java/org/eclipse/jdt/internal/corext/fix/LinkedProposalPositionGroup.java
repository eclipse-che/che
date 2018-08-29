/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.fix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.viewsupport.BindingLabelProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

public class LinkedProposalPositionGroup {

  /**
   * {@link LinkedProposalPositionGroup.PositionInformation} describes a position insinde a position
   * group. The information provided must be accurate after the document change to the proposal has
   * been performed, but doesn't need to reflect the changed done by the linking mode.
   */
  public abstract static class PositionInformation {
    public abstract int getOffset();

    public abstract int getLength();

    public abstract int getSequenceRank();
  }

  public static class Proposal {

    private String fDisplayString;
    private Image fImage;
    private int fRelevance;

    public Proposal(String displayString, Image image, int relevance) {
      fDisplayString = displayString;
      fImage = image;
      fRelevance = relevance;
    }

    public String getDisplayString() {
      return fDisplayString;
    }

    public Image getImage() {
      return fImage;
    }

    public int getRelevance() {
      return fRelevance;
    }

    public void setImage(Image image) {
      fImage = image;
    }

    public String getAdditionalProposalInfo() {
      return null;
    }

    public TextEdit computeEdits(
        int offset, LinkedPosition position, char trigger, int stateMask, LinkedModeModel model)
        throws CoreException {
      return new ReplaceEdit(position.getOffset(), position.getLength(), fDisplayString);
    }
  }

  public static PositionInformation createPositionInformation(
      ITrackedNodePosition pos, int sequenceRank) {
    return new TrackedNodePosition(pos, sequenceRank);
  }

  private static class TrackedNodePosition extends PositionInformation {

    private final ITrackedNodePosition fPos;
    private final int fSequenceRank;

    public TrackedNodePosition(ITrackedNodePosition pos, int sequenceRank) {
      fPos = pos;
      fSequenceRank = sequenceRank;
    }

    @Override
    public int getOffset() {
      return fPos.getStartPosition();
    }

    @Override
    public int getLength() {
      return fPos.getLength();
    }

    @Override
    public int getSequenceRank() {
      return fSequenceRank;
    }
  }

  /**
   * A position that contains all of the given tracked node positions.
   *
   * @since 3.7
   */
  public static class TrackedNodesPosition extends PositionInformation {

    private final Collection<ITrackedNodePosition> fPos;

    /**
     * A position that contains all of the given tracked node positions.
     *
     * @param pos the positions
     */
    public TrackedNodesPosition(Collection<ITrackedNodePosition> pos) {
      fPos = pos;
    }

    @Override
    public int getOffset() {
      int minStart = Integer.MAX_VALUE;
      for (ITrackedNodePosition node : fPos) {
        minStart = Math.min(minStart, node.getStartPosition());
      }
      return minStart == Integer.MAX_VALUE ? -1 : minStart;
    }

    @Override
    public int getLength() {
      int minStart = Integer.MAX_VALUE;
      int maxEnd = 0;
      for (ITrackedNodePosition node : fPos) {
        minStart = Math.min(minStart, node.getStartPosition());
        maxEnd = Math.max(maxEnd, node.getStartPosition() + node.getLength());
      }
      return minStart == Integer.MAX_VALUE ? 0 : maxEnd - getOffset();
    }

    @Override
    public int getSequenceRank() {
      return 0;
    }
  }

  /**
   * A position for the start of the given tracked node position.
   *
   * @since 3.7
   */
  public static class StartPositionInformation extends PositionInformation {

    private ITrackedNodePosition fPos;

    /**
     * A position for the start of the given tracked node position.
     *
     * @param pos the position
     */
    public StartPositionInformation(ITrackedNodePosition pos) {
      fPos = pos;
    }

    @Override
    public int getOffset() {
      return fPos.getStartPosition();
    }

    @Override
    public int getLength() {
      return 0;
    }

    @Override
    public int getSequenceRank() {
      return 0;
    }
  }

  private static final class JavaLinkedModeProposal extends Proposal {
    private final ITypeBinding fTypeProposal;
    private final ICompilationUnit fCompilationUnit;

    public JavaLinkedModeProposal(ICompilationUnit unit, ITypeBinding typeProposal, int relevance) {
      super(
          BindingLabelProvider.getBindingLabel(
              typeProposal, JavaElementLabels.ALL_DEFAULT | JavaElementLabels.ALL_POST_QUALIFIED),
          null,
          relevance);
      fTypeProposal = typeProposal;
      fCompilationUnit = unit;
      ImageDescriptor desc =
          BindingLabelProvider.getBindingImageDescriptor(
              fTypeProposal, BindingLabelProvider.DEFAULT_IMAGEFLAGS);
      if (desc != null) {
        setImage(JavaPlugin.getImageDescriptorRegistry().get(desc));
      }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.corext.fix.PositionGroup.Proposal#computeEdits(int, org.eclipse.jface.text.link.LinkedPosition,
     * char, int, org.eclipse.jface.text.link.LinkedModeModel)
     */
    @Override
    public TextEdit computeEdits(
        int offset, LinkedPosition position, char trigger, int stateMask, LinkedModeModel model)
        throws CoreException {
      ImportRewrite impRewrite = StubUtility.createImportRewrite(fCompilationUnit, true);
      String replaceString = impRewrite.addImport(fTypeProposal);

      MultiTextEdit composedEdit = new MultiTextEdit();
      composedEdit.addChild(
          new ReplaceEdit(position.getOffset(), position.getLength(), replaceString));
      composedEdit.addChild(impRewrite.rewriteImports(null));
      return composedEdit;
    }
  }

  private final String fGroupId;
  private final List<PositionInformation> fPositions;
  private final List<Proposal> fProposals;

  public LinkedProposalPositionGroup(String groupID) {
    fGroupId = groupID;
    fPositions = new ArrayList<PositionInformation>();
    fProposals = new ArrayList<Proposal>();
  }

  public void addPosition(PositionInformation position) {
    fPositions.add(position);
  }

  public void addPosition(ITrackedNodePosition position, int sequenceRank) {
    addPosition(createPositionInformation(position, sequenceRank));
  }

  public void addPosition(ITrackedNodePosition position, boolean isFirst) {
    addPosition(position, isFirst ? 0 : 1);
  }

  public void addProposal(Proposal proposal) {
    fProposals.add(proposal);
  }

  public void addProposal(String displayString, Image image, int relevance) {
    addProposal(new Proposal(displayString, image, relevance));
  }

  public void addProposal(ITypeBinding type, ICompilationUnit cu, int relevance) {
    addProposal(new JavaLinkedModeProposal(cu, type, relevance));
  }

  public String getGroupId() {
    return fGroupId;
  }

  public PositionInformation[] getPositions() {
    return fPositions.toArray(new PositionInformation[fPositions.size()]);
  }

  public Proposal[] getProposals() {
    return fProposals.toArray(new Proposal[fProposals.size()]);
  }
}
