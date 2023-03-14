package remoting.transport.netty.server;

import enums.CompressTypeEnum;
import enums.RpcResponseCodeEnum;
import enums.SerializationTypeEnum;
import factory.SingletonFactory;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import remoting.constants.RpcConstants;
import remoting.dto.RpcMessage;
import remoting.dto.RpcRequest;
import remoting.dto.RpcResponse;
import remoting.handler.RpcRequestHandler;

import java.util.IdentityHashMap;

@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {
    private final RpcRequestHandler rpcRequestHandler;

    public NettyRpcServerHandler(){
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if(msg instanceof RpcRequest){
                log.info("server receive msg : [{}]", msg);
                byte messageType =  ((RpcMessage) msg).getMessageType();
                RpcMessage rpcMessage = RpcMessage.builder()
                        .codec(SerializationTypeEnum.PROTOSTUFF.getCode())
                        .compress(CompressTypeEnum.GZIP.getCode()).build();
                if(messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE){
                    rpcMessage.setData(RpcConstants.PONG);
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                } else {
                    RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
                    Object result = rpcRequestHandler.handle(rpcRequest);
                    log.info(String.format("server get result %s"), result.toString());
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                    if(ctx.channel().isActive() && ctx.channel().isWritable()){
                        RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                        rpcMessage.setData(rpcResponse);
                    } else {
                        RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                        rpcMessage.setData(rpcResponse);
                        log.error("not writable now, message dropped");
                    }
                }
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if( evt instanceof IdleStateEvent){
            IdleState idleState = ((IdleStateEvent)evt).state();
            if(idleState == IdleState.WRITER_IDLE){
                log.info("idle check happen, close the connection.");
                ctx.close();
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
