/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.actions;

import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.che.jface.text.ITextSelection;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICodeAssist;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;

public class SelectionConverter {

  private static final IJavaElement[] EMPTY_RESULT = new IJavaElement[0];

  private SelectionConverter() {
    // no instance
  }

  //	/**
  //	 * Converts the selection provided by the given part into a structured selection. The following
  //	 * conversion rules are used:
  //	 * <ul>
  //	 * <li><code>part instanceof JavaEditor</code>: returns a structured selection using code
  //	 * resolve to convert the editor's text selection.</li>
  //	 * <li><code>part instanceof IWorkbenchPart</code>: returns the part's selection if it is a
  //	 * structured selection.</li>
  //	 * <li><code>default</code>: returns an empty structured selection.</li>
  //	 * </ul>
  //	 *
  //	 * @param part the part
  //	 * @return the selection
  //	 * @throws JavaModelException thrown when the type root can not be accessed
  //	 */
  //	public static IStructuredSelection getStructuredSelection(IWorkbenchPart part) throws
  // JavaModelException {
  //		if (part instanceof JavaEditor)
  //			return new StructuredSelection(codeResolve((JavaEditor)part));
  //		ISelectionProvider provider = part.getSite().getSelectionProvider();
  //		if (provider != null) {
  //			ISelection selection = provider.getSelection();
  //			if (selection instanceof IStructuredSelection)
  //				return (IStructuredSelection)selection;
  //		}
  //		return StructuredSelection.EMPTY;
  //	}
  //
  //
  //	/**
  //	 * Converts the given structured selection into an array of Java elements.
  //	 * An empty array is returned if one of the elements stored in the structured
  //	 * selection is not of type <code>IJavaElement</code>
  //	 * @param selection the selection
  //	 * @return the Java element contained in the selection
  //	 */
  //	public static IJavaElement[] getElements(IStructuredSelection selection) {
  //		if (!selection.isEmpty()) {
  //			IJavaElement[] result = new IJavaElement[selection.size()];
  //			int i = 0;
  //			for (Iterator<?> iter = selection.iterator(); iter.hasNext(); i++) {
  //				Object element = iter.next();
  //				if (!(element instanceof IJavaElement))
  //					return EMPTY_RESULT;
  //				result[i] = (IJavaElement)element;
  //			}
  //			return result;
  //		}
  //		return EMPTY_RESULT;
  //	}
  //
  //	public static boolean canOperateOn(JavaEditor editor) {
  //		if (editor == null)
  //			return false;
  //		return getInput(editor) != null;
  //
  //	}
  //
  //	public static IJavaElement[] codeResolveOrInputForked(JavaEditor editor) throws
  // InvocationTargetException, InterruptedException {
  //		ITypeRoot input= getInput(editor);
  //		if (input == null)
  //			return EMPTY_RESULT;
  //
  //		ITextSelection selection= (ITextSelection)editor.getSelectionProvider().getSelection();
  //		IJavaElement[] result= performForkedCodeResolve(input, selection);
  //		if (result.length == 0) {
  //			result= new IJavaElement[] {input};
  //		}
  //		return result;
  //	}
  //
  //	/**
  //	 * Perform a code resolve at the current selection of an editor
  //	 *
  //	 * @param editor the editor
  //	 * @return the resolved elements (only from primary working copies)
  //	 * @throws JavaModelException when the type root can not be accessed
  //	 */
  //	public static IJavaElement[] codeResolve(JavaEditor editor) throws JavaModelException {
  //		return codeResolve(editor, true);
  //	}
  //
  //	/**
  //	 * Perform a code resolve at the current selection of an editor
  //	 *
  //	 * @param editor the editor
  //	 * @param primaryOnly if <code>true</code> only primary working copies will be returned
  //	 * @return the resolved elements
  //	 * @throws JavaModelException thrown when the type root can not be accessed
  //	 * @since 3.2
  //	 */
  //	public static IJavaElement[] codeResolve(JavaEditor editor, boolean primaryOnly) throws
  // JavaModelException {
  //		ITypeRoot input= getInput(editor, primaryOnly);
  //		if (input != null)
  //			return codeResolve(input, (ITextSelection) editor.getSelectionProvider().getSelection());
  //		return EMPTY_RESULT;
  //	}
  //
  //	/**
  //	 * Perform a code resolve in a separate thread.
  //	 *
  //	 * @param editor the editor
  //	 * @param primaryOnly if <code>true</code> only primary working copies will be returned
  //	 * @return the resolved elements
  //	 * @throws InvocationTargetException which wraps any exception or error which occurs while
  //	 *             running the runnable
  //	 * @throws InterruptedException propagated by the context if the runnable acknowledges
  //	 *             cancelation by throwing this exception
  //	 * @since 3.2
  //	 */
  //	public static IJavaElement[] codeResolveForked(JavaEditor editor, boolean primaryOnly) throws
  // InvocationTargetException, InterruptedException {
  //		ITypeRoot input= getInput(editor, primaryOnly);
  //		if (input != null)
  //			return performForkedCodeResolve(input, (ITextSelection)
  // editor.getSelectionProvider().getSelection());
  //		return EMPTY_RESULT;
  //	}
  //
  //	/**
  //	 * Returns the element surrounding the selection of the given editor.
  //	 *
  //	 * @param editor the editor
  //	 * @return the element surrounding the current selection (only from primary working copies), or
  // <code>null</code> if none
  //	 * @throws JavaModelException if the Java type root does not exist or if an exception occurs
  //	 *             while accessing its corresponding resource
  //	 */
  //	public static IJavaElement getElementAtOffset(JavaEditor editor) throws JavaModelException {
  //		return getElementAtOffset(editor, true);
  //	}
  //
  //	/**
  //	 * Returns the element surrounding the selection of the given editor.
  //	 *
  //	 * @param editor the editor
  //	 * @param primaryOnly if <code>true</code> only primary working copies will be returned
  //	 * @return the element surrounding the current selection, or <code>null</code> if none
  //	 * @throws JavaModelException if the Java type root does not exist or if an exception occurs
  //	 *             while accessing its corresponding resource
  //	 * @since 3.2
  //	 */
  //	public static IJavaElement getElementAtOffset(JavaEditor editor, boolean primaryOnly) throws
  // JavaModelException {
  //		ITypeRoot input= getInput(editor, primaryOnly);
  //		if (input != null)
  //			return getElementAtOffset(input, (ITextSelection)
  // editor.getSelectionProvider().getSelection());
  //		return null;
  //	}
  //
  //	public static IType getTypeAtOffset(JavaEditor editor) throws JavaModelException {
  //		IJavaElement element= SelectionConverter.getElementAtOffset(editor);
  //		IType type= (IType)element.getAncestor(IJavaElement.TYPE);
  //		if (type == null) {
  //			ICompilationUnit unit= SelectionConverter.getInputAsCompilationUnit(editor);
  //			if (unit != null)
  //				type= unit.findPrimaryType();
  //		}
  //		return type;
  //	}
  //
  //	/**
  //	 * Returns the input element of the given editor.
  //	 *
  //	 * @param editor the Java editor
  //	 * @return the type root which is the editor input (only primary working copies), or
  // <code>null</code> if none
  //	 */
  //	public static ITypeRoot getInput(JavaEditor editor) {
  //		return getInput(editor, true);
  //	}
  //
  //	/**
  //	 * Returns the input element of the given editor.
  //	 *
  //	 * @param editor the Java editor
  //	 * @param primaryOnly if <code>true</code> only primary working copies will be returned
  //	 * @return the type root which is the editor input, or <code>null</code> if none
  //	 * @since 3.2
  //	 */
  //	private static ITypeRoot getInput(JavaEditor editor, boolean primaryOnly) {
  //		if (editor == null)
  //			return null;
  //		return EditorUtility.getEditorInputJavaElement(editor, primaryOnly);
  //	}
  //
  //	public static ICompilationUnit getInputAsCompilationUnit(JavaEditor editor) {
  //		Object editorInput= SelectionConverter.getInput(editor);
  //		if (editorInput instanceof ICompilationUnit)
  //			return (ICompilationUnit)editorInput;
  //		return null;
  //	}
  //
  //	public static IClassFile getInputAsClassFile(JavaEditor editor) {
  //		Object editorInput= SelectionConverter.getInput(editor);
  //		if (editorInput instanceof IClassFile)
  //			return (IClassFile)editorInput;
  //		return null;
  //	}
  //
  //	private static IJavaElement[] performForkedCodeResolve(final ITypeRoot input, final
  // ITextSelection selection) throws InvocationTargetException, InterruptedException {
  //		final class CodeResolveRunnable implements IRunnableWithProgress {
  //			IJavaElement[] result;
  //
  //			public void run(IProgressMonitor monitor) throws InvocationTargetException {
  //				try {
  //					result = codeResolve(input, selection);
  //				} catch (JavaModelException e) {
  //					throw new InvocationTargetException(e);
  //				}
  //			}
  //		}
  //		CodeResolveRunnable runnable = new CodeResolveRunnable();
  //		PlatformUI.getWorkbench().getProgressService().busyCursorWhile(runnable);
  //		return runnable.result;
  //	}

