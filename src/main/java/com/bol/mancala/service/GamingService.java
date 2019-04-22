
package com.bol.mancala.service;

import com.bol.mancala.exception.ServiceException;
import com.bol.mancala.model.PlayerModel;
import com.bol.mancala.model.SessionModel;
import java.util.List;

/**
 *
 * @author sujith
 */
public interface GamingService {
    
    public void savePlayer(PlayerModel player) throws ServiceException;
    
    public PlayerModel loadPlayer(String nickname) throws ServiceException;
    
    public List<PlayerModel> listPlayers() throws ServiceException;
    
    public SessionModel loadSessionById(String id) throws ServiceException;
    
    public SessionModel loadSessionByPlayer(String nickname) throws ServiceException;
    
    public void saveGameSession(SessionModel model) throws ServiceException;
    
    public void deleteGameSession(SessionModel model) throws ServiceException;
}
