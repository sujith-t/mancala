
package com.bol.mancala.test;

import com.bol.mancala.dao.XmlDaoImpl;
import com.bol.mancala.exception.ServiceException;
import com.bol.mancala.exception.XmlDaoException;
import com.bol.mancala.model.MoverModel;
import com.bol.mancala.model.PlayerModel;
import com.bol.mancala.model.SessionModel;
import com.bol.mancala.model.SquareModel;
import com.bol.mancala.service.GamingService;
import com.bol.mancala.service.GamingServiceImpl;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import static org.mockito.Mockito.when;

/**
 *
 * @author sujith
 */
public class GamingServiceImplTest {
    
    private GamingService service;
    
    @Before
    public void setUp() throws XmlDaoException, IOException { 
        this.service = new GamingServiceImpl(new XmlDaoImpl());
    }
    
    @After
    public void tearDown() {
        this.service = null;
        File file = new File("/tmp/mancala-gaming.xml");
        file.delete();        
    }

    /**
     * Test Instance
     */    
    @Test
    public void testInstance() {
        assertTrue(this.service instanceof GamingService);
    }
    
    /**
     * Test Save and Load Player
     * @throws ServiceException
     * @throws XmlDaoException
     */    
    @Test
    public void testSaveAndLoadPlayer() throws ServiceException, XmlDaoException {
        PlayerModel player = new PlayerModel();
        Random rand = new Random();
        player.setNickname("hello" + rand.nextInt(10000));
        player.setLastPlayed((new Date()).toString());
        this.service.savePlayer(player);
        
        Map<String, String> map = new HashMap<>();
        map.put("node", "player");
        map.put("attr", "nickname");
        map.put("attrValue", player.getNickname());
        
        PlayerModel retrieved = this.service.loadPlayer(player.getNickname());
        assertEquals(retrieved, player);
    }
    
    /**
     * Test List Players
     * @throws ServiceException
     */    
    @Test
    public void testListPlayers() throws ServiceException {
        List<PlayerModel> list = new ArrayList<>();
        
        for(int x = 0; x < 5; x++) {
            PlayerModel player = new PlayerModel();
            Random rand = new Random();
            player.setNickname("hello" + rand.nextInt(10000));
            player.setLastPlayed((new Date()).toString());
            this.service.savePlayer(player);  
            list.add(player);
        }
        
        List<PlayerModel> retrievalList = this.service.listPlayers();
        assertTrue(retrievalList.containsAll(list));
    } 

    /**
     * Test Save a game session and retrieve by id
     * @throws ServiceException
     */     
    @Test
    public void testSaveSessionAndLoadSessionById() throws ServiceException {
    
        String[] houses = {"red", "blue"};
        Random rand = new Random();
        int x = rand.nextInt(houses.length);
        String selectedHouse = houses[x];
        String opponentHouse = (selectedHouse.equals("red")) ? "blue" : "red";

        x = rand.nextInt(houses.length);
        SessionModel gameSession = new SessionModel();
        UUID uuid = UUID.randomUUID();
        gameSession.setId(uuid.toString());
        gameSession.setLastPlayed((new Date()).toString());
        
        MoverModel initiator = new MoverModel();
        initiator.setNickname("player-" + rand.nextInt(10000));
        initiator.setHouse(selectedHouse);
        initiator.markEngaged();
        
        MoverModel partner = new MoverModel();
        partner.setNickname("player-" + rand.nextInt(10000));
        partner.setHouse(opponentHouse);
        partner.markPending();
        
        if(x == 0) {
            initiator.markAsMyTurn();
            partner.markAsOpponentTurn();
        } else {
            initiator.markAsOpponentTurn();
            partner.markAsMyTurn();
        }
        
        for(int y = 1; y < 8; y++) {
            SquareModel inisq = new SquareModel();
            inisq.setId((selectedHouse.charAt(0) + "" + y).toUpperCase());
            x = (y == 7) ? 0 : 6;
            inisq.setPoints(x);
            
            SquareModel partsq = new SquareModel();
            partsq.setId((opponentHouse.charAt(0) + "" + y).toUpperCase());
            partsq.setPoints(x);
            
            initiator.addSquare(inisq);
            partner.addSquare(partsq);
        }
        
        gameSession.addMover(initiator);
        gameSession.addMover(partner);
        this.service.saveGameSession(gameSession);        
        
        SessionModel retrieved = this.service.loadSessionById(uuid.toString());
        assertEquals(retrieved, gameSession);
        
        //we update a session and test whether it works well
        partner.markEngaged();
        gameSession.setLastPlayed((new Date()).toString());
        gameSession.addMover(partner);
        this.service.saveGameSession(gameSession);
        retrieved = this.service.loadSessionById(uuid.toString());
        assertEquals(retrieved, gameSession);
    }
    
