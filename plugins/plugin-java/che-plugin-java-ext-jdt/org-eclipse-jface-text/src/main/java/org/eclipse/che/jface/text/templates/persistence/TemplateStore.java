/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jface.text.templates.persistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.che.jface.text.templates.ContextTypeRegistry;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateException;

/**
 * A collection of templates. Clients may instantiate this class. In order to load templates
 * contributed using the <code>org.eclipse.ui.editors.templates</code> extension point, use a <code>
 * ContributionTemplateStore</code>.
 *
 * @since 3.0
 */
public class TemplateStore {
  /** The stored templates. */
  private final List fTemplates = new ArrayList();
  /** The preference store. */
  //	private IPreferenceStore    fPreferenceStore;
  /**
   * The key into <code>fPreferenceStore</code> the value of which holds custom templates encoded as
   * XML.
   */
  private String fKey;
  /**
   * The context type registry, or <code>null</code> if all templates regardless of context type
   * should be loaded.
   */
  private ContextTypeRegistry fRegistry;
  /**
   * Set to <code>true</code> if property change events should be ignored (e.g. during writing to
   * the preference store).
   *
   * @since 3.2
   */
  private boolean fIgnorePreferenceStoreChanges = false;
  /**
   * The property listener, if any is registered, <code>null</code> otherwise.
   *
   * @since 3.2
   */
  //	private IPropertyChangeListener fPropertyListener;

  /**
   * Creates a new template store.
   *
   * @param store the preference store in which to store custom templates under <code>key</code>
   * @param key the key into <code>store</code> where to store custom templates
   */
  public TemplateStore(String key) {
    //		Assert.isNotNull(store);
    Assert.isNotNull(key);
    //		fPreferenceStore = store;
    fKey = key;
  }

  /**
   * Creates a new template store with a context type registry. Only templates that specify a
   * context type contained in the registry will be loaded by this store if the registry is not
   * <code>null</code>.
   *
   * @param registry a context type registry, or <code>null</code> if all templates should be loaded
   * @param store the preference store in which to store custom templates under <code>key</code>
   * @param key the key into <code>store</code> where to store custom templates
   */
  public TemplateStore(ContextTypeRegistry registry, /* IPreferenceStore store,*/ String key) {
    this(/*store, */ key);
    fRegistry = registry;
  }

  /**
   * Loads the templates from contributions and preferences.
   *
   * @throws IOException if loading fails.
   */
  public void load() throws IOException {
    fTemplates.clear();
    loadContributedTemplates();
    loadCustomTemplates();
  }

  //	/**
  //	 * Starts listening for property changes on the preference store. If the configured preference
  //	 * key changes, the template store is {@link #load() reloaded}. Call
  //	 * {@link #stopListeningForPreferenceChanges()} to remove any listener and stop the
  //	 * auto-updating behavior.
  //	 *
  //	 * @since 3.2
  //	 */
  //	public final void startListeningForPreferenceChanges() {
  //		if (fPropertyListener == null) {
  //			fPropertyListener= new IPropertyChangeListener() {
  //				public void propertyChange(PropertyChangeEvent event) {
  //					/*
  //					 * Don't load if we are in the process of saving ourselves. We are in sync anyway after the
  //					 * save operation, and clients may trigger reloading by listening to preference store
  //					 * updates.
  //					 */
  //					if (!fIgnorePreferenceStoreChanges && fKey.equals(event.getProperty()))
  //						try {
  //							load();
  //						} catch (IOException x) {
  //							handleException(x);
  //						}
  //				}
  //			};
  //			fPreferenceStore.addPropertyChangeListener(fPropertyListener);
  //		}
  //
  //	}
  //
  //	/**
  //	 * Stops the auto-updating behavior started by calling
  //	 * {@link #startListeningForPreferenceChanges()}.
  //	 *
  //	 * @since 3.2
  //	 */
  //	public final void stopListeningForPreferenceChanges() {
  //		if (fPropertyListener != null) {
  //			fPreferenceStore.removePropertyChangeListener(fPropertyListener);
  //			fPropertyListener= null;
  //		}
  //	}

  /**
   * Handles an {@link IOException} thrown during reloading the preferences due to a preference
   * store update. The default is to write to stderr.
   *
   * @param x the exception
   * @since 3.2
   */
  protected void handleException(IOException x) {
    x.printStackTrace();
  }

  /**
   * Hook method to load contributed templates. Contributed templates are superseded by customized
   * versions of user added templates stored in the preferences.
   *
   * <p>The default implementation does nothing.
   *
   * @throws IOException if loading fails
   */
  protected void loadContributedTemplates() throws IOException {}

