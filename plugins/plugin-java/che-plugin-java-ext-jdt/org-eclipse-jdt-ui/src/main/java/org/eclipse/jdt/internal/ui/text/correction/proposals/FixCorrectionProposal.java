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

import org.eclipse.che.jface.text.ITextViewer;
import org.eclipse.che.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.internal.corext.fix.IProposableFix;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.fix.IMultiFix;
import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.jdt.internal.ui.text.correction.IStatusLineProposal;
import org.eclipse.jdt.ui.cleanup.ICleanUp;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.swt.graphics.Image;

/**
 * A correction proposal which uses an {@link ICleanUpFix} to fix a problem. A fix correction
 * proposal may have an {@link ICleanUp} attached which can be executed instead of the provided
 * IFix.
 */
public class FixCorrectionProposal extends LinkedCorrectionProposal
    implements ICompletionProposalExtension2, IStatusLineProposal {

  private final IProposableFix fFix;
  private final ICleanUp fCleanUp;
  private CompilationUnit fCompilationUnit;

  public FixCorrectionProposal(
      IProposableFix fix,
      ICleanUp cleanUp,
      int relevance,
      Image image,
      IInvocationContext context) {
    super(fix.getDisplayString(), context.getCompilationUnit(), null, relevance, image);
    fFix = fix;
    fCleanUp = cleanUp;
    fCompilationUnit = context.getASTRoot();
  }

  public ICleanUp getCleanUp() {
    return fCleanUp;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.ui.text.correction.ChangeCorrectionProposal#getImage()
   */
  @Override
  public Image getImage() {
    //		IStatus status = getFixStatus();
    //		if (status != null && !status.isOK()) {
    //			ImageImageDescriptor image = new ImageImageDescriptor(super.getImage());
    //
    //			int flag = JavaElementImageDescriptor.WARNING;
    //			if (status.getSeverity() == IStatus.ERROR) {
    //				flag = JavaElementImageDescriptor.ERROR;
    //			}
    //
    //			ImageDescriptor composite =
    //					new JavaElementImageDescriptor(image, flag, new Point(image.getImageData().width,
    // image.getImageData().height));
    //			return composite.createImage();
    //		} else {
    return super.getImage();
    //		}
  }

  public IStatus getFixStatus() {
    return fFix.getStatus();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.ui.text.correction.CUCorrectionProposal#getAdditionalProposalInfo()
   */
  @Override
  public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
    StringBuffer result = new StringBuffer();

    IStatus status = getFixStatus();
    if (status != null && !status.isOK()) {
      result.append("<b>"); // $NON-NLS-1$
      if (status.getSeverity() == IStatus.WARNING) {
        result.append(CorrectionMessages.FixCorrectionProposal_WarningAdditionalProposalInfo);
      } else if (status.getSeverity() == IStatus.ERROR) {
        result.append(CorrectionMessages.FixCorrectionProposal_ErrorAdditionalProposalInfo);
      }
      result.append("</b>"); // $NON-NLS-1$
      result.append(status.getMessage());
      result.append("<br><br>"); // $NON-NLS-1$
    }

    String info = fFix.getAdditionalProposalInfo();
    if (info != null) {
      result.append(info);
    } else {
      result.append(super.getAdditionalProposalInfo(monitor));
    }

    return result.toString();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.ui.text.correction.ChangeCorrectionProposal#getRelevance()
   */
  @Override
  public int getRelevance() {
    IStatus status = getFixStatus();
    if (status != null && !status.isOK()) {
      return super.getRelevance() - 100;
    } else {
      return super.getRelevance();
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.ui.text.correction.CUCorrectionProposal#createTextChange()
   */
  @Override
  protected TextChange createTextChange() throws CoreException {
    CompilationUnitChange createChange = fFix.createChange(null);
    createChange.setSaveMode(TextFileChange.LEAVE_DIRTY);

    //		if (fFix instanceof ILinkedFix) {
    //			setLinkedProposalModel(((ILinkedFix)fFix).getLinkedPositions());
    //		}

    return createChange;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#apply(org.eclipse.jface.text.ITextViewer, char, int, int)
   */
  public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
    //		if (stateMask == SWT.CTRL && fCleanUp != null) {
    //			CleanUpRefactoring refactoring= new CleanUpRefactoring();
    //			refactoring.addCompilationUnit(getCompilationUnit());
    //			refactoring.addCleanUp(fCleanUp);
    //			refactoring.setLeaveFilesDirty(true);
    //
    //			int stopSeverity= RefactoringCore.getConditionCheckingFailedSeverity();
    //			Shell shell= JavaPlugin.getActiveWorkbenchShell();
    //			IRunnableContext context= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    //			RefactoringExecutionHelper executer= new RefactoringExecutionHelper(refactoring,
    // stopSeverity, RefactoringSaveHelper.SAVE_NOTHING, shell, context);
    //			try {
    //				executer.perform(true, true);
    //			} catch (InterruptedException e) {
    //			} catch (InvocationTargetException e) {
    //				JavaPlugin.log(e);
    //			}
    //			return;
    //		}
    apply(viewer.getDocument());
  }

  //	public void resolve(MultiFixTarget[] targets, final IProgressMonitor monitor) throws
  // CoreException {
  //		if (targets.length == 0)
  //			return;
  //
  //		if (fCleanUp == null)
  //			return;
  //
  //		String changeName;
  //		String[] descriptions= fCleanUp.getStepDescriptions();
  //		if (descriptions.length == 1) {
  //			changeName= descriptions[0];
  //		} else {
  //			changeName= CorrectionMessages.FixCorrectionProposal_MultiFixChange_label;
  //		}
  //
  //		final CleanUpRefactoring refactoring= new CleanUpRefactoring(changeName);
  //		for (int i= 0; i < targets.length; i++) {
  //			refactoring.addCleanUpTarget(targets[i]);
  //		}
  //
  //		refactoring.addCleanUp(fCleanUp);
  //
  //		IRunnableContext context= new IRunnableContext() {
  //			public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws
  // InvocationTargetException, InterruptedException {
  //				runnable.run(monitor == null ? new NullProgressMonitor() : monitor);
  //			}
  //		};
  //
  //		Shell shell= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
  //		RefactoringExecutionHelper helper= new RefactoringExecutionHelper(refactoring, IStatus.INFO,
  // RefactoringSaveHelper.SAVE_REFACTORING, shell, context);
  //		try {
  //			helper.perform(true, true);
  //		} catch (InterruptedException e) {
  //		} catch (InvocationTargetException e) {
  //			Throwable cause= e.getCause();
  //			if (cause instanceof CoreException) {
  //				throw (CoreException)cause;
  //			} else {
  //				throw new CoreException(new Status(IStatus.ERROR, JavaUI.ID_PLUGIN,
  // cause.getLocalizedMessage(), cause));
  //			}
  //		}
  //	}

  public void selected(ITextViewer viewer, boolean smartToggle) {}

  public void unselected(ITextViewer viewer) {}

  public boolean validate(IDocument document, int offset, DocumentEvent event) {
    return false;
  }

  /** {@inheritDoc} */
  public String getStatusMessage() {
    if (fCleanUp == null) return null;

    int count = computeNumberOfFixesForCleanUp(fCleanUp);

    if (count == -1) {
      return CorrectionMessages.FixCorrectionProposal_HitCtrlEnter_description;
    } else if (count < 2) {
      return null;
    } else {
      return Messages.format(
          CorrectionMessages.FixCorrectionProposal_hitCtrlEnter_variable_description,
          new Integer(count));
    }
  }

  /**
   * Compute the number of problems that can be fixed by the clean up in a compilation unit.
   *
   * @param cleanUp the clean up
   * @return the maximum number of fixes or -1 if unknown
   * @since 3.6
   */
  public int computeNumberOfFixesForCleanUp(ICleanUp cleanUp) {
    return cleanUp instanceof IMultiFix
        ? ((IMultiFix) cleanUp).computeNumberOfFixes(fCompilationUnit)
        : -1;
  }
}
