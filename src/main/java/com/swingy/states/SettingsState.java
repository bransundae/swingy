package com.swingy.states;

import com.swingy.Main;
import com.swingy.input.KeyInput;
import com.swingy.input.MouseInput;
import com.swingy.game.entities.Entity;
import com.swingy.rendering.textures.Texture;
import com.swingy.rendering.ui.Button;
import com.swingy.util.Fonts;
import com.swingy.rendering.ui.Window;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

import static com.swingy.console.Console.console;
import static com.swingy.database.SwingyDB.swingyDB;
import static com.swingy.states.MenuState.swingy;

public class SettingsState implements State {

    private StateManager stateManager;

    private Button[] options;
    private  int currentButtonSelection;

    private int buttonBaseHeight = Window.HEIGHT / 100 * 20;
    private int buttonIncrement = Window.HEIGHT / 100 * 10;
    private int fontSize = Window.HEIGHT / 100 * 5;
    private int fontBold = Window.HEIGHT / 100 * 6;
    private int fontTitle = Window.HEIGHT / 100 * 10;

    private int cooldown;

    @Override
    public void init() {
        currentButtonSelection = 0;
        options = new Button[4];
        options[0] = new Button("Increase Music Volume", (buttonBaseHeight + 0 * buttonIncrement),
                new Font("Arial", Font.PLAIN, fontSize),
                new Font("Arial", Font.BOLD, fontBold),
                Color.WHITE,
                Color.YELLOW);
        options[1] = new Button("Decrease Music Volume", (buttonBaseHeight + 1 * buttonIncrement),
                new Font("Arial", Font.PLAIN, fontSize),
                new Font("Arial", Font.BOLD, fontBold),
                Color.WHITE,
                Color.YELLOW);
        options[2] = new Button("Reset Database", (buttonBaseHeight + 2 * buttonIncrement),
                new Font("Arial", Font.PLAIN, fontSize),
                new Font("Arial", Font.BOLD, fontBold),
                Color.WHITE,
                Color.YELLOW);
        options[3] = new Button("Back", (buttonBaseHeight + 3 * buttonIncrement),
                new Font("Arial", Font.PLAIN, fontSize),
                new Font("Arial", Font.BOLD, fontBold),
                Color.WHITE,
                Color.YELLOW);

        //Output console options and wait for userSelection
        console.userSelection(this);
    }

    @Override
    public State enterState(StateManager stateManager, State callingState) {
        this.stateManager = stateManager;
        cooldown = 50;
        init();
        stateManager.setTick(true);
        return this;
    }

    @Override
    public void exitState() {
        this.stateManager.setTick(false);
        options = null;
    }

    @Override
    public String getName() {
        return "settings";
    }

    @Override
    public void tick(StateManager stateManager) {

        String userInput = null;
        try {
            userInput = console.tick();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        cooldown--;

        if (cooldown <= 0) {

            if (userInput != null){
                cooldown = 50;
                if (userInput.equalsIgnoreCase("gui")) {
                    swingy.setGui(true);
                    stateManager.setTick(false);
                    stateManager.setState("settings", this);
                }
                else{
                    try {
                        int userOption = Integer.parseInt(userInput);
                        if (userOption > 0 && userOption < 5) {
                            currentButtonSelection = Integer.parseInt(userInput) - 1;
                            select(stateManager);
                        }
                        else
                            System.out.println("INVALID INPUT...");
                    }catch (NumberFormatException e){
                        System.out.println("INVALID INPUT...");
                    }
                }
            }

            if (KeyInput.wasPressed(KeyEvent.VK_UP) || KeyInput.wasPressed(KeyEvent.VK_W)) {
                cooldown = 50;
                currentButtonSelection--;
                if (currentButtonSelection < 0) {
                    currentButtonSelection = options.length - 1;
                }
            }

            if (KeyInput.wasPressed(KeyEvent.VK_DOWN) || KeyInput.wasPressed(KeyEvent.VK_S)) {
                cooldown = 50;
                currentButtonSelection++;
                if (currentButtonSelection > options.length - 1) {
                    currentButtonSelection = 0;
                }
            }
        }

        boolean clicked = false;

        if (options != null) {
            for (int i = 0; i < options.length; i++) {
                if (options[i].intersects(new Rectangle(MouseInput.getX(), MouseInput.getY(), 1, 1))) {
                    currentButtonSelection = i;
                    clicked = MouseInput.wasPressed(MouseEvent.BUTTON1);
                }
            }
        }

        if (clicked || KeyInput.wasPressed(KeyEvent.VK_ENTER))
            select(stateManager);

    }

    private void select(StateManager stateManager) {

        switch (currentButtonSelection){
            case 0:
                Main.musicPlayer.increaseVolume();
                break;
            case 1:
                Main.musicPlayer.decreaseVolume();
                break;
            case 2:
                try {
                    swingyDB.deleteAll();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                this.stateManager.setTick(false);
                stateManager.setState("menu", this);
                break;
            case 3:
                this.stateManager.setTick(false);
                stateManager.setState("menu", this);
                break;
        }
    }

    @Override
    public void render(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, Window.WIDTH, Window.HEIGHT);

        Texture background = new Texture("background/2", Window.WIDTH, Window.HEIGHT, false);
        background.render(graphics, 0, 0);

        Fonts.drawString(graphics, new Font("Arial", Font.BOLD, fontTitle), Color.GREEN, "Settings", fontTitle, false);

        if (options != null){
            for (int i = 0; i < options.length; i++) {
                if (i == currentButtonSelection)
                    options[i].setSelected(true);
                else
                    options[i].setSelected(false);
                options[i].render(graphics);
            }
        }
    }

    public void addEntity(Entity entity){
    }

}
