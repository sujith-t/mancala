
package com.bol.mancala.test;

import com.bol.mancala.domain.GameBoard;
import com.bol.mancala.domain.GameConstant;
import com.bol.mancala.domain.MancalaBoardStrategyImpl;
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
        
        Map<GameConstant, Object> params = new HashMap<>();
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
        
        Map<GameConstant, Object> params = new HashMap<>();
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
        
        Map<GameConstant, Object> params = new HashMap<>();
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
        
        Map<GameConstant, Object> params = new HashMap<>();
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
        Map<GameConstant, Object> result = this.mancalaBoard.fetchResults();
        @SuppressWarnings("unchecked")
        List<Integer> scores = (List<Integer>)result.get(GameConstant.MINE_SCORES);
        int reserve = (Integer)result.get(GameConstant.MINE_RESERVE);
        
        Map<GameConstant, Object> params = new HashMap<>();
        params.put(GameConstant.START_SQUARE, squareId);
        this.mancalaBoard.initParameter(params);
        
        result = this.player.play();
        @SuppressWarnings("unchecked")
        List<Integer> latestScores = (List<Integer>)result.get(GameConstant.MINE_SCORES);
        int latestReserve = (Integer)result.get(GameConstant.MINE_RESERVE);
        
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
        Map<GameConstant, Object> result = this.mancalaBoard.fetchResults();
        assertEquals(null, result.get(GameConstant.START_SQUARE));
        
        Map<GameConstant, Object> params = new HashMap<>();
        params.put(GameConstant.START_SQUARE, squareId);
        this.mancalaBoard.initParameter(params);
        result = this.mancalaBoard.fetchResults();
        assertEquals(squareId, (String)result.get(GameConstant.START_SQUARE));
        
        result = this.player.play();
        assertEquals(null, result.get(GameConstant.START_SQUARE));
    }   
}
