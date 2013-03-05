package equestria.canterlot.lunasdreamwalk;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Just a little panel that displays static help text.
 *
 */
public class HelpText extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2870355928307064404L;

	public HelpText() {
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBorder(new EmptyBorder(5,5,5,5));
		this.add(new JLabel("Android users need to use their IMEI (15 digits) to decrypt savegames."));
		this.add(new JLabel("If your Android device has no IMEI (e.g. a Tablet), use your 'Android_Id' (16 digits and letters 'A-F')."));
		this.add(new JLabel("iPhone/iPad users need to use their GLUID instead decrypt savegames. Google to find out where and how to find it."));
		
		this.add(new JLabel("I strongly suggest making backups of your savegames before editing."));
	}
}
