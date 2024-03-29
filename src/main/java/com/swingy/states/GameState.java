package com.swingy.states;

import com.swingy.artifacts.Armor;
import com.swingy.artifacts.Helm;
import com.swingy.artifacts.Weapon;
import com.swingy.battle.FighterMetrics;
import com.swingy.id.MobileIDAssigner;
import com.swingy.game.entities.Entity;
import com.swingy.game.entities.Fighter;
import com.swingy.id.ID;
import com.swingy.input.InputTimer;
import com.swingy.input.KeyInput;
import com.swingy.input.MouseInput;
import com.swingy.map.TileMapGenerator;
import com.swingy.map.Tile;
import com.swingy.metrics.Coordinate;
import com.swingy.rendering.textures.Texture;
import com.swingy.rendering.ui.Button;
import com.swingy.rendering.ui.Window;
import com.swingy.util.NumberHelper;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.swingy.console.Console.console;
import static com.swingy.database.SwingyDB.swingyDB;
import static com.swingy.map.TileMapGenerator.groundIDAssigner;
import static com.swingy.states.MenuState.swingy;

public class GameState extends Canvas implements State {

    private ArrayList<Fighter> fighters;
    private HashMap<String, Tile> tileMap = null;
    private String[][] charMap = null;

    private TileMapGenerator tileMapGenerator;
    private Button[] options;
    private int currentSelection;

    private boolean isResume = false;
    protected boolean gameOver;

    protected static Fighter player;
    protected static Fighter defender;

    public static Coordinate playerCoordinates;

    private StateManager stateManager = null;

    private int cooldown;

    private String[] artifacts = {
            "HELM",
            "WEAPON",
            "ARMOR"
    };

    private int fontSize = Window.HEIGHT / 100 * 5;
    private int fontBold = Window.HEIGHT / 100 * 6;

