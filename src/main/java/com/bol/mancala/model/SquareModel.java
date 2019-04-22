
package com.bol.mancala.model;

import java.util.Objects;
import org.w3c.dom.Element;

/**
 *
 * @author sujith
 */
public final class SquareModel extends Model {

    private String id;
    private int points;
    private boolean nextStart = false;
    
    private Element element;

    public SquareModel() {
        
    }
    
    public SquareModel(Element element) {
        this.toModel(element);
    }
    
    @Override
    public void toModel(Element element) {
        this.id = (element.hasAttribute("id")) ? element.getAttribute("id") : this.id;
        this.points = (element.hasAttribute("points")) ? Integer.parseInt(element.getAttribute("points")) : this.points;
        this.nextStart = (element.hasAttribute("nextstart") && element.getAttribute("nextstart").equals("true")) ? true : this.nextStart;
        
        this.element = element;
    }

    @Override
    public Element toXmlNode() {
        if(this.element == null) {
            return null;
        }
        
        this.element.setAttribute("id", this.id);
        this.element.setAttribute("points", this.points + "");
        
        if(this.nextStart) {
            this.element.setAttribute("nextstart", "true");
        } else {
            this.element.removeAttribute("nextstart");
        }
                
        return this.element;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
    
    public boolean isNextStart() {
        return this.nextStart;
    }   
    
    public void markAsNextStart() {
        this.nextStart = true;
    }    
    
    public void removeNextStart() {
        this.nextStart = false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.id);
        hash = 41 * hash + this.points;
        hash = 41 * hash + (this.nextStart ? 1 : 0);
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
        final SquareModel other = (SquareModel) obj;
        if (this.points != other.points) {
            return false;
        }
        if (this.nextStart != other.nextStart) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
}