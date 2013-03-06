package equestria.canterlot.lunasdreamwalk.editor;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Element;
import equestria.canterlot.lunasdreamwalk.SimpleEditor;

/**
 * A panel which offers various methods to edit a pony's properties.
 * Currently only the "stars" can be changed.
 * 
 */
public class PonySettingsPanel extends JPanel implements InputElement {

    /**
     * 
     */
    private static final long serialVersionUID = -7120372141143324499L;
    private SimpleEditor editor;
    private String id;
    private JTextField ponyLevel;

    /**
     * Create a panel for the specified unique ponyId
     * 
     * @param editor
     *            The editor that this component is part of
     * @param ponyId
     *            the pony id
     */
    public PonySettingsPanel(SimpleEditor editor, String ponyId) {

        this.editor = editor;
        this.id = ponyId;

        this.createElements();

        this.reset();
    }

    private void createElements() {
        this.add(new JLabel("Level: "));
        this.add(this.ponyLevel = new JTextField("-1"));
    }

    public void apply() {
        int value = Integer.parseInt(ponyLevel.getText());

        Element e;
        try {
            e = editor.getXMLElementByString("/MLP_Save/MapZone/GameObjects/Pony_Objects/Object[@ID='" + id + "']/Game/Level");
            e.setAttribute("Level", Integer.toString(value));
        } catch(XPathExpressionException e1) {
            return;
        }

    }

    public void reset() {

        Element e;
        int level = -1;
        try {
            e = editor.getXMLElementByString("/MLP_Save/MapZone/GameObjects/Pony_Objects/Object[@ID='" + id + "']/Game/Level");

            level = Integer.parseInt(e.getAttribute("Level"));

        } catch(XPathExpressionException e1) {

        }

        this.ponyLevel.setText(Integer.toString(level));
    }

    public String name() {
        // Parse the Id into something more appropriate to show to the
        // user
        return id.replace("Pony_", "").replaceAll("_", " ");
    }

}
