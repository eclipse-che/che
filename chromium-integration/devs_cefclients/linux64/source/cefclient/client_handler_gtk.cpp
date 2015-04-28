// Copyright (c) 2014 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

#include <gtk/gtk.h>
#include <libgen.h>
#include <X11/Xatom.h>
#include <X11/Xlib.h>
#undef Success  // Definition conflicts with cef_message_router.h

#include <string>

#include "cefclient/client_handler.h"
#include "include/cef_browser.h"
#include "include/cef_frame.h"
#include "include/cef_url.h"

namespace {

const char kPromptTextId[] = "cef_prompt_text";

// If there's a text entry in the dialog, get the text from the first one and
// return it.
std::string GetPromptText(GtkDialog* dialog) {
  GtkWidget* widget = static_cast<GtkWidget*>(
      g_object_get_data(G_OBJECT(dialog), kPromptTextId));
  if (widget)
    return gtk_entry_get_text(GTK_ENTRY(widget));
  return std::string();
}

std::string GetDescriptionFromMimeType(const std::string& mime_type) {
  // Check for wild card mime types and return an appropriate description.
  static const struct {
    const char* mime_type;
    const char* label;
  } kWildCardMimeTypes[] = {
    { "audio", "Audio Files" },
    { "image", "Image Files" },
    { "text", "Text Files" },
    { "video", "Video Files" },
  };

  for (size_t i = 0;
       i < sizeof(kWildCardMimeTypes) / sizeof(kWildCardMimeTypes[0]); ++i) {
    if (mime_type == std::string(kWildCardMimeTypes[i].mime_type) + "/*")
      return std::string(kWildCardMimeTypes[i].label);
  }

  return std::string();
}

void AddFiltersForAcceptTypes(GtkFileChooser* chooser,
                              const std::vector<CefString>& accept_types,
                              bool include_all_files) {
  bool has_filter = false;

  for (size_t i = 0; i < accept_types.size(); ++i) {
    std::string ascii_type = accept_types[i];
    if (ascii_type.length()) {
      // Just treat as extension if contains '.' as the first character.
      if (ascii_type[0] == '.') {
        GtkFileFilter* filter = gtk_file_filter_new();
        std::string pattern = "*" + ascii_type;
        gtk_file_filter_add_pattern(filter, pattern.c_str());
        gtk_file_filter_set_name(filter, pattern.c_str());
        gtk_file_chooser_add_filter(chooser, filter);
        if (!has_filter)
          has_filter = true;
      } else {
        // Otherwise convert mime type to one or more extensions.
        GtkFileFilter* filter = NULL;
        std::string description = GetDescriptionFromMimeType(ascii_type);
        bool description_from_ext = description.empty();

        std::vector<CefString> ext;
        CefGetExtensionsForMimeType(ascii_type, ext);
        for (size_t x = 0; x < ext.size(); ++x) {
          if (!filter)
            filter = gtk_file_filter_new();
          std::string pattern = "*." + ext[x].ToString();
          gtk_file_filter_add_pattern(filter, pattern.c_str());

          if (description_from_ext) {
            if (x != 0)
              description += ";";
            description += pattern;
          }
        }

        if (filter) {
          gtk_file_filter_set_name(filter, description.c_str());
          gtk_file_chooser_add_filter(chooser, filter);
          if (!has_filter)
            has_filter = true;
        }
      }
    }
  }

  // Add the *.* filter, but only if we have added other filters (otherwise it
  // is implied).
  if (include_all_files && has_filter) {
    GtkFileFilter* filter = gtk_file_filter_new();
    gtk_file_filter_add_pattern(filter, "*");
    gtk_file_filter_set_name(filter, "All Files");
    gtk_file_chooser_add_filter(chooser, filter);
  }
}

}  // namespace

