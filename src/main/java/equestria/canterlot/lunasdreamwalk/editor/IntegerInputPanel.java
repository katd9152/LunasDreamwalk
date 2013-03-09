package equestria.canterlot.lunasdreamwalk.editor;

import java.awt.Color;
import java.math.BigInteger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Element;
import equestria.canterlot.lunasdreamwalk.SimpleEditor;

/**
 * The NumberInputField represents a single attribute of a single Node of
 * the underlying xml document. That attribute only accepts integers.
 * 
 */
public class IntegerInputPanel extends JPanel implements InputElement {

    /**
     * 
     */
    private static final long serialVersionUID = 1749170444373429282L;
    private final SimpleEditor editor;
    private final JTextField input;
    private final String name;
    private final int min, max;
    private final BigInteger bMin, bMax;
    private final String xmlPath;
    private final String attributeName;

    /**
     * @param editor
     *            The editor that this component is part of
     * @param name
     *            A human readable and understandable description of the
     *            to-be-edited attribute
     * @param xmlPath
     *            the tag of the to-be-edited node
     * @param attributeName
     *            the attribute of that node that will be edited
     * @param min
     *            the minimum value for this field (too small values will be
     *            replaced by min)
     * @param max
     *            the maximum value for this field (too high values will be
     *            replaced by max)
     * @throws XPathExpressionException 
     */
    public IntegerInputPanel(SimpleEditor editor, String name, String xmlPath, String attributeName, int min, int max) throws XPathExpressionException {
        this.editor = editor;
        this.name = name;
        this.xmlPath = xmlPath;
        this.attributeName = attributeName;

        this.input = new JTextField();

        this.min = min;
        this.max = max;

        this.bMin = BigInteger.valueOf(min);
        this.bMax = BigInteger.valueOf(max);

        input.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                verify();
            }

            public void removeUpdate(DocumentEvent e) {
                verify();
            }

            public void changedUpdate(DocumentEvent e) {
                verify();
            }

        });

        this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        this.add(input);
        this.add(Box.createHorizontalStrut(10));
        this.add(new JLabel(min + " - " + max));
        reset();
    }

    private void verify() {
        String text = ((JTextField) input).getText();

        if(text.length() == 0) {
            return;
        }

        boolean valid = false;
        try {
            BigInteger value = new BigInteger(input.getText());
            valid = value.compareTo(bMin) >= 0 && value.compareTo(bMax) <= 0;
        } catch(NumberFormatException e) {
            // Not a number at all, replace with last input
            valid = false;
        }

        if(!valid)
            input.setForeground(Color.RED);
        else {
            input.setForeground(Color.BLACK);
        }
    }

    public void reset() throws XPathExpressionException {
        Element e = editor.getXMLElementByString(this.xmlPath);

        input.setText(e.getAttribute(this.attributeName));
    }

    public void apply() throws XPathExpressionException {

        int value = Integer.parseInt(input.getText());

        // sanity checks
        if(value < min) {
            throw new IllegalArgumentException("Value is " + value + ". Has to be > " + min);
        }

        if(value > max) {
            throw new IllegalArgumentException("Value is " + value + ". Has to be < " + max);
        }

        Element e = editor.getXMLElementByString(this.xmlPath);
        e.setAttribute(attributeName, Integer.toString(value));
    }

    public String name() {
        return this.name;
    }
}
