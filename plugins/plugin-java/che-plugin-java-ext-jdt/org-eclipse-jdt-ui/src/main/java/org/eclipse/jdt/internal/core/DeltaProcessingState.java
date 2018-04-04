/**
 * ***************************************************************************** Copyright (c) 2004,
 * 2012 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.util.Util;

/** Keep the global states used during Java element delta processing. */
@SuppressWarnings({"rawtypes", "unchecked"})
public class DeltaProcessingState implements IResourceChangeListener {

  /*
   * Collection of listeners for Java element deltas
   */
  public IElementChangedListener[] elementChangedListeners = new IElementChangedListener[5];
  public int[] elementChangedListenerMasks = new int[5];
  public int elementChangedListenerCount = 0;

  /*
   * Collection of pre Java resource change listeners
   */
  public IResourceChangeListener[] preResourceChangeListeners = new IResourceChangeListener[1];
  public int[] preResourceChangeEventMasks = new int[1];
  public int preResourceChangeListenerCount = 0;

  /*
   * The delta processor for the current thread.
   */
  private ThreadLocal deltaProcessors = new ThreadLocal();
  private JavaModelManager manager;

  public void doNotUse() {
    // reset the delta processor of the current thread to avoid to keep it in memory
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=269476
    this.deltaProcessors.set(null);
  }

  /* A table from IPath (from a classpath entry) to DeltaProcessor.RootInfo */
  public HashMap roots = new HashMap();

  /* A table from IPath (from a classpath entry) to ArrayList of DeltaProcessor.RootInfo
   * Used when an IPath corresponds to more than one root */
  public HashMap otherRoots = new HashMap();

  /* A table from IPath (from a classpath entry) to DeltaProcessor.RootInfo
   * from the last time the delta processor was invoked. */
  public HashMap oldRoots = new HashMap();

  /* A table from IPath (from a classpath entry) to ArrayList of DeltaProcessor.RootInfo
   * from the last time the delta processor was invoked.
   * Used when an IPath corresponds to more than one root */
  public HashMap oldOtherRoots = new HashMap();

  /* A table from IPath (a source attachment path from a classpath entry) to IPath (a root path) */
  public HashMap sourceAttachments = new HashMap();

  /* A table from IJavaProject to IJavaProject[] (the list of direct dependent of the key) */
  public HashMap projectDependencies = new HashMap();

  /* Whether the roots tables should be recomputed */
  public boolean rootsAreStale = true;

  /* Threads that are currently running initializeRoots() */
  private Set initializingThreads = Collections.synchronizedSet(new HashSet());

  /* A table from file system absoulte path (String) to timestamp (Long) */
  public Hashtable externalTimeStamps;

  /*
   * Map from IProject to ClasspathChange
   * Note these changes need to be kept on the delta processing state to ensure we don't loose them
   * (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=271102 Java model corrupt after switching target platform)
   */
  private HashMap classpathChanges = new HashMap();

  /* A table from JavaProject to ClasspathValidation */
  private HashMap classpathValidations = new HashMap();

  /* A table from JavaProject to ProjectReferenceChange */
  private HashMap projectReferenceChanges = new HashMap();

  /* A table from JavaProject to ExternalFolderChange */
  private HashMap externalFolderChanges = new HashMap();

  /**
   * Workaround for bug 15168 circular errors not reported This is a cache of the projects before
   * any project addition/deletion has started.
   */
  private HashSet javaProjectNamesCache;

  /*
   * A list of IJavaElement used as a scope for external archives refresh during POST_CHANGE.
   * This is null if no refresh is needed.
   */
  private HashSet externalElementsToRefresh;

  public DeltaProcessingState(JavaModelManager manager) {
    this.manager = manager;
  }

