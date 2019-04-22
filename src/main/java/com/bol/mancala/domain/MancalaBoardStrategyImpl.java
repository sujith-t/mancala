
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
    
    private final Map<GameConstant, Object> gameResults;
    
    public MancalaBoardStrategyImpl(Player player, SessionModel session) {
        this.gameSession = session;
        this.currentPlayer = player;
        
        MoverModel currentMover = session.getMover(player.getUsername());
        this.currentPlayerTurn = currentMover.isMyTurn();
        List<MoverModel> movers = session.getMovers();
        
        this.currentPlayerScores = new HashMap<>();
        this.opponentScores = new HashMap<>();
        this.gameResults = new HashMap<>();
        
        for(MoverModel mover : movers) {
            String housePrefix = (mover.getHouse().equals(HOUSE_RED)) ? "R" : "B";
            
            for(int x = 1; x < 8; x++) {
                SquareModel square = mover.getSquare(housePrefix + x);
                this.startPosition = (square.isNextStart()) ? square.getId() : this.startPosition;
                
                if(mover.equals(currentMover)) {
                    this.currentPlayerScores.put(housePrefix + x, square.getPoints());
                    this.gameResults.put(GameConstant.MINE_HOUSE, mover.getHouse());
                    this.gameResults.put(GameConstant.MINE_NICKNAME, mover.getNickname());
                } else {
                    this.opponentScores.put(housePrefix + x, square.getPoints());
                    this.gameResults.put(GameConstant.OPPONENT_HOUSE, mover.getHouse());
                    this.gameResults.put(GameConstant.OPPONENT_NICKNAME, mover.getNickname());
                }
            }
        }
        
        if(this.startPosition != null) {
            this.gameResults.put(GameConstant.START_SQUARE, this.startPosition);
        }
        
    }
    
    @Override
    public void move() {
        if(!currentPlayerTurn || this.startPosition == null) {
            return;
        }

        String opponentPlayerNickname = (String)this.gameResults.get(GameConstant.OPPONENT_NICKNAME);
        MoverModel currentMover = this.gameSession.getMover(this.currentPlayer.getUsername());
        MoverModel opponentMover = this.gameSession.getMover(opponentPlayerNickname);
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
        
        /*int startPositionPoints = firstRunScores.get(this.startPosition);
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
            if(startPositionPoints == SCORE_ZERO) {
                
                this.startPosition = inx;
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
        }*/
        
        //adjustment for 2nd run square sets
        /*if(startPositionPoints > SCORE_ZERO) {
            startPrefix = secondRunMover.getHouse().toUpperCase().charAt(0);
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
                if(startPositionPoints == SCORE_ZERO) {
                    
                    this.startPosition = inx;
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
        }*/
 
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
   
        this.gameResults.put(GameConstant.START_SQUARE, this.startPosition);
        this.gameSession.setLastPlayed((new Date()).toString());
        this.gameResults.put(GameConstant.WINNER, winner);
        
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
        
        String opponentPlayerNickname = (String)this.gameResults.get(GameConstant.OPPONENT_NICKNAME);
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

            String opponentPlayerNickname = (String)this.gameResults.get(GameConstant.OPPONENT_NICKNAME);
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

        String opponentNickname = (String)this.gameResults.get(GameConstant.OPPONENT_NICKNAME);   
        boolean currentPlayerWinner = true;
        boolean opponentWinner = true;
        int currentPlayerReserve = 0;
        int opponentReserve = 0;
        
        for(int i = 1; i < 7; i++) {
            String housePrefix = ((String)this.gameResults.get(GameConstant.MINE_HOUSE)).toUpperCase().charAt(0) + "";
            int points = this.currentPlayerScores.get(housePrefix + i);
            currentPlayerReserve = this.currentPlayerScores.get(housePrefix + 7);
            
            if(currentPlayerWinner && points > 0) {
                currentPlayerWinner = false;
            }
            
            housePrefix = ((String)this.gameResults.get(GameConstant.OPPONENT_HOUSE)).toUpperCase().charAt(0) + "";
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
    public void initParameter(Map<GameConstant, Object> map) {
        this.startPosition = (map.containsKey(GameConstant.START_SQUARE)) ? 
                (String)map.get(GameConstant.START_SQUARE) : this.startPosition;
    }

    @Override
    public Map<GameConstant, Object> fetchResults() {
        this.gameResults.put(GameConstant.USER_TURN, this.currentPlayerTurn);
        
        //populate current user's result
        String housePrefix = (String)this.gameResults.get(GameConstant.MINE_HOUSE);
        if(housePrefix.equals(HOUSE_BLUE)) {
            housePrefix = "B";
        } else {
            housePrefix = "R";
        }
        
        List<Integer> scores = new ArrayList<>();
        
        scores.add(this.currentPlayerScores.get(housePrefix + 1));
        scores.add(this.currentPlayerScores.get(housePrefix + 2));
        scores.add(this.currentPlayerScores.get(housePrefix + 3));
        scores.add(this.currentPlayerScores.get(housePrefix + 4));
        scores.add(this.currentPlayerScores.get(housePrefix + 5));
        scores.add(this.currentPlayerScores.get(housePrefix + 6));
        this.gameResults.put(GameConstant.MINE_SCORES, scores);
        this.gameResults.put(GameConstant.MINE_RESERVE, this.currentPlayerScores.get(housePrefix + 7));
 
        //load opponent's scores
        housePrefix = (String)this.gameResults.get(GameConstant.OPPONENT_HOUSE);
        if(housePrefix.equals(HOUSE_BLUE)) {
            housePrefix = "B";
        } else {
            housePrefix = "R";
        }
        
        scores = new ArrayList<>();
        scores.add(this.opponentScores.get(housePrefix + 6));
        scores.add(this.opponentScores.get(housePrefix + 5));
        scores.add(this.opponentScores.get(housePrefix + 4));
        scores.add(this.opponentScores.get(housePrefix + 3));
        scores.add(this.opponentScores.get(housePrefix + 2));
        scores.add(this.opponentScores.get(housePrefix + 1));
        this.gameResults.put(GameConstant.OPPONENT_SCORES, scores);
        this.gameResults.put(GameConstant.OPPONENT_RESERVE, this.opponentScores.get(housePrefix + 7));
        this.gameResults.put(GameConstant.WINNER, this.determineWinner());
        return this.gameResults;
    }
    
}
