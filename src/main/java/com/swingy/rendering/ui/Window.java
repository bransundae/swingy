package com.swingy.rendering.ui;

import com.swingy.game.Swingy;
import com.swingy.input.KeyInput;
import com.swingy.input.MouseInput;
import com.swingy.states.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

public class Window extends Canvas {

    public static final String TITLE = "Swingy";
    public static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    public static final int WIDTH = screenSize.width / 2;
    public static final int HEIGHT = screenSize.height / 2;

    private JFrame frame;

    public void render(StateManager stateManager){
        BufferStrategy bufferStrategy = getBufferStrategy();
        if (bufferStrategy == null){
            createBufferStrategy(2);
            return;
        }

        Graphics graphics = bufferStrategy.getDrawGraphics();

        //MenuState
        stateManager.render(graphics);

        //Clean graphics and display from Buffer Strategy
        graphics.dispose();
        bufferStrategy.show();
    }

    public JFrame getFrame() {
        return frame;
    }

    public void setFrameVisibile(boolean visibile){
        frame.setVisible(visibile);
        frame.requestFocus();
    }

    public Window(Swingy swingy){
        //Frame
        frame = new JFrame(TITLE);
        frame.add(this);
        frame.setSize(WIDTH, HEIGHT);
        frame.setResizable(false);
        frame.setFocusable(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.err.println("Exiting Game");
                swingy.stop();
            }
        });
        frame.setLocationRelativeTo(null);
        frame.setVisible(swingy.getGui());
    }
}
