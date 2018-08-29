/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2007 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.core.internal.filebuffers;

import com.google.inject.Singleton;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The plug-in runtime class for the file buffers plug-in (id <code>"org.eclipse.core.filebuffers"
 * </code>).
 *
 * @since 3.0
 */
@Singleton
public class FileBuffersPlugin /*extends Plugin*/ {
  private static final Logger LOG = LoggerFactory.getLogger(FileBuffersPlugin.class);
  public static final String PLUGIN_ID = "org.eclipse.core.filebuffers"; // $NON-NLS-1$

  /** The shared plug-in instance */
  private static FileBuffersPlugin fgPlugin;
  /** The file buffer manager */
  private ITextFileBufferManager fTextFileBufferManager;

  /** Creates a plug-in instance. */
  public FileBuffersPlugin() {
    Assert.isTrue(fgPlugin == null);
    fgPlugin = this;
  }

  /**
   * Returns the shared instance.
   *
   * @return the default plug-in instance
   */
  public static FileBuffersPlugin getDefault() {
    return fgPlugin;
  }

  /**
   * Returns the text file buffer manager of this plug-in.
   *
   * @return the text file buffer manager of this plug-in
   */
  public synchronized ITextFileBufferManager getFileBufferManager() {
    if (fTextFileBufferManager == null) {
      //			Bundle resourcesBundle = Platform.getBundle("org.eclipse.core.resources"); //$NON-NLS-1$
      //			if (resourcesBundle != null)
      //				fTextFileBufferManager = new ResourceTextFileBufferManager();
      //			else
      fTextFileBufferManager = new TextFileBufferManager();
    }
    return fTextFileBufferManager;
  }

  public void log(IStatus status) {
    LOG.error(status.getMessage(), status.getException());
  }
}
