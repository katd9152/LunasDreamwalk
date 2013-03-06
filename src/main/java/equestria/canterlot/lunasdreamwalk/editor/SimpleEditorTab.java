package equestria.canterlot.lunasdreamwalk.editor;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class SimpleEditorTab extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -7982836439827382944L;

    private int nextLine = 0;

    private JPanel contentPanel;

    public SimpleEditorTab() {

        this.contentPanel = new JPanel();
        this.contentPanel.setLayout(new GridBagLayout());

        this.setLayout(new BorderLayout());
        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(this.contentPanel);
        this.add(scroll);
    }

    /**
     * Append an editor element to the end of the panel
     * 
     * @param field
     *            the element to be appended. If it is an InputElement, it will
     *            also be added to the inputs list.
     */
    public void addLine(String name, JComponent field) {

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(1, 5, 1, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = nextLine;
        c.gridx = 0;
        c.weightx = 0;

        contentPanel.add(new JLabel(name), c);

        c.gridx = 1;
        c.weightx = 1;
        contentPanel.add(field, c);

        this.nextLine++;
    }

}
