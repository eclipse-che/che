/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.jface.text.templates.ContextTypeRegistry;
import org.eclipse.che.jface.text.templates.persistence.TemplateStore;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaCorePreferenceInitializer;
import org.eclipse.jdt.internal.corext.format.CheCodeFormatterInitializer;
import org.eclipse.jdt.internal.corext.template.java.AbstractJavaContextType;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;
import org.eclipse.jdt.internal.corext.template.java.JavaContextType;
import org.eclipse.jdt.internal.corext.util.QualifiedTypeNameHistory;
import org.eclipse.jdt.internal.corext.util.TypeFilter;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.jdt.internal.ui.preferences.MembersOrderPreferenceCache;
import org.eclipse.jdt.internal.ui.text.java.ContentAssistHistory;
import org.eclipse.jdt.internal.ui.viewsupport.ImageDescriptorRegistry;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Evgen Vidolob */
public class JavaPlugin {

  /**
   * The editor part id of the editor that presents Java compilation units (value <code>
   * "org.eclipse.jdt.ui.CompilationUnitEditor"</code>).
   */
  public static final String ID_CU_EDITOR =
      "org.eclipse.jdt.ui.CompilationUnitEditor"; // $NON-NLS-1$
  /** The id of the Java plug-in (value <code>"org.eclipse.jdt.ui"</code>). */
  public static final String ID_PLUGIN = "org.eclipse.jdt.ui"; // $NON-NLS-1$

  public static final String CODEASSIST_LRU_HISTORY =
      "/content_assist_lru_history.xml"; // $NON-NLS-1$

  /** The name of the dialog settings file (value <code>"dialog_settings.xml"</code>). */
  private static final String FN_DIALOG_SETTINGS = "dialog_settings.xml"; // $NON-NLS-1$
  /**
   * The key to store customized code templates.
   *
   * @since 3.0
   */
  private static final String CODE_TEMPLATES_KEY =
      "org.eclipse.jdt.ui.text.custom_code_templates"; // $NON-NLS-1$
  /**
   * The key to store customized templates.
   *
   * @since 3.0
   */
  private static final String TEMPLATES_KEY =
      "org.eclipse.jdt.ui.text.custom_templates"; // $NON-NLS-1$

  private static final Logger LOG = LoggerFactory.getLogger(JavaPlugin.class);
  private static JavaPlugin fgJavaPlugin;
  /** Storage for dialog and wizard data; <code>null</code> if not yet initialized. */
  private IDialogSettings dialogSettings = null;
  /**
   * Default instance of the appearance type filters.
   *
   * @since 3.0
   */
  private TypeFilter fTypeFilter;

  /**
   * The template store for the java editor.
   *
   * @since 3.0
   */
  private TemplateStore fTemplateStore;
  /**
   * The coded template store for the java editor.
   *
   * @since 3.0
   */
  private TemplateStore fCodeTemplateStore;

  /**
   * The code template context type registry for the java editor.
   *
   * @since 3.0
   */
  private ContextTypeRegistry fCodeTemplateContextTypeRegistry;

  /**
   * The template context type registry for the java editor.
   *
   * @since 3.0
   */
  private ContextTypeRegistry fContextTypeRegistry;

  /**
   * Content assist history.
   *
   * @since 3.2
   */
  private ContentAssistHistory fContentAssistHistory;

  /**
   * The AST provider.
   *
   * @since 3.0
   */
  private ASTProvider fASTProvider;

  private MembersOrderPreferenceCache fMembersOrderPreferenceCache;

  /** Storage for preferences. */
  private IPreferenceStore preferenceStore;

  private ImageDescriptorRegistry fImageDescriptorRegistry;
  private String settingsDir;
  private final ResourcesPlugin resourcesPlugin;
  private final ProjectManager registry;
  private String cahPath;

  @Inject
  public JavaPlugin(
      @Named("che.jdt.settings.dir") String settingsDir,
      ResourcesPlugin resourcesPlugin,
      ProjectManager registry) {
    this.settingsDir = settingsDir;
    this.resourcesPlugin = resourcesPlugin;
    this.registry = registry;
    fgJavaPlugin = this;
    cahPath = settingsDir + CODEASSIST_LRU_HISTORY;
  }

  public static void log(Throwable e) {
    LOG.error(e.getMessage(), e);
  }

  public static void log(IStatus status) {
    LOG.error(status.getMessage(), status.getException());
  }

  public static String getPluginId() {
    return ID_PLUGIN;
  }

  public static ImageDescriptorRegistry getImageDescriptorRegistry() {
    return getDefault().internalGetImageDescriptorRegistry();
  }

  public static JavaPlugin getDefault() {
    return fgJavaPlugin;
  }

  /**
   * Registers the given Java template context.
   *
   * @param registry the template context type registry
   * @param id the context type id
   * @param parent the parent context type
   * @since 3.4
   */
  private static void registerJavaContext(
      ContributionContextTypeRegistry registry, String id, TemplateContextType parent) {
    TemplateContextType contextType = registry.getContextType(id);
    Iterator<TemplateVariableResolver> iter = parent.resolvers();
    while (iter.hasNext()) contextType.addResolver(iter.next());
  }

