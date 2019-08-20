package com.swingy.states;

import com.swingy.battle.FighterMetrics;
import com.swingy.id.ID;
import com.swingy.input.KeyInput;
import com.swingy.input.MouseInput;
import com.swingy.rendering.entities.Entity;
import com.swingy.rendering.entities.Fighter;
import com.swingy.rendering.textures.Sprite;
import com.swingy.rendering.textures.SpriteSheet;
import com.swingy.rendering.textures.Texture;
import com.swingy.rendering.ui.Button;
import com.swingy.util.AnimationHelper;
import com.swingy.util.Fonts;
import com.swingy.view.Swingy;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static com.swingy.database.SwingyDB.swingyDB;

public class CharacterSelectionState implements State {
    private ArrayList<Entity> entities;

    private Button[] options;
    private  int currentButtonSelection;

    private Fighter[] characters;
    private int currentCharacterSelection;

    protected Fighter currentFighter;
    ResultSet resultSet = null;

    private int numSaves;

    @Override
    public void init() {
        currentButtonSelection = 0;
        entities = new ArrayList<>();
        currentCharacterSelection = 0;
        options = new Button[4];
        options[0] = new Button("Next", 50, (200 + 0 * 80),
                new Font("Arial", Font.PLAIN, 32),
                new Font("Arial", Font.BOLD, 48),
                Color.WHITE,
                Color.YELLOW);
        options[1] = new Button("Delete", 50, (200 + 1 * 80),
                new Font("Arial", Font.PLAIN, 32),
                new Font("Arial", Font.BOLD, 48),
                Color.WHITE,
                Color.YELLOW);
        options[2] = new Button("Play", 50, (200 + 4 * 80),
                new Font("Arial", Font.PLAIN, 32),
                new Font("Arial", Font.BOLD, 48),
                Color.WHITE,
                Color.YELLOW);
        options[3] = new Button("Back", 50, (200 + 5 * 80),
                new Font("Arial", Font.PLAIN, 32),
                new Font("Arial", Font.BOLD, 48),
                Color.WHITE,
                Color.YELLOW);

        characters = new Fighter[swingyDB.getRowCount()];

        //Initialise Database Saves as Fighters
        int count = 0;
        while (true){
            try {
                if (!resultSet.next()) break;
                switch(resultSet.getString(4)){
                    case "ninja":
                        characters[count] = new Fighter(new Sprite("ninja/idle/1"),
                                (Swingy.WIDTH / 2), 100,
                                new FighterMetrics(resultSet.getString(2), "FIRE"),
                                this, AnimationHelper.createAnimation("ninjaLarge"));
                        characters[count].setPlayerClass(ID.NINJA);
                        break;
                    case "dino":
                        characters[count] = new Fighter(new Sprite("dino/idle/1"),
                                (Swingy.WIDTH / 2), 100,
                                new FighterMetrics(resultSet.getString(2), "EARTH"),
                                this, AnimationHelper.createAnimation("dinoLarge"));
                        characters[count].setPlayerClass(ID.DINO);
                        break;
                    case "robo":
                        characters[count] = new Fighter(new Sprite("robo/idle/1"),
                                (Swingy.WIDTH / 2), 50, new FighterMetrics(resultSet.getString(2), "EARTH"),
                                this, AnimationHelper.createAnimation("roboLarge"));
                        characters[count].setPlayerClass(ID.ROBO);
                        break;
                    case "zombo":
                        characters[count] = new Fighter(new Sprite("zombo/idle/1"),
                                (Swingy.WIDTH / 2), 50, new FighterMetrics(resultSet.getString(2), "WATER"),
                                this, AnimationHelper.createAnimation("zomboLarge"));
                        characters[count].setPlayerClass(ID.ZOMBO);
                        break;
                }
                characters[count].getFighterMetrics().setID((resultSet.getInt(1)));
                characters[count].setPlayerClassName(resultSet.getString(4));
                characters[count].getFighterMetrics().getLevel().setExperience(resultSet.getInt(3));
            } catch (SQLException e) {
                e.printStackTrace();
            }
            count++;
        }
    }

