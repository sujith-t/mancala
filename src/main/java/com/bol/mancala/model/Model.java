
package com.bol.mancala.model;

import org.w3c.dom.Element;

/**
 *
 * @author sujith
 */
public abstract class Model {
    
    public abstract void toModel(Element element);
    
    public abstract Element toXmlNode();
}
