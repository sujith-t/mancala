
package com.bol.mancala.managedbean;

import com.bol.mancala.domain.GameBoard;
import com.bol.mancala.domain.GameConstant;
import com.bol.mancala.domain.MancalaBoardStrategyImpl;
import com.bol.mancala.domain.Player;
import com.bol.mancala.exception.ServiceException;
import com.bol.mancala.model.MoverModel;
import com.bol.mancala.model.PlayerModel;
import com.bol.mancala.model.SessionModel;
import com.bol.mancala.model.SquareModel;
import com.bol.mancala.service.GamingService;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.annotation.ManagedBean;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author sujith
 */
@Named(value = "boardBean")
@RequestScoped
@ManagedBean
public class MancalaGameBoardBean {
 
    private final GamingService gamingService;
    private final Player currentPlayer;
    private SessionModel playSession;
    private final HttpServletRequest httpRequest;
    private Map<GameConstant, Object> result;
    
    @Inject
    public MancalaGameBoardBean(GamingService service) throws ServiceException {
        this.gamingService = service;
        this.httpRequest = (HttpServletRequest)FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
        
        HttpSession session = this.httpRequest.getSession(false);
        this.currentPlayer = (Player)session.getAttribute("player");
        this.playSession = service.loadSessionByPlayer(this.currentPlayer.getUsername());
        
        GameBoard mancalaBoard = new MancalaBoardStrategyImpl(this.currentPlayer, this.playSession);
        this.currentPlayer.setGameBoard(mancalaBoard);
        this.result = mancalaBoard.fetchResults();
    }
    
    public boolean getIsUserTurn() {
        return (boolean)this.result.get(GameConstant.USER_TURN);
    }
    
    @SuppressWarnings("unchecked")
    public List<Integer> getPlayerScores() {
        return (List<Integer>) this.result.get(GameConstant.MINE_SCORES);
    }
    
    @SuppressWarnings("unchecked")
    public List<Integer> getOpponentScores() {
        return (List<Integer>) this.result.get(GameConstant.OPPONENT_SCORES);
    }

    public int getPlayerReserveScore() {
        return (int) this.result.get(GameConstant.MINE_RESERVE);
    }
    
    public int getOpponentReserveScore() {
        return (int) this.result.get(GameConstant.OPPONENT_RESERVE);
    }

    public String getPlayerHouse() {
        return (String) this.result.get(GameConstant.MINE_HOUSE);
    }
    
    public String getPlayerHousePrefix() {
        return this.getPlayerHouse().toUpperCase().charAt(0) + "";
    }    
    
    public String getOpponentHouse() {
        return (String) this.result.get(GameConstant.OPPONENT_HOUSE);
    }  
    
    public String getOpponentHousePrefix() {
        return this.getOpponentHouse().toUpperCase().charAt(0) + "";
    }  
    
    public String getStartSquare() {
        return (String) this.result.get(GameConstant.START_SQUARE);
    }
    
    public String getPlayerNickname() {
        return (String) this.result.get(GameConstant.MINE_NICKNAME);
    } 
    
    public String getOpponentNickname() {
        return (String) this.result.get(GameConstant.OPPONENT_NICKNAME);
    }  
    
    public String getWinner() {
        return (String) this.result.get(GameConstant.WINNER);
    }    
    
    public void playGame() throws ServiceException {
        if(this.currentPlayer == null || this.playSession == null) {
            return;
        }  
        
        String selectedSquareId = this.httpRequest.getParameter("squareId");
        if(selectedSquareId != null) {
            Map<GameConstant, Object> map = new HashMap<>();
            map.put(GameConstant.START_SQUARE, selectedSquareId);
            PlayerModel playerModel = this.gamingService.loadPlayer(this.currentPlayer.getUsername());
            
            this.currentPlayer.getGameBoard().initParameter(map);
            this.result = this.currentPlayer.play();
            playerModel.setLastPlayed((new Date()).toString());
            
            this.gamingService.saveGameSession(this.playSession);
            this.gamingService.savePlayer(playerModel);
        }    
    }
    
    public void decline() throws ServiceException {
        this.gamingService.deleteGameSession(this.playSession);
    }
    
    public void replayAgain() throws ServiceException, IOException {
        this.gamingService.deleteGameSession(this.playSession);
        
        String[] houses = {"red", "blue"};
        Random rand = new Random();
        int x = rand.nextInt(houses.length);
        String selectedHouse = houses[x];
        String opponentHouse = (selectedHouse.equals("red")) ? "blue" : "red";
        
        HttpServletResponse httpResponse = (HttpServletResponse)FacesContext.getCurrentInstance()
                .getExternalContext().getResponse();

        x = rand.nextInt(houses.length);
        this.playSession = new SessionModel();
        UUID uuid = UUID.randomUUID();
        this.playSession.setId(uuid.toString());
        this.playSession.setLastPlayed((new Date()).toString());
        
        MoverModel initiator = new MoverModel();
        initiator.setNickname(this.currentPlayer.getUsername());
        initiator.setHouse(selectedHouse);
        initiator.markEngaged();
        
        MoverModel partner = new MoverModel();
        partner.setNickname(this.getOpponentNickname());
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
        
        this.playSession.addMover(initiator);
        this.playSession.addMover(partner);
        this.gamingService.saveGameSession(this.playSession);
        
        httpResponse.sendRedirect(httpRequest.getContextPath() + "/play");        
    }
}