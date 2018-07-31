/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.projectimport.wizard;

import static org.eclipse.che.api.project.shared.Constants.EVENT_IMPORT_OUTPUT_PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.*;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.function.Consumer;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.eclipse.che.api.project.shared.dto.ImportProgressRecordDto;

/** Handles project import progression reports */
@Singleton
public class ImportProgressJsonRpcHandler {
  private Consumer<ImportProgressRecordDto> consumer;

  @Inject
  public ImportProgressJsonRpcHandler(
      RequestHandlerConfigurator configurator, RequestHandlerManager requestHandlerManager) {
    if (!requestHandlerManager.isRegistered(EVENT_IMPORT_OUTPUT_PROGRESS)) {
      configurator
          .newConfiguration()
          .methodName(EVENT_IMPORT_OUTPUT_PROGRESS)
          .paramsAsDto(ImportProgressRecordDto.class)
          .noResult()
          .withConsumer(
              record -> {
                if (consumer != null) {
                  consumer.accept(record);
                }
              });
    }
  }

  public void setConsumer(Consumer<ImportProgressRecordDto> consumer) {
    this.consumer = consumer;
  }

  public void unsetConsumer() {
    this.consumer = null;
  }
}
