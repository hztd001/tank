package com.cosmos.tank.net;

import com.cosmos.tank.Tank;
import com.cosmos.tank.TankFrame;

import java.io.*;
import java.util.UUID;

public class TankStopMsg extends Msg {

    UUID id;
    int x, y;

    public TankStopMsg(UUID id, int x, int y){
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public TankStopMsg(Tank t){
        this.id = t.getId();
        this.x = t.getX();
        this.y = t.getY();
    }

    public TankStopMsg(){}

    @Override
    public byte[] toBytes(){
        ByteArrayOutputStream baos = null;
        DataOutputStream dos = null;
        byte[] bytes = null;
        try {
            baos = new ByteArrayOutputStream();
            dos = new DataOutputStream(baos);
//            dos.writeInt(UUID.oridnal());
            dos.writeLong(id.getMostSignificantBits());
            dos.writeLong(id.getLeastSignificantBits());
            dos.writeInt(x);
            dos.writeInt(y);
            dos.flush();
            bytes = baos.toByteArray();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                if (baos != null){
                    baos.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            try {
                if (dos != null){
                    dos.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return bytes;
    }

    @Override
    public void parse(byte[] bytes){

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
        try {
            // TODO: 先读TYPE信息， 根据TYPE信息处理不同的消息
//            dis.readInt();

            this.id = new UUID(dis.readLong(), dis.readLong());
            this.x = dis.readInt();
            this.y = dis.readInt();

        }catch (IOException e){
            e.printStackTrace();
        }finally{
            try{
                dis.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void handler(){
        if (this.id.equals(TankFrame.INSTANCE.getMainTank().getId())) return;

        Tank t = TankFrame.INSTANCE.findByUUID(this.id);

        if (t != null){
            t.setMove(false);
            t.setX(this.x);
            t.setY(this.y);

        }

    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getName())
                .append("[")
                .append("uuid=" + id + " |")
                .append("x=" + y + " |")
                .append("y=" + y + " |")
                .append("]");
        return builder.toString();
    }

    @Override
    public MsgType getMsgType(){
        return MsgType.TankStop;
    }
}
