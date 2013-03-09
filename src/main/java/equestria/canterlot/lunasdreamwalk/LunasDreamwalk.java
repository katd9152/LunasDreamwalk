package equestria.canterlot.lunasdreamwalk;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import equestria.canterlot.lunasdreamwalk.util.SaveFileAdapter;
import equestria.canterlot.lunasdreamwalk.util.Util;

/**
 * Main GUI Element of the Savegame editor.
 * 
 */
public class LunasDreamwalk extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 509117784292343936L;

    private static final String version = "1.6";

    private JButton load, retry, save;
    private JTextField imeiGluid, key, fileLocation;
    private JTextArea xml;

    // private byte[] fallbackKey = Util
    // .hexStringToByteArray("30F5000A512D4D00961F00009A8FFD04");

    private File selectedFile = null;
    private SaveFileAdapter saveFileAdapter = null;

    private JPanel editorPanel;
    private SimpleEditor simpleEditor;

    public LunasDreamwalk() {
        this.setTitle("Luna's Dreamwalk v" + version + " (Savegame Editor for Gameloft MLP Game)");
        this.setSize(800, 600);

        // Initialize most GUI elements
        createElements();

        // assign listeners
        assignActionsAndListeners();

    }

    /**
     * Buttons get their respective obvious actions, the GLUID/IMEI input field
     * gets a modification listener to evaluate its content on every change and
     * check if the entered key is valid.
     */
    private void assignActionsAndListeners() {
        load.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                File f = showLoadDialog();

                if(f != null) {
                    selectedFile = f;
                    fileLocation.setText(selectedFile.getAbsolutePath());
                    parseFile(f);
                }
            }
        });

        retry.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                parseFile(selectedFile);
            }
        });

        save.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                File f = showSaveDialog();

                if(f != null) {
                    storeFile(f);
                }
            }
        });

        // Listen for changes in the text
        imeiGluid.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {
                eval();
            }

            public void removeUpdate(DocumentEvent e) {
                eval();
            }

            public void insertUpdate(DocumentEvent e) {
                eval();
            }

            public void eval() {
                byte[] tmp = deriveKeyFromIMEIorGLUID(imeiGluid.getText());

                if(tmp == null) {
                    key.setText("Invalid IMEI/MEID/Android_Id/GLUID");
                    enableFileButtons(false);
                } else {
                    key.setText(Util.byteArrayToHexString(tmp));
                    enableFileButtons(true);
                }
            }
        });
    }

    /**
     * Set the enabled state of all file-related buttons at once
     * 
     * @param enable
     *            the new state
     */
    private void enableFileButtons(boolean enable) {
        this.load.setEnabled(enable);
        this.save.setEnabled(enable);
        this.retry.setEnabled(enable);
    }

    /**
     * Parse the content of a file, hopefully ending up with a xml-Tree of the
     * savegame file's content in a visual editor
     * 
     * @param inputFile
     */
    private void parseFile(File inputFile) {

        int archiveSize;
        byte saveFileContent[] = null;

        {
            InputStream is = null;
            archiveSize = (int) inputFile.length();
            try {
                is = new BufferedInputStream(new FileInputStream(inputFile));
            } catch(FileNotFoundException e) {
                showErrorMessage("File " + inputFile.getAbsolutePath() + " not found");
                return;
            }

            saveFileContent = new byte[archiveSize];
            try {
                is.read(saveFileContent);
                is.close();
            } catch(IOException e) {
                showErrorMessage("Couldn't read file content of " + inputFile.getAbsolutePath());
                return;
            }
        }

        SaveFileAdapter save;
        try {
            save = new SaveFileAdapter(saveFileContent);
        } catch(IOException e) {
            showErrorMessage("Couldn't open Savegame " + e.getMessage());
            return;
        }

        if(!save.sanityCheck()) {
            showErrorMessage("The savegame file seems to be invalid");
            return;
        }

        this.saveFileAdapter = save;

        this.saveFileAdapter.setFirstKey(Util.hexStringToByteArray(key.getText()));

        if(!this.saveFileAdapter.decryptFirstLayer()) {
            showErrorMessage("Couldn't decrypt first security layer of file. Is your IMEI/GLUID correct?");
            return;
        }

        if(!this.saveFileAdapter.decryptSecondLayer()) {
            showErrorMessage("Couldn't decrypt second security layer of file. Maybe the savegame has a different protection scheme?");
            return;
        }

        this.editorPanel.removeAll();
        try {
            this.simpleEditor = new SimpleEditor(this.saveFileAdapter.getXMLContent());
            this.editorPanel.add(this.simpleEditor);
        } catch(Exception e) {
            showErrorMessage("XML Invalid, no simple Editor available: " + e.getClass().toString() + ": " + e.getMessage());
            this.xml.setText(this.saveFileAdapter.getXMLContent());
            this.editorPanel.add(this.xml);
            e.printStackTrace();

        }
        this.editorPanel.revalidate();
        showInfoMessage("Success");
    }

    /**
     * Convenience method for showing small informal messages
     * 
     * @param message
     */
    private void showInfoMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);

    }

    /**
     * Convenience method for showing small error messages
     * 
     * @param message
     */
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Apply all changes and convert the data back into a (hopefully) valid
     * savegame.
     * 
     * @param outputFile
     */
    private void storeFile(File outputFile) {
        try {
            this.simpleEditor.applyAllChanges();
        } catch(Exception e) {
            this.showErrorMessage("Failed to Save:\n\n" + e.getMessage());
            return;
        }

        String content;
        try {
            content = this.simpleEditor.generateSavegame();
        } catch(Exception e) {
            this.showErrorMessage("Coulnd't generate valid savegame: " + e.getMessage());
            return;
        }

        this.saveFileAdapter.setXMLContent(content);

        this.saveFileAdapter.encryptSecondLayer();

        this.saveFileAdapter.setFirstKey(Util.hexStringToByteArray(key.getText()));

        this.saveFileAdapter.encryptFirstLayer();

        try {
            if(!outputFile.exists())
                outputFile.createNewFile();

            OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile));
            os.write(this.saveFileAdapter.fileContent());
            os.close();

        } catch(IOException e) {
            this.showErrorMessage("Couldn't write file: " + e.getMessage());
            return;
        }

        showInfoMessage("Success");
    }

    /**
     * Calculate an encryption key from IMEI, MEID, Android_Id or GLUID
     * 
     */
    private static byte[] deriveKeyFromIMEIorGLUID(String imeiOrGLUID) {

        String withoutSpecialCharacters = imeiOrGLUID.replaceAll("[^a-fA-F0-9]", "");

        // probably GLUID (at least 32 hexadecimal)
        if(withoutSpecialCharacters.length() >= 32) {
            withoutSpecialCharacters = withoutSpecialCharacters.substring(0, 32);
            return Util.hexStringToByteArray(withoutSpecialCharacters);
        }
        // probably an IMEI (15 digits)
        else if(withoutSpecialCharacters.length() == 15 && withoutSpecialCharacters.matches("[0-9]+")) {
            return createAndroidKey(withoutSpecialCharacters);
        }
        // probably a MEID (14 hexadecimal)
        else if(withoutSpecialCharacters.length() == 14) {
            return createAndroidKey(withoutSpecialCharacters);
        }
        // probably a MEID (alternative 18 digits representation)
        else if(withoutSpecialCharacters.length() == 18 && withoutSpecialCharacters.matches("[0-9]+")) {
            return createAndroidKey(withoutSpecialCharacters);
        }
        // probably an Android_Id (16 hexadecimal)
        else if(withoutSpecialCharacters.length() == 16) {
            return createAndroidKey(withoutSpecialCharacters);
        }
        // Not parseable
        else {
            return null;
        }
    }

    private static byte[] createAndroidKey(String input) {
        byte[] key2 = Util.md5(Util.stringToASCIIByteArray(input + "com.gameloft.android.ANMP.GloftPOHM"));

        int[] ints = Util.bytesToInts(Util.reorderBytes(key2));
        int[] newInts = new int[ints.length];
        for(int i = 0; i < ints.length; i++) {
            if(ints[i % 3] < 0) {
                newInts[i] = 0x7FFFFFFF - ints[i];
            } else {
                newInts[i] = ints[i];
            }
        }

        ints = newInts;

        return Util.intsToBytes(ints);
    }

    /**
     * Lots of boring layout stuff
     */
    private void createElements() {
        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = 1;
        c.weightx = 0;
        c.insets = new Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        this.add(new HelpText(), c);

        c.gridy++;
        c.gridwidth = 1;
        c.gridx = 0;
        this.add(new JLabel(" IMEI/MEID/Android_Id/GLUID :"), c);

        c.gridy++;
        this.add(new Label("Derived Key:"), c);

        c.gridy++;
        this.add(new Label("File Location:"), c);

        c.weightx = 1;
        c.gridx = 1;
        c.gridy = 1;
        imeiGluid = new JTextField("");
        this.add(imeiGluid, c);

        c.gridy++;
        key = new JTextField("");
        key.setEditable(false);
        this.add(key, c);

        c.gridy++;
        fileLocation = new JTextField("");
        fileLocation.setEditable(false);
        this.add(fileLocation, c);

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
        load = new JButton("Select Savegame");
        load.setEnabled(false);
        buttons.add(load);

        retry = new JButton("Reload");
        retry.setEnabled(false);
        buttons.add(retry);

        save = new JButton("Store Savegame");
        save.setEnabled(false);
        buttons.add(save);
        c.gridy++;
        c.gridx = 1;
        this.add(buttons, c);

        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridy++;
        c.gridx = 0;
        c.weighty = 1;
        JScrollPane scroll = new JScrollPane();

        xml = new JTextArea("");
        xml.setLineWrap(false);
        xml.setEditable(false);
        scroll.setViewportView(xml);

        editorPanel = new JPanel();
        editorPanel.setLayout(new BorderLayout());
        editorPanel.add(scroll);
        this.add(editorPanel, c);
    }

    /**
     * Let the user select a file as an input source
     * 
     * @return the selected file
     */
    private File showLoadDialog() {
        JFileChooser fc = new JFileChooser(new File(".").getAbsolutePath());

        fc.setDialogTitle("Choose a save file");
        fc.setMultiSelectionEnabled(false);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new FileNameExtensionFilter("Gameloft Savegame '.dat'", "dat"));

        if(JFileChooser.APPROVE_OPTION != fc.showOpenDialog(this)) {
            return null;
        }

        return fc.getSelectedFile();

    }

    /**
     * Let the user select a file for saving
     * 
     * @return the selected file location
     */
    private File showSaveDialog() {
        JFileChooser fc = new JFileChooser(new File(".").getAbsolutePath());

        fc.setDialogTitle("Choose a place to save");
        fc.setSelectedFile(this.selectedFile);
        fc.setMultiSelectionEnabled(false);
        fc.setFileFilter(new FileNameExtensionFilter("Gameloft Savegame '.dat'", "dat"));

        if(JFileChooser.APPROVE_OPTION != fc.showSaveDialog(this)) {
            return null;
        }

        return fc.getSelectedFile();

    }

}
