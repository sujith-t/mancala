
package com.bol.mancala.managedbean;

import com.bol.mancala.domain.Player;
import com.bol.mancala.exception.ServiceException;
import com.bol.mancala.model.MoverModel;
import com.bol.mancala.model.PlayerModel;
import com.bol.mancala.model.SessionModel;
import com.bol.mancala.model.SquareModel;
import com.bol.mancala.service.GamingService;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.annotation.ManagedBean;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author sujith
 */
@Named(value = "invitationBean")
@RequestScoped
@ManagedBean
public class InvitationBean {

    private GamingService gamingService;
    private String nickname;
    
    /**
     * default constructor
     */
    public InvitationBean() {
        
    }
    
    /**
     * Creates a new instance of InvitationBean
     * @param service
     */
    @Inject
    public InvitationBean(GamingService service) {
        this.gamingService = service;
    }

    /**
     * Returns a list of available players
     * 
     * @return List<PlayerModel>
     * @throws ServiceException
     */    
    public List<PlayerModel> getAvailablePlayers() throws ServiceException {
        List<PlayerModel> list = this.gamingService.listPlayers();
        HttpServletRequest httpRequest = (HttpServletRequest)FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
        
        HttpSession session = httpRequest.getSession(false);
        Player player = (Player)session.getAttribute("player");
        
        for(int x = 0; x < list.size(); x++) {
            PlayerModel model = list.get(x);
            //if the loggedin use appears in the list avoid it
            if(player.getUsername().equals(model.getNickname())) {
                list.remove(x);
            }
            
            //avoid players who are already engaged(engaged/pending) in playing
            SessionModel playSession = this.gamingService.loadSessionByPlayer(model.getNickname());
            if(playSession != null) {
                MoverModel mover = playSession.getMover(model.getNickname());
                if(!mover.getEngaged().equals("false")) {
                    list.remove(x);
                }
            }
        }
        
        return list;
    }
    
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public void sendInvitation() throws ServiceException, IOException {
        String[] houses = {"red", "blue"};
        Random rand = new Random();
        int x = rand.nextInt(houses.length);
        String selectedHouse = houses[x];
        String opponentHouse = (selectedHouse.equals("red")) ? "blue" : "red";
        
        HttpServletRequest httpRequest = (HttpServletRequest)FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
        
        HttpSession session = httpRequest.getSession(false);
        Player player = (Player)session.getAttribute("player");
        SessionModel gameSession = this.gamingService.loadSessionByPlayer(player.getUsername());
        
        HttpServletResponse httpResponse = (HttpServletResponse)FacesContext.getCurrentInstance()
                .getExternalContext().getResponse();            
        if(gameSession != null) {
            httpResponse.sendRedirect(httpRequest.getContextPath());
        }

        x = rand.nextInt(houses.length);
        gameSession = new SessionModel();
        UUID uuid = UUID.randomUUID();
        gameSession.setId(uuid.toString());
        gameSession.setLastPlayed((new Date()).toString());
        
        MoverModel initiator = new MoverModel();
        initiator.setNickname(player.getUsername());
        initiator.setHouse(selectedHouse);
        initiator.markEngaged();
        
        MoverModel partner = new MoverModel();
        partner.setNickname(this.nickname);
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
        this.gamingService.saveGameSession(gameSession);
        
        httpResponse.sendRedirect(httpRequest.getContextPath() + "/play");
    }
}
