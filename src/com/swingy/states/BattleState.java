package com.swingy.states;

import com.swingy.Main;
import com.swingy.battle.FighterManager;
import com.swingy.rendering.entities.Entity;
import com.swingy.battle.BattleEngine;
import com.swingy.handlers.GameObjectHandler;
import com.swingy.heroes.Hero;
import com.swingy.id.ID;

import com.swingy.battle.objects.HUD;
import com.swingy.rendering.entities.Fighter;
import com.swingy.rendering.textures.Sprite;
import com.swingy.rendering.textures.SpriteSheet;
import com.swingy.rendering.textures.Texture;
import com.swingy.statics.Statics;
import com.swingy.util.Fonts;
import com.swingy.view.Swingy;

import java.awt.*;
import java.util.ArrayList;


public class BattleState extends Canvas implements State {

    private static final int DEFAULT_WIDTH = Swingy.WIDTH;
    private static final int DEFAULT_HEIGHT = Swingy.HEIGHT;

    private GameObjectHandler gameObjectHandler;

    private Hero a;
    private Hero b;

    private BattleEngine battleEngine;

    private String battleText;

    private boolean battleEnd;

    private GameState gameState;

    private ArrayList<Entity> entities;
    private ArrayList<Fighter> fighters;

    @Override
    public void init() {
        battleEnd = false;
        entities = new ArrayList<>();
        fighters = new ArrayList<>();

        a = new Hero("Asuna", "Water");
        b = new Hero("Ragos", "Fire");

        Fighter tempFighter = null;
        switch (GameState.player.getPlayerClass()){
            case NINJA:
                tempFighter = new Fighter(new Sprite("ninja/idle/1"),
                        DEFAULT_WIDTH / 100 * 5,
                        DEFAULT_HEIGHT / 100 * 50,
                        this, Statics.ninjaLarge);
                tempFighter.setPlayerClass(ID.NINJA);
                tempFighter.setPlayerClassName("ninja");
                break;
            case DINO:
                tempFighter = new Fighter(new Sprite("dino/idle/1"),
                        DEFAULT_WIDTH / 100 * 5,
                        DEFAULT_HEIGHT / 100 * 60,
                        this, Statics.dinoLarge);
                tempFighter.setPlayerClass(ID.DINO);
                tempFighter.setPlayerClassName("dino");
                break;
            case ROBO:
                tempFighter = new Fighter(new Sprite("robo/idle/1"),
                        DEFAULT_WIDTH / 100 * 5,
                        DEFAULT_HEIGHT / 100 * 50,
                        this, Statics.roboLarge);
                tempFighter.setPlayerClass(ID.ROBO);
                tempFighter.setPlayerClassName("robo");
                break;
            case ZOMBO:
                tempFighter = new Fighter(new Sprite("zombo/idle/1"),
                        DEFAULT_WIDTH / 100 * 5,
                        DEFAULT_HEIGHT / 100 * 50,
                        this, Statics.zomboLarge);
                tempFighter.setPlayerClass(ID.ZOMBO);
                tempFighter.setPlayerClassName("zombo");
                break;
        }
        //Preserve Object Info
        tempFighter.setPlayer(true);
        tempFighter.setID(GameState.player.getID());
        fighters.add(tempFighter);

        switch (GameState.defender.getPlayerClass()){
            case NINJA:
                tempFighter = new Fighter(new Sprite("ninja/idle/1"),
                        DEFAULT_WIDTH / 100 * 70,
                        DEFAULT_HEIGHT / 100 * 50,
                        this, Statics.ninjaLargeRef);
                tempFighter.setPlayerClass(ID.NINJA);
                tempFighter.setPlayerClassName("ninja");
                break;
            case DINO:
                tempFighter = new Fighter(new Sprite("dino/idle/1"),
                        DEFAULT_WIDTH / 100 * 60,
                        DEFAULT_HEIGHT / 100 * 60,
                        this, Statics.dinoLargeRef);
                tempFighter.setPlayerClass(ID.DINO);
                tempFighter.setPlayerClassName("dino");
                break;
            case ROBO:
                tempFighter = new Fighter(new Sprite("robo/idle/1"),
                        DEFAULT_WIDTH / 100 * 70,
                        DEFAULT_HEIGHT / 100 * 50,
                        this, Statics.roboLargeRef);
                tempFighter.setPlayerClass(ID.ROBO);
                tempFighter.setPlayerClassName("robo");
                break;
            case ZOMBO:
                tempFighter = new Fighter(new Sprite("zombo/idle/1"),
                        DEFAULT_WIDTH / 100 * 70,
                        DEFAULT_HEIGHT / 100 * 50,
                        this, Statics.zomboLargeRef);
                tempFighter.setPlayerClass(ID.ZOMBO);
                tempFighter.setPlayerClassName("zombo");
                break;
        }
        //Preserve Object Info
        tempFighter.setID(GameState.defender.getID());
        fighters.add(tempFighter);

        gameObjectHandler = new GameObjectHandler();

        HUD challengerHUD = new HUD(DEFAULT_WIDTH / 100 * 5, DEFAULT_HEIGHT / 100 * 5, ID.ChallengerHUD);
        HUD defenderHUD = new HUD(DEFAULT_WIDTH / 100 * 75, DEFAULT_HEIGHT / 100 * 5, ID.DefenderHUD);

        //Initialising GameObject Handler And FighterManager
        for (Fighter f : fighters){
            System.out.printf("IS ALIVE: %s | IS PLAYER %s\n", f.isAlive(), f.isPlayer());
            if (f.isPlayer()) {
                gameObjectHandler.addObject(new FighterManager(DEFAULT_WIDTH / 100 * 5, DEFAULT_HEIGHT / 100 * 40
                        , ID.Challenger, true, challengerHUD, f, this));
            }
            else{
                gameObjectHandler.addObject(new FighterManager(DEFAULT_WIDTH / 100 * 75,  DEFAULT_HEIGHT / 100 * 40
                        , ID.Defender, false, defenderHUD, (Fighter) f, this));
            }
        }

        //Initialising Battle Engine Listeners
        battleEngine = new BattleEngine();

        for (int i = 0; i < gameObjectHandler.getObjects().size(); i++)
            battleEngine.addPropertyChangeListener((FighterManager)gameObjectHandler.getObjects().get(i));

        battleEngine.setChallenger(a);
        battleEngine.setDefender(b);

        //Initialising Animation Listeners
        for (int i = 0; i < fighters.size(); i++)
            fighters.get(i).getAnimation().addPropertyChangeListener((FighterManager)gameObjectHandler.getObjects().get(i));

        battleText = "FIGHT";

        Main.pool.runTask(battleEngine);
    }

