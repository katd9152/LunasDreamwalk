package equestria.canterlot.lunasdreamwalk.editor;

import javax.swing.JTextField;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Element;
import equestria.canterlot.lunasdreamwalk.SimpleEditor;

/**
 * The NumberInputField represents a single attribute of a single Node of
 * the underlying xml document. That attribute only accepts integers.
 * 
 */
public class IntegerInputField extends JTextField implements InputElement {

    /**
     * 
     */
    private static final long serialVersionUID = 1749170444373429282L;
    private SimpleEditor editor;
    private String name;
    private String xmlPath;
    private String attributeName;

    /**
     * @param editor
     *            The editor that this component is part of
     * @param name
     *            A human readable and understandable description of the
     *            to-be-edited attribute
     * @param nodeTag
     *            the tag of the to-be-edited node
     * @param nodeAttribute
     *            the attribute of that node that will be edited
     * @throws XPathExpressionException 
     */
    public IntegerInputField(SimpleEditor editor, String name, String xmlPath, String attributeName) throws XPathExpressionException {
        this.editor = editor;
        this.name = name;
        this.xmlPath = xmlPath;
        this.attributeName = attributeName;

        reset();
    }

    public void reset() throws XPathExpressionException {
        Element e = editor.getXMLElementByString(this.xmlPath);

        this.setText(e.getAttribute(this.attributeName));
    }

    public void apply() throws XPathExpressionException {

        Integer value = Integer.parseInt(this.getText());

        Element e = editor.getXMLElementByString(this.xmlPath);
        e.setAttribute(attributeName, Integer.toString(value));
    }

    public String name() {
        return this.name;
    }
}