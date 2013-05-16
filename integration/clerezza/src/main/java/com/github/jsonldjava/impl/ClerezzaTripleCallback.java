package com.github.jsonldjava.impl;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.clerezza.rdf.core.*;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.impl.TypedLiteralImpl;

import com.github.jsonldjava.core.JSONLDTripleCallback;
import com.github.jsonldjava.utils.Obj;

public class ClerezzaTripleCallback implements JSONLDTripleCallback {

    private MGraph mGraph = new SimpleMGraph();
    private Map<String, BNode> bNodeMap = new HashMap<String, BNode>();

    public void setMGraph(MGraph mGraph) {
        this.mGraph = mGraph;
        bNodeMap = new HashMap<String, BNode>();
    }

    public MGraph getMGraph() {
        return mGraph;
    }

    private void triple(String s, String p, String o, String graph) {
        if (s == null || p == null || o == null) {
            // TODO: i don't know what to do here!!!!
            return;
        }

        NonLiteral subject = getNonLiteral(s);
		UriRef predicate = new UriRef(p);
		NonLiteral object = getNonLiteral(o);
		mGraph.add(new TripleImpl(subject, predicate, object));
    }

    private void triple(String s, String p, String value, String datatype, String language, String graph) {
        NonLiteral subject = getNonLiteral(s);
		UriRef predicate = new UriRef(p);
		Resource object;
		if (language != null) {
			object = new PlainLiteralImpl(value, new Language(language)); 
		} else {
			if (datatype != null) {
				object = new TypedLiteralImpl(value, new UriRef(datatype));
			} else {
				object = new PlainLiteralImpl(value);
			}
		}
      
		mGraph.add(new TripleImpl(subject, predicate, object));
    }

	private NonLiteral getNonLiteral(String s) {
		if (s.startsWith("_:")) {
			return getBNode(s);
		} else {
			return new UriRef(s);
		}
	}

	private BNode getBNode(String s) {
		if (bNodeMap.containsKey(s)) {
			return bNodeMap.get(s);
		} else {
			BNode result = new BNode();
			bNodeMap.put(s, result);
			return result;
		}
	}

	@Override
	public Object call(Map<String, Object> dataset) {
		for (String graphName : dataset.keySet()) {
			List<Map<String,Object>> triples = (List<Map<String, Object>>) dataset.get(graphName);
			if ("@default".equals(graphName)) {
				graphName = null;
			}
			for (Map<String,Object> triple : triples) {
				if ("literal".equals(Obj.get(triple, "object", "type"))) {
					triple((String)Obj.get(triple, "subject", "value"), (String)Obj.get(triple, "predicate", "value"), 
							(String)Obj.get(triple, "object", "value"), (String)Obj.get(triple, "object", "datatype"), (String)Obj.get(triple, "object", "language"), graphName);
				} else {
					triple((String)Obj.get(triple, "subject", "value"), (String)Obj.get(triple, "predicate", "value"), (String)Obj.get(triple, "object", "value"), graphName);
				}
			}
		}

		return getMGraph();
	}

}
