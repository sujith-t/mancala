
package com.bol.mancala.model;

import java.util.Objects;
import org.w3c.dom.Element;

/**
 *
 * @author sujith
 */
public final class PlayerModel extends Model {

    private String nickname;
    private String lastPlayed;
    
    private Element element;
    
    public PlayerModel() {
        
    }
    
    public PlayerModel(Element element) {
        this.toModel(element);
    }
    
    @Override
    public void toModel(Element element) {
        this.nickname = (element.hasAttribute("nickname")) ? element.getAttribute("nickname") : this.nickname;
        this.lastPlayed = (element.hasAttribute("lastplayed")) ? element.getAttribute("lastplayed") : this.lastPlayed;
        this.element = element;
    }

    @Override
    public Element toXmlNode() {
        if(this.element == null) {
            return null;
        }
        
        this.element.setAttribute("nickname", this.nickname);
        this.element.setAttribute("lastplayed", this.lastPlayed);
        
        return this.element;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getLastPlayed() {
        return lastPlayed;
    }

    public void setLastPlayed(String lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.nickname);
        hash = 47 * hash + Objects.hashCode(this.lastPlayed);
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
        final PlayerModel other = (PlayerModel) obj;
        if (!Objects.equals(this.nickname, other.nickname)) {
            return false;
        }
        if (!Objects.equals(this.lastPlayed, other.lastPlayed)) {
            return false;
        }
        return true;
    }
}
