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


package org.pentaho.platform.dataaccess.datasource.wizard.sources.csv;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.pentaho.mantle.client.csrf.CsrfUtil;
import org.pentaho.mantle.client.csrf.IConsumer;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.beans.BogoPojo;
import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.IWizardDatasource;
import org.pentaho.platform.dataaccess.datasource.wizard.IWizardStep;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.MessageHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ColumnInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvTransformGeneratorException;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTO;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTOUtil;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.IWizardModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDSWDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceServiceAsync;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.util.ArrayList;
import java.util.List;

/**
 * User: nbaker Date: 3/22/11
 */
public class CsvDatasource extends AbstractXulEventHandler implements IWizardDatasource {


  private ICsvDatasourceServiceAsync csvDatasourceService;

  private DatasourceModel datasourceModel;
  public FileTransformStats stats;
  private StageDataStep stageStep;
  private CsvPhysicalStep csvStep;
  private boolean finishable;

  private IXulAsyncDSWDatasourceService datasourceService;
  private IWizardModel wizardModel;

  public CsvDatasource( DatasourceModel datasourceModel, IXulAsyncDSWDatasourceService datasourceService,
                        ICsvDatasourceServiceAsync csvDatasourceService ) {
    this.datasourceModel = datasourceModel;
    this.datasourceService = datasourceService;
    this.csvDatasourceService = csvDatasourceService;

    ServiceDefTarget endpoint = (ServiceDefTarget) this.csvDatasourceService;
    endpoint.setServiceEntryPoint( getDatasourceURL() );
    csvStep = new CsvPhysicalStep( datasourceModel, this, csvDatasourceService );
    stageStep = new StageDataStep( datasourceModel, this, csvDatasourceService );

    csvDatasourceService.gwtWorkaround( new BogoPojo(), new AsyncCallback<BogoPojo>() {

      @Override
      public void onFailure( Throwable throwable ) {

      }

      @Override
      public void onSuccess( BogoPojo bogoPojo ) {
        bogoPojo.getAggType();
      }
    } );
  }

  public String getDatasourceURL() {
    String moduleUrl = GWT.getModuleBaseURL();
    if ( moduleUrl.indexOf( "content" ) > -1 ) { //$NON-NLS-1$
      //we are running the client in the context of a BI Server plugin, so
      //point the request to the GWT rpc proxy servlet
      String baseUrl = moduleUrl.substring( 0, moduleUrl.indexOf( "content" ) ); //$NON-NLS-1$
      //NOTE: the dispatch URL ("connectionService") must match the bean id for
      //this service object in your plugin.xml.  "gwtrpc" is the servlet
      //that handles plugin gwt rpc requests in the BI Server.
      return baseUrl + "gwtrpc/CsvDatasourceService"; //$NON-NLS-1$
    }
    //we are running this client in hosted mode, so point to the servlet
    //defined in war/WEB-INF/web.xml
    return moduleUrl + "CsvDatasourceService"; //$NON-NLS-1$
  }

  @Override
  public void activating() throws XulException {
    csvStep.activating();
    stageStep.activating();
  }


  @Override
  public void deactivating() {
    csvStep.deactivate();
    stageStep.deactivate();
  }

  @Override
  public void init( XulDomContainer container, IWizardModel wizardModel ) throws XulException {
    this.wizardModel = wizardModel;
    container.addEventHandler( csvStep );
    container.addEventHandler( stageStep );
    csvStep.init( wizardModel );
    stageStep.init( wizardModel );
  }

  @Override
  @Bindable
  public String getName() {
    return MessageHandler.getString( "csv.datasource.name" );
  }

  @Override
  public List<IWizardStep> getSteps() {
    List<IWizardStep> steps = new ArrayList<IWizardStep>();
    steps.add( csvStep );
    steps.add( stageStep );
    return steps;
  }

