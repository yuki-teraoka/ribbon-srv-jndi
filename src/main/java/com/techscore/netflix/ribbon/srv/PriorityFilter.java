package com.techscore.netflix.ribbon.srv;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.netflix.loadbalancer.ServerListSubsetFilter;

public class PriorityFilter extends ServerListSubsetFilter<SrvServer> {

    @Override
    public List<SrvServer> getFilteredListOfServers(List<SrvServer> servers) {
        if (servers.isEmpty()) {
            return servers;
        }
        Map<Integer, List<SrvServer>> map = servers.stream().collect(Collectors.groupingBy(SrvServer::getPriority));
        return map.keySet().stream().sorted().map(map::get)
          .map(super::getFilteredListOfServers)
          .filter(list -> list.size() > 0)
          .findFirst()
          .orElse(Collections.emptyList());
    }
}