  public static void logErrorMessage(String message) {
    LOG.error(message);
  }

  @PostConstruct
  public void start() {
    //        WorkingCopyOwner.setPrimaryBufferProvider(new WorkingCopyOwner() {
    //            @Override
    //            public IBuffer createBuffer(ICompilationUnit workingCopy) {
    //                ICompilationUnit original = workingCopy.getPrimary();
    //                IResource resource = original.getResource();
    //                if (resource instanceof IFile)
    //                    return new DocumentAdapter(workingCopy, (IFile)resource);
    //                return DocumentAdapter.NULL;
    //            }
    //        });
    new JavaCore();
    fMembersOrderPreferenceCache = new MembersOrderPreferenceCache();
    PreferenceConstants.initializeDefaultValues(PreferenceConstants.getPreferenceStore());
    new JavaCorePreferenceInitializer().initializeDefaultPreferences();
    new CheCodeFormatterInitializer().initializeDefaultPreferences();
  }

  @PreDestroy
  void stop() {
    if (fContentAssistHistory != null) {
      try {
        ContentAssistHistory.store(fContentAssistHistory, cahPath);
      } catch (CoreException e) {
        log(e);
      }
      fContentAssistHistory = null;
    }

    QualifiedTypeNameHistory.getDefault().save();
  }

  /**
   * Returns the AST provider.
   *
   * @return the AST provider
   * @since 3.0
   */
  public synchronized ASTProvider getASTProvider() {
    if (fASTProvider == null) fASTProvider = new ASTProvider();

    return fASTProvider;
  }

  /**
   * Returns the preference store for this UI plug-in. This preference store is used to hold
   * persistent settings for this plug-in in the context of a workbench. Some of these settings will
   * be user controlled, whereas others may be internal setting that are never exposed to the user.
   *
   * <p>If an error occurs reading the preference store, an empty preference store is quietly
   * created, initialized with defaults, and returned.
   *
   * <p><strong>NOTE:</strong> As of Eclipse 3.1 this method is no longer referring to the core
   * runtime compatibility layer and so plug-ins relying on Plugin#initializeDefaultPreferences will
   * have to access the compatibility layer themselves.
   *
   * @return the preference store
   */
  public IPreferenceStore getPreferenceStore() {
    // Create the preference store lazily.
    if (preferenceStore == null) {
      preferenceStore = new PreferenceStore("test");
    }
    return preferenceStore;
  }

  /**
   * Returns the template context type registry for the code generation templates.
   *
   * @return the template context type registry for the code generation templates
   * @since 3.0
   */
  public ContextTypeRegistry getCodeTemplateContextRegistry() {
    if (fCodeTemplateContextTypeRegistry == null) {
      fCodeTemplateContextTypeRegistry = new ContributionContextTypeRegistry();

      CodeTemplateContextType.registerContextTypes(fCodeTemplateContextTypeRegistry);
    }

    return fCodeTemplateContextTypeRegistry;
  }

  /**
   * Returns the template store for the code generation templates.
   *
   * @return the template store for the code generation templates
   * @since 3.0
   */
  public TemplateStore getCodeTemplateStore() {
    if (fCodeTemplateStore == null) {
      //            IPreferenceStore store= getPreferenceStore();
      //            boolean alreadyMigrated= store.getBoolean(CODE_TEMPLATES_MIGRATION_KEY);
      //            if (alreadyMigrated)
      fCodeTemplateStore =
          new ContributionTemplateStore(
              getCodeTemplateContextRegistry(), /*store,*/ CODE_TEMPLATES_KEY);
      //            else {
      //                fCodeTemplateStore= new
      // CompatibilityTemplateStore(getCodeTemplateContextRegistry(), store, CODE_TEMPLATES_KEY,
      // getOldCodeTemplateStoreInstance());
      //                store.setValue(CODE_TEMPLATES_MIGRATION_KEY, true);
      //            }

      try {
        fCodeTemplateStore.load();
      } catch (IOException e) {
        log(e);
      }

      //            fCodeTemplateStore.startListeningForPreferenceChanges();

      // compatibility / bug fixing code for duplicated templates
      // TODO remove for 3.0
      //            CompatibilityTemplateStore.pruneDuplicates(fCodeTemplateStore, true);

    }

    return fCodeTemplateStore;
  }

  /**
   * Returns the template store for the java editor templates.
   *
   * @return the template store for the java editor templates
   * @since 3.0
   */
  public TemplateStore getTemplateStore() {
    if (fTemplateStore == null) {
      //            final IPreferenceStore store= getPreferenceStore();
      //            boolean alreadyMigrated= store.getBoolean(TEMPLATES_MIGRATION_KEY);
      //            if (alreadyMigrated)
      fTemplateStore =
          new ContributionTemplateStore(getTemplateContextRegistry(), /*store, */ TEMPLATES_KEY);
      //            else {
      //                fTemplateStore= new CompatibilityTemplateStore(getTemplateContextRegistry(),
      // store, TEMPLATES_KEY,
      // getOldTemplateStoreInstance());
      //                store.setValue(TEMPLATES_MIGRATION_KEY, true);
      //            }

      try {
        fTemplateStore.load();
      } catch (IOException e) {
        log(e);
      }
      //            fTemplateStore.startListeningForPreferenceChanges();
    }

    return fTemplateStore;
  }

