package remoting.transport.netty.server;

import config.CustomShutdownHook;
import config.RpcServiceConfig;
import factory.SingletonFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import remoting.transport.codec.MessageDecode;
import remoting.transport.codec.RpcMessageEncode;
import sun.rmi.runtime.RuntimeUtil;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class NettyRpcServer {

    public static final int PORT = 9988;
    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(ZkServiceProvierImpl.class);

    public void registerService(RpcServiceConfig rpcServiceConfig){
        serviceProvider.publishService(rpcServiceConfig);
    }

    @SneakyThrows
    public void start(){
        CustomShutdownHook.getCustomShutdownHook().clearAll();
        String host = InetAddress.getLocalHost().getHostAddress();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
                RuntimeUtil.cpus() * 2,
                ThreadPoolFactoryUtil.createThreadFactory("serivce-handler-group", false)
        );

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline cp = socketChannel.pipeline();
                            cp.addLast(new IdleStateHandler(60,0,0, TimeUnit.SECONDS));
                            cp.addLast(new RpcMessageEncode());
                            cp.addLast(new MessageDecode());
                            cp.addLast(serviceHandlerGroup, new NettyRpcServerHandler());
                        }
                    });
            ChannelFuture f = b.bind(host, PORT).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e){
            log.info("occur exception when start server:", e);
        } finally {
            log.error("shutdown bossGroup and workerGroup");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();

        }
    }
}
