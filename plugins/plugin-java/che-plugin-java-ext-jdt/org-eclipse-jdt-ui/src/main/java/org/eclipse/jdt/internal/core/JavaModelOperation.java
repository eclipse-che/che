/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.util.Messages;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Defines behavior common to all Java Model operations
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class JavaModelOperation implements IWorkspaceRunnable, IProgressMonitor {
    public static final    String         HAS_MODIFIED_RESOURCE_ATTR = "hasModifiedResource"; //$NON-NLS-1$
    public static final    String         TRUE                       = JavaModelManager.TRUE;
    /*
     * Constants controlling the insertion mode of an action.
     * @see JavaModelOperation#postAction
     */
    protected static final int            APPEND                     = 1; // insert at the end
    protected static final int            REMOVEALL_APPEND           = 2;
            // remove all existing ones with same ID, and add new one at the end
    protected static final int            KEEP_EXISTING              = 3; // do not insert if already existing with same ID
    /**
     * An empty collection of <code>IJavaElement</code>s - the common
     * empty result if no elements are created, or if this
     * operation is not actually executed.
     */
    protected static final IJavaElement[] NO_ELEMENTS                = new IJavaElement[]{};
    /*
     * A per thread stack of java model operations (PerThreadObject of ArrayList).
     */
    protected static final ThreadLocal    OPERATION_STACKS           = new ThreadLocal();
    /*
     * Whether tracing post actions is enabled.
     */
    protected static boolean POST_ACTION_VERBOSE;
    /**
     * The progress monitor passed into this operation
     */
    public IProgressMonitor progressMonitor = null;
    /*
     * A list of IPostActions.
     */
    protected IPostAction[] actions;
    protected int actionsStart = 0;
    //public static final String FALSE = "false";
    protected int actionsEnd   = -1;
    /*
     * A HashMap of attributes that can be used by operations
     */
    protected HashMap          attributes;
    /**
     * The elements this operation operates on,
     * or <code>null</code> if this operation
     * does not operate on specific elements.
     */
    protected IJavaElement[]   elementsToProcess;
    /**
     * The parent elements this operation operates with
     * or <code>null</code> if this operation
     * does not operate with specific parent elements.
     */
    protected IJavaElement[]   parentElements;
    /**
     * The elements created by this operation - empty
     * until the operation actually creates elements.
     */
    protected IJavaElement[] resultElements = NO_ELEMENTS;
    /**
     * A flag indicating whether this operation is nested.
     */
    protected boolean        isNested       = false;
    /**
     * Conflict resolution policy - by default do not force (fail on a conflict).
     */
    protected boolean        force          = false;

    protected JavaModelOperation() {
        // default constructor used in subclasses
    }

    /**
     * A common constructor for all Java Model operations.
     */
    protected JavaModelOperation(IJavaElement[] elements) {
        this.elementsToProcess = elements;
    }

    /**
     * Common constructor for all Java Model operations.
     */
    protected JavaModelOperation(IJavaElement[] elementsToProcess, IJavaElement[] parentElements) {
        this.elementsToProcess = elementsToProcess;
        this.parentElements = parentElements;
    }

    /**
     * A common constructor for all Java Model operations.
     */
    protected JavaModelOperation(IJavaElement[] elementsToProcess, IJavaElement[] parentElements, boolean force) {
        this.elementsToProcess = elementsToProcess;
        this.parentElements = parentElements;
        this.force = force;
    }

    /**
     * A common constructor for all Java Model operations.
     */
    protected JavaModelOperation(IJavaElement[] elements, boolean force) {
        this.elementsToProcess = elements;
        this.force = force;
    }

    /**
     * Common constructor for all Java Model operations.
     */
    protected JavaModelOperation(IJavaElement element) {
        this.elementsToProcess = new IJavaElement[]{element};
    }

    /*
     * Returns the attribute registered at the given key with the top level operation.
     * Returns null if no such attribute is found.
     */
    protected static Object getAttribute(Object key) {
        ArrayList stack = getCurrentOperationStack();
        if (stack.size() == 0) return null;
        JavaModelOperation
                topLevelOp = (JavaModelOperation)stack.get(0);
        if (topLevelOp.attributes == null) {
            return null;
        } else {
            return topLevelOp.attributes.get(key);
        }
    }

    /*
     * Returns the stack of operations running in the current thread.
     * Returns an empty stack if no operations are currently running in this thread.
     */
    protected static ArrayList getCurrentOperationStack() {
        ArrayList stack = (ArrayList)OPERATION_STACKS.get();
        if (stack == null) {
            stack = new ArrayList();
            OPERATION_STACKS.set(stack);
        }
        return stack;
    }

    /*
     * Registers the given attribute at the given key with the top level operation.
     */
    protected static void setAttribute(Object key, Object attribute) {
        ArrayList operationStack = getCurrentOperationStack();
        if (operationStack.size() == 0)
            return;
        JavaModelOperation
                topLevelOp = (JavaModelOperation)operationStack.get(0);
        if (topLevelOp.attributes == null) {
            topLevelOp.attributes = new HashMap();
        }
        topLevelOp.attributes.put(key, attribute);
    }

    /*
     * Registers the given action at the end of the list of actions to run.
     */
    protected void addAction(IPostAction action) {
        int length = this.actions.length;
        if (length == ++this.actionsEnd) {
            System.arraycopy(this.actions, 0, this.actions = new IPostAction[length * 2], 0, length);
        }
        this.actions[this.actionsEnd] = action;
    }

    /*
     * Registers the given delta with the Java Model Manager.
     */
    protected void addDelta(IJavaElementDelta delta) {
        JavaModelManager.getJavaModelManager().getDeltaProcessor().registerJavaModelDelta(delta);
    }

    /*
     * Registers the given reconcile delta with the Java Model Manager.
     */
    protected void addReconcileDelta(ICompilationUnit workingCopy, IJavaElementDelta delta) {
        HashMap reconcileDeltas = JavaModelManager.getJavaModelManager().getDeltaProcessor().reconcileDeltas;
        org.eclipse.jdt.internal.core.JavaElementDelta previousDelta = (org.eclipse.jdt.internal.core.JavaElementDelta)reconcileDeltas.get(workingCopy);
        if (previousDelta != null) {
            IJavaElementDelta[] children = delta.getAffectedChildren();
            for (int i = 0, length = children.length; i < length; i++) {
                org.eclipse.jdt.internal.core.JavaElementDelta child = (org.eclipse.jdt.internal.core.JavaElementDelta)children[i];
                previousDelta.insertDeltaTree(child.getElement(), child);
            }
            // note that the last delta's AST always takes precedence over the existing delta's AST
            // since it is the result of the last reconcile operation
            if ((delta.getFlags() & IJavaElementDelta.F_AST_AFFECTED) != 0) {
                previousDelta.changedAST(delta.getCompilationUnitAST());
            }

        } else {
            reconcileDeltas.put(workingCopy, delta);
        }
    }

    /*
     * Deregister the reconcile delta for the given working copy
     */
    protected void removeReconcileDelta(ICompilationUnit workingCopy) {
        JavaModelManager.getJavaModelManager().getDeltaProcessor().reconcileDeltas.remove(workingCopy);
    }

    protected void applyTextEdit(ICompilationUnit cu, TextEdit edits) throws JavaModelException {
        try {
            edits.apply(getDocument(cu));
        } catch (BadLocationException e) {
            // content changed under us
            throw new JavaModelException(e, IJavaModelStatusConstants.INVALID_CONTENTS);
        }
    }

    /**
     * @see IProgressMonitor
     */
    public void beginTask(String name, int totalWork) {
        if (this.progressMonitor != null) {
            this.progressMonitor.beginTask(name, totalWork);
        }
    }

    /*
     * Returns whether this operation can modify the package fragment roots.
     */
    protected boolean canModifyRoots() {
        return false;
    }
	/**
	 * Convenience method to copy resources
	 */
	protected void copyResources(IResource[] resources, IPath container) throws JavaModelException {
		IProgressMonitor subProgressMonitor = getSubProgressMonitor(resources.length);
		IWorkspaceRoot root =  ResourcesPlugin.getWorkspace().getRoot();
		try {
			for (int i = 0, length = resources.length; i < length; i++) {
				IResource resource = resources[i];
				IPath destination = container.append(resource.getName());
				if (root.findMember(destination) == null) {
					resource.copy(destination, false, subProgressMonitor);
				}
			}
			setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}
	/**
	 * Convenience method to create a file
	 */
	protected void createFile(IContainer folder, String name, InputStream contents, boolean forceFlag) throws JavaModelException {
		IFile file= folder.getFile(new Path(name));
		try {
			file.create(
				contents,
				forceFlag ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY,
				getSubProgressMonitor(1));
				setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}
	/**
	 * Convenience method to create a folder
	 */
	protected void createFolder(IContainer parentFolder, String name, boolean forceFlag) throws JavaModelException {
		IFolder folder= parentFolder.getFolder(new Path(name));
		try {
			// we should use true to create the file locally. Only VCM should use tru/false
			folder.create(
				forceFlag ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY,
				true, // local
				getSubProgressMonitor(1));
			setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}
	/**
	 * Convenience method to delete an empty package fragment
	 */
	protected void deleteEmptyPackageFragment(
		IPackageFragment fragment,
		boolean forceFlag,
		IResource rootResource)
		throws JavaModelException {

		IContainer resource = (IContainer) ((JavaElement)fragment).resource();

		try {
			resource.delete(
				forceFlag ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY,
				getSubProgressMonitor(1));
			setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
			while (resource instanceof IFolder) {
				// deleting a package: delete the parent if it is empty (e.g. deleting x.y where folder x doesn't have resources but y)
				// without deleting the package fragment root
				resource = resource.getParent();
				if (!resource.equals(rootResource) && resource.members().length == 0) {
					resource.delete(
						forceFlag ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY,
						getSubProgressMonitor(1));
					setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
				}
			}
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}
	/**
	 * Convenience method to delete a resource
	 */
	protected void deleteResource(IResource resource,int flags) throws JavaModelException {
		try {
			resource.delete(flags, getSubProgressMonitor(1));
			setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}
	/**
	 * Convenience method to delete resources
	 */
	protected void deleteResources(IResource[] resources, boolean forceFlag) throws JavaModelException {
		if (resources == null || resources.length == 0) return;
		IProgressMonitor subProgressMonitor = getSubProgressMonitor(resources.length);
		IWorkspace workspace = resources[0].getWorkspace();
		try {
			workspace.delete(
				resources,
				forceFlag ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY,
				subProgressMonitor);
				setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}

    /**
     * Checks with the progress monitor to see whether this operation
     * should be canceled. An operation should regularly call this method
     * during its operation so that the user can cancel it.
     *
     * @throws OperationCanceledException
     *         if cancelling the operation has been requested
     * @see IProgressMonitor#isCanceled
     */
    protected void checkCanceled() {
        if (isCanceled()) {
            throw new OperationCanceledException(Messages.operation_cancelled);
        }
    }

    /**
     * Common code used to verify the elements this operation is processing.
     *
     * @see org.eclipse.jdt.internal.core.JavaModelOperation#verify()
     */
    protected IJavaModelStatus commonVerify() {
        if (this.elementsToProcess == null || this.elementsToProcess.length == 0) {
            return new JavaModelStatus(IJavaModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
        }
        for (int i = 0; i < this.elementsToProcess.length; i++) {
            if (this.elementsToProcess[i] == null) {
                return new JavaModelStatus(IJavaModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
            }
        }
        return org.eclipse.jdt.internal.core.JavaModelStatus.VERIFIED_OK;
    }

    /**
     * @see IProgressMonitor
     */
    public void done() {
        if (this.progressMonitor != null) {
            this.progressMonitor.done();
        }
    }

    /*
     * Returns whether the given path is equals to one of the given other paths.
     */
    protected boolean equalsOneOf(IPath path, IPath[] otherPaths) {
        for (int i = 0, length = otherPaths.length; i < length; i++) {
            if (path.equals(otherPaths[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convenience method to run an operation within this operation
     */
    public void executeNestedOperation(JavaModelOperation operation, int subWorkAmount) throws
                                                                                        JavaModelException {
        IJavaModelStatus status = operation.verify();
        if (!status.isOK()) {
            throw new JavaModelException(status);
        }
        IProgressMonitor subProgressMonitor = getSubProgressMonitor(subWorkAmount);
        // fix for 1FW7IKC, part (1)
        try {
            operation.setNested(true);
            operation.run(subProgressMonitor);
        } catch (CoreException ce) {
            if (ce instanceof JavaModelException) {
                throw (JavaModelException)ce;
            } else {
                // translate the core exception to a java model exception
                if (ce.getStatus().getCode() == IResourceStatus.OPERATION_FAILED) {
                    Throwable e = ce.getStatus().getException();
                    if (e instanceof JavaModelException) {
                        throw (JavaModelException)e;
                    }
                }
                throw new JavaModelException(ce);
            }
        }
    }

    /**
     * Performs the operation specific behavior. Subclasses must override.
     */
    protected abstract void executeOperation() throws JavaModelException;

    /**
     * Returns the compilation unit the given element is contained in,
     * or the element itself (if it is a compilation unit),
     * otherwise <code>null</code>.
     */
    protected ICompilationUnit getCompilationUnitFor(IJavaElement element) {

        return ((JavaElement)element).getCompilationUnit();
    }

    /*
     * Returns the existing document for the given cu, or a DocumentAdapter if none.
     */
    protected IDocument getDocument(ICompilationUnit cu) throws JavaModelException {
        IBuffer buffer = cu.getBuffer();
        if (buffer instanceof IDocument)
            return (IDocument)buffer;
        return new DocumentAdapter(buffer);
    }

    /**
     * Returns the element to which this operation applies,
     * or <code>null</code> if not applicable.
     */
    protected IJavaElement getElementToProcess() {
        if (this.elementsToProcess == null || this.elementsToProcess.length == 0) {
            return null;
        }
        return this.elementsToProcess[0];
    }

    /**
     * Returns the Java Model this operation is operating in.
     */
    public IJavaModel getJavaModel() {
        return JavaModelManager.getJavaModelManager().getJavaModel();
    }

    protected IPath[] getNestedFolders(IPackageFragmentRoot root) throws JavaModelException {
        IPath rootPath = root.getPath();
        IClasspathEntry[] classpath = root.getJavaProject().getRawClasspath();
        int length = classpath.length;
        IPath[] result = new IPath[length];
        int index = 0;
        for (int i = 0; i < length; i++) {
            IPath path = classpath[i].getPath();
            if (rootPath.isPrefixOf(path) && !rootPath.equals(path)) {
                result[index++] = path;
            }
        }
        if (index < length) {
            System.arraycopy(result, 0, result = new IPath[index], 0, index);
        }
        return result;
    }

    /**
     * Returns the parent element to which this operation applies,
     * or <code>null</code> if not applicable.
     */
    protected IJavaElement getParentElement() {
        if (this.parentElements == null || this.parentElements.length == 0) {
            return null;
        }
        return this.parentElements[0];
    }

    /**
     * Returns the parent elements to which this operation applies,
     * or <code>null</code> if not applicable.
     */
    protected IJavaElement[] getParentElements() {
        return this.parentElements;
    }

    /**
     * Returns the elements created by this operation.
     */
    public IJavaElement[] getResultElements() {
        return this.resultElements;
    }

    /*
     * Returns the scheduling rule for this operation (i.e. the resource that needs to be locked
     * while this operation is running.
     * Subclasses can override.
     */
    protected ISchedulingRule getSchedulingRule() {
		return ResourcesPlugin.getWorkspace().getRoot();
    }

    /**
     * Creates and returns a subprogress monitor if appropriate.
     */
    protected IProgressMonitor getSubProgressMonitor(int workAmount) {
        IProgressMonitor sub = null;
        if (this.progressMonitor != null) {
            sub = new SubProgressMonitor(this.progressMonitor, workAmount, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
        }
        return sub;
    }

    /**
     * Returns whether this operation has performed any resource modifications.
     * Returns false if this operation has not been executed yet.
     */
    public boolean hasModifiedResource() {
        return !isReadOnly() && getAttribute(HAS_MODIFIED_RESOURCE_ATTR) == TRUE;
    }

    public void internalWorked(double work) {
        if (this.progressMonitor != null) {
            this.progressMonitor.internalWorked(work);
        }
    }

    /**
     * @see IProgressMonitor
     */
    public boolean isCanceled() {
        if (this.progressMonitor != null) {
            return this.progressMonitor.isCanceled();
        }
        return false;
    }

    /**
     * @see IProgressMonitor
     */
    public void setCanceled(boolean b) {
        if (this.progressMonitor != null) {
            this.progressMonitor.setCanceled(b);
        }
    }

    /**
     * Returns <code>true</code> if this operation performs no resource modifications,
     * otherwise <code>false</code>. Subclasses must override.
     */
    public boolean isReadOnly() {
        return false;
    }

    /*
     * Returns whether this operation is the first operation to run in the current thread.
     */
    protected boolean isTopLevelOperation() {
        ArrayList stack;
        return
                (stack = getCurrentOperationStack()).size() > 0
                && stack.get(0) == this;
    }

	/**
	 * Convenience method to move resources
	 */
	protected void moveResources(IResource[] resources, IPath container) throws JavaModelException {
		IProgressMonitor subProgressMonitor = null;
		if (this.progressMonitor != null) {
			subProgressMonitor = new SubProgressMonitor(this.progressMonitor, resources.length, SubProgressMonitor
 .PREPEND_MAIN_LABEL_TO_SUBTASK);
		}
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		try {
			for (int i = 0, length = resources.length; i < length; i++) {
				IResource resource = resources[i];
				IPath destination = container.append(resource.getName());
				if (root.findMember(destination) == null) {
					resource.move(destination, false, subProgressMonitor);
				}
			}
			setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}

    /*
     * Returns the index of the first registered action with the given id, starting from a given position.
     * Returns -1 if not found.
     */
    protected int firstActionWithID(String id, int start) {
        for (int i = start; i <= this.actionsEnd; i++) {
            if (this.actions[i].getID().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Creates and returns a new <code>IJavaElementDelta</code>
     * on the Java Model.
     */
    public org.eclipse.jdt.internal.core.JavaElementDelta newJavaElementDelta() {
        return new JavaElementDelta(getJavaModel());
    }

    /*
     * Removes the last pushed operation from the stack of running operations.
     * Returns the poped operation or null if the stack was empty.
     */
    protected JavaModelOperation popOperation() {
        ArrayList stack = getCurrentOperationStack();
        int size = stack.size();
        if (size > 0) {
            if (size == 1) { // top level operation
                OPERATION_STACKS.set(null); // release reference (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=33927)
            }
            return (JavaModelOperation)stack.remove(size - 1);
        } else {
            return null;
        }
    }

    /*
     * Registers the given action to be run when the outer most java model operation has finished.
     * The insertion mode controls whether:
     * - the action should discard all existing actions with the same id, and be queued at the end (REMOVEALL_APPEND),
     * - the action should be ignored if there is already an action with the same id (KEEP_EXISTING),
     * - the action should be queued at the end without looking at existing actions (APPEND)
     */
    protected void postAction(IPostAction action, int insertionMode) {
        if (POST_ACTION_VERBOSE) {
            System.out.print("(" + Thread.currentThread() + ") [JavaModelOperation.postAction(IPostAction, int)] Posting action " +
                             action.getID()); //$NON-NLS-1$ //$NON-NLS-2$
            switch (insertionMode) {
                case REMOVEALL_APPEND:
                    System.out.println(" (REMOVEALL_APPEND)"); //$NON-NLS-1$
                    break;
                case KEEP_EXISTING:
                    System.out.println(" (KEEP_EXISTING)"); //$NON-NLS-1$
                    break;
                case APPEND:
                    System.out.println(" (APPEND)"); //$NON-NLS-1$
                    break;
            }
        }

        JavaModelOperation
                topLevelOp = (JavaModelOperation)getCurrentOperationStack().get(0);
        IPostAction[] postActions = topLevelOp.actions;
        if (postActions == null) {
            topLevelOp.actions = postActions = new IPostAction[1];
            postActions[0] = action;
            topLevelOp.actionsEnd = 0;
        } else {
            String id = action.getID();
            switch (insertionMode) {
                case REMOVEALL_APPEND:
                    int index = this.actionsStart - 1;
                    while ((index = topLevelOp.firstActionWithID(id, index + 1)) >= 0) {
                        // remove action[index]
                        System.arraycopy(postActions, index + 1, postActions, index, topLevelOp.actionsEnd - index);
                        postActions[topLevelOp.actionsEnd--] = null;
                    }
                    topLevelOp.addAction(action);
                    break;
                case KEEP_EXISTING:
                    if (topLevelOp.firstActionWithID(id, 0) < 0) {
                        topLevelOp.addAction(action);
                    }
                    break;
                case APPEND:
                    topLevelOp.addAction(action);
                    break;
            }
        }
    }

    /*
     * Returns whether the given path is the prefix of one of the given other paths.
     */
    protected boolean prefixesOneOf(IPath path, IPath[] otherPaths) {
        for (int i = 0, length = otherPaths.length; i < length; i++) {
            if (path.isPrefixOf(otherPaths[i])) {
                return true;
            }
        }
        return false;
    }

    /*
     * Pushes the given operation on the stack of operations currently running in this thread.
     */
    protected void pushOperation(JavaModelOperation operation) {
        getCurrentOperationStack().add(operation);
    }

    /*
     * Removes all actions with the given id from the queue of post actions.
     * Does nothing if no such action is in the queue.
     */
    protected void removeAllPostAction(String actionID) {
        if (POST_ACTION_VERBOSE) {
            System.out.println("(" + Thread.currentThread() + ") [JavaModelOperation.removeAllPostAction(String)] Removing actions " +
                               actionID); //$NON-NLS-1$ //$NON-NLS-2$
        }

        JavaModelOperation
                topLevelOp = (JavaModelOperation)getCurrentOperationStack().get(0);
        IPostAction[] postActions = topLevelOp.actions;
        if (postActions == null) return;
        int index = this.actionsStart - 1;
        while ((index = topLevelOp.firstActionWithID(actionID, index + 1)) >= 0) {
            // remove action[index]
            System.arraycopy(postActions, index + 1, postActions, index, topLevelOp.actionsEnd - index);
            postActions[topLevelOp.actionsEnd--] = null;
        }
    }

    /**
     * Runs this operation and registers any deltas created.
     *
     * @throws CoreException
     *         if the operation fails
     * @see IWorkspaceRunnable
     */
    public void run(IProgressMonitor monitor) throws CoreException {
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
        DeltaProcessor deltaProcessor = manager.getDeltaProcessor();
        int previousDeltaCount = deltaProcessor.javaModelDeltas.size();
        try {
            this.progressMonitor = monitor;
            pushOperation(this);
            try {
                if (canModifyRoots()) {
                    // computes the root infos before executing the operation
                    // noop if aready initialized
                    manager.deltaState.initializeRoots(false/*not initiAfterLoad*/);
                }

                executeOperation();
            } finally {
                if (isTopLevelOperation()) {
                    runPostActions();
                }
            }
        } finally {
            try {
                // reacquire delta processor as it can have been reset during executeOperation()
                deltaProcessor = manager.getDeltaProcessor();

                // update JavaModel using deltas that were recorded during this operation
                for (int i = previousDeltaCount, size = deltaProcessor.javaModelDeltas.size(); i < size; i++) {
                    deltaProcessor.updateJavaModel((IJavaElementDelta)deltaProcessor.javaModelDeltas.get(i));
                }

                // close the parents of the created elements and reset their project's cache (in case we are in an
                // IWorkspaceRunnable and the clients wants to use the created element's parent)
                // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=83646
                for (int i = 0, length = this.resultElements.length; i < length; i++) {
                    IJavaElement element = this.resultElements[i];
                    Openable openable = (Openable)element.getOpenable();
                    if (!(openable instanceof CompilationUnit) || !((CompilationUnit)openable)
                            .isWorkingCopy()) { // a working copy must remain a child of its parent even after a move
                        ((JavaElement)openable.getParent()).close();
                    }
                    switch (element.getElementType()) {
                        case IJavaElement.PACKAGE_FRAGMENT_ROOT:
                        case IJavaElement.PACKAGE_FRAGMENT:
                            deltaProcessor.projectCachesToReset.add(element.getJavaProject());
                            break;
                    }
                }
                deltaProcessor.resetProjectCaches();

                // fire only iff:
                // - the operation is a top level operation
                // - the operation did produce some delta(s)
                // - but the operation has not modified any resource
                if (isTopLevelOperation()) {
                    if ((deltaProcessor.javaModelDeltas.size() > previousDeltaCount || !deltaProcessor.reconcileDeltas.isEmpty())
                        && !hasModifiedResource()) {
                        deltaProcessor.fire(null, DeltaProcessor.DEFAULT_CHANGE_EVENT);
                    } // else deltas are fired while processing the resource delta
                }
            } finally {
                popOperation();
            }
        }
    }

    /**
     * Main entry point for Java Model operations. Runs a Java Model Operation as an IWorkspaceRunnable
     * if not read-only.
     */
    public void runOperation(IProgressMonitor monitor) throws JavaModelException {
        IJavaModelStatus status = verify();
        if (!status.isOK()) {
            throw new JavaModelException(status);
        }
        try {
            if (isReadOnly()) {
                run(monitor);
            } else {
                // Use IWorkspace.run(...) to ensure that resource changes are batched
                // Note that if the tree is locked, this will throw a CoreException, but this is ok
                // as this operation is modifying the tree (not read-only) and a CoreException will be thrown anyway.
//				ResourcesPlugin.getWorkspace().run(this, getSchedulingRule(), IWorkspace.AVOID_UPDATE, monitor);
                //todo use ResourcesPlugin.getWorkspace().run
                run(monitor);
            }
        } catch (CoreException ce) {
            if (ce instanceof JavaModelException) {
                throw (JavaModelException)ce;
            } else {
                if (ce.getStatus().getCode() == IResourceStatus.OPERATION_FAILED) {
                    Throwable e = ce.getStatus().getException();
                    if (e instanceof JavaModelException) {
                        throw (JavaModelException)e;
                    }
                }
                throw new JavaModelException(ce);
            }
        }
    }

    protected void runPostActions() throws JavaModelException {
        while (this.actionsStart <= this.actionsEnd) {
            IPostAction postAction = this.actions[this.actionsStart++];
            if (POST_ACTION_VERBOSE) {
                System.out.println("(" + Thread.currentThread() + ") [JavaModelOperation.runPostActions()] Running action " +
                                   postAction.getID()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            postAction.run();
        }
    }

    /**
     * Sets whether this operation is nested or not.
     *
     * @see CreateElementInCUOperation#checkCanceled
     */
    protected void setNested(boolean nested) {
        this.isNested = nested;
    }

    /**
     * @see IProgressMonitor
     */
    public void setTaskName(String name) {
        if (this.progressMonitor != null) {
            this.progressMonitor.setTaskName(name);
        }
    }

    /**
     * @see IProgressMonitor
     */
    public void subTask(String name) {
        if (this.progressMonitor != null) {
            this.progressMonitor.subTask(name);
        }
    }

    /**
     * Returns a status indicating if there is any known reason
     * this operation will fail.  Operations are verified before they
     * are run.
     * <p/>
     * Subclasses must override if they have any conditions to verify
     * before this operation executes.
     *
     * @see IJavaModelStatus
     */
    protected IJavaModelStatus verify() {
        return commonVerify();
    }

    /**
     * @see IProgressMonitor
     */
    public void worked(int work) {
        if (this.progressMonitor != null) {
            this.progressMonitor.worked(work);
            checkCanceled();
        }
    }

    protected interface IPostAction {
        /*
         * Returns the id of this action.
         * @see JavaModelOperation#postAction
         */
        String getID();

        /*
         * Run this action.
         */
        void run() throws JavaModelException;
    }
}
