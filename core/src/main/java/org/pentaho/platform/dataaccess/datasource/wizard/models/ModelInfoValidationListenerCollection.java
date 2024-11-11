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


package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.io.Serializable;
import java.util.ArrayList;

public class ModelInfoValidationListenerCollection extends ArrayList<IModelInfoValidationListener>
  implements Serializable {

  private static final long serialVersionUID = 5617315105005802369L;

  public void fireModelInfoValid() {
    for ( IModelInfoValidationListener listener : this ) {
      listener.onModelInfoValid();
    }
  }

  public void fireModelInfoInvalid() {
    for ( IModelInfoValidationListener listener : this ) {
      listener.onModelInfoInvalid();
    }
  }

  public void fireCsvInfoValid() {
    for ( IModelInfoValidationListener listener : this ) {
      listener.onCsvValid();
    }
  }

  public void fireCsvInfoInvalid() {
    for ( IModelInfoValidationListener listener : this ) {
      listener.onCsvInValid();
    }
  }


}
