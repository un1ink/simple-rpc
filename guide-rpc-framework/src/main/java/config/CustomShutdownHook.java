package config;

import lombok.extern.slf4j.Slf4j;
import remoting.transport.netty.client.NettyRpcClient;
import remoting.transport.netty.server.NettyRpcServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class CustomShutdownHook {
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook(){
        return CUSTOM_SHUTDOWN_HOOK;
    }

    public void clearAll(){
        log.info("addShutdownHook for clearAll");
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), NettyRpcServer.PORT);
            }catch (UnknownHostException e){

            }
        }));
    }

}
