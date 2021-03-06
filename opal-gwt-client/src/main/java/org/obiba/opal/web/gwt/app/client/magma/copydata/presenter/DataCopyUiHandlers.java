package org.obiba.opal.web.gwt.app.client.magma.copydata.presenter;

import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;

public interface DataCopyUiHandlers extends ModalUiHandlers {
  void cancel();

  /**
   *
   * @param destination Name of the destination datasource
   * @param newName New name of the copied table (if applicable)
   */
  void onSubmit(String destination, String newName);
}
