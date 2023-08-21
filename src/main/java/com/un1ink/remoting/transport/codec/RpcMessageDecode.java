package com.un1ink.remoting.transport.codec;

import com.un1ink.enums.CompressTypeEnum;
import com.un1ink.enums.SerializationTypeEnum;
import com.un1ink.serialize.Serializer;
import com.un1ink.remoting.constants.RpcConstants;
import com.un1ink.compress.Compress;
import com.un1ink.extension.ExtensionLoader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import com.un1ink.remoting.dto.RpcMessage;
import com.un1ink.remoting.dto.RpcRequest;
import com.un1ink.remoting.dto.RpcResponse;

import java.util.Arrays;


@Slf4j
public class RpcMessageDecode extends LengthFieldBasedFrameDecoder {
    public RpcMessageDecode(){
        this(RpcConstants.MAX_FRAME_LENGTH, 5,4, -9, 0);
    }

    public RpcMessageDecode(int maxFrameLength, int i, int i1, int i2, int i3) {
        super(maxFrameLength, i, i1, i2, i3);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if(decoded instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf) decoded;
            if(byteBuf.readableBytes() >= RpcConstants.TOTAL_LENGTH) {
                try{
                    return decodeByteBuf(byteBuf);
                } catch (Exception e) {
                    log.error("Decode bytebuf error!", e);
                    throw e;
                } finally {
                    byteBuf.release();
                }
            }
        }
        return decoded;
    }

    private void checkMagicNumber(ByteBuf in) {
        // read the first 4 bit, which is the magic number, and compare
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        in.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(tmp));
            }
        }
    }

    private void checkVersion(ByteBuf byteBuf) {
        byte version = byteBuf.readByte();
        if(version != RpcConstants.VERSION) {
            throw new RuntimeException("version isn't compatible" + version);
        }
    }

    private Object decodeByteBuf(ByteBuf byteBuf) {
        checkMagicNumber(byteBuf);
        checkVersion(byteBuf);
        int fullLength = byteBuf.readInt();
        byte messageType = byteBuf.readByte();
        byte codecType = byteBuf.readByte();
        byte compressType = byteBuf.readByte();
        int requestId = byteBuf.readInt();
        RpcMessage rpcMessage = RpcMessage.builder()
                .codec(codecType)
                .messageType(messageType)
                .compress(compressType)
                .requestId(requestId).build();
        if(messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE){
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        }
        if(messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE){
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        }

        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
        if(bodyLength > 0){
            byte[] bs = new byte[bodyLength];
            byteBuf.readBytes(bs);
            String compressName = CompressTypeEnum.getName(compressType);
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(compressName);
            bs = compress.decompress(bs);

            String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
            log.info("codec name : [{}]", codecName);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(codecName);
            if(messageType == RpcConstants.REQUEST_TYPE){
                RpcRequest rpcRequest = serializer.deserialize(bs, RpcRequest.class);
                rpcMessage.setData(rpcRequest);
            } else if (messageType == RpcConstants.RESPONSE_TYPE){
                RpcResponse rpcResponse = serializer.deserialize(bs, RpcResponse.class);
                rpcMessage.setData(rpcResponse);
            }

        }
        return rpcMessage;
    }



}