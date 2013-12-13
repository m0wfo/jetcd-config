package com.mowforth;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import jetcd.EtcdClient;
import jetcd.EtcdClientFactory;
import jetcd.EtcdException;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class EtcdConfigTest {

    private EtcdClient client;

    @Before
    public void setup() {
        client = EtcdClientFactory.newInstance();
    }

    @Test
    public void testGetMap() throws EtcdException {
        seedEtcd();
        Map<String, ? extends Object> tree = EtcdConfig.deepList(client, "/");

        assertNotNull(tree);
        assertEquals(tree.get("datacenter"), "test");
        assertTrue(tree.get("node0") instanceof Map);
        Map nodeConfig = (Map)tree.get("node0");
        assertEquals(nodeConfig.get("port"), "1337");
        assertEquals(nodeConfig.get("bind"), "0.0.0.0");
    }

    @Test
    public void testGetConfig() throws EtcdException {
        seedEtcd();
        Map<String, ? extends Object> tree = EtcdConfig.deepList(client, "/");
        Config config = EtcdConfig.getConfig(tree);

        assertNotNull(config);
        assertEquals(config.getString("datacenter"), "test");
        assertEquals(config.getInt("node0.port"), 1337);
        assertEquals(config.getString("node0.bind"), "0.0.0.0");
        assertEquals(config.getString("node0.components.foo"), "enabled");
        assertEquals(config.getString("node0.components.bar"), "enabled");
    }

    @Test
    public void testSeedEtcdFromConfig() throws EtcdException {
        // read from test/resources/application.json
        Config config = ConfigFactory.load();

        EtcdConfig.seedConfig(client, config);
    }

    private void seedEtcd() throws EtcdException {
        client.setKey("datacenter", "test");
        client.setKey("node0/port", "1337");
        client.setKey("node0/bind", "0.0.0.0");
        client.setKey("node0/components/foo", "enabled");
        client.setKey("node0/components/bar", "enabled");
    }
}