    @Override
    public State enterState(State callingState) {
        try {
            resultSet = swingyDB.queryAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if ((numSaves = swingyDB.getRowCount()) > 0)
            init();
        else {
            entities = null;
            characters = null;
            options = null;
        }

        return this;
    }

    @Override
    public void exitState() {
        /*if (characters != null) {
            for (int i = 0; i < characters.length; i++) {
                characters[i] = null;
            }
        }*/
        if (entities != null)
            entities.clear();
        entities = null;
    }

    @Override
    public String getName() {
        return "character-load";
    }

    @Override
    public void tick(StateManager stateManager) {

        if(numSaves <= 0)
            stateManager.setState("menu", this);

        if (KeyInput.wasPressed(KeyEvent.VK_UP) || KeyInput.wasPressed(KeyEvent.VK_W)){
            currentButtonSelection--;
            if (currentButtonSelection < 0){
                currentButtonSelection = options.length - 1;
            }
        }

        if (KeyInput.wasPressed(KeyEvent.VK_DOWN) || KeyInput.wasPressed(KeyEvent.VK_S)){
            currentButtonSelection++;
            if (currentButtonSelection > options.length - 1){
                currentButtonSelection = 0;
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

        if ((clicked || KeyInput.wasPressed(KeyEvent.VK_ENTER)) && characters != null)
            select(stateManager);

        //Tick only selected character
        if (entities != null)
            if (entities.size() > 0)
                entities.get(currentCharacterSelection).tick();
    }

    private void select(StateManager stateManager) {
        switch (currentButtonSelection){
            case 0 :
                currentCharacterSelection++;
                if (currentCharacterSelection >= characters.length)
                    currentCharacterSelection = 0;
                break ;

            case 1 :
                currentFighter = characters[currentCharacterSelection];
                try {
                    swingyDB.deletePlayer(currentFighter.getFighterMetrics().getID());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                stateManager.setState("character-load", this);
                break ;
            case 2 :
                currentFighter = characters[currentCharacterSelection];
                try {
                    swingyDB.setCurrentPlayer(currentFighter.getFighterMetrics().getID());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                GameState gameState = (GameState) stateManager.setState("map", this);
                break ;
            case 3 :
                stateManager.setState("menu", this);
                break ;
        }
    }

    @Override
    public void render(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, Swingy.WIDTH, Swingy.HEIGHT);

        Sprite background = new Sprite(new SpriteSheet(new Texture("background/2", false), Swingy.WIDTH, Swingy.HEIGHT), 1, 1);
        background.render(graphics, 0, 0);

        Fonts.drawString(graphics, new Font("Arial", Font.BOLD, 72), Color.GREEN, "Load Existing Fighter", 72, false);

        if (options != null) {
            for (int i = 0; i < options.length; i++) {
                if (i == currentButtonSelection)
                    options[i].setSelected(true);
                else
                    options[i].setSelected(false);
                options[i].render(graphics);
            }
        }

        Font font = new Font("Arial", Font.PLAIN, 16);
        FontMetrics fontMetrics = graphics.getFontMetrics(font);

        //Draw Current Fighter Stats to Screen
        if (characters != null) {
            int j = 0;
            for (String s : characters[currentCharacterSelection].getFighterMetrics().toStringArray()) {
                Fonts.drawString(graphics, font, Color.GREEN, s, ((Swingy.WIDTH - fontMetrics.stringWidth("swingy") - 80)) + 10, (100 + (j * 100)));
                j++;
            }
        }

        //Render only selected character
        if (entities != null)
            if (entities.size() > 0)
                entities.get(currentCharacterSelection).render(graphics);
    }

    public void addEntity(Entity entity){
        entities.add(entity);
    }
}
