package registry;

public interface ServiceRegistry {
    /*
    * regist service
    * @param rpcServiceName     rpc service name
    * @param InetSocketAddress  service address
    *
    * */
    public void registerService(String rpcServiceName, String inetSocketAddress);

}
