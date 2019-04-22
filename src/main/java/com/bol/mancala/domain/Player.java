
package com.bol.mancala.domain;

import java.util.Map;
import java.util.Objects;
import javax.ejb.Stateful;

/**
 *
 * @author sujith
 */
@Stateful
public class Player {
    
    private String username;
    private GameBoard gameBoardStrategy;

    public Player() {
        
    }

    public String getUsername() {
        return username;
    }

    public Player(String username) {
        this.username = username;
    }

    public void setGameBoard(GameBoard gameBoardStrategy) {
        this.gameBoardStrategy = gameBoardStrategy;
    }
    
    public GameBoard getGameBoard() {
        return this.gameBoardStrategy;
    }    

    public Map<GameConstant, Object> play() {
        this.gameBoardStrategy.move();
        return this.gameBoardStrategy.fetchResults();
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.username);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Player other = (Player) obj;
        if (!Objects.equals(this.username, other.username)) {
            return false;
        }
        return true;
    }
}
