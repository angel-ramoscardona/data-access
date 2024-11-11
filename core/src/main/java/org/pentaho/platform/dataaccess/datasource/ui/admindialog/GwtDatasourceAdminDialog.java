/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.platform.dataaccess.datasource.ui.admindialog;

import org.pentaho.agilebi.modeler.services.IModelerServiceAsync;
import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;
import org.pentaho.platform.dataaccess.datasource.IDatasourceInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint;
import org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceMessages;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDSWDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceServiceManager;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.gwt.GwtXulDomContainer;
import org.pentaho.ui.xul.gwt.GwtXulRunner;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.gwt.util.AsyncConstructorListener;
import org.pentaho.ui.xul.gwt.util.AsyncXulLoader;
import org.pentaho.ui.xul.gwt.util.IXulLoaderCallback;
import org.pentaho.ui.xul.util.DialogController;

import com.google.gwt.core.client.GWT;

public class GwtDatasourceAdminDialog implements IXulLoaderCallback, DialogController<IDatasourceInfo> {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  protected DatasourceAdminDialogController datasourceAdminDialogController;

  protected IXulAsyncDatasourceServiceManager genericDatasourceServiceManager;

  protected IModelerServiceAsync modelerService;

  private IXulAsyncDSWDatasourceService dswService;

  protected AsyncConstructorListener<GwtDatasourceAdminDialog> constructorListener;

  private GwtDatasourceEditorEntryPoint entryPoint;

  private boolean initialized;
  private GwtDatasourceMessages datasourceMessages;

  // ~ Constructors ====================================================================================================

  public GwtDatasourceAdminDialog( final IXulAsyncDatasourceServiceManager genericDatasourceServiceManager,
                                   IModelerServiceAsync modelerService, IXulAsyncDSWDatasourceService dswService,
                                   GwtDatasourceEditorEntryPoint entryPoint,
                                   final AsyncConstructorListener<GwtDatasourceAdminDialog> constructorListener ) {
    this.genericDatasourceServiceManager = genericDatasourceServiceManager;
    this.modelerService = modelerService;
    this.dswService = dswService;
    this.constructorListener = constructorListener;
    this.entryPoint = entryPoint;
    try {
      AsyncXulLoader.loadXulFromUrl( GWT.getModuleBaseURL() + "datasourceAdminDialog.xul",
        GWT.getModuleBaseURL() + "datasourceAdminDialog", this ); //$NON-NLS-1$//$NON-NLS-2$
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  protected GwtDatasourceAdminDialog() {
  }
  // ~ Methods =========================================================================================================

  /**
   * Specified by <code>IXulLoaderCallback</code>.
   */
  public void overlayLoaded() {
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

      ResourceBundle resBundle = (ResourceBundle) container.getResourceBundles().get( 0 );
      datasourceMessages = new GwtDatasourceMessages();
      datasourceMessages.setMessageBundle( resBundle );

      datasourceAdminDialogController = new DatasourceAdminDialogController();
      datasourceAdminDialogController.setBindingFactory( bf );
      datasourceAdminDialogController.setDatasourceServiceManager( genericDatasourceServiceManager );
      datasourceAdminDialogController.setModelerService( modelerService );
      datasourceAdminDialogController.setDSWService( dswService );
      datasourceAdminDialogController.setEntryPoint( entryPoint );
      datasourceAdminDialogController.setMessageBundle( datasourceMessages );
      container.addEventHandler( datasourceAdminDialogController );

      runner.initialize();

      runner.start();

      initialized = true;

      if ( constructorListener != null ) {
        constructorListener.asyncConstructorDone( this );
      }

      datasourceAdminDialogController.onDialogReady();
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
  public void addDialogListener( org.pentaho.ui.xul.util.DialogController.DialogListener<IDatasourceInfo> listener ) {
    checkInitialized();
    datasourceAdminDialogController.addDialogListener( listener );
    datasourceAdminDialogController.onDialogReady();
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void hideDialog() {
    checkInitialized();
    datasourceAdminDialogController.hideDialog();
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void removeDialogListener(
    org.pentaho.ui.xul.util.DialogController.DialogListener<IDatasourceInfo> listener ) {
    checkInitialized();
    datasourceAdminDialogController.removeDialogListener( listener );
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void showDialog() {
    checkInitialized();
    datasourceAdminDialogController.showDialog();
  }

}
