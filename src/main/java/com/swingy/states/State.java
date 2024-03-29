package com.swingy.states;

import com.swingy.game.entities.Entity;

import java.awt.*;

public interface State{
    public void init();
    public State enterState(StateManager stateManager, State callingState);
    public void exitState();
    public String getName();
    public void tick(StateManager stateManager);
    public void render(Graphics graphics);
    public void addEntity(Entity entity);
}
