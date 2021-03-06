package com.johnwesthoff.bending.entity;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.johnwesthoff.bending.Client;
import com.johnwesthoff.bending.Constants;
import com.johnwesthoff.bending.Server;
import com.johnwesthoff.bending.logic.Player;
import com.johnwesthoff.bending.logic.PlayerOnline;
import com.johnwesthoff.bending.logic.World;
import com.johnwesthoff.bending.networking.handlers.AiEvent;
import com.johnwesthoff.bending.networking.handlers.DestroyEvent;
import com.johnwesthoff.bending.networking.handlers.ScoreEvent;
import com.johnwesthoff.bending.util.network.ResourceLoader;

/**
 * @author John
 */
public class EnemyEntity extends Entity {
    public int HP = 500, target = 0, timer = 0, id = 0, master = 0, lastHit = -2;
    public float move = 0;
    float drawX = 0, drawY = 0;
    String name;
    static BufferedImage sprite = ResourceLoader.loadImage("evil.png");
    int air = 100;

    public EnemyEntity(int x, int y, int hspeed, int vspeed, int ma) {
        drawX = X = x;
        drawY = Y = y;
        xspeed = hspeed;
        yspeed = vspeed;
        move = hspeed;
        HP = 500;
        master = ma;
    }

    @Override
    public void drawOverlay(Graphics G, int viewX, int viewY) {

        if (X > viewX && X < viewX + Constants.WIDTH_INT && Y > viewY && Y < viewY + Constants.HEIGHT_INT) {
            // G.setColor(Color.RED);
            // G.fillArc(((int)X-30)-viewX, ((int)Y-30)-viewY, 60, 60, 0, 360);
            // G.setColor(Color.black);
            // G.drawArc(((int)X-30)-viewX, ((int)Y-30)-viewY, 60, 60, 0, (360*HP)/500);
            G.drawImage(sprite, (int) ((drawX - viewX) * 3f) - 32, (int) ((drawY - viewY) * 3f) - 32, null);
            G.setColor(Color.DARK_GRAY);
            G.drawString(name, (int) ((X - (name.length()) + 3) - viewX) * Constants.MULTIPLIER,
                    (int) (Y - viewY - 24) * Constants.MULTIPLIER);
        }
        // System.out.println("HI!");
    }

    boolean jump = false;
    float jumpMove = 0;
    boolean inAir = false;

    @Override
    public void onUpdate(World apples) {
        int min = 9999;
        if (name == null) {
            name = apples.getPlayerName(master);
        }
        double dis;
        if (!apples.serverWorld) {
            for (Player p : apples.playerList) {
                dis = pointDis(X, Y, p.x, p.y);
                if (dis < min && p.ID != master) {
                    min = (int) dis;
                    target = p.ID;
                    if (p.x > X) {
                        move = 2;
                    } else {
                        move = -2;
                    }
                    if (p.y < Y - 64) {
                        jump = true;
                    } else {
                        jump = false;
                    }
                }
            }
            if (min > 300) {
                move = 0;
            }
            dis = pointDis(X, Y, apples.x, apples.y);
            if (dis < min && apples.ID != master) {
                target = -1;
                if (apples.x > X) {
                    move = 2;
                } else {
                    move = -2;
                }
                if (dis > 300) {
                    move = 0;
                }
                if (apples.y < Y - 64) {
                    jump = true;
                } else {
                    jump = false;
                }
            }
            if (move == 0) {
                for (Player p : apples.playerList) {
                    dis = pointDis(X, Y, p.x, p.y);
                    if (dis < min && p.ID == master) {
                        min = (int) dis;
                        target = p.ID;
                        if (p.x > X) {
                            move = 1;
                        } else {
                            move = -1;
                        }
                        if (p.y < Y - 128) {
                            jump = true;
                        } else {
                            jump = false;
                        }
                    }
                }
                dis = pointDis(X, Y, apples.x, apples.y);
                if (dis < min && apples.ID == master) {
                    target = -1;
                    if (apples.x > X) {
                        move = 1;
                    } else {
                        move = -1;
                    }
                    if (apples.y < Y - 128) {
                        jump = true;
                    } else {
                        jump = false;
                    }
                }
            }
        }
        if (((move != 0 && apples.isSolid(X + (move * 8), Y - 4)) || jump) && apples.isSolid(X, Y + 3)) {
            jumpMove = move * 2;
            yspeed = -6;
            inAir = true;
        }
        if (inAir) {
            move = jumpMove;
            if (apples.isSolid(X, Y + 3)) {
                inAir = false;
            }
        }
        if (!apples.isSolid(X, Y - 40)) {
            Y -= 40;
            if (apples.inBounds(X + move, Y + yspeed)) {
                float toMove = move, XXX1 = X + 3, YYY1 = Y - 4, XXX2 = X - 3, YYY2 = Y - 4;
                while (true) {
                    YYY1 += 1;
                    if (!apples.inBounds(XXX1, YYY1)) {
                        break;
                    }
                    if (apples.isSolid(XXX1, YYY1)) {
                        break;
                    }
                }
                while (true) {
                    YYY2 += 1;
                    if (!apples.inBounds(XXX2, YYY2)) {
                        break;
                    }
                    if (apples.isSolid(XXX2, YYY2)) {
                        break;
                    }
                }
                X += !apples.isSolid(X + toMove, Y + yspeed) ? move : 0;
            }
            for (int i = 0; i < 40; i++) {
                if (Y > 0 && apples.isSolid(X, Y + 1)) {
                    break;
                }
                Y += 1;
            }
        }
        if (!apples.isSolid(X, Y + 4)) {
            yspeed = Math.min(4, yspeed + 1);
        } else {
            yspeed = Math.min(0, yspeed);
        }
        if (!apples.isSolid(X, Y + yspeed)) {
            Y += yspeed;
        } else {
            yspeed = 0;
        }
        drawX += (X - drawX) / 2;
        drawY += (Y - drawY) / 2;
    }

