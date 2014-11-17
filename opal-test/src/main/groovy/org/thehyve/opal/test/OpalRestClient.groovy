package org.thehyve.opal.test

import com.google.protobuf.ExtensionRegistry
import org.apache.http.HttpResponse
import org.obiba.opal.rest.client.magma.OpalJavaClient
import org.obiba.opal.rest.client.magma.UriBuilder
import org.obiba.opal.web.model.Database
import org.obiba.opal.web.model.Magma
import org.obiba.opal.web.model.Opal
import org.obiba.opal.web.model.Search

class OpalRestClient {

    OpalJavaClient client

    HttpResponse get(String ... parts) {
        client.get(client.newUri().segment(parts).build())
    }

    List getServices() {
        client.getResources(Opal.ServiceDto.class, client.newUri().segment('services').build(), Opal.ServiceDto.newBuilder())
    }

    Opal.ServiceDto getService(String name) {
        client.getResource(Opal.ServiceDto.class, client.newUri().segment('service', name).build(), Opal.ServiceDto.newBuilder())
    }

    List<Opal.TableIndexStatusDto> getSearchIndexes() {
        client.getResources(Opal.TableIndexStatusDto, client.newUri().segment('service', 'search', 'indices').build(), Opal.TableIndexStatusDto.newBuilder())
    }

    List<Database.DatabaseDto> getSqlDatabases() {
        client.getResources(Database.DatabaseDto, client.newUri().segment('service','system','databases','sql').build(), Database.DatabaseDto.newBuilder())
    }

    def getDatasources() {
        client.getResource(Magma.TableDto, client.newUri().segment('tables').build(), Magma.TableDto.newBuilder())

        //client.getResource(Magma.DatasourceDto, client.newUri().segment('datasources').build(), Magma.DatasourceDto.newBuilder())
        //println client.get(client.newUri().segment('datasources').build()).entity.content.text
        //null
    }

    Search.ValueSetsResultDto search(String ds, String table, Integer limit, Integer offset, String query) {

        UriBuilder builder = client.newUri().segment('datasource',ds,'table', table, 'valueSets', '_search')
        if (limit) {
            builder.query('limit', limit.toString())
        }
        if (offset) {
            builder.query('offset', offset.toString())
        }
        if (query) {
            builder.query('query', query)
        }

        client.getResource(Search.ValueSetsResultDto, builder.build(), Search.ValueSetsResultDto.newBuilder())
    }


    Search.QueryResultDto facetSearch(String ds, String table, Search.QueryTermsDto query) {

        UriBuilder builder = client.newUri().segment('datasource',ds,'table', table, 'facets', '_search')
        client.postResource(Search.QueryResultDto, builder.build(), Search.QueryResultDto.newBuilder(), query)
    }

    static Search.QueryTermsDto createTerms(Search.QueryTermDto ... terms) {
        Search.QueryTermsDto.Builder builder = Search.QueryTermsDto.newBuilder();
        builder.addAllQueries(Arrays.asList(terms))
        builder.build()
    }

    static Search.QueryTermDto createTerm(boolean global, String facet, Search.LogicalTermDto lt) {
        Search.QueryTermDto.Builder builder = Search.QueryTermDto.newBuilder()
        builder.setFacet(facet)
        if (global) {
            builder.setGlobal(global)
        }

        builder.setExtension(Search.LogicalTermDto.filter, lt)
        builder.build()
    }

    static Search.LogicalTermDto createLogical(Search.TermOperator op, Search.FilterDto ... filters) {
        Search.LogicalTermDto.Builder builder = Search.LogicalTermDto.newBuilder()
        builder.setOperator(op)
        List<Search.FilterDto> list = Arrays.asList(filters)
        builder.setExtension(Search.FilterDto.filters, list)
        builder.build()
    }

    static Search.FilterDto createIn(String var, boolean not, String ... values) {
        Search.InTermDto.Builder inBuilder = Search.InTermDto.newBuilder()
        inBuilder.addAllValues(Arrays.asList(values))
        inBuilder.setMinimumMatch(1)
        Search.FilterDto.Builder builder = Search.FilterDto.newBuilder()
        builder.setVariable(var)
        if (not) {
            builder.setNot(true)
        }
        builder.setExtension(Search.InTermDto.terms, inBuilder.build())
        builder.build()
    }

    void testQuery(String ds, String table, String query) {
        Search.ValueSetsResultDto results = search(ds, table, null, null, query)
        println results.totalHits
    }


    void testFacetQuery(String ds, String table, long request) {
        testFacetQuery(ds, table, readQuery(request))
    }

    void testFacetQuery(String ds, String table, Search.QueryTermsDto query) {
        Search.QueryResultDto result = facetSearch(ds, table, query)
        println query
        //println result.getTotalHits()
        result.getFacets(0).filtersList.each { println it.count }
    }

    static Search.QueryTermsDto readQuery(long request) {

        ExtensionRegistry registry =  ExtensionRegistry.newInstance()
        Search.registerAllExtensions(registry)

        File file = new File("/tmp", "req_" + request + ".ser")
        FileInputStream input = new FileInputStream(file)
        try {
            return Search.QueryTermsDto.parseFrom(input, registry)
        } finally {
            input.close()
        }
    }

}
