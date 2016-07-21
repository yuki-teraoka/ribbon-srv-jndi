# ribbon-srv-jndi

## examample


    Properties properties = new Properties();
    properties.put("sample-client.ribbon.NIWSServerListClassName", "com.techscore.netflix.ribbon.srv.ServerList");
    properties.put("sample-client.ribbon.NIWSServerListFilterClassName", "com.techscore.netflix.ribbon.srv.PriorityFilter");
    properties.put("sample-client.ribbon.ServiceName", "_http._tcp.example.com");
    // optional
    properties.put("sample-client.ribbon.DnsServer", "192.168.1.1:53,192.168.1.2:53");
    
    
    ConfigurationManager.loadProperties(properties);
    ILoadBalancer lb = ClientFactory.getNamedLoadBalancer("sample-client");
    NettyHttpClient<ByteBuf, ByteBuf> client = RibbonTransport.newHttpClient(lb, ClientFactory.getNamedConfig("sample-client"));
    HttpClientRequest<ByteBuf> req = HttpClientRequest.createGet("/");
    
    client.submit(req)
      .map(HttpClientResponse::getContent)
      .first().flatMap(o -> o)
      .map(b -> b.toString(Charset.defaultCharset()))
      .first().toBlocking().forEach(System.out::println);
    
    client.shutdown();