    @Override
    public void init() {
        System.out.println("GAME STATE!!! AHOOOGAH!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        gameOver = false;
        isResume = true;
        currentSelection = 0;

        player = swingyDB.getFighter();

        /*swingyDB.setAction("FETCH");
        futureFetch = executorService.submit(swingyDB);

        while (true){
            if (futureFetch.isDone()) {
                try {
                    ResultSet resultSet = futureFetch.get();
                    if (resultSet != null) {
                        if (resultSet.next()) {
                            switch (resultSet.getString(4)) {
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
                                        player = new Fighter(new FighterMetrics(resultSet.getString(2), "ROBO"),
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
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
                break;
            }
        }*/

        tileMapGenerator = new TileMapGenerator(player);
        tileMapGenerator.generate();
        tileMap = tileMapGenerator.getTileMap();
        charMap = tileMapGenerator.getCharMap();

        fighters = new ArrayList<>();

        for(HashMap.Entry<String, Tile> t : tileMap.entrySet()){
            if (t.getValue().getFigtherClassName() != "" && t.getValue().getFigtherClassName() != null){
                for (HashMap.Entry<String, Coordinate> tObject: t.getValue().getCoordinates().entrySet()){
                    System.out.printf("KEY: %S | VALUE %S\n",tObject.getKey(), tObject.getValue());
                    Fighter tempFighter = null;
                    tempFighter = new Fighter(new FighterMetrics(t.getValue().getTileClassName(), t.getValue().getFigtherClassName()),
                            this, null);
                    tempFighter.setPlayerClass(t.getValue().getTileClass());
                    tempFighter.setPlayerClassName(t.getValue().getFigtherClassName().toLowerCase());
                    tempFighter.setMobileID(tObject.getValue());

                    if (tempFighter != null) {
                        if (tempFighter.getMobileID() == tileMapGenerator.getPlayerCoordinate()) {
                            System.out.println("PLAYER COORDINATE ASSIGNED");
                            playerCoordinates = tileMapGenerator.getPlayerCoordinate();
                            player.setSprite(tempFighter.getSprite());
                            player.setMobileID(tempFighter.getMobileID());
                            player.setPlayer(true);
                            fighters.add(player);
                        } else {
                            tempFighter.getFighterMetrics().getLevel().setExperience(player.getFighterMetrics().getLevel().getExperience());
                            fighters.add(tempFighter);
                        }
                    }
                }
            }
        }

        //Output console options and wait for userSelection
        console.userSelection(this);
        printCharMap();
    }

    public void enemyMove(){

        String directions[] = {
                "UP",
                "DOWN",
                "LEFT",
                "RIGHT"
        };

        if (options == null){
            for (HashMap.Entry<String, Tile> t : tileMap.entrySet()){
                if (!t.getValue().getFigtherClassName().equalsIgnoreCase("")){
                    for (HashMap.Entry<String, Coordinate> tObject : t.getValue().getCoordinates().entrySet()){
                        if (tObject.getValue() != playerCoordinates){
                            int seed = 0 + (int)(Math.random() * ((3 - 0) + 1));
                            Tile groundTile = tileMap.get("GROUND");
                            switch (directions[seed]){
                                case "UP":
                                    for (HashMap.Entry<String, Coordinate> tGround : tileMap.get("GROUND").getCoordinates().entrySet()){
                                        if (coordinateCompare(tGround.getValue(), tObject.getValue(), 0, 32, 0, 0)){
                                            //Swap Render Coordinates on adjacent tiles
                                            Coordinate newPlayerCoordinate = tGround.getValue();
                                            Coordinate newGroundCoordinate = tObject.getValue();

                                            t.getValue().replaceCoordinate(tObject.getKey(), newPlayerCoordinate);
                                            groundTile.replaceCoordinate(tGround.getKey(), newGroundCoordinate);

                                            swapCharMapIndices(newPlayerCoordinate, newGroundCoordinate);

                                            for (Fighter f : fighters){
                                                if (f.getMobileID() == newGroundCoordinate)
                                                    f.setMobileID(newPlayerCoordinate);
                                            }

                                            break ;
                                        }
                                    }
                                    break ;
                                case "DOWN":
                                    for (HashMap.Entry<String, Coordinate> tGround : tileMap.get("GROUND").getCoordinates().entrySet()){
                                        if (coordinateCompare(tGround.getValue(), tObject.getValue(), 0, -32, 0, 0)){
                                            //Swap Render Coordinates on adjacent tiles
                                            Coordinate newPlayerCoordinate = tGround.getValue();
                                            Coordinate newGroundCoordinate = tObject.getValue();

                                            t.getValue().replaceCoordinate(tObject.getKey(), newPlayerCoordinate);
                                            groundTile.replaceCoordinate(tGround.getKey(), newGroundCoordinate);

                                            swapCharMapIndices(newPlayerCoordinate, newGroundCoordinate);

                                            for (Fighter f : fighters){
                                                if (f.getMobileID() == newGroundCoordinate)
                                                    f.setMobileID(newPlayerCoordinate);
                                            }

                                            break ;
                                        }
                                    }
                                    break ;
                                case "LEFT":
                                    for (HashMap.Entry<String, Coordinate> tGround : tileMap.get("GROUND").getCoordinates().entrySet()){
                                        if (coordinateCompare(tGround.getValue(), tObject.getValue(), 32, 0, 0, 0)){
                                            //Swap Render Coordinates on adjacent tiles
                                            Coordinate newPlayerCoordinate = tGround.getValue();
                                            Coordinate newGroundCoordinate = tObject.getValue();

                                            t.getValue().replaceCoordinate(tObject.getKey(), newPlayerCoordinate);
                                            groundTile.replaceCoordinate(tGround.getKey(), newGroundCoordinate);

                                            swapCharMapIndices(newPlayerCoordinate, newGroundCoordinate);

                                            for (Fighter f : fighters){
                                                if (f.getMobileID() == newGroundCoordinate)
                                                    f.setMobileID(newPlayerCoordinate);
                                            }

                                            break ;
                                        }
                                    }
                                    break ;
                                case "RIGHT":
                                    for (HashMap.Entry<String, Coordinate> tGround : tileMap.get("GROUND").getCoordinates().entrySet()){
                                        if (coordinateCompare(tGround.getValue(), tObject.getValue(), -32, 0, 0, 0)){
                                            //Swap Render Coordinates on adjacent tiles
                                            Coordinate newPlayerCoordinate = tGround.getValue();
                                            Coordinate newGroundCoordinate = tObject.getValue();

                                            t.getValue().replaceCoordinate(tObject.getKey(), newPlayerCoordinate);
                                            groundTile.replaceCoordinate(tGround.getKey(), newGroundCoordinate);

                                            swapCharMapIndices(newPlayerCoordinate, newGroundCoordinate);

                                            for (Fighter f : fighters){
                                                if (f.getMobileID() == newGroundCoordinate)
                                                    f.setMobileID(newPlayerCoordinate);
                                            }

                                            break ;
                                        }
                                    }
                                    break ;
                            }
                        }
                    }
                }
            }
        }

    }

    @Override
    public State enterState(StateManager stateManager, State callingState) {
        this.stateManager = stateManager;
        cooldown = 100;
        if (gameOver){
            isResume = false;
            fighters.clear();
            options = null;
        }
        if (!isResume)
            init();
        this.stateManager.setTick(true);
        return this;
    }

    @Override
    public void exitState() {
        this.stateManager.setTick(false);
        if (!gameOver) {
            isResume = true;
        }
        else if (gameOver){
            isResume = false;
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

    private boolean coordinateCompare(Coordinate c1, Coordinate c2, float modX1, float modY1, float modX2, float modY2){
        double x1 = c1.getAxisX() + modX1;
        double y1 = c1.getAxisY() + modY1;
        double x2 = c2.getAxisX() + modX2;
        double y2 = c2.getAxisY() + modY2;

        if (x1 == x2 && y1 == y2)
            return true;
        return false;
    }

    private void swapCharMapIndices(Coordinate coordinate1, Coordinate coordinate2){
        //index = (pixelLength - pixelPadding) \ 32

        float pixelLength = coordinate1.getAxisX();
        float pixelPaddding = (Window.WIDTH - (TileMapGenerator.getMapSize() * 32)) / 2;
        int x1 = (int)NumberHelper.round(((pixelLength - pixelPaddding) / 32), 0);

        pixelLength = coordinate1.getAxisY();
        pixelPaddding = (Window.HEIGHT - (TileMapGenerator.getMapSize() * 32)) / 2;
        int y1 = (int)NumberHelper.round(((pixelLength - pixelPaddding) / 32), 0);

        pixelLength = coordinate2.getAxisX();
        pixelPaddding = (Window.WIDTH - (TileMapGenerator.getMapSize() * 32)) / 2;
        int x2 = (int)NumberHelper.round(((pixelLength - pixelPaddding) / 32), 0);

        pixelLength = coordinate2.getAxisY();
        pixelPaddding = (Window.HEIGHT - (TileMapGenerator.getMapSize() * 32)) / 2;
        int y2 = (int)NumberHelper.round(((pixelLength - pixelPaddding) / 32), 0);


        String temp = charMap[y1][x1];
        charMap[y1][x1] = charMap[y2][x2];
        charMap[y2][x2] = temp;
    }

    public void printCharMap(){
        for (int i = 0; i < charMap.length; i++){
            for (int j = 0; j < charMap.length; j++){
                System.out.print(charMap[i][j]);
            }
            System.out.print("\n");
        }
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

        if (userInput == null)
            userInput = "0";

        cooldown--;
        if (cooldown <= 0){
            if (userInput != "0"){
                cooldown = 100;
                if (userInput.equalsIgnoreCase("gui")) {
                    swingy.setGui(true);
                    stateManager.setTick(false);
                    stateManager.setState("map", this);
                }
                else{
                    try {
                        int userOption = Integer.parseInt(userInput);
                        if (userOption < 1 && userOption > 5) {
                            userInput = "0";
                            System.out.println("INVALID INPUT...");
                        }
                    }catch (NumberFormatException e){
                        System.out.println("INVALID INPUT...");
                    }
                }
            }

            if (options != null && userInput != "0") {
                cooldown = 100;
                currentSelection = Integer.parseInt(userInput) - 1;
                select(stateManager);
            }

            if (KeyInput.wasPressed(KeyEvent.VK_UP) || KeyInput.wasPressed(KeyEvent.VK_W) || userInput.equalsIgnoreCase("1")) {
                cooldown = 100;
                System.out.println("Up Pressed!!!!!!");
                if (options == null) {
                    boolean moved = false;
                    Tile playerTile = tileMap.get(player.getPlayerClassName().toUpperCase());
                    for (HashMap.Entry<String, Coordinate> tPlayer : playerTile.getCoordinates().entrySet()) {
                        if (tPlayer.getKey().equalsIgnoreCase("PLAYER")) {
                            Tile groundTile = tileMap.get("GROUND");
                            for (HashMap.Entry<String, Coordinate> tGround : tileMap.get("GROUND").getCoordinates().entrySet()) {
                                if (coordinateCompare(tGround.getValue(), playerCoordinates, 0, 32, 0, 0)) {
                                    //Swap Render Coordinates on adjacent tiles

                                    Coordinate newPlayerCoordinate = tGround.getValue();
                                    Coordinate newGroundCoordinate = tPlayer.getValue();

                                    playerTile.replaceCoordinate("PLAYER", newPlayerCoordinate);
                                    groundTile.replaceCoordinate(tGround.getKey(), newGroundCoordinate);

                                    swapCharMapIndices(newPlayerCoordinate, newGroundCoordinate);

                                    player.setMobileID(newPlayerCoordinate);
                                    playerCoordinates = newPlayerCoordinate;

                                    if (item()) {
                                        String temp = calculateArtifact();
                                        addArtifact(temp);
                                    }

                                    moved = true;
                                    enemyMove();

                                    break;
                                }
                            }

                            if (moved) {
                                System.out.println("Moved UP!!!!!!");
                                printCharMap();
                                for (HashMap.Entry<String, Tile> t : tileMap.entrySet()) {
                                    if (t.getValue().getTileClassName() == "FIGHTER"
                                            || t.getValue().getTileClassName() == "TRAP"
                                            || t.getValue().getTileClassName() == "BORDER") {
                                        for (HashMap.Entry<String, Coordinate> tObject : t.getValue().getCoordinates().entrySet()) {
                                            if (coordinateCompare(tObject.getValue(), playerCoordinates, 0, -32, 0, 0)
                                                    || coordinateCompare(tObject.getValue(), playerCoordinates, 0, 32, 0, 0)
                                                    || coordinateCompare(tObject.getValue(), playerCoordinates, -32, 0, 0, 0)
                                                    || coordinateCompare(tObject.getValue(), playerCoordinates, 32, 0, 0, 0))
                                                collision(tObject.getValue(), t.getValue().getTileClassName(), t.getValue());
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                } else {
                    currentSelection++;
                    if (currentSelection > options.length - 1) {
                        currentSelection = 0;
                    }
                }
            } else if (KeyInput.wasPressed(KeyEvent.VK_DOWN) || KeyInput.wasPressed(KeyEvent.VK_S) || userInput.equalsIgnoreCase("2")) {
                cooldown = 100;
                System.out.println("Down Pressed!!!!!!");
                if (options == null) {
                    Tile playerTile = tileMap.get(player.getPlayerClassName().toUpperCase());
                    for (HashMap.Entry<String, Coordinate> tPlayer : playerTile.getCoordinates().entrySet()) {
                        if (tPlayer.getKey().equalsIgnoreCase("PLAYER")) {
                            boolean moved = false;
                            Tile groundTile = tileMap.get("GROUND");
                            for (HashMap.Entry<String, Coordinate> tGround : tileMap.get("GROUND").getCoordinates().entrySet()) {
                                if (coordinateCompare(tGround.getValue(), playerCoordinates, 0, -32, 0, 0)) {
                                    //Swap Render Coordinates on adjacent tiles

                                    Coordinate newPlayerCoordinate = tGround.getValue();
                                    Coordinate newGroundCoordinate = tPlayer.getValue();

                                    playerTile.replaceCoordinate("PLAYER", newPlayerCoordinate);
                                    groundTile.replaceCoordinate(tGround.getKey(), newGroundCoordinate);

                                    swapCharMapIndices(newPlayerCoordinate, newGroundCoordinate);

                                    player.setMobileID(newPlayerCoordinate);
                                    playerCoordinates = newPlayerCoordinate;

                                    if (item()) {
                                        String temp = calculateArtifact();
                                        addArtifact(temp);
                                    }

                                    moved = true;
                                    enemyMove();

                                    break;
                                }
                            }

                            if (moved) {
                                System.out.println("Moved Down!!!!!!");
                                printCharMap();
                                for (HashMap.Entry<String, Tile> t : tileMap.entrySet()) {
                                    if (t.getValue().getTileClassName() == "FIGHTER"
                                            || t.getValue().getTileClassName() == "TRAP"
                                            || t.getValue().getTileClassName() == "BORDER") {
                                        for (HashMap.Entry<String, Coordinate> tObject : t.getValue().getCoordinates().entrySet()) {
                                            if (coordinateCompare(tObject.getValue(), playerCoordinates, 0, -32, 0, 0)
                                                    || coordinateCompare(tObject.getValue(), playerCoordinates, 0, 32, 0, 0)
                                                    || coordinateCompare(tObject.getValue(), playerCoordinates, -32, 0, 0, 0)
                                                    || coordinateCompare(tObject.getValue(), playerCoordinates, 32, 0, 0, 0))
                                                collision(tObject.getValue(), t.getValue().getTileClassName(), t.getValue());
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                } else {
                    currentSelection--;
                    if (currentSelection < 0) {
                        currentSelection = options.length - 1;
                    }
                }
            } else if (KeyInput.wasPressed(KeyEvent.VK_LEFT) || KeyInput.wasPressed(KeyEvent.VK_A) || userInput.equalsIgnoreCase("3")) {
                cooldown = 100;
                System.out.println("Left Pressed!!!!!!");
                if (options == null) {
                    Tile playerTile = tileMap.get(player.getPlayerClassName().toUpperCase());
                    for (HashMap.Entry<String, Coordinate> tPlayer : playerTile.getCoordinates().entrySet()) {
                        if (tPlayer.getKey().equalsIgnoreCase("PLAYER")) {
                            boolean moved = false;
                            Tile groundTile = tileMap.get("GROUND");
                            for (HashMap.Entry<String, Coordinate> tGround : tileMap.get("GROUND").getCoordinates().entrySet()) {
                                if (coordinateCompare(tGround.getValue(), playerCoordinates, 32, 0, 0, 0)) {
                                    //Swap Render Coordinates on adjacent tiles

                                    Coordinate newPlayerCoordinate = tGround.getValue();
                                    Coordinate newGroundCoordinate = tPlayer.getValue();

                                    playerTile.replaceCoordinate("PLAYER", newPlayerCoordinate);
                                    groundTile.replaceCoordinate(tGround.getKey(), newGroundCoordinate);

                                    swapCharMapIndices(newPlayerCoordinate, newGroundCoordinate);

                                    player.setMobileID(newPlayerCoordinate);
                                    playerCoordinates = newPlayerCoordinate;

                                    if (item()) {
                                        String temp = calculateArtifact();
                                        addArtifact(temp);
                                    }

                                    moved = true;
                                    enemyMove();

                                    break;
                                }
                            }

                            if (moved) {
                                System.out.println("Moved Left!!!!!!");
                                printCharMap();
                                for (HashMap.Entry<String, Tile> t : tileMap.entrySet()) {
                                    if (t.getValue().getTileClassName() == "FIGHTER"
                                            || t.getValue().getTileClassName() == "TRAP"
                                            || t.getValue().getTileClassName() == "BORDER") {
                                        for (HashMap.Entry<String, Coordinate> tObject : t.getValue().getCoordinates().entrySet()) {
                                            if (coordinateCompare(tObject.getValue(), playerCoordinates, 0, -32, 0, 0)
                                                    || coordinateCompare(tObject.getValue(), playerCoordinates, 0, 32, 0, 0)
                                                    || coordinateCompare(tObject.getValue(), playerCoordinates, -32, 0, 0, 0)
                                                    || coordinateCompare(tObject.getValue(), playerCoordinates, 32, 0, 0, 0))
                                                collision(tObject.getValue(), t.getValue().getTileClassName(), t.getValue());
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                } else {
                    currentSelection--;
                    if (currentSelection < 0) {
                        currentSelection = options.length - 1;
                    }
                }
            } else if (KeyInput.wasPressed(KeyEvent.VK_RIGHT) || KeyInput.wasPressed(KeyEvent.VK_D) || userInput.equalsIgnoreCase("4")) {
                cooldown = 100;
                System.out.println("Right Pressed!!!!!!");
                if (options == null) {
                    Tile playerTile = tileMap.get(player.getPlayerClassName().toUpperCase());
                    for (HashMap.Entry<String, Coordinate> tPlayer : playerTile.getCoordinates().entrySet()) {
                        if (tPlayer.getKey().equalsIgnoreCase("PLAYER")) {
                            boolean moved = false;
                            Tile groundTile = tileMap.get("GROUND");
                            for (HashMap.Entry<String, Coordinate> tGround : tileMap.get("GROUND").getCoordinates().entrySet()) {
                                if (coordinateCompare(tGround.getValue(), playerCoordinates, -32, 0, 0, 0)) {
                                    //Swap Render Coordinates on adjacent tiles
                                    Coordinate newPlayerCoordinate = tGround.getValue();
                                    Coordinate newGroundCoordinate = tPlayer.getValue();

                                    playerTile.replaceCoordinate("PLAYER", newPlayerCoordinate);
                                    groundTile.replaceCoordinate(tGround.getKey(), newGroundCoordinate);

                                    swapCharMapIndices(newPlayerCoordinate, newGroundCoordinate);

                                    player.setMobileID(newPlayerCoordinate);
                                    playerCoordinates = newPlayerCoordinate;

                                    if (item()) {
                                        String temp = calculateArtifact();
                                        addArtifact(temp);
                                    }

                                    moved = true;
                                    enemyMove();

                                    break;
                                }
                            }

                            if (moved) {
                                System.out.println("Moved Right!!!!!!");
                                printCharMap();
                                for (HashMap.Entry<String, Tile> t : tileMap.entrySet()) {
                                    if (t.getValue().getTileClassName() == "FIGHTER"
                                            || t.getValue().getTileClassName() == "TRAP"
                                            || t.getValue().getTileClassName() == "BORDER") {
                                        for (HashMap.Entry<String, Coordinate> tObject : t.getValue().getCoordinates().entrySet()) {
                                            if (coordinateCompare(tObject.getValue(), playerCoordinates, 0, -32, 0, 0)
                                                    || coordinateCompare(tObject.getValue(), playerCoordinates, 0, 32, 0, 0)
                                                    || coordinateCompare(tObject.getValue(), playerCoordinates, -32, 0, 0, 0)
                                                    || coordinateCompare(tObject.getValue(), playerCoordinates, 32, 0, 0, 0))
                                                collision(tObject.getValue(), t.getValue().getTileClassName(), t.getValue());
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                } else {
                    currentSelection++;
                    if (currentSelection > options.length - 1) {
                        currentSelection = 0;
                    }
                }
            }

        }
        if (KeyInput.wasPressed(KeyEvent.VK_Q) || userInput.equalsIgnoreCase("5")){
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

        if (clicked || KeyInput.wasPressed(KeyEvent.VK_ENTER) && options != null)
            select(stateManager);
    }

    private void select(StateManager stateManager){
        switch (currentSelection){
            case 0 :
                options = null;
                if (defender != null)
                    this.stateManager.setTick(false);
                    stateManager.setState("battle", this);
                break ;
            case 1 :
                options = null;
                if (flee() == false) {
                    if (defender != null) {
                        this.stateManager.setTick(false);
                        stateManager.setState("battle", this);
                    }
                }
                break ;
        }
    }

    private void createOptions(){
        options = new Button[2];
        options[0] = new Button("Fight", (Window.HEIGHT / 6 * 2),
                new Font("Arial", Font.PLAIN, fontSize),
                new Font("Arial", Font.BOLD, fontBold),
                Color.WHITE,
                Color.YELLOW);
        options[1] = new Button("Flee", (Window.HEIGHT / 6 * 4),
                new Font("Arial", Font.PLAIN, fontSize),
                new Font("Arial", Font.BOLD, fontBold),
                Color.WHITE,
                Color.YELLOW);
        System.out.println("FIGHTER ENCOUNTERED\n1. FIGHT\n2. FLEE");
    }

    @Override
    public void render(Graphics graphics) {
        Texture background = new Texture("background/3", Window.WIDTH, Window.HEIGHT, false);
        background.render(graphics, 0, 0);

        for(HashMap.Entry<String, Tile> t : tileMap.entrySet())
            t.getValue().render(graphics);

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

    }

    public boolean flee(){
        int random = 0 + (int)(Math.random() * ((3 - 0) + 1));
        boolean possible[] = {true, false, true, false};

        if (possible[random] == true)
            defender = null;

        return possible[random];
    }

    public boolean escape(){
        int random = 0 + (int)(Math.random() * ((3 - 0) + 1));
        boolean possible[] = {true, true, true, true};

        return possible[random];
    }

    public boolean item(){
        int random = 0 + (int)(Math.random() * ((8 - 0) + 1));
        boolean possible[] = {false, true, false, false, false, false, false, false, false};

        return possible[random];
    }

    public void setDefender(Coordinate id){
        defender = null;
        for (Fighter f : fighters) {
            if (f.getMobileID() == id)
                defender = f;
        }
    }

    private void collision(Coordinate coordinate, String tileClassName, Tile tile){
        System.out.println(tileClassName);
        switch (tileClassName){
            case "FIGHTER":
                setDefender(coordinate);
                if(defender != null)
                    createOptions();
                break;
            case "TRAP":
                if (!item()) {
                    player.setAlive(false);
                    gameOver = true;
                    this.stateManager.setTick(false);
                    stateManager.setState("menu", this);
                }
                else
                    addArtifact(calculateArtifact());
                break;
            case "BORDER":
                if (escape())
                    passMap();
                break;
        }
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
        this.stateManager.setTick(false);
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
        defender = null;
        tileMap.get("GROUND").addCoordinate("GROUND-" + groundIDAssigner.next(), fighter.getMobileID());

        for (HashMap.Entry<String, Tile> t : tileMap.entrySet()){
            if (t.getValue().getFigtherClassName().equalsIgnoreCase(fighter.getPlayerClassName())){
                for (HashMap.Entry<String, Coordinate> tObject : t.getValue().getCoordinates().entrySet()){
                    if (tObject.getValue() == fighter.getMobileID()) {
                        t.getValue().removeCoordinate(tObject.getKey());
                        break ;
                    }
                }
                break ;
            }
        }
        fighters.remove(fighter);
    }

    protected void gameOver(){
        this.gameOver = true;
    }
}