bool ClientHandler::OnFileDialog(CefRefPtr<CefBrowser> browser,
                                 FileDialogMode mode,
                                 const CefString& title,
                                 const CefString& default_file_name,
                                 const std::vector<CefString>& accept_types,
                                 CefRefPtr<CefFileDialogCallback> callback) {
  std::vector<CefString> files;

  GtkFileChooserAction action;
  const gchar* accept_button;
  if (mode == FILE_DIALOG_OPEN || mode == FILE_DIALOG_OPEN_MULTIPLE) {
    action = GTK_FILE_CHOOSER_ACTION_OPEN;
    accept_button = GTK_STOCK_OPEN;
  } else if (mode == FILE_DIALOG_SAVE) {
    action = GTK_FILE_CHOOSER_ACTION_SAVE;
    accept_button = GTK_STOCK_SAVE;
  } else {
    NOTREACHED();
    return false;
  }

  std::string base_name;
  if (!default_file_name.empty()) {
    base_name =
        basename(const_cast<char*>(default_file_name.ToString().c_str()));
  }

  std::string title_str;
  if (!title.empty()) {
    title_str = title;
  } else {
    switch (mode) {
      case FILE_DIALOG_OPEN:
        title_str = "Open File";
        break;
      case FILE_DIALOG_OPEN_MULTIPLE:
        title_str = "Open Files";
        break;
      case FILE_DIALOG_SAVE:
        title_str = "Save File";
        break;
      default:
        break;
    }
  }

  GtkWidget* window = gtk_widget_get_ancestor(
      GTK_WIDGET(GetMainWindowHandle()), GTK_TYPE_WINDOW);
  GtkWidget* dialog = gtk_file_chooser_dialog_new(
      title_str.c_str(),
      GTK_WINDOW(window),
      action,
      GTK_STOCK_CANCEL, GTK_RESPONSE_CANCEL,
      accept_button, GTK_RESPONSE_ACCEPT,
      NULL);

  if (mode == FILE_DIALOG_OPEN_MULTIPLE) {
    gtk_file_chooser_set_select_multiple(GTK_FILE_CHOOSER(dialog), TRUE);
  } else if (mode == FILE_DIALOG_SAVE) {
    gtk_file_chooser_set_do_overwrite_confirmation(GTK_FILE_CHOOSER(dialog),
                                                   TRUE);
  }

  if (mode == FILE_DIALOG_SAVE && !base_name.empty()) {
    gtk_file_chooser_set_current_name(GTK_FILE_CHOOSER(dialog),
                                      base_name.c_str());
  }

  AddFiltersForAcceptTypes(GTK_FILE_CHOOSER(dialog), accept_types, true);

  bool success = false;

  if (gtk_dialog_run(GTK_DIALOG(dialog)) == GTK_RESPONSE_ACCEPT) {
    if (mode == FILE_DIALOG_OPEN || mode == FILE_DIALOG_SAVE) {
      char* filename = gtk_file_chooser_get_filename(GTK_FILE_CHOOSER(dialog));
      files.push_back(std::string(filename));
      success = true;
    } else if (mode == FILE_DIALOG_OPEN_MULTIPLE) {
      GSList* filenames =
          gtk_file_chooser_get_filenames(GTK_FILE_CHOOSER(dialog));
      if (filenames) {
        for (GSList* iter = filenames; iter != NULL;
             iter = g_slist_next(iter)) {
          std::string path(static_cast<char*>(iter->data));
          g_free(iter->data);
          files.push_back(path);
        }
        g_slist_free(filenames);
        success = true;
      }
    }
  }

  gtk_widget_destroy(dialog);

  if (success)
    callback->Continue(files);
  else
    callback->Cancel();

  return true;
}

void ClientHandler::OnAddressChange(CefRefPtr<CefBrowser> browser,
                                    CefRefPtr<CefFrame> frame,
                                    const CefString& url) {
  CEF_REQUIRE_UI_THREAD();

  if (GetBrowserId() == browser->GetIdentifier() && frame->IsMain()) {
      // Set the edit window text
    std::string urlStr(url);
    gtk_entry_set_text(GTK_ENTRY(edit_handle_), urlStr.c_str());
  }
}

