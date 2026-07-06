package com.arsw.bomberman.game;


public class Player {

    private final Long userId;
    private final String username;
    private int x;
    private int y;
    private boolean alive;
    private int bombsAvailable;

    public static final int MAX_BOMBS = 1; //  1 bomba a la vez

    public Player(Long userId, String username, int startX, int startY) {
        this.userId = userId;
        this.username = username;
        this.x = startX;
        this.y = startY;
        this.alive = true;
        this.bombsAvailable = MAX_BOMBS;
    }

    public Long getUserId(){ 
        return userId; 
    }

    public String getUsername() { 
        return username; 
    }

    public int getX() { 
        return x; 
    }

    public int getY() {
        return y; 
    }

    public boolean isAlive() { 
        return alive; 
    }

    public int getBombsAvailable() { 
        return bombsAvailable; 
    }
        
    public void setPosition(int x, int y) { 
        this.x = x; 
        this.y = y; 
    }

    public void eliminate() { 
        this.alive = false; 
    }

    public void useBomb() { 
        this.bombsAvailable--; 
    }

    public void returnBomb() { 
        this.bombsAvailable++; 
    }
}