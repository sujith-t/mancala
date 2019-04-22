
package com.bol.mancala.model;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author sujith
 */
public final class SessionModel extends Model {
    
    private String id;
    private String lastPlayed;
    private final Map<String, MoverModel> movers;
    
    private Element element;
 
    /**
     * Constructor
     */    
    public SessionModel() {
        this.movers = new HashMap<>();
    }
 
    /**
     * Constructor
     * 
     * @param element
     */    
    public SessionModel(Element element) {
        this.movers = new HashMap<>();
        this.toModel(element);
    }    

    /**
     * Convert an element to a Model object
     * 
     * @param element
     */    
    @Override
    public void toModel(Element element) {
        this.id = (element.hasAttribute("id")) ? element.getAttribute("id") : this.id;
        this.lastPlayed = (element.hasAttribute("lastplayed")) ? element.getAttribute("lastplayed") : this.lastPlayed;
        
        //we expect 2 movers as 2 players playing
        NodeList nlist = element.getElementsByTagName("mover");
        if(nlist.getLength() > 0) {
            for(int x = 0; x < nlist.getLength(); x++) {
                Element node = (Element)nlist.item(x);
                MoverModel mover = new MoverModel(node);
                this.movers.put(mover.getNickname(), mover);
            }
        }
        
        this.element = element;        
    }

    /**
     * Returns XMLNode representation of Model
     * 
     * @return Element
     */    
    @Override
    public Element toXmlNode() {
        if(this.element == null) {
            return null;
        }
        
        this.element.setAttribute("id", this.id);
        this.element.setAttribute("lastplayed", this.lastPlayed);
        
        for(Map.Entry<String, MoverModel> item : this.movers.entrySet()) {
            Element node = item.getValue().toXmlNode();
            this.element.appendChild(node);
        }
        
        return this.element;
    }

    /**
     * Returns Id
     * 
     * @return String
     */    
    public String getId() {
        return id;
    }

    /**
     * Set Id
     * 
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get Last Played Time
     * 
     * @return String
     */    
    public String getLastPlayed() {
        return lastPlayed;
    }

    /**
     * Set Last Played Time
     * 
     * @param lastPlayed
     */    
    public void setLastPlayed(String lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    /**
     * Return Mover By Nickname
     * 
     * @param nickname
     * @return MoverModel
     */     
    public MoverModel getMover(String nickname) {
        return movers.get(nickname);
    }

    /**
     * Add a Mover
     * 
     * @param mover
     */     
    public void addMover(MoverModel mover) {
        this.movers.put(mover.getNickname(), mover);
    }

    /**
     * Returns List of Movers
     * 
     * @return List<MoverModel>
     */     
    public List<MoverModel> getMovers() {
        List<MoverModel> list = new ArrayList<>(this.movers.values());
        return list;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.id);
        hash = 59 * hash + Objects.hashCode(this.lastPlayed);
        hash = 59 * hash + Objects.hashCode(this.movers);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SessionModel other = (SessionModel) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.lastPlayed, other.lastPlayed)) {
            return false;
        }
        if (!Objects.equals(this.movers, other.movers)) {
            return false;
        }
        return true;
    }
}