void ClientHandler::OnTitleChange(CefRefPtr<CefBrowser> browser) {
  CEF_REQUIRE_UI_THREAD();

  std::string titleStr = "WSO2 Developer Studio";

  if (!browser->IsPopup()) {
    // Set the GTK parent window title.
    GtkWidget* window = gtk_widget_get_ancestor(main_handle_,
        GTK_TYPE_WINDOW);
    gtk_window_set_title(GTK_WINDOW(window), titleStr.c_str());
  } else {
    // Retrieve the X11 display shared with Chromium.
    ::Display* display = cef_get_xdisplay();
    DCHECK(display);

    // Retrieve the X11 window handle for the browser.
    ::Window window = browser->GetHost()->GetWindowHandle();
    DCHECK(window != kNullWindowHandle);

    // Retrieve the atoms required by the below XChangeProperty call.
    const char* kAtoms[] = {
      "_NET_WM_NAME",
      "UTF8_STRING"
    };
    Atom atoms[2];
    int result = XInternAtoms(display, const_cast<char**>(kAtoms), 2, false,
                              atoms);
    if (!result)
      NOTREACHED();

    // Set the window title.
    XChangeProperty(display,
                    window,
                    atoms[0],
                    atoms[1],
                    8,
                    PropModeReplace,
                    reinterpret_cast<const unsigned char*>(titleStr.c_str()),
                    titleStr.size());

    // TODO(erg): This is technically wrong. So XStoreName and friends expect
    // this in Host Portable Character Encoding instead of UTF-8, which I
    // believe is Compound Text. This shouldn't matter 90% of the time since
    // this is the fallback to the UTF8 property above.
    XStoreName(display, browser->GetHost()->GetWindowHandle(),
               titleStr.c_str());
  }



}

bool ClientHandler::OnJSDialog(CefRefPtr<CefBrowser> browser,
                               const CefString& origin_url,
                               const CefString& accept_lang,
                               JSDialogType dialog_type,
                               const CefString& message_text,
                               const CefString& default_prompt_text,
                               CefRefPtr<CefJSDialogCallback> callback,
                               bool& suppress_message) {
  CEF_REQUIRE_UI_THREAD();

  GtkButtonsType buttons = GTK_BUTTONS_NONE;
  GtkMessageType gtk_message_type = GTK_MESSAGE_OTHER;
  std::string title = "WSO2 Developer Studio";

  switch (dialog_type) {
    case JSDIALOGTYPE_ALERT:
      buttons = GTK_BUTTONS_NONE;
      gtk_message_type = GTK_MESSAGE_WARNING;
      title = "JavaScript Alert";
      break;

    case JSDIALOGTYPE_CONFIRM:
      buttons = GTK_BUTTONS_CANCEL;
      gtk_message_type = GTK_MESSAGE_QUESTION;
      title = "JavaScript Confirm";
      break;

    case JSDIALOGTYPE_PROMPT:
      buttons = GTK_BUTTONS_CANCEL;
      gtk_message_type = GTK_MESSAGE_QUESTION;
      title = "JavaScript Prompt";
      break;
  }

  js_dialog_callback_ = callback;

  if (!origin_url.empty()) {
    title += " - ";
    title += origin_url.ToString();
  }

  GtkWidget* window = gtk_widget_get_ancestor(
      GTK_WIDGET(GetMainWindowHandle()), GTK_TYPE_WINDOW);
  gtk_dialog_ = gtk_message_dialog_new(GTK_WINDOW(window),
                                       GTK_DIALOG_MODAL,
                                       gtk_message_type,
                                       buttons,
                                       "%s",
                                       message_text.ToString().c_str());
  g_signal_connect(gtk_dialog_,
                   "delete-event",
                   G_CALLBACK(gtk_widget_hide_on_delete),
                   NULL);

  gtk_window_set_title(GTK_WINDOW(gtk_dialog_), title.c_str());

  GtkWidget* ok_button = gtk_dialog_add_button(GTK_DIALOG(gtk_dialog_),
                                               GTK_STOCK_OK,
                                               GTK_RESPONSE_OK);

  if (dialog_type != JSDIALOGTYPE_PROMPT)
    gtk_widget_grab_focus(ok_button);

  if (dialog_type == JSDIALOGTYPE_PROMPT) {
    GtkWidget* content_area =
        gtk_dialog_get_content_area(GTK_DIALOG(gtk_dialog_));
    GtkWidget* text_box = gtk_entry_new();
    gtk_entry_set_text(GTK_ENTRY(text_box),
                       default_prompt_text.ToString().c_str());
    gtk_box_pack_start(GTK_BOX(content_area), text_box, TRUE, TRUE, 0);
    g_object_set_data(G_OBJECT(gtk_dialog_), kPromptTextId, text_box);
    gtk_entry_set_activates_default(GTK_ENTRY(text_box), TRUE);
  }

  gtk_dialog_set_default_response(GTK_DIALOG(gtk_dialog_), GTK_RESPONSE_OK);
  g_signal_connect(gtk_dialog_, "response", G_CALLBACK(OnDialogResponse), this);
  gtk_widget_show_all(GTK_WIDGET(gtk_dialog_));

  return true;
}

