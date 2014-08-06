package org.obiba.opal.core.service;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.easymock.EasyMock;
import org.obiba.magma.Datasource;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.support.StaticDatasource;
import org.obiba.magma.support.StaticValueTable;
import org.obiba.magma.type.TextType;

public class MagmaHelper {

	public static final String VOCAB_VARIABLE = "code";
	public static final String ENTITY = "Participant";
	
    public static String getVocabularyFileUrl() throws Exception {
        File file = new File("src/test/resources/vocabulary.csv");
        URI uri = file.toURI();
        return uri.toURL().toExternalForm();
    }
    
    public static Variable createVocabularyVariable() throws Exception {
    	return createVocabularyVariable(getVocabularyFileUrl());
    }

    public static Variable createVocabularyVariable(String vocabUrl) {
    	Variable.Builder builder = new Variable.Builder(VOCAB_VARIABLE, TextType.get(), ENTITY);
    	builder.addAttribute(ValidationService.VOCABULARY_URL_ATTRIBUTE, vocabUrl);
    	return builder.build();
    }
    
    public static final Value valueOf(Object obj) {
    	if (obj instanceof Value) {
    		return (Value)obj;
    	} else if (obj instanceof String) {
    		return TextType.get().valueOf(obj);
    	} else {
    		throw new UnsupportedOperationException("Not implemented for " + obj);
    	}
    }
    
    public static Datasource createDatasource(boolean validate) {
    	StaticDatasource result = new StaticDatasource("test");
    	if (validate) {
        	result.setAttributeValue(ValidationService.VALIDATE_ATTRIBUTE, TextType.get().valueOf("true"));
    	}
    	return result;
    }
    
    public static StaticValueTable createValueTable(Datasource datasource, Set<String> entities, Variable ... variables) throws Exception {
		StaticValueTable result = new StaticValueTable(datasource, "table", entities);
		for (Variable var: variables) {
			result.addVariable(var);
		}

		return result;
    }
    
    public static void addRow(StaticValueTable table, String entityId, Object ... varnameAndValues ) {
    	List<Object> list = new ArrayList<>();
    	for (int i=0; i<varnameAndValues.length; i+=2) {
    		Variable var = table.getVariable(varnameAndValues[i].toString());
    		list.add(var);
    		list.add(valueOf(varnameAndValues[i+1]));
    	}
    	table.addValues(entityId, list.toArray());
    }
    
    public static ValueSet createValueSet() {
    	ValueSet result = EasyMock.createMock(ValueSet.class);
    	
    	
    	return result;
    }

}
