package org.janusgraph.graphdb.serializer;

import com.google.common.collect.Iterators;
import org.janusgraph.core.*;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.diskstorage.configuration.ModifiableConfiguration;
import org.janusgraph.graphdb.configuration.GraphDatabaseConfiguration;
import org.janusgraph.graphdb.database.StandardJanusGraph;
import org.janusgraph.graphdb.serializer.attributes.*;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.junit.*;

import static org.junit.Assert.*;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public class SerializerGraphConfiguration {

    StandardJanusGraph graph;

    @Before
    public void initialize() {
        ModifiableConfiguration config = GraphDatabaseConfiguration.buildGraphConfiguration();
        config.set(GraphDatabaseConfiguration.STORAGE_BACKEND,"inmemory");
        config.set(GraphDatabaseConfiguration.CUSTOM_ATTRIBUTE_CLASS, TClass1.class.getName(), "attribute1");
        config.set(GraphDatabaseConfiguration.CUSTOM_SERIALIZER_CLASS, TClass1Serializer.class.getName(), "attribute1");
        config.set(GraphDatabaseConfiguration.CUSTOM_ATTRIBUTE_CLASS, TEnum.class.getName(), "attribute4");
        config.set(GraphDatabaseConfiguration.CUSTOM_SERIALIZER_CLASS, TEnumSerializer.class.getName(), "attribute4");
        graph = (StandardJanusGraph) JanusGraphFactory.open(config);
    }

    @After
    public void shutdown() {
        graph.close();
    }

    @Test
    public void testOnlyRegisteredSerialization() {
        JanusGraphManagement mgmt = graph.openManagement();
        PropertyKey time = mgmt.makePropertyKey("time").dataType(Integer.class).make();
        PropertyKey any  = mgmt.makePropertyKey("any").cardinality(Cardinality.LIST).dataType(Object.class).make();
        mgmt.buildIndex("byTime",Vertex.class).addKey(time).buildCompositeIndex();
        EdgeLabel knows = mgmt.makeEdgeLabel("knows").make();
        VertexLabel person = mgmt.makeVertexLabel("person").make();
        mgmt.commit();

        JanusGraphTransaction tx = graph.newTransaction();
        JanusGraphVertex v = tx.addVertex("person");
        v.property("time", 5);
        v.property("any", new Double(5.0));
        v.property("any", new TClass1(5,1.5f));
        v.property("any", TEnum.THREE);
        tx.commit();

        tx = graph.newTransaction();
        v = tx.query().has("time",5).vertices().iterator().next();
        assertEquals(5,(int)v.value("time"));
        assertEquals(3, Iterators.size(v.properties("any")));
        tx.rollback();

        //Verify that non-registered objects aren't allowed
        for (Object o : new Object[]{new TClass2("abc",5)}) {
            tx = graph.newTransaction();
            v = tx.addVertex("person");
            try {
                v.property("any", o); //Should not be allowed
                tx.commit();
                fail();
            } catch (IllegalArgumentException e) {
            } finally {
                if (tx.isOpen()) tx.rollback();
            }

        }
    }


}