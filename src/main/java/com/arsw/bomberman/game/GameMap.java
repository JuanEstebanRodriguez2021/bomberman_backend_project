package com.arsw.bomberman.game;

// Mapa del juego representado como una matriz.
// 0 = celda libre
// 1 = bloque indestructible (pared)
// 2 = bloque destructible
public class GameMap {

    public static final int FREE = 0;
    public static final int WALL = 1;
    public static final int BLOCK = 2;
    public static final int WIDTH = 13;
    public static final int HEIGHT = 11;
    private final int[][] grid;

    public GameMap() {
        this.grid = buildDefaultMap();
    }


    private int[][] buildDefaultMap() {
        int[][] map = new int[HEIGHT][WIDTH];

        for (int row = 0; row < HEIGHT; row++) {
            for (int col = 0; col < WIDTH; col++) {
                if (row % 2 == 1 && col % 2 == 1) {
                    map[row][col] = WALL; // paredes internas indestructibles
                } else if (isSafeCorner(row, col)) {
                    map[row][col] = FREE; // esquinas libres para los jugadores
                } else if (row == 0 || row == HEIGHT - 1 || col == 0 || col == WIDTH - 1) {
                    map[row][col] = WALL; // borde exterior
                } else {
                    map[row][col] = Math.random() < 0.6 ? BLOCK : FREE;
                }
            }
        }

        return map;
    }

    private boolean isSafeCorner(int row, int col) {
        return (row <= 1 && col <= 1) || (row <= 1 && col >= WIDTH - 2) || (row >= HEIGHT - 2 && col <= 1) || (row >= HEIGHT - 2 && col >= WIDTH - 2);
    }

    public int getCell(int row, int col) {
        if (row < 0 || row >= HEIGHT || col < 0 || col >= WIDTH) return WALL;
        return grid[row][col];
    }

    public boolean isWalkable(int row, int col) {
        return getCell(row, col) == FREE;
    }

    public void destroyBlock(int row, int col) {
        if (grid[row][col] == BLOCK) {
            grid[row][col] = FREE;
        }
    }

    public int[][] getGrid() { 
        return grid; 
    }
}