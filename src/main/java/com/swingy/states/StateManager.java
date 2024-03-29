package com.swingy.states;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class StateManager {

    private Map<String, State> map;
    private State currentState;
    private boolean tick = true;

    public StateManager(){
        map = new HashMap<String, State>();
    }

    public void addState(State state){
        map.put(state.getName().toUpperCase(), state);
        /*if (currentState == null){
            state.enterState(this, state);
            currentState = state;
        }*/
    }

    public State setState(String name, State callingState){
        setTick(false);
        State state = map.get(name.toUpperCase());
        if (state == null){
            System.err.println("State <" + name + "> does not exist");
            return callingState;
        }
        if (currentState != null)
            currentState.exitState();
        currentState = state;
        return state.enterState(this, callingState);
    }

    public void setTick(boolean tick) {
        this.tick = tick;
    }

    public void tick(){
        if (tick)
            currentState.tick(this);
    }

    public void render(Graphics graphics){
        currentState.render(graphics);
    }

}
