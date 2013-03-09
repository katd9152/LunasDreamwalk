package equestria.canterlot.lunasdreamwalk.editor;

import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
    private int level;
    private int shards;
    private int xp;
    private JPanel stars;
    private JPanel plus;
    private JPanel minus;

    private static BufferedImage starIcon, starEmptyIcon, starBlueIcon,
                    plusIcon, minusIcon;
    private static final int STAR_SIZE = 15;

    static {
        try {
            starIcon = ImageIO.read(PonySettingsPanel.class.getResource("/images/star.png"));
            starEmptyIcon = ImageIO.read(PonySettingsPanel.class.getResource("/images/star_empty.png"));
            starBlueIcon = ImageIO.read(PonySettingsPanel.class.getResource("/images/star_blue.png"));
            plusIcon = ImageIO.read(PonySettingsPanel.class.getResource("/images/plus.png"));
            minusIcon = ImageIO.read(PonySettingsPanel.class.getResource("/images/minus.png"));

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

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

        this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        this.createElements();

        this.reset();
    }

    private void setLevel(int level) {
        this.level = level > 5 ? 5 : (level < 0 ? 0 : level);
        stars.repaint();
    }

    private void setShards(int shards) {
        this.shards = shards > 10 ? 10 : (shards < 0 ? 0 : shards);
        stars.repaint();
    }

    private void setXP(int xp) {
        this.xp = xp;
    }

    private int getLevel() {
        return this.level;
    }

    private int getShards() {
        return this.shards;
    }

    private int getXP() {
        return this.xp;
    }

    private void createElements() {
        this.add(new JLabel("Level: "));

        minus = new JPanel() {

            private static final long serialVersionUID = 1L;

            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(minusIcon, 0, 0, STAR_SIZE, STAR_SIZE, this);
            }
        };

        minus.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if(getShards() == 10) {
                    setShards(0);

                } else if(getLevel() > 0) {
                    setLevel(getLevel() - 1);
                    setShards(10);
                }
                // always set XP to 0 to avoid conflicts
                setXP(0);
            }
        });

        minus.setSize(STAR_SIZE, STAR_SIZE);
        minus.setMinimumSize(minus.getSize());
        minus.setMaximumSize(minus.getSize());
        minus.setPreferredSize(minus.getSize());

        plus = new JPanel() {

            private static final long serialVersionUID = 1L;

            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(plusIcon, 0, 0, STAR_SIZE, STAR_SIZE, this);
            }
        };

        plus.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if(getShards() == 10) {
                    setShards(0);
                    setLevel(getLevel() + 1);
                } else if(getLevel() < 5) {
                    setShards(10);
                }
                // always set XP to 0 to avoid conflicts
                setXP(0);
            }
        });
        plus.setSize(STAR_SIZE, STAR_SIZE);
        plus.setMinimumSize(plus.getSize());
        plus.setMaximumSize(plus.getSize());
        plus.setPreferredSize(plus.getSize());

        stars = new JPanel() {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                for(int i = 0; i < 5; i++) {

                    if(level > i) {
                        g.drawImage(starIcon, STAR_SIZE * i, 0, STAR_SIZE, STAR_SIZE, null);
                    } else if(level == i && shards == 10) {
                        g.drawImage(starBlueIcon, STAR_SIZE * i, 0, STAR_SIZE, STAR_SIZE, null);
                    } else {
                        g.drawImage(starEmptyIcon, STAR_SIZE * i, 0, STAR_SIZE, STAR_SIZE, null);
                    }
                }
            }
        };
        stars.setOpaque(true);
        stars.setSize(STAR_SIZE * 5, STAR_SIZE);
        stars.setMinimumSize(stars.getSize());
        stars.setMaximumSize(stars.getSize());
        stars.setPreferredSize(stars.getSize());

        this.add(Box.createHorizontalStrut(5));
        this.add(minus);
        this.add(Box.createHorizontalStrut(5));
        this.add(stars);
        this.add(Box.createHorizontalStrut(5));
        this.add(plus);
        this.add(Box.createHorizontalGlue());

    }

    public void apply() {
        int level = getLevel();
        int shards = getShards();
        int xp = getXP();

        // sanity checks, in case we missed some illegal settings before
        if(level < 0)
            level = 0;

        if(level > 5)
            level = 5;

        if(level == 5) {
            shards = 0;
            xp = 0;
        }

        if(shards < 0)
            shards = 0;

        if(shards > 10)
            shards = 10;

        if(shards == 10) {
            xp = 0;
        }

        Element e;
        try {
            e = editor.getXMLElementByString("/MLP_Save/MapZone/GameObjects/Pony_Objects/Object[@ID='" + id + "']/Game/Level");
            e.setAttribute("Level", Integer.toString(level));
            e.setAttribute("Shards", Integer.toString(shards));
            e.setAttribute("CurrentEXP", Integer.toString(xp));
        } catch(XPathExpressionException e1) {
            return;
        }

    }

    public void reset() {

        Element e;
        int level = 0, shards = 0, xp = 0;
        try {
            e = editor.getXMLElementByString("/MLP_Save/MapZone/GameObjects/Pony_Objects/Object[@ID='" + id + "']/Game/Level");

            level = Integer.parseInt(e.getAttribute("Level"));
            shards = Integer.parseInt(e.getAttribute("Shards"));
            xp = Integer.parseInt(e.getAttribute("CurrentEXP"));

        } catch(XPathExpressionException e1) {

        }

        this.setShards(shards);
        this.setLevel(level);
        this.setXP(xp);
        stars.repaint();
    }

    public String name() {
        // Parse the Id into something more appropriate to show to the
        // user
        return id.replace("Pony_", "").replaceAll("_", " ");
    }

}
