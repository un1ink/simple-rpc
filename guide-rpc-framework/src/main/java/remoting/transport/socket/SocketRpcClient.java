package remoting.transport.socket;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import registry.ServiceDiscovery;
import remoting.dto.RpcRequest;
import remoting.transport.RpcRequestTransport;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

@Slf4j
@AllArgsConstructor
public class SocketRpcClient implements RpcRequestTransport {

    private final ServiceDiscovery serviceDiscovery;

    public SocketRpcClient(){
        this.serviceDiscovery = ExtensionLoader.getExtensionLoarder(ServiceDiscovery.class).getExtension)("zk");
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