  @Override
  public void onFinish( final XulServiceCallback<IDatasourceSummary> callback ) {

    datasourceModel.getGuiStateModel().setDataStagingComplete( false );
    setColumnIdsToColumnNamesIfNecessary();

    // set the modelInfo.stageTableName to the database table name generated from the datasourceName
    datasourceModel.getModelInfo().setStageTableName( datasourceModel.generateTableName() );
    String tmpFileName = datasourceModel.getModelInfo().getFileInfo().getTmpFilename();
    String fileName = datasourceModel.getModelInfo().getFileInfo().getFilename();
    if ( fileName == null && tmpFileName != null && tmpFileName.endsWith( ".tmp" ) ) {
      tmpFileName = tmpFileName.substring( 0, tmpFileName.lastIndexOf( ".tmp" ) );
      datasourceModel.getModelInfo().getFileInfo().setFilename( tmpFileName );
    }

    datasourceModel.getModelInfo().setDatasourceName( datasourceModel.getDatasourceName() );

    // Lambda expressions are only supported from GWT 2.8 onwards.
    CsrfUtil.callProtected( csvDatasourceService, new IConsumer<ICsvDatasourceServiceAsync>() {
      @Override
      public void accept( ICsvDatasourceServiceAsync service ) {
        service
          .generateDomain( DatasourceDTOUtil.generateDTO( datasourceModel ), new AsyncCallback<IDatasourceSummary>() {
            public void onFailure( Throwable th ) {
              MessageHandler.getInstance().closeWaitingDialog();
              if ( th instanceof CsvTransformGeneratorException ) {
                MessageHandler.getInstance()
                  .showErrorDetailsDialog( MessageHandler.getString( "ERROR" ), th.getLocalizedMessage(),
                    ( (CsvTransformGeneratorException) th ).getCauseMessage() + ( (CsvTransformGeneratorException) th )
                      .getCauseStackTrace() );
              } else {
                MessageHandler.getInstance().showErrorDialog( MessageHandler.getString( "ERROR" ), th.getMessage() );
              }
              th.printStackTrace();
            }

            public void onSuccess( IDatasourceSummary stats ) {
              CsvDatasource.this.stats = (FileTransformStats) stats;

              MessageHandler.getInstance().closeWaitingDialog();
              callback.success( stats );
            }
          } );
      }
    } );
  }

  private void setColumnIdsToColumnNamesIfNecessary() {
    for ( ColumnInfo ci : datasourceModel.getModelInfo().getColumns() ) {
      if ( ci.getId() == null ) {
        ci.setId( ci.getTitle() );
      }
    }
  }

  @Override
  public String getId() {
    return "CSV";
  }

  @Override
  public boolean isFinishable() {
    return finishable;
  }

  public void setFinishable( boolean finishable ) {
    boolean prevFinishable = this.finishable;
    this.finishable = finishable;
    firePropertyChange( "finishable", prevFinishable, finishable );
  }

  @Override
  public void restoreSavedDatasource( Domain previousDomain, final XulServiceCallback<Void> callback ) {

    String serializedDatasource = (String) previousDomain.getLogicalModels().get( 0 ).getProperty( "datasourceModel" );

    datasourceService.deSerializeModelState( serializedDatasource, new XulServiceCallback<DatasourceDTO>() {
      public void success( DatasourceDTO datasourceDTO ) {
        DatasourceDTO.populateModel( datasourceDTO, datasourceModel );
        datasourceModel.getGuiStateModel().setDirty( false );
        wizardModel.setEditing( true );
        callback.success( null );
      }

      public void error( String s, Throwable throwable ) {
        MessageHandler.getInstance().showErrorDialog( MessageHandler.getString( "ERROR" ), MessageHandler.getString(
          "DatasourceEditor.ERROR_0002_UNABLE_TO_SHOW_DIALOG", throwable.getLocalizedMessage() ) );

        callback.error( s, throwable );
      }
    } );
  }

  @Override
  public void reset() {
    datasourceModel.clearModel();
  }
}
