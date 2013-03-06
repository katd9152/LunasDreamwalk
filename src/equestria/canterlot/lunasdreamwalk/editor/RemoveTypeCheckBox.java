package equestria.canterlot.lunasdreamwalk.editor;

import javax.swing.JCheckBox;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import equestria.canterlot.lunasdreamwalk.SimpleEditor;

/**
 * The RemoveTypeCheckBox will offer to remove all child nodes of a
 * specified node. It's name will also contain the number of nodes that
 * currently exist.
 * 
 */
public class RemoveTypeCheckBox extends JCheckBox implements InputElement {

    /**
     * 
     */
    private static final long serialVersionUID = 1749170444373429283L;
    private SimpleEditor editor;
    private String name;
    private String xmlPath;

    /**
     * @param editor
     *            The editor that this element is part of
     * @param name
     *            A short name for the elements that will be removed.
     * @param nodeTag
     *            The tag of the parent node that would get all its children
     *            removed
     * @throws XPathExpressionException 
     */
    public RemoveTypeCheckBox(SimpleEditor editor, String name, String xmlPath) throws XPathExpressionException {
        this.editor = editor;
        this.name = name;
        this.xmlPath = xmlPath;

        reset();
    }

    public void reset() throws XPathExpressionException {
        Element e = editor.getXMLElementByString(this.xmlPath);
        int amount = 0;

        NodeList nl = e.getChildNodes();
        amount = nl.getLength();

        this.name = "Remove " + amount + " " + this.name;

    }

    public void apply() throws XPathExpressionException {

        if(!this.isSelected()) {
            return;
        }

        Element e = editor.getXMLElementByString(this.xmlPath);
        
        Node child = null;
        while((child = e.getFirstChild()) != null) {
            e.removeChild(child);
        }
    }

    public String name() {
        return this.name;
    }
}