bool ClientHandler::OnBeforeUnloadDialog(
    CefRefPtr<CefBrowser> browser,
    const CefString& message_text,
    bool is_reload,
    CefRefPtr<CefJSDialogCallback> callback) {
  CEF_REQUIRE_UI_THREAD();

  const std::string& new_message_text =
      message_text.ToString() + "\n\nIs it OK to leave/reload this page?";
  bool suppress_message = false;

  return OnJSDialog(browser, CefString(), CefString(), JSDIALOGTYPE_CONFIRM,
                    new_message_text, CefString(), callback, suppress_message);
}

void ClientHandler::OnResetDialogState(CefRefPtr<CefBrowser> browser) {
  CEF_REQUIRE_UI_THREAD();

  if (!gtk_dialog_)
    return;
  gtk_widget_destroy(gtk_dialog_);
  gtk_dialog_ = NULL;
  js_dialog_callback_ = NULL;
}

// static
void ClientHandler::OnDialogResponse(GtkDialog* dialog,
                                     gint response_id,
                                     ClientHandler* handler) {
  CEF_REQUIRE_UI_THREAD();

  DCHECK_EQ(dialog, GTK_DIALOG(handler->gtk_dialog_));
  switch (response_id) {
    case GTK_RESPONSE_OK:
      handler->js_dialog_callback_->Continue(true, GetPromptText(dialog));
      break;
    case GTK_RESPONSE_CANCEL:
    case GTK_RESPONSE_DELETE_EVENT:
      handler->js_dialog_callback_->Continue(false, CefString());
      break;
    default:
      NOTREACHED();
  }

  handler->OnResetDialogState(NULL);
}

void ClientHandler::SendNotification(NotificationType type) {
  // TODO(port): Implement this method.
}

void ClientHandler::SetLoading(bool isLoading) {
  CEF_REQUIRE_UI_THREAD();

  if (isLoading)
    gtk_widget_set_sensitive(GTK_WIDGET(stop_handle_), true);
  else
    gtk_widget_set_sensitive(GTK_WIDGET(stop_handle_), false);
}

void ClientHandler::SetNavState(bool canGoBack, bool canGoForward) {
  CEF_REQUIRE_UI_THREAD();

  if (canGoBack)
    gtk_widget_set_sensitive(GTK_WIDGET(back_handle_), true);
  else
    gtk_widget_set_sensitive(GTK_WIDGET(back_handle_), false);

  if (canGoForward)
    gtk_widget_set_sensitive(GTK_WIDGET(forward_handle_), true);
  else
    gtk_widget_set_sensitive(GTK_WIDGET(forward_handle_), false);
}

std::string ClientHandler::GetDownloadPath(const std::string& file_name) {
  return std::string();
}
