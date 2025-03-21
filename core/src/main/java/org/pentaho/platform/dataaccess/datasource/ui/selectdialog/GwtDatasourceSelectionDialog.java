/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.dataaccess.datasource.ui.selectdialog;

import com.google.gwt.core.client.GWT;
import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.EmbeddedWizard;
import org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceMessages;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDSWDatasourceService;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.gwt.GwtXulDomContainer;
import org.pentaho.ui.xul.gwt.GwtXulRunner;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.gwt.util.AsyncConstructorListener;
import org.pentaho.ui.xul.gwt.util.AsyncXulLoader;
import org.pentaho.ui.xul.gwt.util.IXulLoaderCallback;
import org.pentaho.ui.xul.util.DialogController;

public class GwtDatasourceSelectionDialog implements IXulLoaderCallback, DialogController<LogicalModelSummary> {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  protected DatasourceSelectionDialogController datasourceSelectionDialogController;

  protected String context;
  protected EmbeddedWizard gwtDatasourceEditor;

  protected IXulAsyncDSWDatasourceService datasourceService;

  protected AsyncConstructorListener<GwtDatasourceSelectionDialog> constructorListener;

  private boolean initialized;

  // ~ Constructors ====================================================================================================

  public GwtDatasourceSelectionDialog( String context, final IXulAsyncDSWDatasourceService datasourceService,
                                       final EmbeddedWizard gwtDatasourceEditor,
                                       final AsyncConstructorListener<GwtDatasourceSelectionDialog>
                                         constructorListener ) {
    this.context = context;

    this.gwtDatasourceEditor = gwtDatasourceEditor;
    this.datasourceService = datasourceService;
    this.constructorListener = constructorListener;
    try {
      AsyncXulLoader.loadXulFromUrl( GWT.getModuleBaseURL() + "datasourceSelectionDialog.xul",
        GWT.getModuleBaseURL() + "datasourceSelectionDialog", this ); //$NON-NLS-1$//$NON-NLS-2$
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  protected GwtDatasourceSelectionDialog() {
  }
  // ~ Methods =========================================================================================================

  /**
   * Specified by <code>IXulLoaderCallback</code>.
   */
  public void overlayLoaded() {
  }

  public void reset() {
    if ( datasourceSelectionDialogController != null ) {
      datasourceSelectionDialogController.reset();
    }
  }

  /**
   * Specified by <code>IXulLoaderCallback</code>.
   */
  public void overlayRemoved() {
  }

  public void xulLoaded( final GwtXulRunner runner ) {
    try {
      GwtXulDomContainer container = (GwtXulDomContainer) runner.getXulDomContainers().get( 0 );

      BindingFactory bf = new GwtBindingFactory( container.getDocumentRoot() );

      // begin DatasourceSelectionDialogController setup
      datasourceSelectionDialogController = new DatasourceSelectionDialogController( context );
      datasourceSelectionDialogController.setBindingFactory( bf );
      datasourceSelectionDialogController.setDatasourceService( datasourceService );
      container.addEventHandler( datasourceSelectionDialogController );
      // end DatasourceSelectionDialogController setup

      datasourceSelectionDialogController.setDatasourceDialogController( gwtDatasourceEditor );

      ResourceBundle resBundle = (ResourceBundle) container.getResourceBundles().get( 0 );
      GwtDatasourceMessages datasourceMessages = new GwtDatasourceMessages();
      datasourceMessages.setMessageBundle( resBundle );
      datasourceSelectionDialogController.setMessageBundle( datasourceMessages );

      runner.initialize();

      runner.start();

      initialized = true;

      if ( constructorListener != null ) {
        constructorListener.asyncConstructorDone( this );
      }

      datasourceSelectionDialogController.onDialogReady();
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  protected void checkInitialized() {
    if ( !initialized ) {
      throw new IllegalStateException( "You must wait until the constructor listener is notified." ); //$NON-NLS-1$
    }
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void addDialogListener(
    org.pentaho.ui.xul.util.DialogController.DialogListener<LogicalModelSummary> listener ) {
    checkInitialized();
    datasourceSelectionDialogController.addDialogListener( listener );
    datasourceSelectionDialogController.onDialogReady();
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void hideDialog() {
    checkInitialized();
    datasourceSelectionDialogController.hideDialog();
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void removeDialogListener(
    org.pentaho.ui.xul.util.DialogController.DialogListener<LogicalModelSummary> listener ) {
    checkInitialized();
    datasourceSelectionDialogController.removeDialogListener( listener );
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void showDialog() {
    checkInitialized();
    datasourceSelectionDialogController.showDialog();
  }

  public void setContext( String context ) {
    datasourceSelectionDialogController.setContext( context );
  }
}