  /**
   * Adds a template to the internal store. The added templates must have a unique id.
   *
   * @param data the template data to add
   */
  protected void internalAdd(TemplatePersistenceData data) {
    if (!data.isCustom()) {
      // check if the added template is not a duplicate id
      String id = data.getId();
      for (Iterator it = fTemplates.iterator(); it.hasNext(); ) {
        TemplatePersistenceData d2 = (TemplatePersistenceData) it.next();
        if (d2.getId() != null && d2.getId().equals(id)) return;
      }
      fTemplates.add(data);
    }
  }

  /**
   * Saves the templates to the preferences.
   *
   * @throws IOException if the templates cannot be written
   */
  public void save() throws IOException {
    //		ArrayList custom= new ArrayList();
    //		for (Iterator it= fTemplates.iterator(); it.hasNext();) {
    //			TemplatePersistenceData data= (TemplatePersistenceData) it.next();
    //			if (data.isCustom() && !(data.isUserAdded() && data.isDeleted())) // don't save deleted
    // user-added templates
    //				custom.add(data);
    //		}
    //
    //		StringWriter output= new StringWriter();
    //		TemplateReaderWriter writer= new TemplateReaderWriter();
    //		writer.save((TemplatePersistenceData[]) custom.toArray(new
    // TemplatePersistenceData[custom.size()]), output);
    //
    //		fIgnorePreferenceStoreChanges= true;
    //		try {
    //			fPreferenceStore.setValue(fKey, output.toString());
    //			if (fPreferenceStore instanceof IPersistentPreferenceStore)
    //				((IPersistentPreferenceStore)fPreferenceStore).save();
    //		} finally {
    //			fIgnorePreferenceStoreChanges= false;
    //		}
  }

  /**
   * Adds a template encapsulated in its persistent form.
   *
   * @param data the template to add
   */
  public void add(TemplatePersistenceData data) {

    if (!validateTemplate(data.getTemplate())) return;

    if (data.isUserAdded()) {
      fTemplates.add(data);
    } else {
      for (Iterator it = fTemplates.iterator(); it.hasNext(); ) {
        TemplatePersistenceData d2 = (TemplatePersistenceData) it.next();
        if (d2.getId() != null && d2.getId().equals(data.getId())) {
          d2.setTemplate(data.getTemplate());
          d2.setDeleted(data.isDeleted());
          d2.setEnabled(data.isEnabled());
          return;
        }
      }

      // add an id which is not contributed as add-on
      if (data.getTemplate() != null) {
        TemplatePersistenceData newData =
            new TemplatePersistenceData(data.getTemplate(), data.isEnabled());
        fTemplates.add(newData);
      }
    }
  }

  /**
   * Removes a template from the store.
   *
   * @param data the template to remove
   */
  public void delete(TemplatePersistenceData data) {
    if (data.isUserAdded()) fTemplates.remove(data);
    else data.setDeleted(true);
  }

  /** Restores all contributed templates that have been deleted. */
  public void restoreDeleted() {
    for (Iterator it = fTemplates.iterator(); it.hasNext(); ) {
      TemplatePersistenceData data = (TemplatePersistenceData) it.next();
      if (data.isDeleted()) data.setDeleted(false);
    }
  }

  /**
   * Deletes all user-added templates and reverts all contributed templates.
   *
   * @param doSave <code>true</code> if the store should be saved after restoring
   * @since 3.5
   */
  public void restoreDefaults(boolean doSave) {
    //		String oldValue= null;
    //		if (!doSave)
    //			oldValue= fPreferenceStore.getString(fKey);
    //
    //		try {
    //			fIgnorePreferenceStoreChanges= true;
    //			fPreferenceStore.setToDefault(fKey);
    //		} finally {
    //			fIgnorePreferenceStoreChanges= false;
    //		}
    //
    //		try {
    //			load();
    //		} catch (IOException x) {
    //			// can't log from jface-text
    //			x.printStackTrace();
    //		}
    //
    //		if (oldValue != null) {
    //			try {
    //				fIgnorePreferenceStoreChanges= true;
    //				fPreferenceStore.putValue(fKey, oldValue);
    //			} finally {
    //				fIgnorePreferenceStoreChanges= false;
    //			}
    //		}
  }

  /**
   * Deletes all user-added templates and reverts all contributed templates.
   *
   * <p><strong>Note:</strong> the store will be saved after restoring.
   */
  public void restoreDefaults() {
    restoreDefaults(true);
  }

  /**
   * Returns all enabled templates.
   *
   * @return all enabled templates
   */
  public Template[] getTemplates() {
    return getTemplates(null);
  }