    /**
     * Test retrieve session by player
     * @throws ServiceException
     */     
    @Test
    public void testLoadSessionByPlayer() throws ServiceException {
    
        String[] houses = {"red", "blue"};
        Random rand = new Random();
        int x = rand.nextInt(houses.length);
        String selectedHouse = houses[x];
        String opponentHouse = (selectedHouse.equals("red")) ? "blue" : "red";

        x = rand.nextInt(houses.length);
        SessionModel gameSession = new SessionModel();
        UUID uuid = UUID.randomUUID();
        gameSession.setId(uuid.toString());
        gameSession.setLastPlayed((new Date()).toString());
        
        String playerToSearch = "player-" + rand.nextInt(10000);
        MoverModel initiator = new MoverModel();
        initiator.setNickname(playerToSearch);
        initiator.setHouse(selectedHouse);
        initiator.markEngaged();
        
        MoverModel partner = new MoverModel();
        partner.setNickname("player-" + rand.nextInt(10000));
        partner.setHouse(opponentHouse);
        partner.markPending();
        
        if(x == 0) {
            initiator.markAsMyTurn();
            partner.markAsOpponentTurn();
        } else {
            initiator.markAsOpponentTurn();
            partner.markAsMyTurn();
        }
        
        for(int y = 1; y < 8; y++) {
            SquareModel inisq = new SquareModel();
            inisq.setId((selectedHouse.charAt(0) + "" + y).toUpperCase());
            x = (y == 7) ? 0 : 6;
            inisq.setPoints(x);
            
            SquareModel partsq = new SquareModel();
            partsq.setId((opponentHouse.charAt(0) + "" + y).toUpperCase());
            partsq.setPoints(x);
            
            initiator.addSquare(inisq);
            partner.addSquare(partsq);
        }
        
        gameSession.addMover(initiator);
        gameSession.addMover(partner);
        this.service.saveGameSession(gameSession);        
        
        SessionModel retrieved = this.service.loadSessionByPlayer(playerToSearch);
        assertEquals(retrieved, gameSession);
    }    
    
    /**
     * Test Delete Game Session
     * @throws ServiceException
     */     
    @Test
    public void testDeleteSession() throws ServiceException {
    
        String[] houses = {"red", "blue"};
        Random rand = new Random();
        int x = rand.nextInt(houses.length);
        String selectedHouse = houses[x];
        String opponentHouse = (selectedHouse.equals("red")) ? "blue" : "red";

        x = rand.nextInt(houses.length);
        SessionModel gameSession = new SessionModel();
        UUID uuid = UUID.randomUUID();
        gameSession.setId(uuid.toString());
        gameSession.setLastPlayed((new Date()).toString());
        
        String playerToSearch = "player-" + rand.nextInt(10000);
        MoverModel initiator = new MoverModel();
        initiator.setNickname(playerToSearch);
        initiator.setHouse(selectedHouse);
        initiator.markEngaged();
        
        MoverModel partner = new MoverModel();
        partner.setNickname("player-" + rand.nextInt(10000));
        partner.setHouse(opponentHouse);
        partner.markPending();
        
        if(x == 0) {
            initiator.markAsMyTurn();
            partner.markAsOpponentTurn();
        } else {
            initiator.markAsOpponentTurn();
            partner.markAsMyTurn();
        }
        
        for(int y = 1; y < 8; y++) {
            SquareModel inisq = new SquareModel();
            inisq.setId((selectedHouse.charAt(0) + "" + y).toUpperCase());
            x = (y == 7) ? 0 : 6;
            inisq.setPoints(x);
            
            SquareModel partsq = new SquareModel();
            partsq.setId((opponentHouse.charAt(0) + "" + y).toUpperCase());
            partsq.setPoints(x);
            
            initiator.addSquare(inisq);
            partner.addSquare(partsq);
        }
        
        gameSession.addMover(initiator);
        gameSession.addMover(partner);
        this.service.saveGameSession(gameSession);        
        
        SessionModel retrieved = this.service.loadSessionById(uuid.toString());
        assertEquals(retrieved, gameSession);
        
        //now delete the session and retrieve back
        this.service.deleteGameSession(gameSession);
        retrieved = this.service.loadSessionById(uuid.toString());
        assertNotEquals(retrieved, gameSession);
        assertEquals(null, retrieved);
    }    
}
