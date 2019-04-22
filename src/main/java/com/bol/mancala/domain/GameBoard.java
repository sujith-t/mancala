
package com.bol.mancala.domain;

import java.util.Map;

/**
 *
 * @author sujith
 */
public interface GameBoard {
    
    public void initParameter(Map<GameConstant, Object> map);
    
    public void move();
    
    public Map<GameConstant, Object> fetchResults();
}
