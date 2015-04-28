// Copyright (c) 2014 The Chromium Embedded Framework Authors.
// Portions Copyright (c) 2012 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#ifndef CEF_TESTS_CEFCLIENT_PRINT_HANDLER_GTK_H_
#define CEF_TESTS_CEFCLIENT_PRINT_HANDLER_GTK_H_
#pragma once

#include <gtk/gtk.h>
#include <gtk/gtkunixprint.h>

#include "include/cef_print_handler.h"

class ClientPrintHandlerGtk : public CefPrintHandler {
 public:
  ClientPrintHandlerGtk();

  // CefPrintHandler methods
  virtual void OnPrintSettings(CefRefPtr<CefPrintSettings> settings,
                               bool get_defaults) OVERRIDE;
  virtual bool OnPrintDialog(
      bool has_selection,
      CefRefPtr<CefPrintDialogCallback> callback) OVERRIDE;
  virtual bool OnPrintJob(const CefString& document_name,
                          const CefString& pdf_file_path,
                          CefRefPtr<CefPrintJobCallback> callback) OVERRIDE;
  virtual void OnPrintReset() OVERRIDE;

 private:
  void OnDialogResponse(GtkDialog *dialog,
                        gint response_id);
  void OnJobCompleted(GtkPrintJob* print_job,
                      GError* error);

  static void OnDialogResponseThunk(GtkDialog *dialog,
                                    gint response_id,
                                    ClientPrintHandlerGtk* handler) {
    handler->OnDialogResponse(dialog, response_id);
  }
  static void OnJobCompletedThunk(GtkPrintJob* print_job,
                                  void* handler,
                                  GError* error) {
    static_cast<ClientPrintHandlerGtk*>(handler)->
        OnJobCompleted(print_job, error);
  }

  // Print dialog settings. ClientPrintHandlerGtk owns |dialog_| and holds
  // references to the other objects.
  GtkWidget* dialog_;
  GtkPrintSettings* gtk_settings_;
  GtkPageSetup* page_setup_;
  GtkPrinter* printer_;

  CefRefPtr<CefPrintDialogCallback> dialog_callback_;
  CefRefPtr<CefPrintJobCallback> job_callback_;

  IMPLEMENT_REFCOUNTING(ClientPrintHandlerGtk);
};

#endif  // CEF_TESTS_CEFCLIENT_PRINT_HANDLER_GTK_H_

