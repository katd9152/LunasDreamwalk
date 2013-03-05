package equestria.canterlot.lunasdreamwalk;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SimpleEditor extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8589145875345431936L;
	private Document xmlDocument;
	private List<InputElement> inputs = new ArrayList<InputElement>();

	private int nextLine = 0;

	/**
	 * An InputElement is intended to show the current state of a part of the
	 * underlying XML-Object and allow to declare specific changes to that part,
	 * e.g. modifying values or removing nodes.
	 * 
	 * Those changes can then be applied when needed.
	 * 
	 */
	private interface InputElement {
		/**
		 * Apply changes to the underlying XML-Object
		 */
		public void apply();

		/**
		 * Reset element with data from underlying XML-Object
		 */
		public void reset();

		/**
		 * Get an identifier that can be shown to the user in the UI and error
		 * messages to identify this Input element
		 * 
		 * @return a name/very short description
		 */
		public String name();
	}

	/**
	 * The NumberInputField represents a single attribute of a single Node of
	 * the underlying xml document. That attribute only accepts integers.
	 * 
	 */
	private class IntegerInputField extends JTextField implements InputElement {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1749170444373429282L;
		private String name;
		private String nodeTag;
		private String nodeAttribute;

		/**
		 * 
		 * @param name
		 *            A human readable and understandable description of the
		 *            to-be-edited attribute
		 * @param nodeTag
		 *            the tag of the to-be-edited node
		 * @param nodeAttribute
		 *            the attribute of that node that will be edited
		 */
		public IntegerInputField(String name, String nodeTag,
				String nodeAttribute) {
			this.name = name;
			this.nodeTag = nodeTag;
			this.nodeAttribute = nodeAttribute;

			reset();
		}

		public void reset() {
			NodeList nl = xmlDocument.getElementsByTagName(nodeTag);
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				NamedNodeMap attr = n.getAttributes();
				Node item = attr.getNamedItem(nodeAttribute);
				if (item != null) {
					this.setText(item.getNodeValue());
				}
			}
		}

		public void apply() {

			Integer value = Integer.parseInt(this.getText());

			NodeList nl = xmlDocument.getElementsByTagName(nodeTag);
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				NamedNodeMap attr = n.getAttributes();
				Node item = attr.getNamedItem(nodeAttribute);
				if (item != null) {
					item.setNodeValue(value.toString());
				}
			}
		}

		public String name() {
			return this.name;
		}
	}

	/**
	 * The RemoveTypeCheckBox will offer to remove all child nodes of a
	 * specified node. It's name will also contain the number of nodes that
	 * currently exist.
	 * 
	 */
	private class RemoveTypeCheckBox extends JCheckBox implements InputElement {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1749170444373429283L;
		private String name;
		private String nodeTag;

		/**
		 * 
		 * @param name
		 *            A short name for the elements that will be removed.
		 * @param nodeTag
		 *            The tag of the parent node that would get all its children
		 *            removed
		 */
		public RemoveTypeCheckBox(String name, String nodeType) {
			this.name = name;
			this.nodeTag = nodeType;

			reset();
		}

		public void reset() {
			NodeList nl = xmlDocument.getElementsByTagName(this.nodeTag);
			int amount = 0;
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				amount += n.getChildNodes().getLength();
			}

			this.name = "Remove " + amount + " " + this.name;

		}

		public void apply() {

			if (!this.isSelected()) {
				return;
			}

			NodeList nl = xmlDocument.getElementsByTagName(this.nodeTag);
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				NodeList nl2 = n.getChildNodes();
				for (int i2 = 0; i2 < nl2.getLength(); i++) {
					n.removeChild(nl2.item(i2));
				}
			}
		}

		public String name() {
			return this.name;
		}
	}

	/**
	 * The simple editor works with an String that represents an xml tree.
	 * 
	 * @param xmlContent
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public SimpleEditor(String xmlContent) throws ParserConfigurationException,
			SAXException, IOException {

		this.xmlDocument = this.parseSavegame(xmlContent);

		createElements();

	}

	/**
	 * Set up the GUI elements
	 */
	private void createElements() {

		this.setLayout(new GridBagLayout());

		this.addLine(new IntegerInputField("Coins", "PlayerData", "Coins"));
		this.addLine(new IntegerInputField("Gems", "PlayerData", "Hearts"));
		this.addLine(new IntegerInputField("Hearts", "PlayerData", "Social"));
		this.addLine(new IntegerInputField("Loyalty Shards", "Shards",
				"Loyalty"));
		this.addLine(new IntegerInputField("Kindness Shards", "Shards",
				"Kindness"));
		this.addLine(new IntegerInputField("Honesty Shards", "Shards",
				"Honesty"));
		this.addLine(new IntegerInputField("Generosity Shards", "Shards",
				"Generosity"));
		this.addLine(new IntegerInputField("Laughter Shards", "Shards",
				"Laughter"));
		this.addLine(new IntegerInputField("Magic Shards", "Shards", "Magic"));

		this.addLine(new RemoveTypeCheckBox("Rubble/Stones/Roots",
				"Clearable_Objects"));
		this.addLine(new RemoveTypeCheckBox("Parasprites", "Parasprite_Objects"));
	}

	/**
	 * Append an editor element to the end of the editor
	 * 
	 * @param field
	 *            the element to be appended. If it is an InputElement, it will
	 *            also be added to the inputs list.
	 */
	private void addLine(JComponent field) {

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3, 3, 3, 3);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = nextLine;
		c.gridx = 0;
		c.weightx = 0;

		if (field instanceof InputElement) {
			this.add(new JLabel(((InputElement) field).name() + ":"), c);

			this.inputs.add((InputElement) field);
		}

		c.gridx = 1;
		c.weightx = 1;
		this.add(field, c);

		this.nextLine++;
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
	private Document parseSavegame(String xml)
			throws ParserConfigurationException, SAXException, IOException {
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

		transformer.transform(new DOMSource(this.xmlDocument),
				new StreamResult(writer));

		return writer.getBuffer().toString() + "    ";
	}

	/**
	 * Go through all registered input elements and apply their changes one
	 * after another, stopping if a problem occurs.
	 * 
	 * @throws Exception
	 */
	public void applyAllChanges() throws Exception {
		for (InputElement i : inputs) {
			try {
				i.apply();
			} catch (Exception e) {
				throw new Exception("Couldn't apply changes by " + i.name()
						+ ": " + e.getMessage());
			}
		}
	}
}
