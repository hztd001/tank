package com.cosmos.tank.net;

import com.cosmos.tank.TankFrame;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;


public class Client {
    // 设为单例模式
    private static final Client INSTANCE = new Client();
    private Client(){}
    public static Client getInstance(){return INSTANCE;}

    private Channel channel = null;

    public void connect(){
        EventLoopGroup group = new NioEventLoopGroup(1);

        Bootstrap b = new Bootstrap();
        try {
            ChannelFuture f = b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientChannelInitializer())
                    .connect("localhost", 9999);

            f.addListener(new ChannelFutureListener(){
                @Override
                public void operationComplete(ChannelFuture future) throws Exception{
                    if (!future.isSuccess()){
                        System.out.println("not connected");
                    } else {
                        System.out.println("connected");
                        // initialize the channel
                        channel = future.channel();
                    }
                }
            });
            f.sync();

            f.channel().closeFuture().sync();
            System.out.println("connection closed");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }
    }
    public void send(Msg msg){
//        ByteBuf buf = Unpooled.copiedBuffer(msg.getBytes());
        System.out.println(msg.toString());
        channel.writeAndFlush(msg);
    }

    public void closeConnect(){
//        this.send("_bye_");
    }
}

class ClientChannelInitializer extends ChannelInitializer<SocketChannel>{
    @Override
    protected void initChannel(SocketChannel ch) throws Exception{
        ch.pipeline() // 处理接收和发送的信息， encode decode
                .addLast(new MsgEncoder())
                .addLast(new MsgDecoder())
                .addLast(new ClientHandler());
    }
}
// SimpleChannelInboundHandler 可指定泛型
class ClientHandler extends SimpleChannelInboundHandler<Msg>{
    @Override
    public void channelRead0(ChannelHandlerContext ctx, Msg msg) throws Exception{
//        ByteBuf buf = null;
//        try {
//            buf = (ByteBuf) msg;
//            byte[] bytes = new byte[buf.readableBytes()];
//            buf.getBytes(buf.readerIndex(), buf);
//            String msgAccepted = new String(bytes);
////            ClientFrame.INSTANCE.updateText(msgAccepted);
//        }finally {
//            if (buf != null) ReferenceCountUtil.release(buf);
//        }
        msg.handler();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        ctx.writeAndFlush(new TankJoinMsg(TankFrame.INSTANCE.getMainTank()));
    }
}