  /*
   * Need to clone defensively the listener information, in case some listener is reacting to some notification iteration by adding/changing/removing
   * any of the other (for example, if it deregisters itself).
   */
  public synchronized void addElementChangedListener(
      IElementChangedListener listener, int eventMask) {
    for (int i = 0; i < this.elementChangedListenerCount; i++) {
      if (this.elementChangedListeners[i] == listener) {

        // only clone the masks, since we could be in the middle of notifications and one listener
        // decide to change
        // any event mask of another listeners (yet not notified).
        int cloneLength = this.elementChangedListenerMasks.length;
        System.arraycopy(
            this.elementChangedListenerMasks,
            0,
            this.elementChangedListenerMasks = new int[cloneLength],
            0,
            cloneLength);
        this.elementChangedListenerMasks[i] |= eventMask; // could be different
        return;
      }
    }
    // may need to grow, no need to clone, since iterators will have cached original arrays and max
    // boundary and we only add to the end.
    int length;
    if ((length = this.elementChangedListeners.length) == this.elementChangedListenerCount) {
      System.arraycopy(
          this.elementChangedListeners,
          0,
          this.elementChangedListeners = new IElementChangedListener[length * 2],
          0,
          length);
      System.arraycopy(
          this.elementChangedListenerMasks,
          0,
          this.elementChangedListenerMasks = new int[length * 2],
          0,
          length);
    }
    this.elementChangedListeners[this.elementChangedListenerCount] = listener;
    this.elementChangedListenerMasks[this.elementChangedListenerCount] = eventMask;
    this.elementChangedListenerCount++;
  }

  /*
   * Adds the given element to the list of elements used as a scope for external jars refresh.
   */
  public synchronized void addForRefresh(IJavaElement externalElement) {
    if (this.externalElementsToRefresh == null) {
      this.externalElementsToRefresh = new HashSet();
    }
    this.externalElementsToRefresh.add(externalElement);
  }

  public synchronized void addPreResourceChangedListener(
      IResourceChangeListener listener, int eventMask) {
    for (int i = 0; i < this.preResourceChangeListenerCount; i++) {
      if (this.preResourceChangeListeners[i] == listener) {
        this.preResourceChangeEventMasks[i] |= eventMask;
        return;
      }
    }
    // may need to grow, no need to clone, since iterators will have cached original arrays and max
    // boundary and we only add to the end.
    int length;
    if ((length = this.preResourceChangeListeners.length) == this.preResourceChangeListenerCount) {
      System.arraycopy(
          this.preResourceChangeListeners,
          0,
          this.preResourceChangeListeners = new IResourceChangeListener[length * 2],
          0,
          length);
      System.arraycopy(
          this.preResourceChangeEventMasks,
          0,
          this.preResourceChangeEventMasks = new int[length * 2],
          0,
          length);
    }
    this.preResourceChangeListeners[this.preResourceChangeListenerCount] = listener;
    this.preResourceChangeEventMasks[this.preResourceChangeListenerCount] = eventMask;
    this.preResourceChangeListenerCount++;
  }

  public DeltaProcessor getDeltaProcessor() {
    DeltaProcessor deltaProcessor = (DeltaProcessor) this.deltaProcessors.get();
    if (deltaProcessor != null) return deltaProcessor;
    deltaProcessor = new DeltaProcessor(this, manager);
    this.deltaProcessors.set(deltaProcessor);
    return deltaProcessor;
  }

  public ClasspathChange addClasspathChange(
      IProject project,
      IClasspathEntry[] oldRawClasspath,
      IPath oldOutputLocation,
      IClasspathEntry[] oldResolvedClasspath) {
    synchronized (this.classpathChanges) {
      ClasspathChange change = (ClasspathChange) this.classpathChanges.get(project);
      if (change == null) {
        change =
            new ClasspathChange(
                (JavaProject)
                    JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(project),
                oldRawClasspath,
                oldOutputLocation,
                oldResolvedClasspath);
        this.classpathChanges.put(project, change);
      } else {
        if (change.oldRawClasspath == null) change.oldRawClasspath = oldRawClasspath;
        if (change.oldOutputLocation == null) change.oldOutputLocation = oldOutputLocation;
        if (change.oldResolvedClasspath == null) change.oldResolvedClasspath = oldResolvedClasspath;
      }
      return change;
    }
  }

  public ClasspathChange getClasspathChange(IProject project) {
    synchronized (this.classpathChanges) {
      return (ClasspathChange) this.classpathChanges.get(project);
    }
  }

  public HashMap removeAllClasspathChanges() {
    synchronized (this.classpathChanges) {
      HashMap result = this.classpathChanges;
      this.classpathChanges = new HashMap(result.size());
      return result;
    }
  }

