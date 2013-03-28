/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.math;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.core.magma.math.CategoricalVariableSummary;
import org.obiba.opal.search.StatsIndexManager;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.model.Math.CategoricalSummaryDto;
import org.obiba.opal.web.model.Math.FrequencyDto;
import org.obiba.opal.web.model.Math.SummaryStatisticsDto;
import org.obiba.opal.web.search.support.EsQueryExecutor;
import org.obiba.opal.web.search.support.QueryTermJsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class CategoricalSummaryResource extends AbstractSummaryResource {

  private static final Logger log = LoggerFactory.getLogger(CategoricalSummaryResource.class);

  public CategoricalSummaryResource(OpalSearchService opalSearchService, StatsIndexManager statsIndexManager,
      ElasticSearchProvider esProvider, ValueTable valueTable, Variable variable) {
    super(opalSearchService, statsIndexManager, esProvider, valueTable, variable, null);
  }

  @GET
  @POST
  public Response get(@QueryParam("distinct") boolean distinct) {
    CategoricalSummaryDto summary = canQueryEsIndex() ? queryEs() : queryMagma(distinct);

    SummaryStatisticsDto statisticsDto = SummaryStatisticsDto.newBuilder().setResource(getVariable().getName())
        .setExtension(CategoricalSummaryDto.categorical, summary).build();
    return TimestampedResponses.ok(getValueTable(), statisticsDto).build();
  }

  private CategoricalSummaryDto queryEs() {

    try {

      String indexName = statsIndexManager.getIndex(getValueTable()).getIndexName();
      QueryTermJsonBuilder.QueryTermsFiltersBuilder filtersBuilder = new QueryTermJsonBuilder.QueryTermsFiltersBuilder()
          .setFieldName("_type").addFilterValue(indexName);

      QueryTermJsonBuilder queryBuilder = new QueryTermJsonBuilder() //
          .setTermFieldName("_id") //
          .setTermFieldValue(getVariable().getVariableReference(getValueTable())) //
          .setTermFilters(filtersBuilder.build());
      JSONObject query = queryBuilder.build();
      log.debug("ES query: {}", query.toString(2));

      JSONObject response = new EsQueryExecutor(esProvider).execute(query);
      log.debug("ES Response: {}", response.toString(2));

      JSONObject jsonHitsInfo = response.getJSONObject("hits");
      if(jsonHitsInfo.getInt("total") != 1) {
        throw new RuntimeException("Cannot find indexed summary for variable " + getVariable().getName());
      }
      return parseJsonSummary(jsonHitsInfo.getJSONArray("hits").getJSONObject(0).getJSONObject("_source"));

    } catch(JSONException e) {
      throw new RuntimeException(e);
    }
  }

  private CategoricalSummaryDto parseJsonSummary(JSONObject jsonSummary) throws JSONException {
    CategoricalSummaryDto.Builder dtoBuilder = CategoricalSummaryDto.newBuilder();
    dtoBuilder.setMode(jsonSummary.getString("cat-summary-mode")).setN(jsonSummary.getLong("cat-summary-n"));

    JSONArray jsonValues = jsonSummary.getJSONArray("cat-summary-freq-value");
    JSONArray jsonFreq = jsonSummary.getJSONArray("cat-summary-freq-freq");
    JSONArray jsonPct = jsonSummary.getJSONArray("cat-summary-freq-pct");

    int nbValues = jsonValues.length();
    for(int i = 0; i < nbValues; i++) {
      dtoBuilder.addFrequencies(FrequencyDto.newBuilder().setValue(jsonValues.getString(i)).setFreq(jsonFreq.getLong(i))
          .setPct(jsonPct.getDouble(i)));
    }

    return dtoBuilder.build();
  }

  private CategoricalSummaryDto queryMagma(boolean distinct) {
    // TODO remove distinct param
    // TODO store this new summary to ES
    CategoricalVariableSummary summary = new CategoricalVariableSummary.Builder(getVariable()) //
        .distinct(distinct) //
        .addTable(getValueTable()) //
        .build();

    CategoricalSummaryDto.Builder dtoBuilder = CategoricalSummaryDto.newBuilder();
    dtoBuilder.setMode(summary.getMode()).setN(summary.getN());
    for(CategoricalVariableSummary.Frequency frequency : summary.getFrequencies()) {
      dtoBuilder.addFrequencies(FrequencyDto.newBuilder().setValue(frequency.getValue()).setFreq(frequency.getFreq())
          .setPct(frequency.getPct()));
    }

    return dtoBuilder.build();
  }
}