  /**
   * Returns the template context type registry for the java plug-in.
   *
   * @return the template context type registry for the java plug-in
   * @since 3.0
   */
  public synchronized ContextTypeRegistry getTemplateContextRegistry() {
    if (fContextTypeRegistry == null) {
      ContributionContextTypeRegistry registry = new ContributionContextTypeRegistry(ID_CU_EDITOR);

      TemplateContextType all_contextType = registry.getContextType(JavaContextType.ID_ALL);
      ((AbstractJavaContextType) all_contextType).initializeContextTypeResolvers();

      registerJavaContext(registry, JavaContextType.ID_MEMBERS, all_contextType);
      registerJavaContext(registry, JavaContextType.ID_STATEMENTS, all_contextType);

      //            registerJavaContext(registry, SWTContextType.ID_ALL, all_contextType);
      //            all_contextType= registry.getContextType(SWTContextType.ID_ALL);
      //
      //            registerJavaContext(registry, SWTContextType.ID_MEMBERS, all_contextType);
      //            registerJavaContext(registry, SWTContextType.ID_STATEMENTS, all_contextType);

      fContextTypeRegistry = registry;
    }

    return fContextTypeRegistry;
  }

  public synchronized TypeFilter getTypeFilter() {
    if (fTypeFilter == null) fTypeFilter = new TypeFilter();
    return fTypeFilter;
  }

  private synchronized ImageDescriptorRegistry internalGetImageDescriptorRegistry() {
    if (fImageDescriptorRegistry == null) fImageDescriptorRegistry = new ImageDescriptorRegistry();
    return fImageDescriptorRegistry;
  }

  /**
   * Returns the Java content assist history.
   *
   * @return the Java content assist history
   * @since 3.2
   */
  public ContentAssistHistory getContentAssistHistory() {
    if (fContentAssistHistory == null) {
      try {
        fContentAssistHistory = ContentAssistHistory.load(cahPath);
      } catch (CoreException x) {
        log(x);
      }
      if (fContentAssistHistory == null) fContentAssistHistory = new ContentAssistHistory();
    }

    return fContentAssistHistory;
  }

  public IPath getStateLocation() {
    return new Path(settingsDir);
  }

  public MembersOrderPreferenceCache getMemberOrderPreferenceCache() {
    return fMembersOrderPreferenceCache;
  }

  public IDialogSettings getDialogSettings() {
    if (dialogSettings == null) {
      loadDialogSettings();
    }
    return dialogSettings;
  }

  /**
   * Loads the dialog settings for this plug-in. The default implementation first looks for a
   * standard named file in the plug-in's read/write state area; if no such file exists, the
   * plug-in's install directory is checked to see if one was installed with some default settings;
   * if no file is found in either place, a new empty dialog settings is created. If a problem
   * occurs, an empty settings is silently used.
   *
   * <p>This framework method may be overridden, although this is typically unnecessary.
   */
  protected void loadDialogSettings() {
    dialogSettings = new DialogSettings("Workbench"); // $NON-NLS-1$

    // bug 69387: The instance area should not be created (in the call to
    // #getStateLocation) if -data @none or -data @noDefault was used
    IPath dataLocation = new Path(settingsDir);
    //        if (dataLocation != null) {
    // try r/w state area in the local file system
    String readWritePath = dataLocation.append(FN_DIALOG_SETTINGS).toOSString();
    File settingsFile = new File(readWritePath);
    if (settingsFile.exists()) {
      try {
        dialogSettings.load(readWritePath);
      } catch (IOException e) {
        // load failed so ensure we have an empty settings
        dialogSettings = new DialogSettings("Workbench"); // $NON-NLS-1$
      }

      //                return;
    }
    //        }

    //        // otherwise look for bundle specific dialog settings
    //        URL dsURL = BundleUtility.find(getBundle(), FN_DIALOG_SETTINGS);
    //        if (dsURL == null) {
    //            return;
    //        }
    //
    //        InputStream is = null;
    //        try {
    //            is = dsURL.openStream();
    //            BufferedReader reader = new BufferedReader(
    //                    new InputStreamReader(is, "utf-8")); //$NON-NLS-1$
    //            dialogSettings.load(reader);
    //        } catch (IOException e) {
    //            // load failed so ensure we have an empty settings
    //            dialogSettings = new DialogSettings("Workbench"); //$NON-NLS-1$
    //        } finally {
    //            try {
    //                if (is != null) {
    //                    is.close();
    //                }
    //            } catch (IOException e) {
    //                // do nothing
    //            }
    //        }
  }
}