  //	public synchronized ClasspathValidation addClasspathValidation(JavaProject project) {
  //		ClasspathValidation validation = (ClasspathValidation) this.classpathValidations.get(project);
  //		if (validation == null) {
  //			validation = new ClasspathValidation(project);
  //			this.classpathValidations.put(project, validation);
  //	    }
  //		return validation;
  //	}
  //
  public synchronized void addExternalFolderChange(
      JavaProject project, IClasspathEntry[] oldResolvedClasspath) {
    ExternalFolderChange change = (ExternalFolderChange) this.externalFolderChanges.get(project);
    if (change == null) {
      change = new ExternalFolderChange(project, oldResolvedClasspath);
      this.externalFolderChanges.put(project, change);
    }
  }

  public synchronized void addProjectReferenceChange(
      JavaProject project, IClasspathEntry[] oldResolvedClasspath) {
    ProjectReferenceChange change =
        (ProjectReferenceChange) this.projectReferenceChanges.get(project);
    if (change == null) {
      change = new ProjectReferenceChange(project, oldResolvedClasspath);
      this.projectReferenceChanges.put(project, change);
    }
  }

  public void initializeRoots(boolean initAfterLoad) {

    // recompute root infos only if necessary
    HashMap[] rootInfos = null;
    if (this.rootsAreStale) {
      Thread currentThread = Thread.currentThread();
      boolean addedCurrentThread = false;
      try {
        // if reentering initialization (through a container initializer for example) no need to
        // compute roots again
        // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=47213
        if (!this.initializingThreads.add(currentThread)) return;
        addedCurrentThread = true;

        // all classpaths in the workspace are going to be resolved
        // ensure that containers are initialized in one batch
        manager.forceBatchInitializations(initAfterLoad);

        rootInfos = getRootInfos(false /*don't use previous session values*/);

      } finally {
        if (addedCurrentThread) {
          this.initializingThreads.remove(currentThread);
        }
      }
    }
    synchronized (this) {
      this.oldRoots = this.roots;
      this.oldOtherRoots = this.otherRoots;
      if (this.rootsAreStale && rootInfos != null) { // double check again
        this.roots = rootInfos[0];
        this.otherRoots = rootInfos[1];
        this.sourceAttachments = rootInfos[2];
        this.projectDependencies = rootInfos[3];
        this.rootsAreStale = false;
      }
    }
  }

  synchronized void initializeRootsWithPreviousSession() {
    HashMap[] rootInfos = getRootInfos(true /*use previous session values*/);
    if (rootInfos != null) {
      this.roots = rootInfos[0];
      this.otherRoots = rootInfos[1];
      this.sourceAttachments = rootInfos[2];
      this.projectDependencies = rootInfos[3];
      this.rootsAreStale = false;
    }
  }

