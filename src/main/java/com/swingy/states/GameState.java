package com.swingy.states;

import com.swingy.artifacts.Armor;
import com.swingy.artifacts.Helm;
import com.swingy.artifacts.Weapon;
import com.swingy.battle.FighterMetrics;
import com.swingy.id.MobileIDAssigner;
import com.swingy.rendering.entities.Entity;
import com.swingy.rendering.entities.Fighter;
import com.swingy.id.ID;
import com.swingy.id.IDAssigner;
import com.swingy.input.KeyInput;
import com.swingy.input.MouseInput;
import com.swingy.map.TileMapGenerator;
import com.swingy.map.Tile;
import com.swingy.rendering.textures.Texture;
import com.swingy.rendering.ui.Button;
import com.swingy.view.Swingy;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static com.swingy.database.SwingyDB.swingyDB;

public class GameState extends Canvas implements State {

    private ArrayList<Entity> entities;
    private ArrayList<Fighter> fighters;
    private Tile[][] tileMap = null;

    private TileMapGenerator tileMapGenerator;
    private int playerIndex;
    private Button[] options;
    private int currentSelection;

    private MobileIDAssigner idAssigner;

    private boolean isResume = false;
    protected boolean gameOver;

    protected static Fighter player;
    protected static Fighter defender;

    private StateManager stateManager = null;

    private String[] artifacts = {
            "HELM",
            "WEAPON",
            "ARMOR"
    };

