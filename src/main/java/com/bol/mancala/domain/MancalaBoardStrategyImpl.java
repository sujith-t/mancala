
package com.bol.mancala.domain;

import com.bol.mancala.model.MoverModel;
import com.bol.mancala.model.SessionModel;
import com.bol.mancala.model.SquareModel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sujith
 */
public class MancalaBoardStrategyImpl implements GameBoard {

    private final SessionModel gameSession;
    private final Player currentPlayer;
    private boolean currentPlayerTurn = false;
    private String startPosition;
    private int loopCount = 0;
    
    private Map<String, Integer> currentPlayerScores;
    private Map<String, Integer> opponentScores;
    private final static int SCORE_ZERO = 0;
    private final static int SCORE_ONE = 1;
    private final static int POSITION_RESERVE = 7;
    private final static String HOUSE_RED = "red";
    private final static String HOUSE_BLUE = "blue";
    
    //private final Map<GameConstant, Object> gameResults;
    private final MancalaResult result;
    
    public MancalaBoardStrategyImpl(Player player, SessionModel session) {
        this.gameSession = session;
        this.currentPlayer = player;
        
        MoverModel currentMover = session.getMover(player.getUsername());
        this.currentPlayerTurn = currentMover.isMyTurn();
        List<MoverModel> movers = session.getMovers();
        
        this.currentPlayerScores = new HashMap<>();
        this.opponentScores = new HashMap<>();
        this.result = new MancalaResult();
        
        for(MoverModel mover : movers) {
            String housePrefix = mover.getHouse().toUpperCase().charAt(0) + "";
            
            for(int x = 1; x < 8; x++) {
                SquareModel square = mover.getSquare(housePrefix + x);
                this.startPosition = (square.isNextStart()) ? square.getId() : this.startPosition;
                
                if(mover.equals(currentMover)) {
                    this.currentPlayerScores.put(housePrefix + x, square.getPoints());
                    this.result.setMyHouse(mover.getHouse());
                    this.result.setMyNickname(mover.getNickname());
                } else {
                    this.opponentScores.put(housePrefix + x, square.getPoints());
                    this.result.setOpponentHouse(mover.getHouse());
                    this.result.setOpponentNickname(mover.getNickname());
                }
            }
        }
        
        if(this.startPosition != null) {
            this.result.setStartPosition(this.startPosition);
        }
        
    }
    
    @Override
    public void move() {
        String opponentPlayerNickname = this.result.getOpponentNickname();
        MoverModel currentMover = this.gameSession.getMover(this.currentPlayer.getUsername());
        MoverModel opponentMover = this.gameSession.getMover(opponentPlayerNickname);
        
        if(!currentPlayerTurn || this.startPosition == null || currentMover.getEngaged().equals("pending") || opponentMover.getEngaged().equals("pending")) {
            return;
        }

        MoverModel firstRunMover = currentMover;
        MoverModel secondRunMover = opponentMover;
        
        Map<String, Integer> firstRunScores = this.currentPlayerScores;
        Map<String, Integer> secondRunScores = this.opponentScores;
        boolean startOnSelfHouses = true;
        
        char startPrefix = this.startPosition.charAt(0);
        
        if(currentMover.getHouse().toUpperCase().charAt(0) != startPrefix) {
            firstRunScores = this.opponentScores;
            secondRunScores = this.currentPlayerScores;
            firstRunMover = opponentMover;
            secondRunMover = currentMover;
            startOnSelfHouses = false;
        }
 
        int points = firstRunScores.get(this.startPosition);
        while(points > 0) {
            
            points = this.runOnFirstSetOfSquares(firstRunMover, firstRunScores, startOnSelfHouses, points);
            points = this.runOnSecondSetOfSquares(secondRunMover, secondRunScores, startOnSelfHouses, points);

            //this means the loop is running many times
            this.loopCount = this.loopCount + 1;
        }
        
        //if a player wins, we mark the game as won
        String winner = this.determineWinner();
        if(winner != null) {
            SquareModel square = firstRunMover.getSquare(this.startPosition);
            if(square != null) {
                square.removeNextStart();
            }

            square = secondRunMover.getSquare(this.startPosition);
            if(square != null) {
                square.removeNextStart();
            }
            
            firstRunMover.markAsOpponentTurn();
            secondRunMover.markAsOpponentTurn();
            this.startPosition = null;
            this.currentPlayerTurn = false;
        }
   
        this.result.setStartPosition(this.startPosition);
        this.gameSession.setLastPlayed((new Date()).toString());
        this.result.setWinner(winner);
        
        //final update on the scoremaps
        if(startOnSelfHouses) {
            this.currentPlayerScores = firstRunScores;
            this.opponentScores = secondRunScores;
        } else {
            this.currentPlayerScores = secondRunScores;
            this.opponentScores = firstRunScores;
        }

        this.loopCount = 0;
    }

