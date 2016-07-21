package com.techscore.netflix.ribbon.srv.jndi;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.spi.DirObjectFactory;
import javax.naming.spi.InitialContextFactory;

import com.google.common.base.Throwables;
import com.techscore.netflix.ribbon.srv.SrvServer;

public class DnsSrvContextFactory implements InitialContextFactory {
    @Override
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
        @SuppressWarnings("unchecked")
        Hashtable<Object, Object> env = (Hashtable<Object, Object>) environment.clone();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
        env.put(Context.OBJECT_FACTORIES, this.getClass().getName() + "$SrvServerListFactory");
        env.put("com.sun.jndi.dns.lookup.attr", "IN SRV");
        return new InitialDirContext(env);
    }

    public static class SrvServerListFactory implements DirObjectFactory {

        @Override
        public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
            return null;
        }

        @Override
        public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment, Attributes attrs)
                throws Exception {
            List<SrvServer> results = new ArrayList<>();
            String fqn = name.toString();
            if (!fqn.endsWith(".")) {
                fqn = fqn + '.';
            }
            try {
                NamingEnumeration<?> allAttr = attrs.getAll();
                while (allAttr.hasMore()) {
                    Attribute attr = (Attribute) allAttr.next();
                    String rrType = attr.getID();
                    if (!rrType.equals("SRV")) {
                        continue;
                    }
                    int size = attr.size();
                    for (int i = 0; i < size; i++) {
                        String line = (String) attr.get(i);
                        results.add(parseSrv(fqn, line));
                    }
                }
            } catch (NamingException e) {
                throw Throwables.propagate(e);
            }
            return results;
        }

        private SrvServer parseSrv(String name, String line) {
            String[] args = line.split(" ", 4);
            int priority = Integer.parseInt(args[0], 10);
            int weight = Integer.parseInt(args[1], 10);
            int port = Integer.parseInt(args[2], 10);
            String target = args[3];
            return new SrvServer(name, priority, weight, port, target);
        };
    }

}
