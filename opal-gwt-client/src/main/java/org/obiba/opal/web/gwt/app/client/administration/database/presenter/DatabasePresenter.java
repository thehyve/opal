/*******************************************************************************
 * Copyright 2012(c) OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.database.presenter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.administration.database.event.DatabaseCreatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.database.event.DatabaseUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.validation.ConstraintViolationErrorsEvent;
import org.obiba.opal.web.gwt.app.client.validation.ConstraintViolationUtils;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.DatabaseDto;
import org.obiba.opal.web.model.client.opal.JdbcDriverDto;
import org.obiba.opal.web.model.client.opal.SqlDatabaseDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;
import org.obiba.opal.web.model.client.ws.ConstraintViolationErrorDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class DatabasePresenter extends ModalPresenterWidget<DatabasePresenter.Display> implements DatabaseUiHandlers {

  public enum Mode {
    CREATE, UPDATE
  }

  public enum Usage {
    IMPORT(translations.importLabel(), SqlSchema.values()),
    STORAGE(translations.storageLabel(), SqlSchema.HIBERNATE),
    EXPORT(translations.exportLabel(), SqlSchema.HIBERNATE, SqlSchema.JDBC);

    private final String label;

    private final SqlSchema[] supportedSqlSchemas;

    Usage(String label, SqlSchema... supportedSqlSchemas) {
      this.label = label;
      this.supportedSqlSchemas = supportedSqlSchemas;
    }

    public String getLabel() {
      return label;
    }

    public SqlSchema[] getSupportedSqlSchemas() {
      return supportedSqlSchemas;
    }
  }

  public enum SqlSchema {
    HIBERNATE(translations.hibernateDatasourceLabel()), //
    JDBC(translations.jdbcDatasourceLabel()), //
    LIMESURVEY("Limesurvey");

    private final String label;

    SqlSchema(String label) {
      this.label = label;
    }

    public String getLabel() {
      return label;
    }

  }

  private static final Translations translations = GWT.create(Translations.class);

  private static final TranslationMessages translationMessages = GWT.create(TranslationMessages.class);

  private Mode dialogMode;

  private ValidationHandler methodValidationHandler;

  @Inject
  public DatabasePresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  @Override
  public void save() {
    switch(dialogMode) {
      case CREATE:
        createDatabase();
        break;
      case UPDATE:
        updateDatabase();
        break;
    }
  }

  @Override
  public void cancel() {
    getView().hideDialog();
  }

  @Override
  protected void onBind() {
    setDialogMode(Mode.CREATE);

    ResourceRequestBuilderFactory.<JsArray<JdbcDriverDto>>newBuilder() //
        .forResource(DatabaseResources.drivers()) //
        .withCallback(new ResourceCallback<JsArray<JdbcDriverDto>>() {

          @Override
          public void onResource(Response response, JsArray<JdbcDriverDto> resource) {
            getView().setAvailableDrivers(resource);
          }
        }).get().send();

    methodValidationHandler = new MethodValidationHandler();

    getView().getUsageChangeHandlers().addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        Usage usage = Usage.valueOf(getView().getUsageText().getText());
        getView().toggleDefaultStorage(usage == Usage.STORAGE);
        getView().setAvailableSqlSchemas(usage.getSupportedSqlSchemas());
      }
    });
  }

  private void setDialogMode(Mode dialogMode) {
    this.dialogMode = dialogMode;
    getView().setDialogMode(dialogMode);
  }

  /**
   * Setup the dialog for creating a method
   */
  public void createNewDatabase() {
    setDialogMode(Mode.CREATE);
  }

  /**
   * Setup the dialog for updating an existing method
   *
   * @param dto method to update
   */
  public void updateDatabase(DatabaseDto dto) {
    setDialogMode(Mode.UPDATE);
    displayDatabase(dto);
  }

  private void displayDatabase(DatabaseDto dto) {
    SqlDatabaseDto sqlDatabaseDto = (SqlDatabaseDto) dto.getExtension(SqlDatabaseDto.DatabaseDtoExtensions.settings);
    GWT.log("DatabaseDto: " + dto);
    GWT.log("SqlDatabaseDto: " + sqlDatabaseDto);
    getView().getName().setText(dto.getName());
    getView().getUsageText().setText(dto.getUsage() == null ? null : dto.getUsage().getName());
    getView().getDriver().setText(sqlDatabaseDto.getDriverClass());
    getView().getDefaultStorage().setValue(dto.getDefaultStorage());
    getView().getUrl().setText(sqlDatabaseDto.getUrl());
    getView().getUsername().setText(sqlDatabaseDto.getUsername());
    getView().getPassword().setText(sqlDatabaseDto.getPassword());
    getView().getProperties().setText(sqlDatabaseDto.getProperties());
    getView().getSqlSchema()
        .setText(sqlDatabaseDto.getSqlSchema() == null ? null : sqlDatabaseDto.getSqlSchema().getName());
  }

  private DatabaseDto getDto() {
    DatabaseDto dto = DatabaseDto.create();
    SqlDatabaseDto sqlDto = SqlDatabaseDto.create();

    dto.setName(getView().getName().getText());
    dto.setUsage(parseUsage(getView().getUsageText().getText()));
    dto.setDefaultStorage(getView().getDefaultStorage().getValue());

    sqlDto.setUrl(getView().getUrl().getText());
    sqlDto.setDriverClass(getView().getDriver().getText());
    sqlDto.setUsername(getView().getUsername().getText());
    sqlDto.setPassword(getView().getPassword().getText());
    sqlDto.setProperties(getView().getProperties().getText());
    sqlDto.setSqlSchema(parseSqlSchema(getView().getSqlSchema().getText()));

    dto.setExtension(SqlDatabaseDto.DatabaseDtoExtensions.settings, sqlDto);
    return dto;
  }

  private DatabaseDto.Usage parseUsage(String usageTxt) {
    if(DatabaseDto.Usage.EXPORT.getName().equals(usageTxt)) {
      return DatabaseDto.Usage.EXPORT;
    }
    if(DatabaseDto.Usage.IMPORT.getName().equals(usageTxt)) {
      return DatabaseDto.Usage.IMPORT;
    }
    if(DatabaseDto.Usage.STORAGE.getName().equals(usageTxt)) {
      return DatabaseDto.Usage.STORAGE;
    }
    throw new IllegalArgumentException("Unknown database usage: " + usageTxt);
  }

  private SqlDatabaseDto.SqlSchema parseSqlSchema(String sqlSchemaTxt) {
    if(SqlDatabaseDto.SqlSchema.HIBERNATE.getName().equals(sqlSchemaTxt)) {
      return SqlDatabaseDto.SqlSchema.HIBERNATE;
    }
    if(SqlDatabaseDto.SqlSchema.JDBC.getName().equals(sqlSchemaTxt)) {
      return SqlDatabaseDto.SqlSchema.JDBC;
    }
    if(SqlDatabaseDto.SqlSchema.LIMESURVEY.getName().equals(sqlSchemaTxt)) {
      return SqlDatabaseDto.SqlSchema.LIMESURVEY;
    }
    throw new IllegalArgumentException("Unknown Sql Schema: " + sqlSchemaTxt);
  }

  private void updateDatabase() {
    if(methodValidationHandler.validate()) {
      DatabaseDto dto = getDto();
      ResponseCodeCallback callbackHandler = new CreateOrUpdateCallBack(dto);
      ResourceRequestBuilderFactory.newBuilder() //
          .forResource(DatabaseResources.database(dto.getName())) //
          .put() //
          .withResourceBody(DatabaseDto.stringify(dto)) //
          .withCallback(Response.SC_OK, callbackHandler) //
          .withCallback(Response.SC_CREATED, callbackHandler) //
          .withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
    }
  }

  private void createDatabase() {
    if(methodValidationHandler.validate()) {
      DatabaseDto dto = getDto();
      ResponseCodeCallback callbackHandler = new CreateOrUpdateCallBack(dto);
      ResourceRequestBuilderFactory.newBuilder() //
          .forResource(DatabaseResources.databases()) //
          .post() //
          .withResourceBody(DatabaseDto.stringify(dto)) //
          .withCallback(Response.SC_OK, callbackHandler) //
          .withCallback(Response.SC_CREATED, callbackHandler) //
          .withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
    }
  }

  private class MethodValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<FieldValidator>();
        validators.add(new RequiredTextValidator(getView().getName(), "NameIsRequired") //
            .setId(Display.FormField.NAME.name()));
        validators.add(new RequiredTextValidator(getView().getUrl(), "UrlIsRequired") //
            .setId(Display.FormField.URL.name()));
        validators.add(new RequiredTextValidator(getView().getUsername(), "UsernameIsRequired") //
            .setId(Display.FormField.USERNAME.name()));
        validators.add(new RequiredTextValidator(getView().getPassword(), "PasswordIsRequired") //
            .setId(Display.FormField.PASSWORD.name()));
        validators.add(new RequiredTextValidator(getView().getUsageText(), "UsageIsRequired") //
            .setId(Display.FormField.USAGE.name()));
        validators.add(new RequiredTextValidator(getView().getSqlSchema(), "SqlSchemaIsRequired") //
            .setId(Display.FormField.SQL_SCHEMA.name()));
        validators.add(new RequiredTextValidator(getView().getDriver(), "DriverIsRequired") //
            .setId(Display.FormField.DRIVER.name()));
      }
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(Display.FormField.valueOf(id), message);
    }
  }

  private class CreateOrUpdateCallBack implements ResponseCodeCallback {

    private final DatabaseDto dto;

    private CreateOrUpdateCallBack(DatabaseDto dto) {
      this.dto = dto;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      switch(response.getStatusCode()) {
        case Response.SC_OK:
          getView().hideDialog();
          getEventBus().fireEvent(new DatabaseUpdatedEvent(dto));
          break;
        case Response.SC_CREATED:
          getView().hideDialog();
          getEventBus().fireEvent(new DatabaseCreatedEvent(dto));
          break;
        default:
          ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
          Collection<ConstraintViolationErrorDto> violationDtos = ConstraintViolationUtils.parseErrors(error);
          if(violationDtos.isEmpty()) {
            String errorMessage = translations.userMessageMap().get(error.getStatus());
            getView().showError(errorMessage == null
                ? translationMessages
                .unknownResponse(error.getStatus(), String.valueOf(JsArrays.toList(error.getArgumentsArray())))
                : errorMessage);
          } else {
            ConstraintViolationErrorsEvent.fire(getView().asWidget(), violationDtos);
          }
      }
    }
  }

  public interface Display extends PopupView, HasUiHandlers<DatabaseUiHandlers> {

    enum FormField {
      NAME,
      DRIVER,
      URL,
      USERNAME,
      PASSWORD,
      USAGE,
      SQL_SCHEMA
    }

    void hideDialog();

    void setAvailableDrivers(JsArray<JdbcDriverDto> resource);

    void setAvailableSqlSchemas(SqlSchema... sqlSchemas);

    void setDialogMode(Mode dialogMode);

    void showError(String message);

    void showError(@Nullable FormField formField, String message);

    HasText getName();

    HasText getUsageText();

    HasText getSqlSchema();

    HasText getUrl();

    HasText getDriver();

    HasText getUsername();

    HasText getPassword();

    HasText getProperties();

    HasValue<Boolean> getDefaultStorage();

    HasChangeHandlers getUsageChangeHandlers();

    void toggleDefaultStorage(boolean show);

  }

}