  public static IJavaElement[] codeResolve(IJavaElement input, ITextSelection selection)
      throws JavaModelException {
    if (input instanceof ICodeAssist) {
      if (input instanceof ICompilationUnit) {
        JavaModelUtil.reconcile((ICompilationUnit) input);
      }
      IJavaElement[] elements =
          ((ICodeAssist) input).codeSelect(selection.getOffset() + selection.getLength(), 0);
      if (elements.length > 0) {
        return elements;
      }
    }
    return EMPTY_RESULT;
  }

  //	public static IJavaElement getElementAtOffset(ITypeRoot input, ITextSelection selection) throws
  // JavaModelException {
  //		if (input instanceof ICompilationUnit) {
  //			JavaModelUtil.reconcile((ICompilationUnit)input);
  //		}
  //		IJavaElement ref = input.getElementAt(selection.getOffset());
  //		if (ref == null)
  //			return input;
  //		return ref;
  //	}
  //
  //	public static IJavaElement resolveEnclosingElement(JavaEditor editor, ITextSelection selection)
  // throws JavaModelException {
  //		ITypeRoot input = getInput(editor);
  //		if (input != null)
  //			return resolveEnclosingElement(input, selection);
  //		return null;
  //	}
  //
  public static IJavaElement resolveEnclosingElement(IJavaElement input, ITextSelection selection)
      throws JavaModelException {
    IJavaElement atOffset = null;
    if (input instanceof ICompilationUnit) {
      ICompilationUnit cunit = (ICompilationUnit) input;
      JavaModelUtil.reconcile(cunit);
      atOffset = cunit.getElementAt(selection.getOffset());
    } else if (input instanceof IClassFile) {
      IClassFile cfile = (IClassFile) input;
      atOffset = cfile.getElementAt(selection.getOffset());
    } else {
      return null;
    }
    if (atOffset == null) {
      return input;
    } else {
      int selectionEnd = selection.getOffset() + selection.getLength();
      IJavaElement result = atOffset;
      if (atOffset instanceof ISourceReference) {
        ISourceRange range = ((ISourceReference) atOffset).getSourceRange();
        while (range.getOffset() + range.getLength() < selectionEnd) {
          result = result.getParent();
          if (!(result instanceof ISourceReference)) {
            result = input;
            break;
          }
          range = ((ISourceReference) result).getSourceRange();
        }
      }
      return result;
    }
  }
  //
  //	/**
  //	 * Shows a dialog for resolving an ambiguous Java element. Utility method that can be called by
  // subclasses.
  //	 *
  //	 * @param elements the elements to select from
  //	 * @param shell the parent shell
  //	 * @param title the title of the selection dialog
  //	 * @param message the message of the selection dialog
  //	 * @return returns the selected element or <code>null</code> if the dialog has been cancelled
  //	 */
  //	public static IJavaElement selectJavaElement(IJavaElement[] elements, Shell shell, String
  // title, String message) {
  //		int nResults = elements.length;
  //		if (nResults == 0)
  //			return null;
  //		if (nResults == 1)
  //			return elements[0];
  //
  //		int flags = JavaElementLabelProvider.SHOW_DEFAULT | JavaElementLabelProvider.SHOW_QUALIFIED |
  // JavaElementLabelProvider.SHOW_ROOT;
  //
  //		ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, new
  // JavaElementLabelProvider(flags));
  //		dialog.setTitle(title);
  //		dialog.setMessage(message);
  //		dialog.setElements(elements);
  //
  //		if (dialog.open() == Window.OK) {
  //			return (IJavaElement)dialog.getFirstResult();
  //		}
  //		return null;
  //	}
}
