
package com.bol.mancala.domain;

import java.util.Map;

/**
 *
 * @author sujith
 */
public interface GameBoard {
    
    public void initParameter(Map<GameConstant, String> map);
    
    public void move();
    
    public PlayResult fetchResults();
}
