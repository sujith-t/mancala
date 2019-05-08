
package com.bol.mancala.test;

import com.bol.mancala.domain.GameBoard;
import com.bol.mancala.domain.GameConstant;
import com.bol.mancala.domain.MancalaBoardStrategyImpl;
import com.bol.mancala.domain.MancalaResult;
import com.bol.mancala.domain.Player;
import com.bol.mancala.model.MoverModel;
import com.bol.mancala.model.SessionModel;
import com.bol.mancala.model.SquareModel;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author sujith
 */
public class MancalaBoardStrategyImplTest {
    
    private Player player;
    private Player opponent;
    private SessionModel gameSession;
    private GameBoard mancalaBoard;
    
    @Before
    public void setUp() {
        Random rand = new Random();
        this.player = new Player("player-" + rand.nextInt(100000));
        this.opponent = new Player("opponent-" + rand.nextInt(100000));
        
        String selectedHouse = "red";
        String opponentHouse = "blue";

        this.gameSession = new SessionModel();
        UUID uuid = UUID.randomUUID();
        this.gameSession.setId(uuid.toString());
        this.gameSession.setLastPlayed((new Date()).toString());
        
        MoverModel initiator = new MoverModel();
        initiator.setNickname(this.player.getUsername());
        initiator.setHouse(selectedHouse);
        initiator.markEngaged();
        
        MoverModel partner = new MoverModel();
        partner.setNickname(this.opponent.getUsername());
        partner.setHouse(opponentHouse);
        partner.markEngaged();
        
        initiator.markAsMyTurn();
        partner.markAsOpponentTurn();
  
        for(int y = 1; y < 8; y++) {
            SquareModel inisq = new SquareModel();
            inisq.setId((selectedHouse.charAt(0) + "" + y).toUpperCase());
            int x = (y == 7) ? 0 : 6;
            inisq.setPoints(x);
            
            SquareModel partsq = new SquareModel();
            partsq.setId((opponentHouse.charAt(0) + "" + y).toUpperCase());
            partsq.setPoints(x);
            
            initiator.addSquare(inisq);
            partner.addSquare(partsq);
        }
        
        this.gameSession.addMover(initiator);
        this.gameSession.addMover(partner);
        
        //initialize mancala board
        this.mancalaBoard = new MancalaBoardStrategyImpl(this.player, this.gameSession);
        this.player.setGameBoard(this.mancalaBoard);
    }
    
    @After
    public void tearDown() {
        this.player = null;
        this.mancalaBoard = null;
        this.gameSession = null;
    }

    /**
     * Test Instance
     */    
    @Test
    public void testInstance() {
        assertTrue(this.mancalaBoard instanceof GameBoard);
        assertFalse(this.mancalaBoard == null);
    }
    
    /**
     * Test Whether The results change when start square is not selected by initiator
     */    
    @Test
    public void testResultsWhenStartSquareNotSelected() {
        String squareId = "R1";
        MoverModel initiator = this.gameSession.getMover(this.player.getUsername());
        int myScoreBeforePlay = initiator.getSquare(squareId).getPoints();
        this.player.play();
        int myScoreAfterPlay = initiator.getSquare(squareId).getPoints();
        assertEquals(myScoreBeforePlay, myScoreAfterPlay);
        
        Map<GameConstant, String> params = new HashMap<>();
        params.put(GameConstant.START_SQUARE, squareId);
        this.mancalaBoard.initParameter(params);
        this.player.play();
        myScoreAfterPlay = initiator.getSquare(squareId).getPoints();
        assertNotEquals(myScoreBeforePlay, myScoreAfterPlay);
    }  
    
    /**
     * Test whether the results change when trying to play during opponent's turn
     */    
    @Test
    public void testResultsWhenNotPlayersTurn() {
        String squareId = "R1";
        MoverModel initiator = this.gameSession.getMover(this.player.getUsername());
        int myScoreBeforePlay = initiator.getSquare(squareId).getPoints();
        initiator.markAsOpponentTurn();
        this.player.play();
        int myScoreAfterPlay = initiator.getSquare(squareId).getPoints();
        assertEquals(myScoreBeforePlay, myScoreAfterPlay);
        
        Map<GameConstant, String> params = new HashMap<>();
        params.put(GameConstant.START_SQUARE, squareId);
        this.mancalaBoard.initParameter(params);
        initiator.markAsMyTurn();
        this.player.play();
        myScoreAfterPlay = initiator.getSquare(squareId).getPoints();
        assertNotEquals(myScoreBeforePlay, myScoreAfterPlay);
    }

    /**
     * when current user is not engaged = pending the score can't change
     */    
    @Test
    public void testNoScoreChangeWhenPending() {
        String squareId = "R1";
        MoverModel initiator = this.gameSession.getMover(this.player.getUsername());
        int myScoreBeforePlay = initiator.getSquare(squareId).getPoints();
        initiator.markPending();
        this.player.play();
        int myScoreAfterPlay = initiator.getSquare(squareId).getPoints();
        assertEquals(myScoreBeforePlay, myScoreAfterPlay);
        
        Map<GameConstant, String> params = new HashMap<>();
        params.put(GameConstant.START_SQUARE, squareId);
        this.mancalaBoard.initParameter(params);
        initiator.markEngaged();
        this.player.play();
        myScoreAfterPlay = initiator.getSquare(squareId).getPoints();
        assertNotEquals(myScoreBeforePlay, myScoreAfterPlay);    
    }    
    
