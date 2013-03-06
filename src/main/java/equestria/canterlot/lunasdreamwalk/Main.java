package equestria.canterlot.lunasdreamwalk;

import javax.swing.JFrame;
import javax.swing.UIManager;

/**
 * Start the main gui in a safe way
 * 
 */
public class Main {

    private static void createAndShowGUI() {
        LunasDreamwalk ld = new LunasDreamwalk();
        ld.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ld.setVisible(true);
    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception e) {
            // I don't care
        }

        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                createAndShowGUI();
            }
        });
    }
}
