package com.cosmos.tank;

import com.cosmos.tank.net.Client;
import com.cosmos.tank.net.TankStartMovingMsg;
import com.cosmos.tank.net.TankStopMsg;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;

public class TankFrame extends Frame {

    public static final TankFrame INSTANCE = new TankFrame();
    Random r = new Random();
    static final int GAME_WIDTH = 1080, GAME_HEIGHT = 960;
    // new Tank对象时，将自己传入Tank，从而让Tank可以引用TankFrame
    private Tank myTank = new Tank(r.nextInt(GAME_WIDTH), r.nextInt(GAME_HEIGHT), Dir.DOWN, Group.GOOD, this);
//    private Tank myTank = new Tank(500, 400, Dir.DOWN, Group.GOOD, this);
    // 定义队列存储子弹
    List<Bullet> bullets = new ArrayList<>();
    // 定义队列存储敌方坦克
    // Map 根据根据key:UUID直接获取对应tank,效率较高(list获取得全部遍历；map 直接对key取hash,所以速度快)
    Map<UUID,Tank> tanks = new HashMap<>();
    // 定义存储爆炸队列
    List<EXplode> explodes = new ArrayList<>();

    EXplode eXplode = new EXplode(100, 100, this);
    /*
    继承Frame类，定义构造方法
     */
    private TankFrame() {

        // setVisible(true)设置为可见
//        setVisible(true);
        // 设置窗口大小
        setSize(GAME_WIDTH, GAME_HEIGHT);
        //设置窗口为不可变
        setResizable(false);
        setTitle("tank war");

        // 调用自定义key监听内部类
        this.addKeyListener(new MyKeyListener());

        // 添加window监听事件，点击窗口退出按钮，执行退出; new WindowAdapter() 匿名内部类
        /*
        addWindowListener 要求我们传入WindowListener;此处我们,new WindowAdapter是实现了
        windowListener的接口（可以认为是windowListener的子类);重写windowClosing方法，实现系统退出功能
         */
        addWindowListener(new WindowAdapter() {
            /**
             * Invoked when a window is in the process of being closed.
             * The close operation can be overridden at this point.
             *
             */
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

    }

    public void addTank(Tank t){
        for (int i=0; i<tanks.size(); i++){
            if (t.getId().equals(tanks.get(i).getId())){
                return;
            }
        }
        tanks.put(t.getId(), t);
    }

    public Tank findByUUID(UUID id){

        return tanks.get(id);
    }

    public Tank getMainTank(){
        return this.myTank;
    }
    // 处理双缓冲（内存-> 显存）；解决闪烁
    Image offScreenImage = null;
    @Override
    public void update(Graphics g) {
        if(offScreenImage == null) {
            offScreenImage = this.createImage(GAME_WIDTH, GAME_HEIGHT);
        }
        Graphics gOffScreen = offScreenImage.getGraphics();
        Color c = gOffScreen.getColor();
        gOffScreen.setColor(Color.BLACK);
        gOffScreen.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        gOffScreen.setColor(c);
        paint(gOffScreen);
        g.drawImage(offScreenImage, 0, 0, null);
    }

    // 重写paint方法；paint方法是窗口绘制时系统自动调用(每次绘制都会调用)
    @Override
    public void paint(Graphics g) {
        myTank.paint(g);
        Color color = g.getColor();
        g.setColor(color.WHITE);
        g.drawString("bullets:" + bullets.size(),10, 60);
        g.drawString("tanks:" + tanks.size(),10, 80);
        g.drawString("explodes:" + explodes.size(),10, 100);
        g.setColor(color);
        // 遍历子弹夹，绘制子弹

        for (int i=0; i<bullets.size(); i++){
            bullets.get(i).paint(g);
        }

        // 画出敌方坦克
        tanks.values().stream().forEach((e) -> e.paint(g));

        // 遍历每颗子弹和坦克，进行碰撞检测
        for (int i = 0; i < bullets.size(); i++) {
            for (int j = 0; j < tanks.size(); j++) {
                bullets.get(i).collideWith(tanks.get(j));
            }
        }

        // 画出爆炸画面
        for (int i = 0; i < explodes.size(); i++) {
            explodes.get(i).paint(g);
        }

    }

    //定义内部类继承KeyAdapter（处理键盘事件类）
     class MyKeyListener extends KeyAdapter{

        boolean bL = false;
        boolean bR = false;
        boolean bU = false;
        boolean bD = false;
        @Override
        // 监听key被按下时自动调 用
        public void keyPressed(KeyEvent e) {

            // 判断key状态
            int keyCode = e.getKeyCode();
            switch (keyCode){
                case KeyEvent.VK_LEFT:
                    bL = true;
                    break;
                case KeyEvent.VK_RIGHT:
                    bR = true;
                    break;
                case KeyEvent.VK_UP:
                    bU = true;
                    break;
                case KeyEvent.VK_DOWN:
                    bD = true;
                    break;
                default: // 默认不匹配break
                    break;
            }
            setMainTankDir();
        }

        private void setMainTankDir() {
            if (!bU && !bL && !bR && !bD) {
                myTank.setMove(false);
                Client.getInstance().send(new TankStopMsg(getMainTank()));
            }
            else {
                if (bL) myTank.setDir(Dir.LEFT);
                if (bU) myTank.setDir(Dir.UP);
                if (bR) myTank.setDir(Dir.RIGHT);
                if (bD) myTank.setDir(Dir.DOWN);
                

                if (! myTank.isMove())
                    // 发出坦克移动消息
                    Client.getInstance().send(new TankStartMovingMsg(getMainTank()));
                // 更改坦克方向后，将move设为true
                myTank.setMove(true);
            }
        }

        // 监听key弹起时自动调用
        @Override
        public void keyReleased(KeyEvent e) {

            int keyCode = e.getKeyCode();
            switch (keyCode){
                case KeyEvent.VK_LEFT:
                    bL = false;
                    break;
                case KeyEvent.VK_RIGHT:
                    bR = false;
                    break;
                case KeyEvent.VK_UP:
                    bU = false;
                    break;
                case KeyEvent.VK_DOWN:
                    bD = false;
                    break;
                case KeyEvent.VK_CONTROL:
                    myTank.fire();
                    break;
                default:
                    break;
            }
            setMainTankDir();

            new Thread(() -> new Audio("audio/tank_move.wav").play()).start();
        }
    }
}