    /**
     * when opponent user is not engaged = pending the score can't change
     */    
    @Test
    public void testNoScoreChangeWhenPendingForOpponent() {
        String squareId = "B1";
        MoverModel tmpopponent = this.gameSession.getMover(this.opponent.getUsername());
        int myScoreBeforePlay = tmpopponent.getSquare(squareId).getPoints();
        tmpopponent.markPending();
        this.player.play();
        int myScoreAfterPlay = tmpopponent.getSquare(squareId).getPoints();
        assertEquals(myScoreBeforePlay, myScoreAfterPlay);
        
        Map<GameConstant, String> params = new HashMap<>();
        params.put(GameConstant.START_SQUARE, squareId);
        this.mancalaBoard.initParameter(params);
        tmpopponent.markEngaged();
        this.player.play();
        myScoreAfterPlay = tmpopponent.getSquare(squareId).getPoints();
        assertNotEquals(myScoreBeforePlay, myScoreAfterPlay);    
    } 
    
    /**
     * Test scores getting added correctly to the next ones and current one becomes 0
     */    
    @Test
    public void testScoresAdditions() {
        String squareId = "R1";
        MancalaResult result = (MancalaResult)this.mancalaBoard.fetchResults();

        List<Integer> scores = result.getMyScores();
        int reserve = result.getMyReserveScore();
        
        Map<GameConstant, String> params = new HashMap<>();
        params.put(GameConstant.START_SQUARE, squareId);
        this.mancalaBoard.initParameter(params);
        
        result = (MancalaResult)this.player.play();
        List<Integer> latestScores = result.getMyScores();
        int latestReserve = result.getMyReserveScore();
        
        for(int x = 0; x < 6; x++) {
            int score = scores.get(x);
            
            //R1 = 0
            if(x == 0) {
                assertNotEquals(0, score);
                assertEquals(0, latestScores.get(x).intValue());
            } else {
                assertEquals((score + 1), latestScores.get(x).intValue());
            }
        }
        
        assertEquals(0, reserve);
        assertEquals(1, latestReserve);
    } 
    
    /**
     * when the last piece lands into the reserve square, next start
     * would be user's choice which is null
     */    
    @Test
    public void testStartSquareMadeNull() {
        String squareId = "R1";
        MancalaResult result = (MancalaResult)this.mancalaBoard.fetchResults();
        assertEquals(null, result.getStartPosition());
        
        Map<GameConstant, String> params = new HashMap<>();
        params.put(GameConstant.START_SQUARE, squareId);
        this.mancalaBoard.initParameter(params);
        result = (MancalaResult)this.mancalaBoard.fetchResults();
        assertEquals(squareId, result.getStartPosition());
        
        result = (MancalaResult)this.player.play();
        assertEquals(null, result.getStartPosition());
    }
    
    /**
     * find the next starting square when having more points
     * start from R3
     */    
    @Test
    public void testWhenASquareHasMorePoints() {
        String squareToStart = "R3";
        int score = 15;
  
        MoverModel currentMover = this.gameSession.getMover(this.player.getUsername());
        SquareModel square = currentMover.getSquare(squareToStart);
        square.setPoints(score);
        square.markAsNextStart();
        currentMover.addSquare(square);
        this.gameSession.addMover(currentMover);  
        
        this.mancalaBoard = new MancalaBoardStrategyImpl(this.player, this.gameSession);
        MancalaResult resultBeforePlay = (MancalaResult)this.mancalaBoard.fetchResults();
        assertEquals(squareToStart, resultBeforePlay.getStartPosition());
        
        List<Integer> scores = resultBeforePlay.getMyScores();
        for(int x = 0; x < scores.size(); x++) {
            //R3 position
            if(x == 2) {
                assertEquals(score, scores.get(x).intValue());
            } else {
                assertEquals(6, scores.get(x).intValue());
            }
        }
        
        this.player.setGameBoard(this.mancalaBoard);
        MancalaResult resultAfterPlay = (MancalaResult)this.player.play();
        assertEquals("R5", resultAfterPlay.getStartPosition());
        
        List<Integer> latestScores = resultAfterPlay.getMyScores();
        for(int x = 0; x < latestScores.size(); x++) {
            //R3 position
            if(x == 2) {
                assertEquals(1, latestScores.get(x).intValue());
            } else if (x == 0 || x == 2 ) {
                assertEquals(1, latestScores.get(x).intValue());
            } else if (x == 3 || x == 4 ) {
                assertEquals(8, latestScores.get(x).intValue());
            } else {
                assertEquals(7, latestScores.get(x).intValue());
            }
        }        
    }  
    
    /**
     * current player looses his chance and opponent gets it
     * start from R3
     */    
    @Test
    public void testPlayerLoosesTurnAndOpponentGets() {
        String squareToStart = "R3";
        int score = 13;
  
        MoverModel currentMover = this.gameSession.getMover(this.player.getUsername());
        SquareModel square = currentMover.getSquare(squareToStart);
        square.setPoints(score);
        square.markAsNextStart();
        currentMover.addSquare(square);
        this.gameSession.addMover(currentMover);  
        
        this.mancalaBoard = new MancalaBoardStrategyImpl(this.player, this.gameSession);
        MancalaResult resultBeforePlay = (MancalaResult)this.mancalaBoard.fetchResults();
        assertEquals(squareToStart, resultBeforePlay.getStartPosition());
        assertTrue(resultBeforePlay.isCurrentPlayerTurn());
        
        this.player.setGameBoard(this.mancalaBoard);
        MancalaResult resultAfterPlay = (MancalaResult)this.player.play();
        assertEquals(null, resultAfterPlay.getStartPosition());
        assertFalse(resultAfterPlay.isCurrentPlayerTurn());
        
        List<Integer> latestScores = resultAfterPlay.getMyScores();
        assertEquals(1, latestScores.get(2).intValue());
    }    
}
