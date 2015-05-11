package org.thehyve.opal.test

import org.obiba.opal.rest.client.magma.OpalJavaClient
import org.obiba.opal.web.model.Opal
import org.obiba.opal.web.model.Search

/**
 * Created by carlos on 4/18/14.
 */
class Main {

    static OpalRestClient client


    static void main(String ... args) {
        int port = 8080
        //port = 9080
        //OpalJavaClient ojc = new OpalJavaClient('https://obibatest2.thehyve.net/ws','administrator','password')
        OpalJavaClient ojc = new OpalJavaClient("http://localhost:$port/ws",'administrator','password')
        client = new OpalRestClient(client: ojc)

        //testQuery('testrobert','newview', 'testrobert-newview-Gender:("Female")') //24
        //testFacetQuery('testrobert','newview', 1415802989258L) //24

        //**********************

        //testQuery('testrobert','newview', 'testrobert-newview-ICD10:("E00")') //15
        //testFacetQuery('testrobert','newview', 1415802930505L) //0

        //testFacetQueries()
        //testEidAuth()
        testConnectionFromMica()
    }

    static testEidAuth() {

        for (Opal.AuthClientDto dto: client.getAuthClients()) {
            println("${dto.getName()} ${dto.getRedirectUrl()}")
        }

    }

    static void testQueries() {
        //String query = 'multitest-donour-color:Red'
        //String query = 'multitest-donour-color:Black'
        String query = 'multitest-donour-color:Red AND multitest-donour-code:Foo'
        //String query = 'multitest-donour-color:Red AND multitest-donour-color:Blue'
        //String query = 'multitest-donour-color:Red AND multitest-donour-color:Black'
        //String query = 'multitest-donour-gender:("MALE")'
        //String query = 'multitest-donour-color:("Red" OR "Black")'

        Search.ValueSetsResultDto results = client.search("multitest","donour",null, null, query)

    }

    static void testConnectionFromMica() {
        Search.QueryTermsDto query = Search.QueryTermsDto.newBuilder().build();
        client.testFacetQuery('carlos','view', query)
    }

    static void testFacetQueries() {
        //String query = 'testrobert-newview-ICD10:("ch3" OR "E00")'
        //Search.ValueSetsResultDto results = client.search("testrobert","newview",null, null, query)
        //println results.totalHits

        //testQuery('testrobert','newview', 'testrobert-newview-Age:(31)') //correct: 2
        //testQuery('testrobert','newview', 'NOT testrobert-newview-Age:(31)') //correct: 46

        //testQuery('testrobert','newview', 'testrobert-newview-Gender:(Male)') //correct: 24
        //testQuery('testrobert','newview', 'testrobert-newview-Gender:("Male")') //correct: 24
        //testQuery('testrobert','newview', 'NOT testrobert-newview-Gender:(Male)') //correct: 24

        //testQuery('testrobert','newview', 'testrobert-newview-ICD10:("E00")') //correct: 15
        //testQuery('testrobert','newview', 'testrobert-newview-ICD10:(E00)') //correct: 15
        //testQuery('testrobert','newview', 'NOT testrobert-newview-ICD10:(E00)') //correct: 33

        //Search.QueryTermsDto query = createFaultyInQuery('Age', false, '31') //correct: 2
        Search.QueryTermsDto query = createFaultyInQuery('Age', true, '31') //wrong: 148 (should be 46)

        //Search.QueryTermsDto query = createFaultyInQuery('Gender', false, 'Male') //correct: 24
        //Search.QueryTermsDto query = createFaultyInQuery('Gender', true, 'Male') //wrong: 126 (should be 24)

        //Search.QueryTermsDto query = createFaultyInQuery('ICD10', false, 'E00') //wrong: 0 (should be 15)
        //Search.QueryTermsDto query = createFaultyInQuery('ICD10', true, 'E00') //wrong: 150 (should be 33)

        client.testFacetQuery('testrobert','newview', query)
    }

    static Search.QueryTermsDto createFaultyInQuery(String variable, boolean not, String ... values) {
        Search.LogicalTermDto lt = client.createLogical(Search.TermOperator.AND_OP,
                client.createIn(variable, not, values))
        Search.QueryTermDto term1 = client.createTerm(true, '21', lt)
        Search.QueryTermDto term2 = client.createTerm(false, '_matched', lt)
        client.createTerms(term1, term2)
    }

}
