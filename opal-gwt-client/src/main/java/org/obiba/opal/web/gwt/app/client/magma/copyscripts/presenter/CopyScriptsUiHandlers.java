package org.obiba.opal.web.gwt.app.client.magma.copyscripts.presenter;

import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;

public interface CopyScriptsUiHandlers extends ModalUiHandlers {
  void cancel();

  /**
   *
   * @param destination Name of the destination datasource
   * @param newName New name of the copied table (if applicable)
   */
  void onSubmit(String destination, String newName);
}
