/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.shared.dto.progressor;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface ProgressReportDto {
  /** @return the task */
  String getTask();

  /** @param task the task to set */
  void setTask(String task);

  /** @return the status */
  String getStatus();

  /** @param status the status to set */
  void setStatus(String status);

  /** @return the complete */
  boolean isComplete();

  /** @param complete the complete to set */
  void setComplete(boolean complete);

  /** @return the totalWork */
  int getTotalWork();

  /** @param totalWork the totalWork to set */
  void setTotalWork(int totalWork);

  /** @return the workDone */
  int getWorkDone();

  /** @param workDone the workDone to set */
  void setWorkDone(int workDone);

  /** @return the task type */
  String getSubTask();

  /** @param subTask the subTask to set */
  void setSubTask(String subTask);

  /** @return the report id */
  String getId();

  /** @param id the report id to set */
  void setId(String id);
}