  /**
   * Returns all enabled templates for the given context type.
   *
   * @param contextTypeId the id of the context type of the requested templates, or <code>null
   *     </code> if all templates should be returned
   * @return all enabled templates for the given context type
   */
  public Template[] getTemplates(String contextTypeId) {
    List templates = new ArrayList();
    for (Iterator it = fTemplates.iterator(); it.hasNext(); ) {
      TemplatePersistenceData data = (TemplatePersistenceData) it.next();
      if (data.isEnabled()
          && !data.isDeleted()
          && (contextTypeId == null || contextTypeId.equals(data.getTemplate().getContextTypeId())))
        templates.add(data.getTemplate());
    }

    return (Template[]) templates.toArray(new Template[templates.size()]);
  }

  /**
   * Returns the first enabled template that matches the name.
   *
   * @param name the name of the template searched for
   * @return the first enabled template that matches both name and context type id, or <code>null
   *     </code> if none is found
   */
  public Template findTemplate(String name) {
    return findTemplate(name, null);
  }

  /**
   * Returns the first enabled template that matches both name and context type id.
   *
   * @param name the name of the template searched for
   * @param contextTypeId the context type id to clip unwanted templates, or <code>null</code> if
   *     any context type is OK
   * @return the first enabled template that matches both name and context type id, or <code>null
   *     </code> if none is found
   */
  public Template findTemplate(String name, String contextTypeId) {
    Assert.isNotNull(name);

    for (Iterator it = fTemplates.iterator(); it.hasNext(); ) {
      TemplatePersistenceData data = (TemplatePersistenceData) it.next();
      Template template = data.getTemplate();
      if (data.isEnabled()
          && !data.isDeleted()
          && (contextTypeId == null || contextTypeId.equals(template.getContextTypeId()))
          && name.equals(template.getName())) return template;
    }

    return null;
  }

  /**
   * Returns the first enabled template that matches the given template id.
   *
   * @param id the id of the template searched for
   * @return the first enabled template that matches id, or <code>null</code> if none is found
   * @since 3.1
   */
  public Template findTemplateById(String id) {
    TemplatePersistenceData data = getTemplateData(id);
    if (data != null && !data.isDeleted()) return data.getTemplate();

    return null;
  }

  /**
   * Returns all template data.
   *
   * @param includeDeleted whether to include deleted data
   * @return all template data, whether enabled or not
   */
  public TemplatePersistenceData[] getTemplateData(boolean includeDeleted) {
    List datas = new ArrayList();
    for (Iterator it = fTemplates.iterator(); it.hasNext(); ) {
      TemplatePersistenceData data = (TemplatePersistenceData) it.next();
      if (includeDeleted || !data.isDeleted()) datas.add(data);
    }

    return (TemplatePersistenceData[]) datas.toArray(new TemplatePersistenceData[datas.size()]);
  }

  /**
   * Returns the template data of the template with id <code>id</code> or <code>null</code> if no
   * such template can be found.
   *
   * @param id the id of the template data
   * @return the template data of the template with id <code>id</code> or <code>null</code>
   * @since 3.1
   */
  public TemplatePersistenceData getTemplateData(String id) {
    Assert.isNotNull(id);
    for (Iterator it = fTemplates.iterator(); it.hasNext(); ) {
      TemplatePersistenceData data = (TemplatePersistenceData) it.next();
      if (id.equals(data.getId())) return data;
    }

    return null;
  }

  private void loadCustomTemplates() throws IOException {
    //		String pref= fPreferenceStore.getString(fKey);
    //		if (pref != null && pref.trim().length() > 0) {
    //			Reader input= new StringReader(pref);
    //			TemplateReaderWriter reader= new TemplateReaderWriter();
    //			TemplatePersistenceData[] datas= reader.read(input);
    //			for (int i= 0; i < datas.length; i++) {
    //				TemplatePersistenceData data= datas[i];
    //				add(data);
    //			}
    //		}
  }

  /**
   * Validates a template against the context type registered in the context type registry. Returns
   * always <code>true</code> if no registry is present.
   *
   * @param template the template to validate
   * @return <code>true</code> if validation is successful or no context type registry is specified,
   *     <code>false</code> if validation fails
   */
  private boolean validateTemplate(Template template) {
    String contextTypeId = template.getContextTypeId();
    if (contextExists(contextTypeId)) {
      if (fRegistry != null)
        try {
          fRegistry.getContextType(contextTypeId).validate(template.getPattern());
        } catch (TemplateException e) {
          return false;
        }
      return true;
    }

    return false;
  }

  /**
   * Returns <code>true</code> if a context type id specifies a valid context type or if no context
   * type registry is present.
   *
   * @param contextTypeId the context type id to look for
   * @return <code>true</code> if the context type specified by the id is present in the context
   *     type registry, or if no registry is specified
   */
  private boolean contextExists(String contextTypeId) {
    return contextTypeId != null
        && (fRegistry == null || fRegistry.getContextType(contextTypeId) != null);
  }

  /**
   * Returns the registry.
   *
   * @return Returns the registry
   */
  protected final ContextTypeRegistry getRegistry() {
    return fRegistry;
  }
}
