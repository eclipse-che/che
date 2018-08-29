/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2012 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.che.ide.ext.java.shared.dto.Problem;
import org.eclipse.che.jface.text.contentassist.ICompletionProposal;
import org.eclipse.che.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;
import org.eclipse.jface.text.source.Annotation;

public class JavaCorrectionProcessor
    implements org.eclipse.che.jface.text.quickassist.IQuickAssistProcessor {

  private static final String QUICKFIX_PROCESSOR_CONTRIBUTION_ID =
      "quickFixProcessors"; // $NON-NLS-1$
  private static final String QUICKASSIST_PROCESSOR_CONTRIBUTION_ID =
      "quickAssistProcessors"; // $NON-NLS-1$

  private static ContributedProcessorDescriptor[] fgContributedAssistProcessors = null;
  private static ContributedProcessorDescriptor[] fgContributedCorrectionProcessors = null;

  private static ContributedProcessorDescriptor[] getProcessorDescriptors(
      String contributionId, boolean testMarkerTypes) {
    //		IConfigurationElement[] elements=
    // Platform.getExtensionRegistry().getConfigurationElementsFor(JavaPlugin.ID_PLUGIN,
    // contributionId);
    ArrayList<ContributedProcessorDescriptor> res = new ArrayList<>();
    //
    //		for (int i= 0; i < elements.length; i++) {
    //			ContributedProcessorDescriptor desc= new ContributedProcessorDescriptor(elements[i],
    // testMarkerTypes);
    //			IStatus status= desc.checkSyntax();
    //			if (status.isOK()) {
    //				res.add(desc);
    //			} else {
    //				JavaPlugin.log(status);
    //			}
    //		}

    if (contributionId == QUICKFIX_PROCESSOR_CONTRIBUTION_ID) {
      res.add(new ContributedProcessorDescriptor(new QuickFixProcessor(), testMarkerTypes));
    } else {
      res.add(new ContributedProcessorDescriptor(new QuickAssistProcessor(), testMarkerTypes));
      res.add(
          new ContributedProcessorDescriptor(new AdvancedQuickAssistProcessor(), testMarkerTypes));
    }
    return res.toArray(new ContributedProcessorDescriptor[res.size()]);
  }

  private static ContributedProcessorDescriptor[] getCorrectionProcessors() {
    if (fgContributedCorrectionProcessors == null) {
      fgContributedCorrectionProcessors =
          getProcessorDescriptors(QUICKFIX_PROCESSOR_CONTRIBUTION_ID, true);
    }
    return fgContributedCorrectionProcessors;
  }

  private static ContributedProcessorDescriptor[] getAssistProcessors() {
    if (fgContributedAssistProcessors == null) {
      fgContributedAssistProcessors =
          getProcessorDescriptors(QUICKASSIST_PROCESSOR_CONTRIBUTION_ID, false);
    }
    return fgContributedAssistProcessors;
  }

  public static boolean hasCorrections(ICompilationUnit cu, int problemId, String markerType) {
    ContributedProcessorDescriptor[] processors = getCorrectionProcessors();
    SafeHasCorrections collector = new SafeHasCorrections(cu, problemId);
    for (int i = 0; i < processors.length; i++) {
      if (processors[i].canHandleMarkerType(markerType)) {
        collector.process(processors[i]);
        if (collector.hasCorrections()) {
          return true;
        }
      }
    }
    return false;
  }

  //	public static boolean isQuickFixableType(Annotation annotation) {
  //		return (annotation instanceof IJavaAnnotation || annotation instanceof SimpleMarkerAnnotation)
  // && !annotation.isMarkedDeleted();
  //	}

  public static boolean hasCorrections(Annotation annotation) {
    //		if (annotation instanceof IJavaAnnotation) {
    //			IJavaAnnotation javaAnnotation= (IJavaAnnotation) annotation;
    //			int problemId= javaAnnotation.getId();
    //			if (problemId != -1) {
    //				ICompilationUnit cu= javaAnnotation.getCompilationUnit();
    //				if (cu != null) {
    //					return hasCorrections(cu, problemId, javaAnnotation.getMarkerType());
    //				}
    //			}
    //		}
    //		if (annotation instanceof SimpleMarkerAnnotation) {
    //			return hasCorrections(((SimpleMarkerAnnotation) annotation).getMarker());
    //		}
    //		return false;
    throw new UnsupportedOperationException("hasCorrections");
  }

  //	private static boolean hasCorrections(IMarker marker) {
  //		if (marker == null || !marker.exists())
  //			return false;
  //
  //		IMarkerHelpRegistry registry= IDE.getMarkerHelpRegistry();
  //		return registry != null && registry.hasResolutions(marker);
  //	}

  public static boolean hasAssists(IInvocationContext context) {
    ContributedProcessorDescriptor[] processors = getAssistProcessors();
    SafeHasAssist collector = new SafeHasAssist(context);

    for (int i = 0; i < processors.length; i++) {
      collector.process(processors[i]);
      if (collector.hasAssists()) {
        return true;
      }
    }
    return false;
  }

  //	private JavaCorrectionAssistant fAssistant;
  private String fErrorMessage;

  /*
   * Constructor for JavaCorrectionProcessor.
   */
  public JavaCorrectionProcessor(/*JavaCorrectionAssistant assistant*/ ) {
    //		fAssistant= assistant;
    //		fAssistant.addCompletionListener(new ICompletionListener() {
    //
    //			public void assistSessionEnded(ContentAssistEvent event) {
    //				fAssistant.setStatusLineVisible(false);
    //			}
    //
    //			public void assistSessionStarted(ContentAssistEvent event) {
    //				fAssistant.setStatusLineVisible(true);
    //				fAssistant.setStatusMessage(getJumpHintStatusLineMessage());
    //			}
    //
    //			public void selectionChanged(ICompletionProposal proposal, boolean smartToggle) {
    //				if (proposal instanceof IStatusLineProposal) {
    //					IStatusLineProposal statusLineProposal= (IStatusLineProposal)proposal;
    //					String message= statusLineProposal.getStatusMessage();
    //					if (message != null) {
    //						fAssistant.setStatusMessage(message);
    //						return;
    //					}
    //				}
    //				fAssistant.setStatusMessage(getJumpHintStatusLineMessage());
    //			}
    //
    //			private String getJumpHintStatusLineMessage() {
    //				if (fAssistant.isUpdatedOffset()) {
    //					String key= getQuickAssistBinding();
    //					if (key == null)
    //						return CorrectionMessages.JavaCorrectionProcessor_go_to_original_using_menu;
    //					else
    //						return
    // Messages.format(CorrectionMessages.JavaCorrectionProcessor_go_to_original_using_key, key);
    //				} else if (fAssistant.isProblemLocationAvailable()) {
    //					String key= getQuickAssistBinding();
    //					if (key == null)
    //						return CorrectionMessages.JavaCorrectionProcessor_go_to_closest_using_menu;
    //					else
    //						return
    // Messages.format(CorrectionMessages.JavaCorrectionProcessor_go_to_closest_using_key, key);
    //				} else
    //					return ""; //$NON-NLS-1$
    //			}
    //
    //			private String getQuickAssistBinding() {
    //				final IBindingService bindingSvc= (IBindingService)
    // PlatformUI.getWorkbench().getAdapter(IBindingService.class);
    //				return
    // bindingSvc.getBestActiveBindingFormattedFor(ITextEditorActionDefinitionIds.QUICK_ASSIST);
    //			}
    //		});
  }

  /*
   * @see IContentAssistProcessor#computeCompletionProposals(ITextViewer, int)
   */
  public ICompletionProposal[] computeQuickAssistProposals(
      IQuickAssistInvocationContext quickAssistContext) {
    //		ISourceViewer viewer= quickAssistContext.getSourceViewer();
    //		int documentOffset= quickAssistContext.getOffset();
    //
    ////		IEditorPart part= fAssistant.getEditor();
    //
    //		ICompilationUnit cu= JavaUI.getWorkingCopyManager().getWorkingCopy(part.getEditorInput());
    //		IAnnotationModel model=
    // JavaUI.getDocumentProvider().getAnnotationModel(part.getEditorInput());
    //
    //		AssistContext context= null;
    //		if (cu != null) {
    //			int length= viewer != null ? viewer.getSelectedRange().y : 0;
    //			context= new AssistContext(cu, viewer, /*part, */documentOffset, length);
    //		}
    //
    //		Annotation[] annotations= fAssistant.getAnnotationsAtOffset();
    //
    //		fErrorMessage= null;
    //
    //		ICompletionProposal[] res= null;
    //		if (model != null && context != null && annotations != null) {
    //			ArrayList<IJavaCompletionProposal> proposals= new ArrayList<IJavaCompletionProposal>(10);
    //			IStatus status= collectProposals(context, model, annotations, true,
    // !fAssistant.isUpdatedOffset(), proposals);
    //			res= proposals.toArray(new ICompletionProposal[proposals.size()]);
    //			if (!status.isOK()) {
    //				fErrorMessage= status.getMessage();
    //				JavaPlugin.log(status);
    //			}
    //		}
    //
    //		if (res == null || res.length == 0) {
    //			return new ICompletionProposal[] { new
    // ChangeCorrectionProposal(CorrectionMessages.NoCorrectionProposal_description, new
    // NullChange(""), IProposalRelevance.NO_SUGGESSTIONS_AVAILABLE, null) }; //$NON-NLS-1$
    //		}
    //		if (res.length > 1) {
    //			Arrays.sort(res, new CompletionProposalComparator());
    //		}
    //		return res;
    throw new UnsupportedOperationException("computeQuickAssistProposals");
  }

  public static IStatus collectProposals(
      IInvocationContext context, /*IAnnotationModel model, */
      List<Problem> annotations,
      boolean addQuickFixes,
      boolean addQuickAssists,
      Collection<IJavaCompletionProposal> proposals) {
    ArrayList<ProblemLocation> problems = new ArrayList<>();

    // collect problem locations and corrections from marker annotations
    for (Problem curr : annotations) {
      problems.add(new ProblemLocation(curr));
    }
    //		for (int i= 0; i < annotations.length; i++) {
    //			Annotation curr= annotations[i];
    //			ProblemLocation problemLocation= null;
    //			if (curr instanceof IJavaAnnotation) {
    //				problemLocation= getProblemLocation((IJavaAnnotation) curr, model);
    //				if (problemLocation != null) {
    //					problems.add(problemLocation);
    //				}
    //			}
    ////			if (problemLocation == null && addQuickFixes && curr instanceof SimpleMarkerAnnotation) {
    ////				collectMarkerProposals((SimpleMarkerAnnotation) curr, proposals);
    ////			}
    //		}
    MultiStatus resStatus = null;

    IProblemLocation[] problemLocations = problems.toArray(new IProblemLocation[problems.size()]);
    if (addQuickFixes) {
      IStatus status = collectCorrections(context, problemLocations, proposals);
      if (!status.isOK()) {
        resStatus =
            new MultiStatus(
                JavaPlugin.ID_PLUGIN,
                IStatus.ERROR,
                CorrectionMessages.JavaCorrectionProcessor_error_quickfix_message,
                null);
        resStatus.add(status);
      }
    }
    if (addQuickAssists) {
      IStatus status = collectAssists(context, problemLocations, proposals);
      if (!status.isOK()) {
        if (resStatus == null) {
          resStatus =
              new MultiStatus(
                  JavaPlugin.ID_PLUGIN,
                  IStatus.ERROR,
                  CorrectionMessages.JavaCorrectionProcessor_error_quickassist_message,
                  null);
        }
        resStatus.add(status);
      }
    }
    if (resStatus != null) {
      return resStatus;
    }
    return Status.OK_STATUS;
  }

  //	private static ProblemLocation getProblemLocation(IJavaAnnotation javaAnnotation,
  // IAnnotationModel model) {
  //		int problemId= javaAnnotation.getId();
  //		if (problemId != -1) {
  //			Position pos= model.getPosition((Annotation) javaAnnotation);
  //			if (pos != null) {
  //				return new ProblemLocation(pos.getOffset(), pos.getLength(), javaAnnotation); // java
  // problems all handled by the quick assist processors
  //			}
  //		}
  //		return null;
  //	}

  //	private static void collectMarkerProposals(SimpleMarkerAnnotation annotation,
  // Collection<IJavaCompletionProposal> proposals) {
  //		IMarker marker= annotation.getMarker();
  //		IMarkerResolution[] res= IDE.getMarkerHelpRegistry().getResolutions(marker);
  //		if (res.length > 0) {
  //			for (int i= 0; i < res.length; i++) {
  //				proposals.add(new MarkerResolutionProposal(res[i], marker));
  //			}
  //		}
  //	}

  private abstract static class SafeCorrectionProcessorAccess implements ISafeRunnable {
    private MultiStatus fMulti = null;
    private ContributedProcessorDescriptor fDescriptor;

    public void process(ContributedProcessorDescriptor[] desc) {
      for (int i = 0; i < desc.length; i++) {
        fDescriptor = desc[i];
        SafeRunner.run(this);
      }
    }

    public void process(ContributedProcessorDescriptor desc) {
      fDescriptor = desc;
      SafeRunner.run(this);
    }

    public void run() throws Exception {
      safeRun(fDescriptor);
    }

    protected abstract void safeRun(ContributedProcessorDescriptor processor) throws Exception;

    public void handleException(Throwable exception) {
      if (fMulti == null) {
        fMulti =
            new MultiStatus(
                JavaPlugin.ID_PLUGIN,
                IStatus.OK,
                CorrectionMessages.JavaCorrectionProcessor_error_status,
                null);
      }
      fMulti.merge(
          new Status(
              IStatus.ERROR,
              JavaPlugin.ID_PLUGIN,
              IStatus.ERROR,
              CorrectionMessages.JavaCorrectionProcessor_error_status,
              exception));
    }

    public IStatus getStatus() {
      if (fMulti == null) {
        return Status.OK_STATUS;
      }
      return fMulti;
    }
  }

  private static class SafeCorrectionCollector extends SafeCorrectionProcessorAccess {
    private final IInvocationContext fContext;
    private final Collection<IJavaCompletionProposal> fProposals;
    private IProblemLocation[] fLocations;

    public SafeCorrectionCollector(
        IInvocationContext context, Collection<IJavaCompletionProposal> proposals) {
      fContext = context;
      fProposals = proposals;
    }

    public void setProblemLocations(IProblemLocation[] locations) {
      fLocations = locations;
    }

    @Override
    public void safeRun(ContributedProcessorDescriptor desc) throws Exception {
      IQuickFixProcessor curr =
          (IQuickFixProcessor)
              desc.getProcessor(fContext.getCompilationUnit(), IQuickFixProcessor.class);
      if (curr != null) {
        IJavaCompletionProposal[] res = curr.getCorrections(fContext, fLocations);
        if (res != null) {
          for (int k = 0; k < res.length; k++) {
            fProposals.add(res[k]);
          }
        }
      }
    }
  }

  private static class SafeAssistCollector extends SafeCorrectionProcessorAccess {
    private final IInvocationContext fContext;
    private final IProblemLocation[] fLocations;
    private final Collection<IJavaCompletionProposal> fProposals;

    public SafeAssistCollector(
        IInvocationContext context,
        IProblemLocation[] locations,
        Collection<IJavaCompletionProposal> proposals) {
      fContext = context;
      fLocations = locations;
      fProposals = proposals;
    }

    @Override
    public void safeRun(ContributedProcessorDescriptor desc) throws Exception {
      IQuickAssistProcessor curr =
          (IQuickAssistProcessor)
              desc.getProcessor(fContext.getCompilationUnit(), IQuickAssistProcessor.class);
      if (curr != null) {
        IJavaCompletionProposal[] res = curr.getAssists(fContext, fLocations);
        if (res != null) {
          for (int k = 0; k < res.length; k++) {
            fProposals.add(res[k]);
          }
        }
      }
    }
  }

  private static class SafeHasAssist extends SafeCorrectionProcessorAccess {
    private final IInvocationContext fContext;
    private boolean fHasAssists;

    public SafeHasAssist(IInvocationContext context) {
      fContext = context;
      fHasAssists = false;
    }

    public boolean hasAssists() {
      return fHasAssists;
    }

    @Override
    public void safeRun(ContributedProcessorDescriptor desc) throws Exception {
      IQuickAssistProcessor processor =
          (IQuickAssistProcessor)
              desc.getProcessor(fContext.getCompilationUnit(), IQuickAssistProcessor.class);
      if (processor != null && processor.hasAssists(fContext)) {
        fHasAssists = true;
      }
    }
  }

  private static class SafeHasCorrections extends SafeCorrectionProcessorAccess {
    private final ICompilationUnit fCu;
    private final int fProblemId;
    private boolean fHasCorrections;

    public SafeHasCorrections(ICompilationUnit cu, int problemId) {
      fCu = cu;
      fProblemId = problemId;
      fHasCorrections = false;
    }

    public boolean hasCorrections() {
      return fHasCorrections;
    }

    @Override
    public void safeRun(ContributedProcessorDescriptor desc) throws Exception {
      IQuickFixProcessor processor =
          (IQuickFixProcessor) desc.getProcessor(fCu, IQuickFixProcessor.class);
      if (processor != null && processor.hasCorrections(fCu, fProblemId)) {
        fHasCorrections = true;
      }
    }
  }

  public static IStatus collectCorrections(
      IInvocationContext context,
      IProblemLocation[] locations,
      Collection<IJavaCompletionProposal> proposals) {
    ContributedProcessorDescriptor[] processors = getCorrectionProcessors();
    SafeCorrectionCollector collector = new SafeCorrectionCollector(context, proposals);
    for (int i = 0; i < processors.length; i++) {
      ContributedProcessorDescriptor curr = processors[i];
      IProblemLocation[] handled = getHandledProblems(locations, curr);
      if (handled != null) {
        collector.setProblemLocations(handled);
        collector.process(curr);
      }
    }
    return collector.getStatus();
  }

  private static IProblemLocation[] getHandledProblems(
      IProblemLocation[] locations, ContributedProcessorDescriptor processor) {
    // implementation tries to avoid creating a new array
    boolean allHandled = true;
    ArrayList<IProblemLocation> res = null;
    for (int i = 0; i < locations.length; i++) {
      IProblemLocation curr = locations[i];
      if (processor.canHandleMarkerType(curr.getMarkerType())) {
        if (!allHandled) { // first handled problem
          if (res == null) {
            res = new ArrayList<IProblemLocation>(locations.length - i);
          }
          res.add(curr);
        }
      } else if (allHandled) {
        if (i > 0) { // first non handled problem
          res = new ArrayList<IProblemLocation>(locations.length - i);
          for (int k = 0; k < i; k++) {
            res.add(locations[k]);
          }
        }
        allHandled = false;
      }
    }
    if (allHandled) {
      return locations;
    }
    if (res == null) {
      return null;
    }
    return res.toArray(new IProblemLocation[res.size()]);
  }

  public static IStatus collectAssists(
      IInvocationContext context,
      IProblemLocation[] locations,
      Collection<IJavaCompletionProposal> proposals) {
    ContributedProcessorDescriptor[] processors = getAssistProcessors();
    SafeAssistCollector collector = new SafeAssistCollector(context, locations, proposals);
    collector.process(processors);

    return collector.getStatus();
  }

  /*
   * @see IContentAssistProcessor#getErrorMessage()
   */
  public String getErrorMessage() {
    return fErrorMessage;
  }

  /*
   * @see org.eclipse.jface.text.quickassist.IQuickAssistProcessor#canFix(org.eclipse.jface.text.source.Annotation)
   * @since 3.2
   */
  public boolean canFix(Annotation annotation) {
    return hasCorrections(annotation);
  }

  /*
   * @see org.eclipse.jface.text.quickassist.IQuickAssistProcessor#canAssist(org.eclipse.jface.text.quickassist
   * .IQuickAssistInvocationContext)
   * @since 3.2
   */
  public boolean canAssist(IQuickAssistInvocationContext invocationContext) {
    if (invocationContext instanceof IInvocationContext)
      return hasAssists((IInvocationContext) invocationContext);
    return false;
  }
}
