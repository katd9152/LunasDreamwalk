package equestria.canterlot.lunasdreamwalk.editor;

import javax.xml.xpath.XPathExpressionException;

/**
 * An InputElement is intended to show the current state of a part of the
 * underlying XML-Object and allow to declare specific changes to that part,
 * e.g. modifying values or removing nodes.
 * 
 * Those changes can then be applied when needed.
 * 
 */
public interface InputElement {

    /**
     * Apply changes to the underlying XML-Object
     */
    public void apply() throws XPathExpressionException;

    /**
     * Reset element with data from underlying XML-Object
     * @throws XPathExpressionException 
     */
    public void reset() throws XPathExpressionException;

    /**
     * Get an identifier that can be shown to the user in the UI and error
     * messages to identify this Input element
     * 
     * @return a name/very short description
     */
    public String name();
}