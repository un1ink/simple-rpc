package registry;

import remoting.dto.RpcRequest;
import remoting.dto.RpcResponse;

import java.net.InetSocketAddress;

public interface ServiceDiscovery {
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
