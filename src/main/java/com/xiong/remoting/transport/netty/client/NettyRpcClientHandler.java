package com.xiong.remoting.transport.netty.client;

import com.xiong.remoting.constants.RpcConstants;
import com.xiong.enums.CompressTypeEnum;
import com.xiong.enums.SerializationTypeEnum;
import com.xiong.factory.SingletonFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import com.xiong.remoting.dto.RpcMessage;
import com.xiong.remoting.dto.RpcResponse;
import java.net.InetSocketAddress;

@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
    private final NettyRpcClient nettyRpcClient;
    private final UnprocessedRequests unprocessedRequest;

    public NettyRpcClientHandler(){
        this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
        this.unprocessedRequest = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try{
            log.info("client receive msg [{}]", msg);
            if(msg instanceof RpcMessage){
                RpcMessage rpcMessage = (RpcMessage) msg;
                byte messageType = rpcMessage.getMessageType();
                if(messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE){
                    log.info("heart [{}]", rpcMessage.getData());
                }else if(messageType == RpcConstants.RESPONSE_TYPE){
                    RpcResponse<Object> rpcResponse = (RpcResponse<Object>) rpcMessage.getData();
                    unprocessedRequest.complete(rpcResponse);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    @SneakyThrows
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt){
        // when a channel is idle
        if(evt instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent) evt).state();
            if(state == IdleState.WRITER_IDLE){
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMessage = RpcMessage.builder()
                        .data(RpcConstants.PING)
                        .codec(SerializationTypeEnum.KYRO.getCode())
                        .compress(CompressTypeEnum.GZIP.getCode())
                        .messageType(RpcConstants.HEARTBEAT_REQUEST_TYPE).build();
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }

    }


}
