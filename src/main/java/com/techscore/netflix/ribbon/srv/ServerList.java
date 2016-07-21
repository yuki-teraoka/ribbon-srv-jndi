package com.techscore.netflix.ribbon.srv;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;

public class ServerList extends AbstractServerList<SrvServer> {
    private String serviceName;
    private InitialDirContext dirContext;

    private static final CommonClientConfigKey<String> CONFIG_KEY_SERVICE_NAME=new CommonClientConfigKey<String>("ServiceName"){};
    private static final CommonClientConfigKey<String> CONFIG_KEY_DNS_SERVER=new CommonClientConfigKey<String>("DnsServer"){};
    
    private static final String CONFIG_KEY_JNDI = "jndi.";
    
    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
        String serviceName = clientConfig.getPropertyAsString(CONFIG_KEY_SERVICE_NAME, null);
        this.serviceName = Preconditions.checkNotNull(serviceName, CONFIG_KEY_SERVICE_NAME.key() + " is required.");

        Map<String, Object> jndiEnv = new HashMap<>();
        String dnsServer = clientConfig.getPropertyAsString(CONFIG_KEY_DNS_SERVER, null);

        clientConfig.getProperties().forEach((key, value) -> {
            if (key.startsWith(CONFIG_KEY_JNDI)) {
                String newKey = key.substring(CONFIG_KEY_JNDI.length());
                jndiEnv.put(newKey, value);
            }
        });

        String providerUrl;
        if (dnsServer != null) {
            providerUrl = Stream.of(dnsServer.split(",")).collect(Collectors.joining(" ", "dns://", ""));
            jndiEnv.put(Context.PROVIDER_URL, providerUrl);
        }
        Properties environments = new Properties();
        if (jndiEnv != null) {
            environments.putAll(jndiEnv);
        }
        environments.put(Context.INITIAL_CONTEXT_FACTORY, "com.techscore.netflix.ribbon.srv.jndi.DnsSrvContextFactory");
        try {
            this.dirContext = new InitialDirContext(environments);
        } catch (NamingException e) {
            throw Throwables.propagate(e);
        }
    }


    @Override
    public List<SrvServer> getInitialListOfServers() {
        return getServers();
    }

    @Override
    public List<SrvServer> getUpdatedListOfServers() {
        return getServers();
    }

    @SuppressWarnings("unchecked")
    private List<SrvServer> getServers() {
        try {
            System.out.println(dirContext.lookup(serviceName));
            return (List<SrvServer>) dirContext.lookup(serviceName);
        } catch (NamingException e) {
            throw Throwables.propagate(e);
        }
    }
}