  private HashMap[] getRootInfos(boolean usePreviousSession) {
    HashMap newRoots = new HashMap();
    HashMap newOtherRoots = new HashMap();
    HashMap newSourceAttachments = new HashMap();
    HashMap newProjectDependencies = new HashMap();

    IJavaModel model = manager.getJavaModel();
    IJavaProject[] projects;
    try {
      projects = model.getJavaProjects();
    } catch (JavaModelException e) {
      // nothing can be done
      return null;
    }
    for (int i = 0, length = projects.length; i < length; i++) {
      JavaProject project = (JavaProject) projects[i];
      IClasspathEntry[] classpath;
      try {
        //				if (usePreviousSession) {
        //					PerProjectInfo perProjectInfo = project.getPerProjectInfo();
        //					project.resolveClasspath(perProjectInfo, true/*use previous session values*/,
        // false/*don't add classpath change*/);
        //					classpath = perProjectInfo.resolvedClasspath;
        //				} else {
        classpath = project.getResolvedClasspath();
        //				}
      } catch (JavaModelException e) {
        // continue with next project
        continue;
      }
      for (int j = 0, classpathLength = classpath.length; j < classpathLength; j++) {
        IClasspathEntry entry = classpath[j];
        if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
          IJavaProject key =
              model.getJavaProject(entry.getPath().segment(0)); // TODO (jerome) reuse handle
          IJavaProject[] dependents = (IJavaProject[]) newProjectDependencies.get(key);
          if (dependents == null) {
            dependents = new IJavaProject[] {project};
          } else {
            int dependentsLength = dependents.length;
            System.arraycopy(
                dependents,
                0,
                dependents = new IJavaProject[dependentsLength + 1],
                0,
                dependentsLength);
            dependents[dependentsLength] = project;
          }
          newProjectDependencies.put(key, dependents);
          continue;
        }

        // root path
        IPath path = entry.getPath();
        if (newRoots.get(path) == null) {
          newRoots.put(
              path,
              new DeltaProcessor.RootInfo(
                  project,
                  path,
                  ((ClasspathEntry) entry).fullInclusionPatternChars(),
                  ((ClasspathEntry) entry).fullExclusionPatternChars(),
                  entry.getEntryKind()));
        } else {
          ArrayList rootList = (ArrayList) newOtherRoots.get(path);
          if (rootList == null) {
            rootList = new ArrayList();
            newOtherRoots.put(path, rootList);
          }
          rootList.add(
              new DeltaProcessor.RootInfo(
                  project,
                  path,
                  ((ClasspathEntry) entry).fullInclusionPatternChars(),
                  ((ClasspathEntry) entry).fullExclusionPatternChars(),
                  entry.getEntryKind()));
        }

        // source attachment path
        if (entry.getEntryKind() != IClasspathEntry.CPE_LIBRARY) continue;
        String propertyString = null;
        //				try {
        //					propertyString = Util.getSourceAttachmentProperty(path);
        //				} catch (JavaModelException e) {
        //					e.printStackTrace();
        //				}
        IPath sourceAttachmentPath;
        if (propertyString != null) {
          int index = propertyString.lastIndexOf(PackageFragmentRoot.ATTACHMENT_PROPERTY_DELIMITER);
          sourceAttachmentPath =
              (index < 0) ? new Path(propertyString) : new Path(propertyString.substring(0, index));
        } else {
          sourceAttachmentPath = entry.getSourceAttachmentPath();
        }
        if (sourceAttachmentPath != null) {
          newSourceAttachments.put(sourceAttachmentPath, path);
        }
      }
    }
    return new HashMap[] {newRoots, newOtherRoots, newSourceAttachments, newProjectDependencies};
  }

  //	public synchronized ClasspathValidation[] removeClasspathValidations() {
  //	    int length = this.classpathValidations.size();
  //	    if (length == 0) return null;
  //	    ClasspathValidation[]  validations = new ClasspathValidation[length];
  //	    this.classpathValidations.values().toArray(validations);
  //	    this.classpathValidations.clear();
  //	    return validations;
  //	}
  //
  //	public synchronized ExternalFolderChange[] removeExternalFolderChanges() {
  //	    int length = this.externalFolderChanges.size();
  //	    if (length == 0) return null;
  //	    ExternalFolderChange[]  updates = new ExternalFolderChange[length];
  //	    this.externalFolderChanges.values().toArray(updates);
  //	    this.externalFolderChanges.clear();
  //	    return updates;
  //	}
  //
  //	public synchronized ProjectReferenceChange[] removeProjectReferenceChanges() {
  //	    int length = this.projectReferenceChanges.size();
  //	    if (length == 0) return null;
  //	    ProjectReferenceChange[]  updates = new ProjectReferenceChange[length];
  //	    this.projectReferenceChanges.values().toArray(updates);
  //	    this.projectReferenceChanges.clear();
  //	    return updates;
  //	}

  public synchronized HashSet removeExternalElementsToRefresh() {
    HashSet result = this.externalElementsToRefresh;
    this.externalElementsToRefresh = null;
    return result;
  }

  public synchronized void removeElementChangedListener(IElementChangedListener listener) {

    for (int i = 0; i < this.elementChangedListenerCount; i++) {

      if (this.elementChangedListeners[i] == listener) {

        // need to clone defensively since we might be in the middle of listener notifications
        // (#fire)
        int length = this.elementChangedListeners.length;
        IElementChangedListener[] newListeners = new IElementChangedListener[length];
        System.arraycopy(this.elementChangedListeners, 0, newListeners, 0, i);
        int[] newMasks = new int[length];
        System.arraycopy(this.elementChangedListenerMasks, 0, newMasks, 0, i);

        // copy trailing listeners
        int trailingLength = this.elementChangedListenerCount - i - 1;
        if (trailingLength > 0) {
          System.arraycopy(this.elementChangedListeners, i + 1, newListeners, i, trailingLength);
          System.arraycopy(this.elementChangedListenerMasks, i + 1, newMasks, i, trailingLength);
        }

        // update manager listener state (#fire need to iterate over original listeners through a
        // local variable to hold onto
        // the original ones)
        this.elementChangedListeners = newListeners;
        this.elementChangedListenerMasks = newMasks;
        this.elementChangedListenerCount--;
        return;
      }
    }
  }

  public synchronized void removePreResourceChangedListener(IResourceChangeListener listener) {

    for (int i = 0; i < this.preResourceChangeListenerCount; i++) {

      if (this.preResourceChangeListeners[i] == listener) {

        // need to clone defensively since we might be in the middle of listener notifications
        // (#fire)
        int length = this.preResourceChangeListeners.length;
        IResourceChangeListener[] newListeners = new IResourceChangeListener[length];
        int[] newEventMasks = new int[length];
        System.arraycopy(this.preResourceChangeListeners, 0, newListeners, 0, i);
        System.arraycopy(this.preResourceChangeEventMasks, 0, newEventMasks, 0, i);

        // copy trailing listeners
        int trailingLength = this.preResourceChangeListenerCount - i - 1;
        if (trailingLength > 0) {
          System.arraycopy(this.preResourceChangeListeners, i + 1, newListeners, i, trailingLength);
          System.arraycopy(
              this.preResourceChangeEventMasks, i + 1, newEventMasks, i, trailingLength);
        }

        // update manager listener state (#fire need to iterate over original listeners through a
        // local variable to hold onto
        // the original ones)
        this.preResourceChangeListeners = newListeners;
        this.preResourceChangeEventMasks = newEventMasks;
        this.preResourceChangeListenerCount--;
        return;
      }
    }
  }

  public void resourceChanged(final IResourceChangeEvent event) {
    for (int i = 0; i < this.preResourceChangeListenerCount; i++) {
      // wrap callbacks with Safe runnable for subsequent listeners to be called when some are
      // causing grief
      final IResourceChangeListener listener = this.preResourceChangeListeners[i];
      if ((this.preResourceChangeEventMasks[i] & event.getType()) != 0)
        SafeRunner.run(
            new ISafeRunnable() {
              public void handleException(Throwable exception) {
                Util.log(
                    exception,
                    "Exception occurred in listener of pre Java resource change notification"); // $NON-NLS-1$
              }

              public void run() throws Exception {
                listener.resourceChanged(event);
              }
            });
    }
    try {
      getDeltaProcessor().resourceChanged(event);
    } finally {
      // TODO (jerome) see 47631, may want to get rid of following so as to reuse delta processor ?
      if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
        this.deltaProcessors.set(null);
      } else {
        // If we are going to reuse the delta processor of this thread, don't hang on to state
        // that isn't meant to be reused. https://bugs.eclipse.org/bugs/show_bug.cgi?id=273385
        getDeltaProcessor().overridenEventType = -1;
      }
    }
  }

  public Hashtable getExternalLibTimeStamps() {
    if (this.externalTimeStamps == null) {
      Hashtable timeStamps = new Hashtable();
      File timestampsFile = getTimeStampsFile();
      DataInputStream in = null;
      try {
        in = new DataInputStream(new BufferedInputStream(new FileInputStream(timestampsFile)));
        int size = in.readInt();
        while (size-- > 0) {
          String key = in.readUTF();
          long timestamp = in.readLong();
          timeStamps.put(Path.fromPortableString(key), new Long(timestamp));
        }
      } catch (IOException e) {
        if (timestampsFile.exists())
          Util.log(e, "Unable to read external time stamps"); // $NON-NLS-1$
      } finally {
        if (in != null) {
          try {
            in.close();
          } catch (IOException e) {
            // nothing we can do: ignore
          }
        }
      }
      this.externalTimeStamps = timeStamps;
    }
    return this.externalTimeStamps;
  }

  public IJavaProject findJavaProject(String name) {
    if (getOldJavaProjecNames().contains(name)) return manager.getJavaModel().getJavaProject(name);
    return null;
  }

  /*
   * Workaround for bug 15168 circular errors not reported
   * Returns the list of java projects before resource delta processing
   * has started.
   */
  public synchronized HashSet getOldJavaProjecNames() {
    if (this.javaProjectNamesCache == null) {
      HashSet result = new HashSet();
      IJavaProject[] projects;
      try {
        projects = manager.getJavaModel().getJavaProjects();
      } catch (JavaModelException e) {
        return this.javaProjectNamesCache;
      }
      for (int i = 0, length = projects.length; i < length; i++) {
        IJavaProject project = projects[i];
        result.add(project.getElementName());
      }
      return this.javaProjectNamesCache = result;
    }
    return this.javaProjectNamesCache;
  }

  public synchronized void resetOldJavaProjectNames() {
    this.javaProjectNamesCache = null;
  }

  private File getTimeStampsFile() {
    return JavaCore.getPlugin()
        .getStateLocation()
        .append("externalLibsTimeStamps")
        .toFile(); // $NON-NLS-1$
  }

  public void saveExternalLibTimeStamps() throws CoreException {
    if (this.externalTimeStamps == null) return;

    // cleanup to avoid any leak ( https://bugs.eclipse.org/bugs/show_bug.cgi?id=244849 )
    HashSet toRemove = new HashSet();
    if (this.roots != null) {
      Enumeration keys = this.externalTimeStamps.keys();
      while (keys.hasMoreElements()) {
        Object key = keys.nextElement();
        if (this.roots.get(key) == null) {
          toRemove.add(key);
        }
      }
    }

    File timestamps = getTimeStampsFile();
    DataOutputStream out = null;
    try {
      out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(timestamps)));
      out.writeInt(this.externalTimeStamps.size() - toRemove.size());
      Iterator entries = this.externalTimeStamps.entrySet().iterator();
      while (entries.hasNext()) {
        Map.Entry entry = (Map.Entry) entries.next();
        IPath key = (IPath) entry.getKey();
        if (!toRemove.contains(key)) {
          out.writeUTF(key.toPortableString());
          Long timestamp = (Long) entry.getValue();
          out.writeLong(timestamp.longValue());
        }
      }
    } catch (IOException e) {
      IStatus status =
          new Status(
              IStatus.ERROR,
              JavaCore.PLUGIN_ID,
              IStatus.ERROR,
              "Problems while saving timestamps",
              e); // $NON-NLS-1$
      throw new CoreException(status);
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {
          // nothing we can do: ignore
        }
      }
    }
  }

  /*
   * Update the roots that are affected by the addition or the removal of the given container resource.
   */
  public synchronized void updateRoots(
      IPath containerPath, IResourceDelta containerDelta, DeltaProcessor deltaProcessor) {
    Map updatedRoots;
    Map otherUpdatedRoots;
    if (containerDelta.getKind() == IResourceDelta.REMOVED) {
      updatedRoots = this.oldRoots;
      otherUpdatedRoots = this.oldOtherRoots;
    } else {
      updatedRoots = this.roots;
      otherUpdatedRoots = this.otherRoots;
    }
    int containerSegmentCount = containerPath.segmentCount();
    boolean containerIsProject = containerSegmentCount == 1;
    Iterator iterator = updatedRoots.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry entry = (Map.Entry) iterator.next();
      IPath path = (IPath) entry.getKey();
      if (containerPath.isPrefixOf(path) && !containerPath.equals(path)) {
        IResourceDelta rootDelta =
            (IResourceDelta)
                containerDelta.findMember(path.removeFirstSegments(containerSegmentCount));
        if (rootDelta == null) continue;
        DeltaProcessor.RootInfo rootInfo = (DeltaProcessor.RootInfo) entry.getValue();

        if (!containerIsProject
            || !rootInfo
                .project
                .getPath()
                .isPrefixOf(
                    path)) { // only consider folder roots that are not included in the container
          deltaProcessor.updateCurrentDeltaAndIndex(
              rootDelta, IJavaElement.PACKAGE_FRAGMENT_ROOT, rootInfo);
        }

        ArrayList rootList = (ArrayList) otherUpdatedRoots.get(path);
        if (rootList != null) {
          Iterator otherProjects = rootList.iterator();
          while (otherProjects.hasNext()) {
            rootInfo = (DeltaProcessor.RootInfo) otherProjects.next();
            if (!containerIsProject
                || !rootInfo
                    .project
                    .getPath()
                    .isPrefixOf(path)) { // only consider folder roots that are not included in the
              // container
              deltaProcessor.updateCurrentDeltaAndIndex(
                  rootDelta, IJavaElement.PACKAGE_FRAGMENT_ROOT, rootInfo);
            }
          }
        }
      }
    }
  }
}
