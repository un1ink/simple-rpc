package remoting.transport.codec;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import remoting.constants.RpcConstants;

import java.util.concurrent.ExecutionException;

@Slf4j
public class RpcMessageDecode extends LengthFieldBasedFrameDecoder {
    public RpcMessageDecode(){
        this(RpcConstants.MAX_FRAME_LENGTH, 5,4, -9, 0);
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
    }

    private Object decodeByteBuf(ByteBuf byteBuf) {
        checkMagicNumber(byteBuf);
        checkVersion(byteBuf);
    }

    private void checkVersion(ByteBuf byteBuf) {
        byte version = byteBuf.readByte();
        if(version != RpcConstants.VERSION) {
            throw new RuntimeException("version isn't compatible" + version);
        }
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

    public RpcMessageDecode(int maxFrameLength, int i, int i1, int i2, int i3) {
        super();
    }
}