    @Override
    public String getName() {
        return "battle";
    }

    @Override
    public State enterState(State callingState) {
        init();
        gameState = (GameState)callingState;
        return this;
    }

    @Override
    public void exitState() {
        entities.clear();
        gameObjectHandler.clear();
    }

    @Override
    public void tick(StateManager stateManager) {
        gameObjectHandler.tick();

        for(Entity e: entities)
            e.tick();

        for(Fighter f: fighters)
            f.tick();

        if (battleEnd) {
            for (Fighter f : fighters) {
                System.out.printf("IS ALIVE: %s | IS PLAYER %s\n", f.isAlive(), f.isPlayer());
                if (!f.isAlive())
                    gameState.removeFighter(f);
            }
            System.out.println(battleText);
            if (battleText.equalsIgnoreCase("VICTORY"))
                stateManager.setState("map", this);
            else
                stateManager.setState("menu", this);
        }
    }

    @Override
    public void render(Graphics graphics) {

        Font font = new Font("Arial", Font.BOLD, 72);
        FontMetrics fontMetrics = graphics.getFontMetrics(font);

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);

        Sprite background = new Sprite(new SpriteSheet(new Texture("background/4", false), Swingy.WIDTH, Swingy.HEIGHT), 1, 1);
        background.render(graphics, 0, 0);
        //graphics.setColor(Color.BLUE);
        //graphics.fillRoundRect(DEFAULT_WIDTH / 100 * 6, DEFAULT_HEIGHT / 100 * 10 , 80, 80, 80, 80);
        //graphics.fillRoundRect(DEFAULT_WIDTH / 100 * 14, DEFAULT_HEIGHT / 100 * 10 , 80, 80, 80, 80);

        gameObjectHandler.render(graphics);
        for(Entity e: entities)
            e.render(graphics);

        for(Fighter f: fighters)
            f.render(graphics);

        Fonts.drawString(graphics, font, Color.GREEN, battleText, (Swingy.WIDTH - fontMetrics.stringWidth(battleText)) / 2, Swingy.HEIGHT / 2);

        graphics.dispose();
    }

    @Override
    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public void setBattleText(String text){
        this.battleText = text;
    }

    public void setBattleEnd(boolean battleEnd) {
        this.battleEnd = battleEnd;
    }

    public ArrayList<Fighter> getFighters() {
        return fighters;
    }

    public void setFighters(ArrayList<Fighter> fighters) {
        this.fighters = fighters;
    }
}
