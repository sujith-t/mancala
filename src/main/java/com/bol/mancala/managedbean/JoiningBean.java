
package com.bol.mancala.managedbean;

import com.bol.mancala.domain.Player;
import com.bol.mancala.exception.ServiceException;
import com.bol.mancala.model.PlayerModel;
import com.bol.mancala.service.GamingService;
import java.io.IOException;
import java.util.Date;
import javax.annotation.ManagedBean;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author sujith
 */
@Named(value = "joiningBean")
@RequestScoped
@ManagedBean
public class JoiningBean {

    private String username;
    private final HttpSession session;
    private final GamingService gamingService;
    
    //5 years
    private final static int MAX_COOKIE_TIME = 3600 * 24 * 365 * 5;

    /**
     * Creates a new instance of JoiningBean
     * @param service
     */
    @Inject
    public JoiningBean(GamingService service) {
        HttpServletRequest httpRequest = (HttpServletRequest)FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
        
        this.session = httpRequest.getSession(true);
        this.gamingService = service;
    }

    /**
     * Validates Nickname
     * 
     * @param context
     * @param comp
     * @param value
     * @throws ServiceException
     */
    public void validateNickname(FacesContext context, UIComponent comp, Object value) throws ServiceException {
        String nickname = (String) value;
        nickname = nickname.trim();
        
        PlayerModel player = this.gamingService.loadPlayer(nickname);
        if(player != null) {
            ((UIInput) comp).setValid(false);
            FacesMessage message = new FacesMessage("The nickname is already taken, choose another");
            context.addMessage(comp.getClientId(context), message);
        }          
    }    

    /**
     * Return Username/Nickname
     * 
     * @return String
     */    
    public String getUsername() {
        return username;
    }

    /**
     * Set Username/Nickname
     * 
     * @param username
     */    
    public void setUsername(String username) {
        this.username = username.trim();
    }    

    /**
     * Save user who wishes to join the game
     * 
     * @throws ServiceException
     * @throws IOException
     */    
    public void saveJoiner() throws ServiceException, IOException {
    
        Player player = new Player(this.username);
        
        HttpServletResponse httpResponse = (HttpServletResponse)FacesContext.getCurrentInstance()
                .getExternalContext().getResponse();

        PlayerModel model = new PlayerModel();
        model.setNickname(this.username);
        model.setLastPlayed((new Date()).toString());
        this.gamingService.savePlayer(model);
        
        this.session.setAttribute("player", player);
        Cookie cookie = new Cookie("mancalaplayer", this.username);
        cookie.setMaxAge(MAX_COOKIE_TIME);
        httpResponse.addCookie(cookie);
        httpResponse.sendRedirect("../invite");        
    }
}
