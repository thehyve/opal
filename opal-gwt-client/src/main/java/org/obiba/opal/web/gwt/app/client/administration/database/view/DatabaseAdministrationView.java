/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.database.view;

import org.obiba.opal.web.gwt.app.client.administration.database.presenter.DatabaseAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.presenter.DatabaseAdministrationPresenter.Display;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.opal.DatabaseDto;
import org.obiba.opal.web.model.client.opal.SqlDatabaseDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.gwtplatform.mvp.client.ViewImpl;

import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.DELETE_ACTION;
import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.EDIT_ACTION;

public class DatabaseAdministrationView extends ViewImpl implements DatabaseAdministrationPresenter.Display {

  @UiTemplate("DatabaseAdministrationView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DatabaseAdministrationView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget uiWidget;

  @UiField
  Button addDatabaseButton;

  @UiField
  SimplePager databaseTablePager;

  @UiField
  Table<DatabaseDto> databaseTable;

  @UiField
  Panel permissionsPanel;

  @UiField
  Panel breadcrumbs;

  @UiField
  Panel permissions;

  ActionsColumn<DatabaseDto> actionsColumn = new ActionsColumn<DatabaseDto>(new ActionsProvider<DatabaseDto>() {

    private final String[] all = new String[] { TEST_ACTION, EDIT_ACTION, DELETE_ACTION };

    private final String[] immutable = new String[] { TEST_ACTION };

    @Override
    public String[] allActions() {
      return all;
    }

    @Override
    public String[] getActions(DatabaseDto value) {
      return value.getEditable() ? allActions() : immutable;
    }
  });

  public DatabaseAdministrationView() {
    uiWidget = uiBinder.createAndBindUi(this);
    databaseTablePager.setDisplay(databaseTable);
    databaseTable.addColumn(Columns.NAME, translations.nameLabel());
    databaseTable.addColumn(Columns.URL, translations.urlLabel());
    databaseTable.addColumn(Columns.DRIVER, translations.driverLabel());
    databaseTable.addColumn(Columns.USERNAME, translations.usernameLabel());
    databaseTable.addColumn(actionsColumn, translations.actionsLabel());
  }

  @Override
  public Widget asWidget() {
    return uiWidget;
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if(slot == Display.Slots.Permissions) {
      permissions.clear();
      permissions.add(content);
    }
  }

  @Override
  public HasClickHandlers getAddButton() {
    return addDatabaseButton;
  }

  @Override
  public HasActionHandler<DatabaseDto> getActions() {
    return actionsColumn;
  }

  @Override
  public HasData<DatabaseDto> getDatabaseTable() {
    return databaseTable;
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @Override
  public HasAuthorization getPermissionsAuthorizer() {
    return new WidgetAuthorizer(permissionsPanel);
  }

  private static final class Columns {

    static final Column<DatabaseDto, String> NAME = new TextColumn<DatabaseDto>() {

      @Override
      public String getValue(DatabaseDto object) {
        return object.getName();
      }
    };

    static final Column<DatabaseDto, String> URL = new TextColumn<DatabaseDto>() {

      @Override
      public String getValue(DatabaseDto dto) {
        return ((SqlDatabaseDto) dto.getExtension(SqlDatabaseDto.DatabaseDtoExtensions.settings)).getUrl();
      }
    };

    static final Column<DatabaseDto, String> DRIVER = new TextColumn<DatabaseDto>() {

      @Override
      public String getValue(DatabaseDto dto) {
        return ((SqlDatabaseDto) dto.getExtension(SqlDatabaseDto.DatabaseDtoExtensions.settings)).getDriverClass();
      }
    };

    static final Column<DatabaseDto, String> USERNAME = new TextColumn<DatabaseDto>() {

      @Override
      public String getValue(DatabaseDto dto) {
        return ((SqlDatabaseDto) dto.getExtension(SqlDatabaseDto.DatabaseDtoExtensions.settings)).getUsername();
      }
    };

  }

}