    @Override
    public void init() {

        gameOver = false;
        isResume = true;
        entities = new ArrayList<Entity>();
        currentSelection = 0;

        try {
            ResultSet resultSet = swingyDB.queryPlayer();
            if (resultSet.next()){
                switch(resultSet.getString(4)){
                    case "ninja":
                        player = new Fighter(new FighterMetrics(resultSet.getString(2), "NINJA"),
                                this, null);
                        player.setPlayerClass(ID.NINJA);
                        break;
                    case "dino":
                        player = new Fighter(new FighterMetrics(resultSet.getString(2), "DINO"),
                                this, null);
                        player.setPlayerClass(ID.DINO);
                        break;
                    case "robo":
                        player = new Fighter(new FighterMetrics(resultSet.getString(2), "DINO"),
                                this, null);
                        player.setPlayerClass(ID.ROBO);
                        break;
                    case "zombo":
                        player = new Fighter(new FighterMetrics(resultSet.getString(2), "ZOMBO"),
                                this, null);
                        player.setPlayerClass(ID.ZOMBO);
                        break;
                }
                player.getFighterMetrics().setID(resultSet.getInt(1));
                player.setPlayerClassName(resultSet.getString(4));
                player.getFighterMetrics().getLevel().setExperience(resultSet.getInt(3));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tileMapGenerator = new TileMapGenerator(player);
        tileMapGenerator.generate();
        tileMap = tileMapGenerator.getTileMap();

        fighters = new ArrayList<>();
        idAssigner = new MobileIDAssigner();
        int idCounter = 0;
        for (int i = 0; i < tileMap.length; i++){
            for (int j = 0; j < tileMap.length; j++){
                if (tileMap[i][j].getTileClassName() != "" && tileMap[i][j].getTileClassName() != null) {
                    Fighter tempFighter = null;
                    tempFighter = new Fighter(new Texture("terrain/" + tileMap[i][j].getTileClassName().toLowerCase() + "/1", false),
                            0, 0,
                            new FighterMetrics(tileMap[i][j].getTileClassName(), tileMap[i][j].getTileClassName()),
                            this, null);
                    tempFighter.setPlayerClass(tileMap[i][j].getTileClass());
                    tempFighter.setPlayerClassName(tileMap[i][j].getTileClassName().toLowerCase());
                    tempFighter.setMobileID(idAssigner.addID(tileMap[i][j].getCoordinate(idCounter++)));

                    String parts[] = tempFighter.getMobileID().split("-");
                    tempFighter.setX(Double.parseDouble(parts[0]));
                    tempFighter.setX(Double.parseDouble(parts[1]));

                    if (tempFighter != null) {
                        if (tempFighter.getMobileID().equalsIgnoreCase(tileMapGenerator.getPlayerCoordinate())) {
                            player.setSprite(tempFighter.getSprite());
                            player.setMobileID(tempFighter.getMobileID());
                            player.setX(tempFighter.getX());
                            player.setX(tempFighter.getY());
                            player.setPlayer(true);
                            fighters.add(player);
                        } else {
                            fighters.add(tempFighter);
                            tempFighter.getFighterMetrics().getLevel().setExperience(player.getFighterMetrics().getLevel().getExperience());
                        }
                    }
                }
            }
        }
    }

    public void enemyMove(){

        /*for (Fighter p: fighters) {
            if (p != player && p.isAlive()) {
                ArrayList<String> directions = new ArrayList<>();
                int originX = (Swingy.WIDTH - (tileMap.length * 32)) / 2;
                int originY = (Swingy.HEIGHT - (tileMap.length * 32)) / 2;
                int indexX = (int) ((p.getX() - originX));
                int indexY = (int) ((p.getY() - originY));

                if (indexX > 0)
                    indexX /= 32;
                if (indexY > 0)
                    indexY /= 32;

                if (indexX + 1 < tileMap.length && indexX + 1 > -1 && indexY < tileMap.length && indexY > -1) {
                    if (tileMap[indexY][indexX + 1].getTileClass() == ID.GROUND)
                        directions.add("right");

                }
                if (indexX - 1 < tileMap.length && indexX - 1 > -1 && indexY < tileMap.length && indexY > -1) {
                    if (tileMap[indexY][indexX - 1].getTileClass() == ID.GROUND)
                        directions.add("left");
                }
                if (indexX < tileMap.length && indexX > -1 && indexY + 1 < tileMap.length && indexY + 1 > -1) {
                    if (tileMap[indexY + 1][indexX].getTileClass() == ID.GROUND)
                        directions.add("down");
                }
                if (indexX < tileMap.length && indexX > -1 && indexY - 1 < tileMap.length && indexY - 1 > -1) {
                    if (tileMap[indexY - 1][indexX].getTileClass() == ID.GROUND)
                        directions.add("up");
                }

                double seed = Math.random();
                double i = 0;

                for (String d : directions) {
                    double probability = (++i/directions.size());
                    if (seed < probability) {
                        Tile tempTile;
                        if (d == "left") {
                            tempTile = tileMap[indexY][indexX - 1];
                            tileMap[indexY][indexX - 1] = tileMap[indexY][indexX];
                            tileMap[indexY][indexX] = tempTile;

                            tileMap[indexY][indexX - 1].moveX(-32);
                            tileMap[indexY][indexX].moveX(32);

                            p.moveX(-32);
                        }  else if (d == "up") {
                            tempTile = tileMap[indexY - 1][indexX];
                            tileMap[indexY - 1][indexX] = tileMap[indexY][indexX];
                            tileMap[indexY][indexX] = tempTile;

                            tileMap[indexY - 1][indexX].moveY(-32);
                            tileMap[indexY][indexX].moveY(32);

                            p.moveY(-32);
                        }
                        else if (d == "right") {
                            tempTile = tileMap[indexY][indexX + 1];
                            tileMap[indexY][indexX + 1] = tileMap[indexY][indexX];
                            tileMap[indexY][indexX] = tempTile;

                            tileMap[indexY][indexX + 1].moveX(32);
                            tileMap[indexY][indexX].moveX(-32);

                            p.moveX(32);
                        }
                        else {
                            tempTile = tileMap[indexY + 1][indexX];
                            tileMap[indexY + 1][indexX] = tileMap[indexY][indexX];
                            tileMap[indexY][indexX] = tempTile;

                            tileMap[indexY + 1][indexX].moveY(32);
                            tileMap[indexY][indexX].moveY(-32);

                            p.moveY(32);
                        }
                        break;
                    }
                }
            }
        }*/
    }

    @Override
    public State enterState(State callingState) {
        if (gameOver){
            isResume = false;
            entities.clear();
            fighters.clear();
            options = null;
        }
        if (!isResume)
            init();
        return this;
    }

    @Override
    public void exitState() {
        if (!gameOver)
            isResume = true;
        else if (gameOver){
            isResume = false;
            entities.clear();
            fighters.clear();
            options = null;
        }
        try {
            swingyDB.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "map";
    }

    public String calculateArtifact(){
        double seed = Math.random();

        if (seed < 2.0 / 6.0)
            return artifacts[0];
        else if (seed < 4.0 / 6.0)
            return artifacts[1];
        else
            return artifacts[2];
    }

    @Override
    public void tick(StateManager stateManager) {
        this.stateManager = stateManager;
        if (KeyInput.wasPressed(KeyEvent.VK_UP) || KeyInput.wasPressed(KeyEvent.VK_W)){
            if (options == null) {
                for (int i = 0; i < tileMap.length; i++) {
                    for (int j = 0; j < tileMap.length; j++) {
                        if (tileMap[i][j].isPlayer()) {
                            if (i - 1 > -1) {
                                if (tileMap[i - 1][j].getTileClass() == ID.GROUND) {
                                    Tile tempTile = tileMap[i - 1][j];
                                    tileMap[i - 1][j] = tileMap[i][j];
                                    tileMap[i][j] = tempTile;

                                    tileMap[i - 1][j].moveY(-32);
                                    tileMap[i][j].moveY(32);
                                    //remove tile method to remove old render coordinate of player and add new rendor coordinate

                                    player.moveY(-32);
                                    j = tileMap.length;
                                    i = tileMap.length;

                                    moveLogic();
                                }
                            }
                        }
                    }
                }
            }
            else {
                currentSelection++;
                if (currentSelection > options.length - 1){
                    currentSelection = 0;
                }
            }
        }

        if (KeyInput.wasPressed(KeyEvent.VK_DOWN) || KeyInput.wasPressed(KeyEvent.VK_S)){
            if (options == null) {
                for (int i = 0; i < tileMap.length; i++) {
                    for (int j = 0; j < tileMap.length; j++) {
                        if (tileMap[i][j].isPlayer()) {
                            if (i + 1 < tileMap.length) {
                                if (tileMap[i + 1][j].getTileClass() == ID.GROUND) {
                                    Tile tempTile = tileMap[i + 1][j];
                                    tileMap[i + 1][j] = tileMap[i][j];
                                    tileMap[i][j] = tempTile;

                                    tileMap[i + 1][j].moveY(32);
                                    tileMap[i][j].moveY(-32);

                                    player.moveY(32);
                                    j = tileMap.length;
                                    i = tileMap.length;

                                    moveLogic();
                                }
                            }
                        }
                    }
                }
            }
            else {
                currentSelection--;
                if (currentSelection < 0){
                    currentSelection = options.length - 1;
                }
            }
        }

        if (KeyInput.wasPressed(KeyEvent.VK_LEFT) || KeyInput.wasPressed(KeyEvent.VK_A)){
            if (options == null) {
                for (int i = 0; i < tileMap.length; i++) {
                    for (int j = 0; j < tileMap.length; j++) {
                        if (tileMap[i][j].isPlayer()) {
                            if (j - 1 > -1) {
                                if (tileMap[i][j - 1].getTileClass() == ID.GROUND) {
                                    Tile tempTile = tileMap[i][j - 1];
                                    tileMap[i][j - 1] = tileMap[i][j];
                                    tileMap[i][j] = tempTile;

                                    tileMap[i][j - 1].moveX(-32);
                                    tileMap[i][j].moveX(32);

                                    player.moveX(-32);
                                    j = tileMap.length;
                                    i = tileMap.length;

                                    moveLogic();
                                }
                            }
                        }
                    }
                }
            }
            else {
                currentSelection--;
                if (currentSelection < 0){
                    currentSelection = options.length - 1;
                }
            }
        }

        if (KeyInput.wasPressed(KeyEvent.VK_RIGHT) || KeyInput.wasPressed(KeyEvent.VK_D)){
            if(options == null) {
                for (int i = 0; i < tileMap.length; i++) {
                    for (int j = 0; j < tileMap.length; j++) {
                        if (tileMap[i][j] != null) {
                            if (tileMap[i][j].isPlayer()) {
                                if (j + 1 < tileMap.length) {
                                    if (tileMap[i][j + 1].getTileClass() == ID.GROUND) {
                                        Tile tempTile = tileMap[i][j + 1];
                                        tileMap[i][j + 1] = tileMap[i][j];
                                        tileMap[i][j] = tempTile;

                                        tileMap[i][j + 1].moveX(32);
                                        tileMap[i][j].moveX(-32);

                                        player.moveX(32);
                                        j = tileMap.length;
                                        i = tileMap.length;

                                        moveLogic();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else {
                currentSelection++;
                if (currentSelection > options.length - 1){
                    currentSelection = 0;
                }
            }
        }

        if (KeyInput.wasPressed(KeyEvent.VK_Q)){
            quitMap();
        }

        boolean clicked = false;

        if (options != null) {
            for (int i = 0; i < options.length; i++) {
                if (options[i].intersects(new Rectangle(MouseInput.getX(), MouseInput.getY(), 1, 1))) {
                    currentSelection = i;
                    clicked = MouseInput.wasPressed(MouseEvent.BUTTON1);
                }
            }
        }

        if (clicked || KeyInput.wasPressed(KeyEvent.VK_ENTER))
            select(stateManager);

        for (Entity e : entities)
            e.tick();
    }

    private void select(StateManager stateManager){
        switch (currentSelection){
            case 0 :
                options = null;
                stateManager.setState("battle", this);
                break ;
            case 1 :
                options = null;
                if (flee() == false);
                    stateManager.setState("battle", this);
                break ;
        }
    }

    private void moveLogic(){
        if (item()){
            String temp = calculateArtifact();
            addArtifact(temp);
        }
        collision();
        enemyMove();
        collision();
    }

    @Override
    public void render(Graphics graphics) {

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, Swingy.WIDTH, Swingy.HEIGHT);

        Texture background = new Texture(new Texture("background/3", false), 1, 1, Swingy.WIDTH, Swingy.HEIGHT);
        background.render(graphics, 0, 0);

        for (Entity e : entities)
            e.render(graphics);

        for (int i = 0; i < tileMap.length; i++){
            for (int j = 0; j < tileMap.length; j++){
                if (tileMap[i][j] != null)
                    tileMap[i][j].render(graphics);
            }
        }

        if (options != null) {
            for (int i = 0; i < options.length; i++) {
                if (i == currentSelection)
                    options[i].setSelected(true);
                else
                    options[i].setSelected(false);
                options[i].render(graphics);
            }
        }
    }

    @Override
    public void addEntity(Entity entity){
        entities.add(entity);
    }

    public boolean flee(){
       /* int random = 0 + (int)(Math.random() * ((3 - 0) + 1));
        boolean possible[] = {true, true, true, true};*/

        /*if (possible[random] == true)
            defender = null;*/

        return true;
    }

    public boolean escape(){
        int random = 0 + (int)(Math.random() * ((3 - 0) + 1));
        boolean possible[] = {true, true, true, false};

        return possible[random];
    }

    public boolean item(){
        int random = 0 + (int)(Math.random() * ((3 - 0) + 1));
        boolean possible[] = {false, true, false, false};

        return possible[random];
    }

    public void setDefender(int id){
        /*for (Fighter f : fighters) {
            if (f.getMobileID() == id)
                defender = f;
        }*/
    }

    public boolean collision(){
        for (int i =  0; i < tileMap.length; i++){
            for (int j = 0; j < tileMap.length; j++) {
                if (tileMap[i][j] != null) {
                    if (tileMap[i][j].isPlayer()) {
                        if (j + 1 < tileMap.length) {
                            if (tileMap[i][j + 1].getTileClass() == ID.NINJA
                                    || tileMap[i][j + 1].getTileClass() == ID.DINO
                                    || tileMap[i][j + 1].getTileClass() == ID.ROBO
                                    || tileMap[i][j + 1].getTileClass() == ID.ZOMBO) {

                                setDefender(tileMap[i][j + 1].getMobileID());

                                options = new Button[2];
                                options[0] = new Button("Fight", (500 + 0 * 80),
                                        new Font("Arial", Font.PLAIN, 32),
                                        new Font("Arial", Font.BOLD, 48),
                                        Color.WHITE,
                                        Color.YELLOW);
                                options[1] = new Button("Flee", (500 + 1 * 80),
                                        new Font("Arial", Font.PLAIN, 32),
                                        new Font("Arial", Font.BOLD, 48),
                                        Color.WHITE,
                                        Color.YELLOW);

                                return true;
                            } else if (tileMap[i][j + 1].getTileClass() == ID.LAVA
                                    || tileMap[i][j + 1].getTileClass() == ID.PIT) {
                                if (!item()) {
                                    System.out.println("DEATH BY DEEP END");
                                    player.setAlive(false);
                                    gameOver = true;
                                    stateManager.setState("menu", this);
                                }
                                else
                                    addArtifact(calculateArtifact());
                            }
                            else if (tileMap[i][j + 1].getTileClass() == ID.BORDER) {
                                System.out.println("PASSED LEVEL");
                                if (escape())
                                    passMap();
                            }
                        }
                        if (j - 1 > -1) {
                            if (tileMap[i][j - 1].getTileClass() == ID.NINJA
                                    || tileMap[i][j - 1].getTileClass() == ID.DINO
                                    || tileMap[i][j - 1].getTileClass() == ID.ROBO
                                    || tileMap[i][j - 1].getTileClass() == ID.ZOMBO) {

                                setDefender(tileMap[i][j - 1].getMobileID());

                                options = new Button[2];
                                options[0] = new Button("Fight", (500 + 0 * 80),
                                        new Font("Arial", Font.PLAIN, 32),
                                        new Font("Arial", Font.BOLD, 48),
                                        Color.WHITE,
                                        Color.YELLOW);
                                options[1] = new Button("Flee", (500 + 1 * 80),
                                        new Font("Arial", Font.PLAIN, 32),
                                        new Font("Arial", Font.BOLD, 48),
                                        Color.WHITE,
                                        Color.YELLOW);

                                return true;
                            } else if (tileMap[i][j - 1].getTileClass() == ID.LAVA
                                    || tileMap[i][j - 1].getTileClass() == ID.PIT) {
                                if (!item()) {
                                    System.out.println("DEATH BY DEEP END");
                                    player.setAlive(false);
                                    gameOver = true;
                                    stateManager.setState("menu", this);
                                }
                                else
                                    addArtifact(calculateArtifact());
                            }
                            else if (tileMap[i][j - 1].getTileClass() == ID.BORDER) {
                                System.out.println("PASSED LEVEL");
                                if (escape())
                                    passMap();
                            }
                        }
                        if (i + 1 < tileMap.length) {
                            if (tileMap[i + 1][j].getTileClass() == ID.NINJA
                                    || tileMap[i + 1][j].getTileClass() == ID.DINO
                                    || tileMap[i + 1][j].getTileClass() == ID.ROBO
                                    || tileMap[i + 1][j].getTileClass() == ID.ZOMBO) {

                                setDefender(tileMap[i + 1][j].getMobileID());

                                options = new Button[2];
                                options[0] = new Button("Fight", (500 + 0 * 80),
                                        new Font("Arial", Font.PLAIN, 32),
                                        new Font("Arial", Font.BOLD, 48),
                                        Color.WHITE,
                                        Color.YELLOW);
                                options[1] = new Button("Flee", (500 + 1 * 80),
                                        new Font("Arial", Font.PLAIN, 32),
                                        new Font("Arial", Font.BOLD, 48),
                                        Color.WHITE,
                                        Color.YELLOW);
                                return true;
                            } else if (tileMap[i + 1][j].getTileClass() == ID.LAVA
                                    || tileMap[i + 1][j].getTileClass() == ID.PIT) {
                                if (!item()) {
                                    System.out.println("DEATH BY DEEP END");
                                    player.setAlive(false);
                                    gameOver = true;
                                    stateManager.setState("menu", this);
                                }
                                else
                                    addArtifact(calculateArtifact());
                            }
                            else if (tileMap[i + 1][j].getTileClass() == ID.BORDER) {
                                System.out.println("PASSED LEVEL");
                                if (escape())
                                    passMap();
                            }
                        }
                        if (i - 1 > -1) {
                            if (tileMap[i - 1][j].getTileClass() == ID.NINJA
                                    || tileMap[i - 1][j].getTileClass() == ID.DINO
                                    || tileMap[i - 1][j].getTileClass() == ID.ROBO
                                    || tileMap[i - 1][j].getTileClass() == ID.ZOMBO) {

                                setDefender(tileMap[i - 1][j].getMobileID());

                                options = new Button[2];
                                options[0] = new Button("Fight", (500 + 0 * 80),
                                        new Font("Arial", Font.PLAIN, 32),
                                        new Font("Arial", Font.BOLD, 48),
                                        Color.WHITE,
                                        Color.YELLOW);
                                options[1] = new Button("Flee", (500 + 1 * 80),
                                        new Font("Arial", Font.PLAIN, 32),
                                        new Font("Arial", Font.BOLD, 48),
                                        Color.WHITE,
                                        Color.YELLOW);
                                return true;
                            } else if (tileMap[i - 1][j].getTileClass() == ID.LAVA
                                    || tileMap[i - 1][j].getTileClass() == ID.PIT) {
                                if (!item()) {
                                    System.out.println("DEATH BY DEEP END");
                                    player.setAlive(false);
                                    gameOver = true;
                                    stateManager.setState("menu", this);
                                }
                                else
                                    addArtifact(calculateArtifact());
                            }
                            else if (tileMap[i - 1][j].getTileClass() == ID.BORDER) {
                                System.out.println("PASSED LEVEL");
                                if (escape())
                                    passMap();
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private void passMap(){
        try {
            swingyDB.updatePlayer(player);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.gameOver = true;
        stateManager.setState("map", this);
    }

    private void quitMap(){
        try {
            swingyDB.updatePlayer(player);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.gameOver = true;
        stateManager.setState("menu", this);
    }

    private void addArtifact(String artifact){
        switch (artifact){
            case "HELM":
                player.getFighterMetrics().addArtifact(new Helm("helm_small", ID.HELM, player.getFighterMetrics().getLevel().getLevel()));
                break;
            case "ARMOR":
                player.getFighterMetrics().addArtifact(new Armor("armor_small", ID.ARMOR, player.getFighterMetrics().getLevel().getLevel()));
                break;
            case "WEAPON":
                player.getFighterMetrics().addArtifact(new Weapon("weapon_small", ID.WEAPON, player.getFighterMetrics().getLevel().getLevel()));
                break;
        }
    }

    protected void removeFighter(Fighter fighter){
        /*for (int i = 0; i < tileMap.length ;i++){
            for (int j = 0; j < tileMap.length; j++){
                if (tileMap[i][j].getMobileID() == fighter.getMobileID()) {

                    tileMap[i][j] = new Tile(tileMap[i][j].getX(), tileMap[i][j].getY(), new Texture(new Texture("terrain/ground", false), 2, 2, 32), ID.GROUND);

                    fighters.remove(fighter);
                    entities.remove(fighter);

                    i = tileMap.length;
                    j = tileMap.length;
                }
            }
        }*/
    }

    protected void gameOver(){
        this.gameOver = true;
    }
}