package com.xiong.remoting.transport.socket;

import com.xiong.enums.ServiceDiscoveryEnum;
import com.xiong.extension.ExtensionLoader;
import com.xiong.registry.ServiceDiscovery;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.xiong.remoting.dto.RpcRequest;
import com.xiong.remoting.transport.RpcRequestTransport;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

@Slf4j
@AllArgsConstructor
public class SocketRpcClient implements RpcRequestTransport {

    private final ServiceDiscovery serviceDiscovery;

    public SocketRpcClient(){
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension(ServiceDiscoveryEnum.ZK.getName());
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        try(Socket socket = new Socket()){
            socket.connect(inetSocketAddress);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(rpcRequest);
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            return objectInputStream.readObject();
        }catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
