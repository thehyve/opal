/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datasource.onyx;

import java.util.Iterator;
import java.util.List;

import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.engine.variable.VariableDataSet;
import org.obiba.onyx.util.data.Data;
import org.obiba.opal.core.domain.data.DataItem;
import org.obiba.opal.core.domain.data.Dataset;
import org.obiba.opal.core.domain.data.Entity;
import org.obiba.opal.datasource.EntityProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

/**
 *
 */
public class OnyxDatasetReader extends AbstractOnyxReader<Dataset> implements ItemStreamReader<Dataset> {

  private static final Logger log = LoggerFactory.getLogger(OnyxDatasetReader.class);

  private EntityProvider entityProvider;

  private Iterator<String> entryIterator;

  public void setEntityProvider(EntityProvider entityProvider) {
    this.entityProvider = entityProvider;
  }

  @Override
  protected void doOpen(ExecutionContext executionContext) throws ItemStreamException {
    this.entryIterator = getDataInputStrategy().listEntries().iterator();
  }

  public Dataset read() throws Exception, UnexpectedInputException, ParseException {
    if(entryIterator.hasNext()) {
      String entryName = entryIterator.next();
      while(isParticipantEntry(entryName) == false && entryIterator.hasNext()) {
        entryName = entryIterator.next();
      }
      if(isParticipantEntry(entryName)) {
        log.info("Processing entry {}", entryName);
        VariableDataSet variableDataSetRoot = readVariableDataset(entryName);

        Entity entity = entityProvider.fetchEntity("onyx", entryName.replace(".xml", ""));

        // TODO: Lookup an existing dataset with same datasource and extraction date. If it already exists, then we
        // should skip it
        Dataset dataset = new Dataset(entity, "onyx", variableDataSetRoot.getExportDate());

        for(VariableData vd : variableDataSetRoot.getVariableDatas()) {
          List<Data> datum = vd.getDatas();
          // TODO: OPAL-30 handle occurrences correctly
          if(datum.size() == 1) {
            DataItem dataItem = new DataItem(dataset, vd.getVariablePath(), datum.get(0).getValueAsString());
            dataset.getDataItems().add(dataItem);
          }
        }
        return dataset;
      }
    }
    return null;
  }

}