    private int runOnFirstSetOfSquares(MoverModel firstRunMover, Map<String, Integer> firstRunScores, boolean startOnSelfHouses, int startPositionPoints) {
 
        //when loop running more than 1ce stop index getting shifted
        String startPrefix = this.startPosition.charAt(0) + "";
        int startIndex = Integer.parseInt(this.startPosition.charAt(1) + "");  

        if(this.loopCount > 0) {
            startIndex = 0;
            startPrefix = (startPrefix.equals("R")) ? "B" : "R";
            this.startPosition = startPrefix + 1;
        }
        
        String opponentPlayerNickname = this.result.getOpponentNickname();
        MoverModel currentMover = this.gameSession.getMover(this.currentPlayer.getUsername());
        MoverModel opponentMover = this.gameSession.getMover(opponentPlayerNickname);        
        
        firstRunScores.put(this.startPosition, SCORE_ZERO);
        firstRunMover.getSquare(this.startPosition).setPoints(SCORE_ZERO);
        firstRunMover.getSquare(this.startPosition).removeNextStart();

        //adjusting the first run square sets
        for(int x = (startIndex + 1); x < 8; x++) {
            //if starting in opponent house and goes to their reserve position(7) don't add points 
            if(!startOnSelfHouses && x == POSITION_RESERVE) {
                continue;
            }
            
            String inx = startPrefix + "" + x;
            int points = firstRunMover.getSquare(inx).getPoints() + SCORE_ONE;
            firstRunScores.put(inx, points);
            firstRunMover.getSquare(inx).setPoints(points);
            firstRunMover.getSquare(inx).removeNextStart();
            
            startPositionPoints = startPositionPoints - SCORE_ONE;
            this.startPosition = inx;
            
            if(startPositionPoints == SCORE_ZERO) {
                //give current user another chance to start
                if(startOnSelfHouses && x == POSITION_RESERVE) {
                    this.startPosition = null;
                }
                
                //mark as starting position for the next play button hit
                if(this.startPosition != null && points > SCORE_ONE) {
                    firstRunMover.getSquare(inx).markAsNextStart();
                //on the final landing square when no score other than newly added one then current user's turn ends
                } else if(this.startPosition != null && points == SCORE_ONE) {
                    this.currentPlayerTurn = false;
                    this.startPosition = null;
                    
                    currentMover.markAsOpponentTurn();
                    opponentMover.markAsMyTurn();
                }
                break;
            }
        }
        
        //this would return remaining points after addding to the same row squares
        return startPositionPoints;
    }
    
