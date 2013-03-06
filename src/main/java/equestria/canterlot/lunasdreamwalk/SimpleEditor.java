package equestria.canterlot.lunasdreamwalk;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import equestria.canterlot.lunasdreamwalk.editor.InputElement;
import equestria.canterlot.lunasdreamwalk.editor.IntegerInputField;
import equestria.canterlot.lunasdreamwalk.editor.PonySettingsPanel;
import equestria.canterlot.lunasdreamwalk.editor.RemoveTypeCheckBox;
import equestria.canterlot.lunasdreamwalk.editor.SimpleEditorTab;

public class SimpleEditor extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 8589145875345431936L;
    private Document xmlDocument;
    private List<InputElement> inputs = new ArrayList<InputElement>();

    /**
     * The simple editor works with an String that represents an xml tree.
     * 
     * @param xmlContent
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public SimpleEditor(String xmlContent) throws ParserConfigurationException, SAXException, IOException {

        this.xmlDocument = this.parseSavegame(xmlContent);

        createElements();

    }

    private List<String> getPonyIdList() {

        List<String> ponies = new ArrayList<String>();
        try {
            NodeList e = getXMLElementsByString("/MLP_Save/MapZone/GameObjects/Pony_Objects/Object[@ID]");

            for(int i = 0; i < e.getLength(); i++) {
                ponies.add(e.item(i).getAttributes().getNamedItem("ID").getNodeValue());
            }
        } catch(XPathExpressionException e1) {
            return null;
        }

        // Sort by name
        Collections.sort(ponies);
        return ponies;
    }

    /**
     * Set up the GUI elements
     */
    private void createElements() {

        JTabbedPane tabPane = new JTabbedPane();

        SimpleEditorTab playerTab = new SimpleEditorTab();
        SimpleEditorTab mapTab = new SimpleEditorTab();
        SimpleEditorTab ponyTab = new SimpleEditorTab();

        tabPane.addTab("Player", playerTab);
        tabPane.addTab("Map", mapTab);
        tabPane.addTab("Ponies", ponyTab);

        String pdata = "/MLP_Save/PlayerData";
        String sdata = "/MLP_Save/PlayerData/Shards";
        String clearable = "/MLP_Save/MapZone[@ID='0']/GameObjects/Clearable_Objects";
        String parasprites = "/MLP_Save/MapZone[@ID='0']/GameObjects/Parasprite_Objects";

        try {
            addInputElement(playerTab, new IntegerInputField(this, "Coins", pdata, "Coins"));
            addInputElement(playerTab, new IntegerInputField(this, "Gems", pdata, "Hearts"));
            addInputElement(playerTab, new IntegerInputField(this, "Hearts", pdata, "Social"));
            addInputElement(playerTab, new IntegerInputField(this, "Loyalty Shards", sdata, "Loyalty"));
            addInputElement(playerTab, new IntegerInputField(this, "Kindness Shards", sdata, "Kindness"));
            addInputElement(playerTab, new IntegerInputField(this, "Honesty Shards", sdata, "Honesty"));
            addInputElement(playerTab, new IntegerInputField(this, "Generosity Shards", sdata, "Generosity"));
            addInputElement(playerTab, new IntegerInputField(this, "Laughter Shards", sdata, "Laughter"));
            addInputElement(playerTab, new IntegerInputField(this, "Magic Shards", sdata, "Magic"));

            addInputElement(mapTab, new RemoveTypeCheckBox(this, "Rubble/Stones/Roots", clearable));
            addInputElement(mapTab, new RemoveTypeCheckBox(this, "Parasprites", parasprites));

            List<String> ponies = getPonyIdList();

            for(String pony : ponies) {
                addInputElement(ponyTab, new PonySettingsPanel(this, pony));
            }

        } catch(XPathExpressionException e) {
            e.printStackTrace();
        }
        this.setLayout(new BorderLayout());
        this.add(tabPane);
    }

    private void addInputElement(SimpleEditorTab tab, JComponent element) {
        String name = "";
        if(element instanceof InputElement) {
            name = ((InputElement) element).name();
            this.inputs.add((InputElement) element);
        }
        tab.addLine(name + ":", element);
    }

    public Element getXMLElementByString(String location) throws XPathExpressionException {

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        XPathExpression expr = xpath.compile(location);
        Element result = (Element) expr.evaluate(xmlDocument, XPathConstants.NODE);
        return result;
    }

    public NodeList getXMLElementsByString(String location) throws XPathExpressionException {

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        XPathExpression expr = xpath.compile(location);
        NodeList result = (NodeList) expr.evaluate(xmlDocument, XPathConstants.NODESET);
        return result;
    }

    /**
     * Convert a String into an xml Document
     * 
     * @param xml
     *            The String that will be converted
     * @return the Document
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private Document parseSavegame(String xml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        return builder.parse(new InputSource(new StringReader(xml.trim())));
    }

    /**
     * Convert the content of the xml Document to a String
     * 
     * @return the content as String
     * @throws TransformerException
     */
    public String generateSavegame() throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();

        transformer.transform(new DOMSource(this.xmlDocument), new StreamResult(writer));

        return writer.getBuffer().toString() + "    ";
    }

    /**
     * Go through all registered input elements and apply their changes one
     * after another, stopping if a problem occurs.
     * 
     * @throws Exception
     */
    public void applyAllChanges() throws Exception {
        for(InputElement i : inputs) {
            try {
                i.apply();
            } catch(Exception e) {
                throw new Exception("Couldn't apply changes by " + i.name() + ": " + e.getMessage());
            }
        }
    }
}
