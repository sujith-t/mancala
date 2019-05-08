
package com.bol.mancala.domain;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author sujith
 */
public class MancalaResult implements PlayResult {
    
    private String myHouse;
    private String myNickname;
    private String opponentHouse;
    private String opponentNickname;
    private String startPosition;
    private String winner;
    private boolean currentPlayerTurn = false;
    private List<Integer> myScores;
    private int myReserveScore;
    private List<Integer> opponentScores;
    private int opponentReserveScore;

    public String getMyHouse() {
        return myHouse;
    }

    public void setMyHouse(String myHouse) {
        this.myHouse = myHouse;
    }

    public String getMyNickname() {
        return myNickname;
    }

    public void setMyNickname(String myNickname) {
        this.myNickname = myNickname;
    }

    public String getOpponentHouse() {
        return opponentHouse;
    }

    public void setOpponentHouse(String opponentHouse) {
        this.opponentHouse = opponentHouse;
    }

    public String getOpponentNickname() {
        return opponentNickname;
    }

    public void setOpponentNickname(String opponentNickname) {
        this.opponentNickname = opponentNickname;
    }

    public String getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(String startPosition) {
        this.startPosition = startPosition;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public boolean isCurrentPlayerTurn() {
        return currentPlayerTurn;
    }

    public void setCurrentPlayerTurn(boolean currentPlayerTurn) {
        this.currentPlayerTurn = currentPlayerTurn;
    }

    public List<Integer> getMyScores() {
        return myScores;
    }

    public void setMyScores(List<Integer> myScores) {
        this.myScores = myScores;
    }

    public int getMyReserveScore() {
        return myReserveScore;
    }

    public void setMyReserveScore(int myReserveScore) {
        this.myReserveScore = myReserveScore;
    }

    public List<Integer> getOpponentScores() {
        return opponentScores;
    }

    public void setOpponentScores(List<Integer> opponentScores) {
        this.opponentScores = opponentScores;
    }

    public int getOpponentReserveScore() {
        return opponentReserveScore;
    }

    public void setOpponentReserveScore(int opponentReserveScore) {
        this.opponentReserveScore = opponentReserveScore;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.myHouse);
        hash = 53 * hash + Objects.hashCode(this.myNickname);
        hash = 53 * hash + Objects.hashCode(this.opponentHouse);
        hash = 53 * hash + Objects.hashCode(this.opponentNickname);
        hash = 53 * hash + Objects.hashCode(this.startPosition);
        hash = 53 * hash + Objects.hashCode(this.winner);
        hash = 53 * hash + (this.currentPlayerTurn ? 1 : 0);
        hash = 53 * hash + Objects.hashCode(this.myScores);
        hash = 53 * hash + this.myReserveScore;
        hash = 53 * hash + Objects.hashCode(this.opponentScores);
        hash = 53 * hash + this.opponentReserveScore;
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
        final MancalaResult other = (MancalaResult) obj;
        if (this.currentPlayerTurn != other.currentPlayerTurn) {
            return false;
        }
        if (this.myReserveScore != other.myReserveScore) {
            return false;
        }
        if (this.opponentReserveScore != other.opponentReserveScore) {
            return false;
        }
        if (!Objects.equals(this.myHouse, other.myHouse)) {
            return false;
        }
        if (!Objects.equals(this.myNickname, other.myNickname)) {
            return false;
        }
        if (!Objects.equals(this.opponentHouse, other.opponentHouse)) {
            return false;
        }
        if (!Objects.equals(this.opponentNickname, other.opponentNickname)) {
            return false;
        }
        if (!Objects.equals(this.startPosition, other.startPosition)) {
            return false;
        }
        if (!Objects.equals(this.winner, other.winner)) {
            return false;
        }
        if (!Objects.equals(this.myScores, other.myScores)) {
            return false;
        }
        if (!Objects.equals(this.opponentScores, other.opponentScores)) {
            return false;
        }
        return true;
    }
}