    private int runOnSecondSetOfSquares(MoverModel secondRunMover, Map<String, Integer> secondRunScores, boolean startOnSelfHouses, int startPositionPoints) {
    
        //adjustment for 2nd run square sets
        if(startPositionPoints > SCORE_ZERO) {

            String opponentPlayerNickname = this.result.getOpponentNickname();
            MoverModel currentMover = this.gameSession.getMover(this.currentPlayer.getUsername());
            MoverModel opponentMover = this.gameSession.getMover(opponentPlayerNickname);  
        
            char startPrefix = secondRunMover.getHouse().toUpperCase().charAt(0);
            for(int x = 1; x < 8; x++) {
                //when start on own house and running on opposition houses for the 2nd round don't allow adding points to their reserve position(7)
                if(startOnSelfHouses && x == POSITION_RESERVE) {
                    continue;
                }
                
                String inx = startPrefix + "" + x;
                int points = secondRunMover.getSquare(inx).getPoints() + SCORE_ONE;
                secondRunScores.put(inx, points);
                secondRunMover.getSquare(inx).setPoints(points);
                secondRunMover.getSquare(inx).removeNextStart();
                
                startPositionPoints = startPositionPoints - SCORE_ONE;
                this.startPosition = inx;
                
                if(startPositionPoints == SCORE_ZERO) {
                    //current user landed on his own reserve, give another chance to start
                    if(!startOnSelfHouses && x == POSITION_RESERVE) {
                        this.startPosition = null;
                    }
                    
                    //mark as starting position for the next play button hit
                    if(this.startPosition != null && points > SCORE_ONE) {
                        secondRunMover.getSquare(inx).markAsNextStart();
                    //on the final landing square when no score other than newly added one then current user's turn ends
                    } else if(this.startPosition != null && points == SCORE_ONE) {
                        this.currentPlayerTurn = false;
                        this.startPosition = null;
                        
                        currentMover.markAsOpponentTurn();
                        opponentMover.markAsMyTurn();                        
                    }
                    break;
                }
            }
        }
        
        //return remaining points after running 1set + second set
        return startPositionPoints;
    }
    
    private String determineWinner() {

        String opponentNickname = this.result.getOpponentNickname();
        boolean currentPlayerWinner = true;
        boolean opponentWinner = true;
        int currentPlayerReserve = 0;
        int opponentReserve = 0;
        
        for(int i = 1; i < 7; i++) {
            String housePrefix = this.result.getMyHouse().toUpperCase().charAt(0) + "";
            int points = this.currentPlayerScores.get(housePrefix + i);
            currentPlayerReserve = this.currentPlayerScores.get(housePrefix + 7);
            
            if(currentPlayerWinner && points > 0) {
                currentPlayerWinner = false;
            }
            
            housePrefix = this.result.getOpponentHouse().toUpperCase().charAt(0) + "";
            points = this.opponentScores.get(housePrefix + i);
            opponentReserve = this.opponentScores.get(housePrefix + 7);
                    
            if(opponentWinner && points > 0) {
                opponentWinner = false;
            }            
        }
        
        if(currentPlayerWinner && !opponentWinner) {
            return this.currentPlayer.getUsername();
        }
        
        if(opponentWinner && !currentPlayerWinner) {
            return opponentNickname;
        }
        
        if(currentPlayerWinner && opponentWinner) {
            if(currentPlayerReserve > opponentReserve) {
                return this.currentPlayer.getUsername();
            } else if(opponentReserve > currentPlayerReserve) {
                return opponentNickname;
            } else {
                return "NONE";
            }
        }
        
        return null;
    }
    
    @Override
    public void initParameter(Map<GameConstant, String> map) {
        this.startPosition = (map.containsKey(GameConstant.START_SQUARE)) ? 
                map.get(GameConstant.START_SQUARE) : this.startPosition;
    }

    @Override
    public PlayResult fetchResults() {
        this.result.setCurrentPlayerTurn(this.currentPlayerTurn);
        this.result.setStartPosition(this.startPosition);
        
        //populate current user's result
        String housePrefix = this.result.getMyHouse().toUpperCase().charAt(0) + "";   
        List<Integer> scores = new ArrayList<>();
        for(int x = 1; x < 7; x++) {
            scores.add(this.currentPlayerScores.get(housePrefix + x));
        }
        
        this.result.setMyScores(scores);
        this.result.setMyReserveScore(this.currentPlayerScores.get(housePrefix + 7));
 
        //load opponent's scores
        housePrefix = this.result.getOpponentHouse().toUpperCase().charAt(0) + "";
        scores = new ArrayList<>();
        for(int x = 6; x > 0; x--) {
            scores.add(this.opponentScores.get(housePrefix + x));
        }
        
        this.result.setOpponentScores(scores);
        this.result.setOpponentReserveScore(this.opponentScores.get(housePrefix + 7));
        this.result.setWinner(this.determineWinner());
        
        return this.result;
    }
}