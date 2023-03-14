package remoting.transport.netty.client;

import enums.CompressTypeEnum;
import factory.SingletonFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import registry.ServiceDiscovery;
import remoting.constants.RpcConstants;
import remoting.dto.RpcMessage;
import remoting.dto.RpcRequest;
import remoting.dto.RpcResponse;
import remoting.transport.RpcRequestTransport;
import remoting.transport.codec.MessageDecode;
import remoting.transport.codec.RpcMessageEncode;
import java.net.InetSocketAddress;
import java.util.concurrent.*;
import enums.SerializationTypeEnum;

@Slf4j
public class NettyRpcClient implements RpcRequestTransport {

    private EventLoopGroup eventLoopGroup;
    private Bootstrap bootstrap;
    private final ServiceDiscovery serviceDiscovery;
    private final UnprocessedRequests unprocessedRequests;
    private final ChannelProvider channelProvider;


    public NettyRpcClient(){
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline cp = socketChannel.pipeline();
                        cp.addLast(new IdleStateHandler(0,5,0,TimeUnit.SECONDS));
                        cp.addLast(new RpcMessageEncode());
                        cp.addLast(new MessageDecode());
                        cp.addLast(new NettyRpcClientHandler());
                    }
                });
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension(ServiceDiscovery.ZK.getName());
    }

    /**
     * connect server and get the channel ,so that you can send rpc message to server
     *
     * @param inetSocketAddress server address
     * @return the channel
     */
    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress){
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future ->{
            if(future.isSuccess()){
                log.info("The client has connected [{}] successfully", inetSocketAddress.toString());
            }else{
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }


    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        Channel channel = getChannel(inetSocketAddress);

        if(channel.isActive()){
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            RpcMessage rpcMessage = RpcMessage.builder().data(rpcRequest)
                    .codec(SerializationTypeEnum.PROTOSTUFF.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType(RpcConstants.REQUEST_TYPE).build();
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future ->{
                if(future.isSuccess()){
                    log.info("client send message : [{}]", rpcMessage);
                }else{
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("Send failed :" , future.cause());
                }

            });
        } else {
            throw new IllegalStateException();
        }

        return resultFuture;

    }

    public Channel getChannel(InetSocketAddress inetSocketAddress){
        Channel channel = channelProvider.get(inetSocketAddress);
        if(channel == null){
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }

    public void close(){
        eventLoopGroup.shutdownGracefully();
    }
}