    @Override
    public void onServerUpdate(Server handle) {
        jump = false;
        if (HP <= 0) {
            handle.sendMessage(DestroyEvent.getPacket(this));
            this.setAlive(false);
            if (Server.gameMode == Server.SURVIVAL) {
                PlayerOnline P = handle.getPlayer(lastHit);
                if (P != null) {
                    P.score++;
                    handle.sendMessage(ScoreEvent.getPacket(P));
                }
            }
        }
        int min = 9999;
        for (PlayerOnline p : handle.playerList) {
            double dis = pointDis(X, Y, p.x, p.y);
            if (dis < min && p.ID != master) {
                min = (int) dis;
                target = p.ID;
                if (p.x > X) {
                    move = 2;
                } else {
                    move = -2;
                }

                if (p.y < Y - 64) {
                    jump = true;
                } else {
                    jump = false;
                }
            }
        }
        if (min > 300) {
            move = 0;
            for (PlayerOnline p : handle.playerList) {
                double dis = pointDis(X, Y, p.x, p.y);
                if (dis < min && p.ID == master) {
                    min = (int) dis;
                    target = p.ID;
                    if (p.x > X) {
                        move = 1;
                    } else {
                        move = -1;
                    }

                    if (p.y < Y - 128) {
                        jump = true;
                    } else {
                        jump = false;
                    }
                }
            }
        }
        if (!handle.earth.isType((int) X, (int) Y, Constants.AIR)) {
            if (air-- < 0) {
                HP -= 2;
            }
        } else {
            air = 100;
        }
        if (handle.earth.isType((int) X, (int) Y, Constants.LAVA)) {
            HP -= 2;
        }
        if (((move != 0 && handle.earth.isSolid(X + (move * 8), Y - 4)) || jump) && handle.earth.isSolid(X, Y + 3)) {
            jumpMove = move * 3 / 2;
            yspeed = -6;
            inAir = true;
        }
        if (timer++ > 90) {
            // System.out.println(X);
            handle.sendMessage(AiEvent.getPacket(this));
            timer = 0;
        }
    }

    @Override
    public void checkAndHandleCollision(Client client) {

        if (client.checkCollision(X, Y) && master != client.ID
                && (client.gameMode <= 0 || !client.myTeam.contains(master))) {
            client.hurt(7);
            client.world.vspeed -= 4;
            client.xspeed += 4 - client.random.nextInt(8);
            client.lastHit = master;
            client.killMessage = "~ was defeated by `'s dark minion.";
        }
    }

    @Override
    public void cerealize(ByteBuffer out) {
        try {
            Server.putString(out, this.getClass().getName());
            out.putInt((int) X);
            out.putInt((int) Y);
            out.putInt((int) move);
            out.putInt((int) yspeed);
            out.putInt(HP);
            out.putInt(target);
            out.putInt(id);
        } catch (Exception ex) {
            Logger.getLogger(ExplosionEntity.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public EnemyEntity addStuff(int i, int it) {
        target = i;
        id = it;
        return this;
    }

    /**
     * Method to reconstruct an enemy in a given world
     * 
     * @param in
     * @param world World in which the enemy should be reconstructed
     */
    public static void reconstruct(ByteBuffer in, World world) {
        // System.out.println("IM BACK!");
        world.entityList.add(new EnemyEntity(in.getInt(), in.getInt(), in.getInt(), in.getInt(), in.getInt())
                .addStuff(in.getInt(), in.getInt()));

    }

    @Override
    public void onDraw(Graphics G, int viewX, int viewY) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change
        // body of generated methods, choose Tools | Templates.
    }

}
