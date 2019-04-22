
package com.bol.mancala.filter;

import com.bol.mancala.domain.Player;
import com.bol.mancala.exception.ServiceException;
import com.bol.mancala.model.MoverModel;
import com.bol.mancala.model.PlayerModel;
import com.bol.mancala.model.SessionModel;
import com.bol.mancala.service.GamingService;
import java.io.IOException;
import java.util.Date;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author sujith
 */
public class UserValidateFilter implements Filter {
    
    private static final boolean DEBUG = true;
    private final GamingService gamingService;

    private FilterConfig filterConfig = null;
    
    /**
     * Constructor
     * @param service
     */    
    @Inject
    public UserValidateFilter(GamingService service) {
        this.gamingService = service;
    }

    /**
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpResponse = (HttpServletResponse)response;
        HttpSession session = httpRequest.getSession(false);
        Cookie[] cookies = httpRequest.getCookies();
        Player loggedInUser = null;

        if(session != null) {
            loggedInUser = (Player)session.getAttribute("player");
        } 
        
        //we check in cookie
        if(loggedInUser == null && cookies != null) {
            for (Cookie cookie : cookies) {
                if(cookie.getName().equals("mancalaplayer")) {
                    
                    try {
                        loggedInUser = new Player(cookie.getValue());
                        session = httpRequest.getSession(true);
                        session.setAttribute("player", loggedInUser);

                        PlayerModel model = new PlayerModel();
                        model.setNickname(cookie.getValue());
                        model.setLastPlayed((new Date()).toString());
                        this.gamingService.savePlayer(model);
                        
                        log("UserValidateFilter: activating user from cookie");
                        break;
                    } catch (ServiceException ex) {
                        throw new ServletException("Writing to gaming-xml failed", ex);
                    }
                }
            }
        }
        
        String currentUrl = httpRequest.getRequestURL().toString();
        if(httpRequest.getQueryString() != null) {
            currentUrl = "?" + httpRequest.getQueryString();
        }
        
        request.setAttribute("url", currentUrl);
        
        try {
            String url = this.decideUserNavigationUrl(loggedInUser, currentUrl);
            if(url != null) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + url);
                return;
            }
            chain.doFilter(request, response);
        } catch (ServiceException ex) {
            throw new ServletException("Failed in finding navigation route for user", ex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sending to URLs
     * 
     * @returns String
     */    
    private String decideUserNavigationUrl(Player loggedInUser, String currentUrl) throws ServiceException {
        
        if(currentUrl.endsWith("xhtml") || currentUrl.endsWith("html") || currentUrl.endsWith("jsp") || currentUrl.endsWith("jspx")) {
            log("UserValidateFilter: attempt to go to direct xhtml/html/jsp/jsp pages");
            return "/";
        }
        
        if(loggedInUser == null && !currentUrl.endsWith("join") && !currentUrl.endsWith(".js") && !currentUrl.endsWith(".css")) {
            log("UserValidateFilter: Anonymous user hits " + currentUrl + " sending to /join");
            return "/join";
        }
        
        if(loggedInUser != null) {
            SessionModel playSession = this.gamingService.loadSessionByPlayer(loggedInUser.getUsername());
            if(playSession == null && !currentUrl.endsWith("invite") && !currentUrl.endsWith(".js") && !currentUrl.endsWith(".css")) {
                log("UserValidateFilter: " + loggedInUser.getUsername() + " user hits " + currentUrl + " sending to /invite");
                return "/invite";
            }
            
            if(playSession != null) {
                MoverModel currentMover = playSession.getMover(loggedInUser.getUsername());
                
                //someone already sent an invite
                if(currentMover.getEngaged().equals("pending") && !currentUrl.endsWith("decision") && !currentUrl.endsWith(".js") && !currentUrl.endsWith(".css")) {
                    log("UserValidateFilter: " + loggedInUser.getUsername()+ " user hits " + currentUrl + " sending to /invite?action=decision");
                    return "/invite?action=decision";
                }

                //someone already sent an invite
                if(currentMover.getEngaged().equals("true") && !currentUrl.endsWith("play") && !currentUrl.endsWith(".js") && !currentUrl.endsWith(".css")) {
                    log("UserValidateFilter: " + loggedInUser.getUsername() + " user hits " + currentUrl + " sending to /play");
                    return "/play";
                }                
            }
        }
        
        return null;
    }

    /**
     * Destroy method for this filter
     */
    @Override
    public void destroy() {      
        
    }

    /**
     * Init method for this filter
     * @param filterConfig
     */
    @Override
    public void init(FilterConfig filterConfig) {        
        this.filterConfig = filterConfig;
        if (filterConfig != null) {
            if (DEBUG) {                
                log("UserValidateFilter:Initializing filter");
            }
        }
    }
    
    /**
     * Message Logging
     * @param msg
     */    
    public void log(String msg) {
        filterConfig.getServletContext().log(msg);        
    }
}