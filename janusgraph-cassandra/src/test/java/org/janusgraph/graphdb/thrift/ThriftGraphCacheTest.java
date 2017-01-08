package org.janusgraph.graphdb.thrift;

import org.janusgraph.CassandraStorageSetup;
import org.janusgraph.StorageSetup;
import org.janusgraph.diskstorage.configuration.WriteConfiguration;
import org.janusgraph.graphdb.JanusGraphTest;
import org.junit.BeforeClass;

public class ThriftGraphCacheTest extends JanusGraphTest {

    @Override
    public WriteConfiguration getConfiguration() {
        return StorageSetup.addPermanentCache(CassandraStorageSetup.getCassandraThriftConfiguration(getClass().getSimpleName()));
    }


    @BeforeClass
    public static void beforeClass() {
        CassandraStorageSetup.startCleanEmbedded();
    }



    @Override
    protected boolean isLockingOptimistic() {
        return true;
    }
}