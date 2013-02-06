/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.magma;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.AbstractDatasourceWrapper;
import org.obiba.opal.core.domain.participant.identifier.IParticipantIdentifier;
import org.obiba.opal.core.unit.FunctionalUnit;

/**
 *
 */
public class FunctionalUnitDatasource extends AbstractDatasourceWrapper {

  @Nonnull
  private final FunctionalUnit unit;

  @Nonnull
  private final FunctionalUnitView.Policy policy;

  @Nonnull
  private final ValueTable keysTable;

  @Nullable
  private final IParticipantIdentifier identifierGenerator;

  private final boolean ignoreUnknownIdentifier;

  public FunctionalUnitDatasource(@Nonnull Datasource wrapped, @Nonnull FunctionalUnit unit,
      @Nonnull FunctionalUnitView.Policy policy, @Nonnull ValueTable keysTable,
      @Nullable IParticipantIdentifier identifierGenerator, boolean ignoreUnknownIdentifier) {
    super(wrapped);

    this.unit = unit;
    this.policy = policy;
    this.keysTable = keysTable;
    this.identifierGenerator = identifierGenerator;
    this.ignoreUnknownIdentifier = ignoreUnknownIdentifier;
  }

  @Override
  public ValueTable getValueTable(String name) throws NoSuchValueTableException {
    return new FunctionalUnitView(unit, policy, super.getValueTable(name), keysTable, identifierGenerator,
        ignoreUnknownIdentifier);
  }

  @Override
  public Set<ValueTable> getValueTables() {
    Set<ValueTable> views = new HashSet<ValueTable>();
    for(ValueTable sourceTable : super.getValueTables()) {
      views.add(
          new FunctionalUnitView(unit, policy, sourceTable, keysTable, identifierGenerator, ignoreUnknownIdentifier));
    }
    return views;
  }
}
