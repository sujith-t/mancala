
package com.bol.mancala.managedbean;

import com.bol.mancala.domain.Player;
import com.bol.mancala.exception.ServiceException;
import com.bol.mancala.model.MoverModel;
import com.bol.mancala.model.SessionModel;
import com.bol.mancala.service.GamingService;
import java.io.IOException;
import java.util.Date;
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
@Named(value = "matchBean")
@RequestScoped
@ManagedBean
public class MatchBean {

    private GamingService gamingService;
    private String initiator;

    private String inviteTime;
    private String receipient;
    private SessionModel gameSession;
    private boolean validInvitation = false;
    
    /**
     * Creates a new instance of MatchBean
     * @param service
     * @throws ServiceException
     */
    @Inject
    public MatchBean(GamingService service) throws ServiceException {
        HttpServletRequest httpRequest = (HttpServletRequest)FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
        
        HttpSession session = httpRequest.getSession(false);
        Player receiver = (Player)session.getAttribute("player");
        this.receipient = (receiver != null) ? receiver.getUsername() : this.receipient;
        
        this.gamingService = service;
        this.gameSession = this.gamingService.loadSessionByPlayer(receipient);
        
        if(this.gameSession != null && this.gameSession.getMovers().size() > 0) {
            this.inviteTime = this.gameSession.getLastPlayed();
            MoverModel recipientMover = this.gameSession.getMover(this.receipient);
            
            for(MoverModel mover : this.gameSession.getMovers()) {
                if(this.receipient != null && !mover.getNickname().equals(this.receipient) && 
                        recipientMover.getEngaged().equals("pending") && mover.getEngaged().equals("true")) {
                    this.initiator = mover.getNickname();
                    this.validInvitation = true;
                    break;
                }
            }
        }
    }

    /**
     * Returns Game Initiator
     * 
     * @return String
     */    
    public String getInitiator() {
        return initiator;
    }

    /**
     * Returns Game Invite Time
     * 
     * @return String
     */     
    public String getInviteTime() {
        return inviteTime;
    }

    /**
     * Returns Game Invitation Receiver
     * 
     * @return String
     */     
    public String getReceipient() {
        return receipient;
    }

    /**
     * Returns whether Invitation Valid
     * 
     * @return String
     */    
    public boolean isValidInvitation() {
        return validInvitation;
    }
    
    /**
     * Accepting Invitation
     * 
     * @throws ServiceException
     * @throws IOException
     */    
    public void accept() throws ServiceException, IOException {
        if(!this.validInvitation) {
            return;
        }
        
        MoverModel mover = this.gameSession.getMover(this.receipient);
        mover.markEngaged();
        this.gameSession.addMover(mover);
        this.gameSession.setLastPlayed((new Date()).toString());
        
        this.gamingService.saveGameSession(this.gameSession);
        
        HttpServletResponse httpResponse = (HttpServletResponse)FacesContext.getCurrentInstance()
                .getExternalContext().getResponse();        
        httpResponse.sendRedirect("../play");
    }
    
    /**
     * Declining Invitation
     * 
     * @throws ServiceException
     * @throws IOException
     */    
    public void decline() throws ServiceException, IOException {
        if(!this.validInvitation) {
            return;
        }  
        
        this.gamingService.deleteGameSession(this.gameSession);
        HttpServletResponse httpResponse = (HttpServletResponse)FacesContext.getCurrentInstance()
                .getExternalContext().getResponse();        
        httpResponse.sendRedirect("../invite");        
    }    
}
