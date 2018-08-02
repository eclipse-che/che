/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.maven.server;

import java.io.File;
import java.rmi.RemoteException;
import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferListener;
import org.eclipse.aether.transfer.TransferResource;
import org.eclipse.che.commons.lang.Size;

/** @author Evgen Vidolob */
public class ArtifactTransferListener implements TransferListener {
  private MavenServerProgressNotifierImpl notifier;

  public ArtifactTransferListener(MavenServerProgressNotifierImpl notifier) {
    this.notifier = notifier;
  }

  private String getMassage(TransferEvent event) {
    TransferResource resource = event.getResource();
    File file = resource.getFile();
    String repo = " [" + resource.getRepositoryUrl() + "]";
    if (file == null) {
      return resource.getResourceName() + repo;
    } else {
      return file.getName() + repo;
    }
  }

  @Override
  public void transferInitiated(TransferEvent transferEvent) throws TransferCancelledException {
    try {
      notifier.setPercentUndefined(true);
      notifier.setText(getMassage(transferEvent));
    } catch (RemoteException e) {
      throw new RuntimeRemoteException(e);
    }
  }

  @Override
  public void transferStarted(TransferEvent transferEvent) throws TransferCancelledException {
    transferProgressed(transferEvent);
  }

  @Override
  public void transferProgressed(TransferEvent transferEvent) throws TransferCancelledException {
    TransferResource resource = transferEvent.getResource();

    long contentLength = resource.getContentLength();
    String sizeString;
    if (contentLength <= 0) {
      sizeString = Size.toHumanSize(transferEvent.getTransferredBytes()) + "/ ?";
    } else {
      sizeString =
          Size.toHumanSize(transferEvent.getTransferredBytes())
              + " / "
              + Size.toHumanSize(contentLength);
    }

    try {
      notifier.setText(sizeString);
      if (contentLength <= 0) {
        notifier.setPercentUndefined(true);
      } else {
        notifier.setPercentUndefined(false);
        notifier.setPercent((double) transferEvent.getTransferredBytes() / (double) contentLength);
      }
    } catch (RemoteException e) {
      throw new RuntimeRemoteException(e);
    }
  }

  @Override
  public void transferCorrupted(TransferEvent transferEvent) throws TransferCancelledException {
    transferFailed(transferEvent);
  }

  @Override
  public void transferSucceeded(TransferEvent transferEvent) {
    try {
      notifier.setText("Downloaded: " + getMassage(transferEvent));
      notifier.setPercentUndefined(true);
    } catch (RemoteException e) {
      throw new RuntimeRemoteException(e);
    }
  }

  @Override
  public void transferFailed(TransferEvent transferEvent) {
    try {
      notifier.setText("Download failed: " + getMassage(transferEvent));
      notifier.setPercentUndefined(true);
    } catch (RemoteException e) {
      throw new RuntimeRemoteException(e);
    }
  }
}
