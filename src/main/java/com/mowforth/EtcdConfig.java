package com.mowforth;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import jetcd.EtcdClient;
import jetcd.EtcdClientFactory;
import jetcd.EtcdException;

import java.util.Map;

/**
 * Utilities for reading configurations from etcd.
 *
 * <p>These helper utilities allow Typesafe {@link Config} objects
 * to be derived from a suitable etcd configuration.</p>
 */
public class EtcdConfig {

    /**
     * Get the contents of the configuration hierarchy
     * at a given root key.
     *
     * <p>Similar to {@link EtcdClient#list(String)}, but
     * it recursively scans subdirectories for their contents.</p>
     *
     * <p>Useful for generating static configuration objects where all
     * nested objects have to be known ahead of time.</p>
     *
     * @param client an {@link EtcdClient} instance
     * @param root the top-level node to start listing at
     * @return A map of the entire tree under the root key
     * @throws EtcdException
     */
    public static Map<String, ? extends Object> deepList(EtcdClient client, String root) throws EtcdException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(root));
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        Map<String, String> list = client.list(root);
        for (Map.Entry<String, String> entry : list.entrySet()) {
            String[] parts = entry.getKey().split("/");
            Preconditions.checkState(parts.length > 0);
            String shortName = parts[parts.length - 1];
            if (entry.getValue() == null) {
                builder.put(shortName, deepList(client, entry.getKey()));
            } else {
                builder.put(shortName, entry.getValue());
            }
        }
        return builder.build();
    }

    /**
     * Generate a Typesafe config object from a deep map.
     *
     * @param conf a nested map- see {@link #deepList(jetcd.EtcdClient, String)}
     * @return an immutable {@link Config} instance
     */
    public static Config getConfig(Map<String, ? extends Object> conf) {
        return ConfigValueFactory.fromMap(conf).toConfig();
    }

    public static void seedConfig(EtcdClient client, Config config) throws EtcdException {
        Map<String, Object> tree = config.root().unwrapped();
        
    }

    public static void main(String[] args) throws Exception {
        EtcdClient client = EtcdClientFactory.newInstance();
        Map m = deepList(client, "/");
        Config conf = getConfig(m);
    }
}
