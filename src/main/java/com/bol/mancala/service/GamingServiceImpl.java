
package com.bol.mancala.service;

import com.bol.mancala.dao.XmlDao;
import com.bol.mancala.exception.ServiceException;
import com.bol.mancala.exception.XmlDaoException;
import com.bol.mancala.model.MoverModel;
import com.bol.mancala.model.PlayerModel;
import com.bol.mancala.model.SessionModel;
import com.bol.mancala.model.SquareModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.DependsOn;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.inject.Inject;
import org.w3c.dom.Element;

/**
 *
 * @author sujith
 */
@Local
@Singleton
@DependsOn({"XmlDaoImpl"})
public class GamingServiceImpl implements GamingService {

    private XmlDao xmlDao;

    /**
     * constructor
     */
    public GamingServiceImpl() {
        
    }

    /**
     * constructor
     * @param dao
     */
    @Inject
    public GamingServiceImpl(XmlDao dao) {
        this.xmlDao = dao;
    }

    /**
     * Saving Player
     * @param player
     * @throws ServiceException
     */    
    @Override
    public void savePlayer(PlayerModel player) throws ServiceException {
        
        if(player.toXmlNode() == null) {           
            PlayerModel existingPlayer = this.loadPlayer(player.getNickname());
            if(existingPlayer != null) {
                return;
            }
            
            Element element = this.xmlDao.createElement("player");
            player.toModel(element);
        }
        
        try {
            Element element = player.toXmlNode();
            this.xmlDao.commit(element, "SAVE");
        } catch (XmlDaoException ex) {
            throw new ServiceException("Saving the player failed in the gaming xml", ex);
        }
    }

    @Override
    public void saveGameSession(SessionModel model) throws ServiceException {
        if(model.toXmlNode() == null) {    
            SessionModel existingSession = this.loadSessionById(model.getId());
            if(existingSession != null) {
                return;
            }
            
            Element nsession = this.xmlDao.createElement("session");
            for(MoverModel mover: model.getMovers()) {
                
                for(SquareModel square : mover.getSquares()) {
                    Element nsquare = this.xmlDao.createElement("square");
                    square.toModel(nsquare);
                    mover.addSquare(square);
                }
                
                Element nmover = this.xmlDao.createElement("mover");
                mover.toModel(nmover);
                model.addMover(mover);
            }
            model.toModel(nsession);
        }
        
        try {
            Element element = model.toXmlNode();
            this.xmlDao.commit(element, "SAVE");
        } catch (XmlDaoException ex) {
            throw new ServiceException("Saving the game session failed in the gaming xml", ex);
        }        
    }

    @Override
    public void deleteGameSession(SessionModel model) throws ServiceException {
        try {
            Element element = model.toXmlNode();
            if(element != null) {
                this.xmlDao.commit(element, "DELETE");
            }
        } catch (XmlDaoException ex) {
            throw new ServiceException("Deleting the game session failed in the gaming xml", ex);
        }
    }

    /**
     * Return a player by nickname
     * @param nickname
     * @return PlayerModel
     * @throws ServiceException
     */    
    @Override
    public PlayerModel loadPlayer(String nickname) throws ServiceException {

        try {
            Map<String, String> map = new HashMap<>();
            map.put("node", "player");
            map.put("attr", "nickname");
            map.put("attrValue", nickname);
            
            List<PlayerModel> list = this.xmlDao.fetchAll(PlayerModel.class, map);
            if(list.size() > 0) {
                return list.get(0);
            }
        } catch(XmlDaoException e) {
            throw new ServiceException("Unable to retrieve player by nickname", e);
        }
        
        return null;
    }

    /**
     * Return a list of players logged in
     * 
     * @return List<PlayerModel>
     * @throws ServiceException
     */     
    @Override
    public List<PlayerModel> listPlayers() throws ServiceException {
        
        try {
            Map<String, String> map = new HashMap<>();
            map.put("node", "player");
            
            return this.xmlDao.fetchAll(PlayerModel.class, map);

        } catch(XmlDaoException e) {
            throw new ServiceException("Unable to retrieve all players", e);
        }     
    }

    /**
     * Return Session By Session Id
     * 
     * @param id
     * @return SessionModel
     * @throws ServiceException
     */    
    @Override
    public SessionModel loadSessionById(String id) throws ServiceException {
        
        try {
            Map<String, String> map = new HashMap<>();
            map.put("node", "session");
            map.put("attr", "id");
            map.put("attrValue", id);
            
            List<SessionModel> list = this.xmlDao.fetchAll(SessionModel.class, map);
            if(list.size() > 0) {
                return list.get(0);
            }
        } catch(XmlDaoException e) {
            throw new ServiceException("Unable to retrieve player by nickname", e);
        }
        
        return null;        
    }

    /**
     * Return Session By user nickname/username
     * 
     * @param nickname
     * @return SessionModel
     * @throws ServiceException
     */     
    @Override
    public SessionModel loadSessionByPlayer(String nickname) throws ServiceException {
        try {
            Map<String, String> map = new HashMap<>();
            map.put("node", "mover");
            map.put("attr", "nickname");
            map.put("attrValue", nickname);
            
            List<MoverModel> list = this.xmlDao.fetchAll(MoverModel.class, map);
            if(list.size() > 0) {
                MoverModel model = list.get(0);
                Element element = (Element)model.toXmlNode().getParentNode();
                
                if(element.getNodeName().equals("session")) {
                    return new SessionModel(element);
                }
            }
        } catch(XmlDaoException e) {
            throw new ServiceException("Unable to retrieve player by nickname", e);
        }
        
        return null;
    }
}
