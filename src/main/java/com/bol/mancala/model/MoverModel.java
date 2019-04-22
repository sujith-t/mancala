
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
public final class MoverModel extends Model {

    private String nickname;
    private String house;
    private String engaged;
    private boolean myTurn = false;
    
    private Element element;
    
    private final Map<String, SquareModel> squares;

    public MoverModel() {
        this.squares = new HashMap<>();
    }
    /**
     *
     * @param element
     */
    public MoverModel(Element element) {
        this.squares = new HashMap<>();
        this.toModel(element);
    }
    
    @Override
    public void toModel(Element element) {
        this.nickname = (element.hasAttribute("nickname")) ? element.getAttribute("nickname") : this.nickname;
        this.house = (element.hasAttribute("house")) ? element.getAttribute("house") : this.house;
        this.engaged = (element.hasAttribute("engaged")) ? element.getAttribute("engaged") : this.engaged;
        this.myTurn = (element.hasAttribute("playturn") && element.getAttribute("playturn").equals("true")) ? true : this.myTurn;
        
        //we expect 7 squares per mover (6 squares, 1 collector square)
        NodeList nlist = element.getElementsByTagName("square");
        if(nlist.getLength() > 0) {
            for(int x = 0; x < nlist.getLength(); x++) {
                Element node = (Element)nlist.item(x);
                SquareModel square = new SquareModel(node);
                this.squares.put(square.getId(), square);
            }
        }        
        
        this.element = element;        
    }

    @Override
    public Element toXmlNode() {
        if(this.element == null) {
            return null;
        }
        
        this.element.setAttribute("nickname", this.nickname);
        this.element.setAttribute("house", this.house);
        this.element.setAttribute("engaged", this.engaged);
        
        if(this.myTurn) {
            this.element.setAttribute("playturn", "true");
        } else {
            this.element.removeAttribute("playturn");
        }
        
        for(Map.Entry<String, SquareModel> item : this.squares.entrySet()) {
            Element node = item.getValue().toXmlNode();
            this.element.appendChild(node);
        }
        
        return this.element;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public String getEngaged() {
        return engaged;
    }
    
    public void markEngaged() {
        this.engaged = "true";
    }
    
    public void markPending() {
        this.engaged = "pending";
    }
    
    public SquareModel getSquare(String id) {
        return this.squares.get(id);
    }

    public void addSquare(SquareModel square) {
        this.squares.put(square.getId(), square);
    }    

    public List<SquareModel> getSquares() {
        List<SquareModel> list = new ArrayList<>(this.squares.values());
        return list;        
    }

    public boolean isMyTurn() {
        return myTurn;
    }

    public void markAsMyTurn() {
        this.myTurn = true;
    }
    
    public void markAsOpponentTurn() {
        this.myTurn = false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.nickname);
        hash = 97 * hash + Objects.hashCode(this.house);
        hash = 97 * hash + Objects.hashCode(this.engaged);
        hash = 97 * hash + Objects.hashCode(this.myTurn);
        hash = 97 * hash + Objects.hashCode(this.squares);
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
        final MoverModel other = (MoverModel) obj;
        if (!Objects.equals(this.nickname, other.nickname)) {
            return false;
        }
        if (!Objects.equals(this.house, other.house)) {
            return false;
        }
        if (!Objects.equals(this.engaged, other.engaged)) {
            return false;
        }
        if (!Objects.equals(this.myTurn, other.myTurn)) {
            return false;
        }
        if (!Objects.equals(this.squares, other.squares)) {
            return false;
        }
        return true;
    }
}