package com.cosmos.tank;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Tank{
    private int x, y;
    private Dir dir = Dir.DOWN;
    private Group group = Group.BAD;
    private final static int SPEED = 2;
    private boolean move = true;
    private TankFrame tf = null;
    public static int WIDTH = ResourceMgr.tankU.getWidth();
    public static int HEIGHT = ResourceMgr.tankU.getHeight();

    private boolean living = true;
    private Random random = new Random();

    Tank(int x, int y, Dir dir, TankFrame tf, Group group){
        super();
        this.x = x;
        this.y = y;
        this.dir = dir;
        this.tf = tf;
        this.group = group;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public void setDir(Dir dir) {
        this.dir = dir;
    }

    public Dir getDir() {
        return dir;
    }

    public void setMove(boolean move) {
        this.move = move;
    }

    // 子弹发射方法(调用fire;new一颗子弹add到子弹容器)
    public void fire() {
        // 计算子弹在坦克中心位置发出
        int bX = this.x + Tank.WIDTH / 2 - Bullet.WIDTH / 2;
        int bY = this.y + Tank.HEIGHT / 2 - Bullet.HEIGHT / 2;
        // 将TankFrame 传给Bullet; Bullet可以继续引用TankFrame
        // 定义弹夹中子弹属性；
        tf.bullets.add(new Bullet(bX, bY, this.dir, this.tf, this.group));

        // 定义爆炸声音
        if (this.group == group.GOOD) new Thread(() -> new Audio("audio/tank_fire.wav").play()).start();

    }

    public boolean isMove(){
        return move;
    }

    // 重写paint方法；paint方法是窗口绘制时系统自动调用(每次绘制都会调用)
    public void paint(Graphics g) {

        // living为false, 删除坦克
        if (!living) tf.tanks.remove(this);
        // 读取tank图片
        switch (dir){
            case LEFT:
                g.drawImage(ResourceMgr.tankL, x, y, null);
                break;
            case RIGHT:
                g.drawImage(ResourceMgr.tankR, x, y, null);
                break;
            case UP:
                g.drawImage(ResourceMgr.tankU, x, y, null);
                break;
            case DOWN:
                g.drawImage(ResourceMgr.tankD, x, y, null);
                break;
        }
        moving();
    }

    private void moving() {
        // move为false,不进行移动
        if (!move) return;

        switch (dir) {
            case UP:
                y -= SPEED;
                break;
            case DOWN:
                y += SPEED;
                break;
            case LEFT:
                x -= SPEED;
                break;
            case RIGHT:
                x += SPEED;
                break;
        }
        // 敌方坦克移动时随机发射子弹
        if (random.nextInt(10) > 8) this.fire();
    }

    public void die(){
        living = false;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
