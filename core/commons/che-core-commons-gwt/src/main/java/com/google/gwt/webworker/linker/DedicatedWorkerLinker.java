/*
 * Copyright 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.webworker.linker;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.LinkerOrder.Order;
import com.google.gwt.core.ext.linker.impl.SelectionScriptLinker;

/**
 * Linker for HTML 5 Dedicated Web Workers.
 */
@LinkerOrder(Order.PRIMARY)
public class DedicatedWorkerLinker extends SelectionScriptLinker {

  @Override
  public String getDescription() {
    return "Dedicated Web Worker Linker";
  }

  @Override
  protected String getCompilationExtension(TreeLogger logger,
      LinkerContext context) throws UnableToCompleteException {
    return ".cache.js";
  }

  @Override
  protected String getModulePrefix(TreeLogger logger, LinkerContext context,
      String strongName) throws UnableToCompleteException {
    return "";
  }

  @Override
  protected String getModuleSuffix(TreeLogger logger, LinkerContext context)
      throws UnableToCompleteException {
    return "";
  }

  @Override
  protected String getSelectionScriptTemplate(TreeLogger logger,
      LinkerContext context) throws UnableToCompleteException {
    return "com/google/gwt/webworker/linker/DedicatedWorkerTemplate.js";
  }

}
