package de.triona.console;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.text.*;

/**
 *
 * @author Bernhard
 */
public class ConsoleFrame extends javax.swing.JFrame implements MouseListener, KeyListener {

    private static final Logger logger = Logger.getLogger(ConsoleFrame.class.getName());
    private boolean serverIsRunning = false;
    public static String SERVER_DIR = "C:/java/jboss-as-7.1.1.Final/bin/";
    public static String START_COMMAND = "standalone.bat";
    public static String STOP_COMMAND = "jboss-cli.bat --connect command=:shutdown";
    public static String OWN_PKG = "de.ard.hr.zonk";
    private BufferedReader br;
    private StringBuilder output = new StringBuilder();
    private StyledDocument doc;
    private FindFrame findFrame;
    private FilterFrame filterFrame;
    private FindModel findModel;
    private int findIdx = 0;
    Color colTraceFilter = new Color(224, 224, 224);
    Color colDebugFilter = Color.LIGHT_GRAY;
    Color colInfoFilter = Color.GRAY;
    Color colWarnFilter = new Color(0x9F, 0x60, 0); //Color.ORANGE;
    Color colErrorFilter = Color.RED;
    Color colOwnFilter = Color.BLACK;
    Color colAllFilter = Color.BLACK;
    private boolean hasFilterError = false;
    private boolean hasFilterWarn = false;
    private boolean hasFilterOwn = false;
    private boolean hasFilterInfo = false;
    private Border lineBorder = new LineBorder(Color.WHITE, 1);
    private boolean hasFilterAll = true;

    public ConsoleFrame() {
        initComponents();
        init();
    }

    private void init() {
        doc = txtPane.getStyledDocument();
        addStylesToDocument(doc);
        addListenerToConsole();
        findFrame = new FindFrame(this);
        filterFrame = new FilterFrame(this);
        serverIsRunning = isServerRunning();
        enableStartStopButtons(serverIsRunning);
        setQuickfilterColors();
        resetQuickfilterBorders();
        if (!new OptionsFrame().validateServerSettings(SERVER_DIR, START_COMMAND, STOP_COMMAND, OWN_PKG)) {
            String msg = "Your server settings could not be validated.\n";
            msg += "Please check your server settings in the options menu.\n\n";
            msg += "Do you want to open your Options Menu now\nto change your server settings?";
            int value = JOptionPane.showConfirmDialog(this, msg, "Open Options Menu now?", JOptionPane.YES_NO_OPTION);
            if (value == JOptionPane.YES_OPTION) {
                showOptionsFrame();
            }
        }
    }

    private void resetQuickfilterBorders() {
        lblFilterAll.setBorder(lineBorder);
        lblFilterError.setBorder(lineBorder);
        lblFilterWarn.setBorder(lineBorder);
        lblFilterInfo.setBorder(lineBorder);
        lblFilterOwn.setBorder(lineBorder);
    }

    private void setQuickfilterColors() {
        lblFilterAll.setForeground(colAllFilter);
        lblFilterError.setForeground(colErrorFilter);
        lblFilterWarn.setForeground(colWarnFilter);
        lblFilterInfo.setForeground(colInfoFilter);
        lblFilterOwn.setForeground(colOwnFilter);
    }

    private boolean isServerRunning() {
        InputStream is = null;
        try {
            URL localhost = new URL("http://localhost:8080/index.html");
            is = localhost.openStream();
            logger.info("Server seems to be up, connection to localhost succeeded");
        } catch (IOException ex) {
            logger.info("Server seems to be down, no connection to localhost: " + ex.getMessage());
            return false;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        return true;
    }

    private void addListenerToConsole() {
        txtPane.addMouseListener(this);
        txtPane.addKeyListener(this);
    }

    private void test() {
        try {
            String logfile = "C:/java/jboss-as-7.1.1.Final/standalone/log/server.log";
            BufferedReader br0 = new BufferedReader(new FileReader(logfile));
            setReader(br0);
            br0.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public void setReader(BufferedReader br) {
        logger.info("setReader br=" + br);
        try {
            String line;

            while ((line = br.readLine()) != null) {
                line = line + '\n';
                doc.insertString(doc.getLength(), line, getStyle(doc, line));
                output.append(line);
            }
            logger.info("setReader done");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * The order of the lines represents the priority<br> Ex. if ERROR is stated first, then it will overwrite OWN,
     * which is stated second
     */
    private Style getStyle(StyledDocument doc, String line) {
        if (line.contains("ERROR") || line.contains("SEVERE")) {
            return doc.getStyle("error");
        } else if (line.contains("WARN")) {
            return doc.getStyle("warn");
        } else if (line.contains(OWN_PKG)) {
            return doc.getStyle("own");
        } else if (line.contains("INFO")) {
            return doc.getStyle("info");
        } else if (line.contains("DEBUG") || line.contains("FINE")) {
            return doc.getStyle("debug");
        } else if (line.contains("TRACE") || line.contains("FINER") || line.contains("FINEST")) {
            return doc.getStyle("debug");
        }
        return doc.getStyle("regular");
    }

    protected final void addStylesToDocument(StyledDocument doc) {
        //Initialize some styles.
        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = doc.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "SansSerif");

        Style trace = doc.addStyle("trace", def);
        StyleConstants.setItalic(trace, true);
        StyleConstants.setForeground(trace, colTraceFilter);

        Style debug = doc.addStyle("debug", def);
        StyleConstants.setForeground(debug, colDebugFilter);

        Style info = doc.addStyle("info", def);
        StyleConstants.setForeground(info, colInfoFilter);

        Style warn = doc.addStyle("warn", def);
        StyleConstants.setForeground(warn, colWarnFilter);

        Style error = doc.addStyle("error", def);
//        StyleConstants.setItalic(error, true);
        StyleConstants.setForeground(error, colErrorFilter);

        Style own = doc.addStyle("own", def);
        StyleConstants.setForeground(own, colOwnFilter);
        StyleConstants.setBold(own, true);
    }

    private void enableStartStopButtons(boolean serverIsRunning) {
        btnStart.setEnabled(!serverIsRunning);
        btnStop.setEnabled(serverIsRunning);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupMenu = new javax.swing.JPopupMenu();
        itmFind = new javax.swing.JMenuItem();
        itmFilter = new javax.swing.JMenuItem();
        itmClear = new javax.swing.JMenuItem();
        pnlLeft = new javax.swing.JPanel();
        btnStart = new javax.swing.JToggleButton();
        btnStop = new javax.swing.JToggleButton();
        btnOptions = new javax.swing.JButton();
        lblQuickfilter = new javax.swing.JLabel();
        pnlQuickfilter = new javax.swing.JPanel();
        lblFilterAll = new javax.swing.JLabel();
        lblFilterError = new javax.swing.JLabel();
        lblFilterWarn = new javax.swing.JLabel();
        lblFilterInfo = new javax.swing.JLabel();
        lblFilterOwn = new javax.swing.JLabel();
        tabOutput = new javax.swing.JTabbedPane();
        scp = new javax.swing.JScrollPane();
        txtPane = new javax.swing.JTextPane();

        itmFind.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        itmFind.setText("Find");
        itmFind.setToolTipText("Find");
        itmFind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmFindActionPerformed(evt);
            }
        });
        popupMenu.add(itmFind);

        itmFilter.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_MASK));
        itmFilter.setText("Filter");
        itmFilter.setToolTipText("Filter");
        itmFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmFilterActionPerformed(evt);
            }
        });
        popupMenu.add(itmFilter);

        itmClear.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        itmClear.setText("Clear");
        itmClear.setToolTipText("Clear Console");
        itmClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmClearActionPerformed(evt);
            }
        });
        popupMenu.add(itmClear);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Server Console");
        setName("frame");
        setPreferredSize(new java.awt.Dimension(1000, 300));

        pnlLeft.setLayout(new javax.swing.BoxLayout(pnlLeft, javax.swing.BoxLayout.Y_AXIS));

        btnStart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/play_active.gif"))); // NOI18N
        btnStart.setSelected(true);
        btnStart.setToolTipText("Start Server");
        btnStart.setBorderPainted(false);
        btnStart.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/images/play_inactive.gif"))); // NOI18N
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });
        pnlLeft.add(btnStart);

        btnStop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/stop_active.gif"))); // NOI18N
        btnStop.setToolTipText("Stop Server");
        btnStop.setBorderPainted(false);
        btnStop.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/images/stop_inactive.gif"))); // NOI18N
        btnStop.setEnabled(false);
        btnStop.setFocusCycleRoot(true);
        btnStop.setOpaque(true);
        btnStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStopActionPerformed(evt);
            }
        });
        pnlLeft.add(btnStop);

        btnOptions.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/options.gif"))); // NOI18N
        btnOptions.setToolTipText("Options");
        btnOptions.setBorderPainted(false);
        btnOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOptionsActionPerformed(evt);
            }
        });
        pnlLeft.add(btnOptions);

        lblQuickfilter.setText("Filter:");
        pnlLeft.add(lblQuickfilter);

        pnlQuickfilter.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 1, 5));

        lblFilterAll.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblFilterAll.setText("*");
        lblFilterAll.setToolTipText("Show everything");
        lblFilterAll.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblFilterAllMouseClicked(evt);
            }
        });
        pnlQuickfilter.add(lblFilterAll);

        lblFilterError.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblFilterError.setText("E");
        lblFilterError.setToolTipText("Show lines containing 'Error' messages");
        lblFilterError.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblFilterErrorMouseClicked(evt);
            }
        });
        pnlQuickfilter.add(lblFilterError);

        lblFilterWarn.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblFilterWarn.setText("W");
        lblFilterWarn.setToolTipText("Show lines containing 'Warning' messages");
        lblFilterWarn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblFilterWarnMouseClicked(evt);
            }
        });
        pnlQuickfilter.add(lblFilterWarn);

        lblFilterInfo.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblFilterInfo.setText("I");
        lblFilterInfo.setToolTipText("Show lines containg 'Info' messages");
        lblFilterInfo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblFilterInfoMouseClicked(evt);
            }
        });
        pnlQuickfilter.add(lblFilterInfo);

        lblFilterOwn.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblFilterOwn.setText("O");
        lblFilterOwn.setToolTipText("Show lines containing messages from your own packages");
        lblFilterOwn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblFilterOwnMouseClicked(evt);
            }
        });
        pnlQuickfilter.add(lblFilterOwn);

        pnlLeft.add(pnlQuickfilter);

        getContentPane().add(pnlLeft, java.awt.BorderLayout.LINE_START);

        tabOutput.setToolTipText("Server Output");
        tabOutput.setName("Server Output");

        scp.setViewportView(txtPane);

        tabOutput.addTab("Server Output", scp);

        getContentPane().add(tabOutput, java.awt.BorderLayout.CENTER);
        tabOutput.getAccessibleContext().setAccessibleName("Server Output");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStopActionPerformed
        if (serverIsRunning) {
            logger.info("stop server");
            SwingWorker worker = new SwingWorker() {

                @Override
                protected Object doInBackground() throws Exception {

                    RunCommand runCommand = new RunCommand();
                    Process pr = runCommand.runCommand(SERVER_DIR + STOP_COMMAND);
                    br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                    setReader(br);
                    if (br != null) {
                        br.close();
                        logger.info("br.close");
                    }
                    return null;
                }
            };
            worker.execute();
            serverIsRunning = false;
            enableStartStopButtons(serverIsRunning);
        }
    }//GEN-LAST:event_btnStopActionPerformed

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        if (!serverIsRunning) {
            logger.info("start server");

            SwingWorker worker = new SwingWorker() {

                @Override
                protected Object doInBackground() throws Exception {
                    try {
                        RunCommand runCommand = new RunCommand();
                        Process pr = runCommand.runCommand(SERVER_DIR + START_COMMAND);
                        br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                        setReader(br);
                        br.close();
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                    return null;
                }
            };
            worker.execute();
            serverIsRunning = true;
            enableStartStopButtons(serverIsRunning);
        }
    }//GEN-LAST:event_btnStartActionPerformed

    private void btnOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOptionsActionPerformed
        showOptionsFrame();
    }//GEN-LAST:event_btnOptionsActionPerformed

    private void itmClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmClearActionPerformed
        clearTextPane();
        output = new StringBuilder();
    }//GEN-LAST:event_itmClearActionPerformed

    private void itmFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmFilterActionPerformed
        showFilterFrame();
    }//GEN-LAST:event_itmFilterActionPerformed

    private void itmFindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmFindActionPerformed
        showFindFrame();
    }//GEN-LAST:event_itmFindActionPerformed

    private void lblFilterErrorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblFilterErrorMouseClicked
        hasFilterError = !hasFilterError;
        resetQuickfilterBorders();
        setLabelBorder(evt.getSource(), hasFilterError);
        filter("ERROR", false, true);
    }//GEN-LAST:event_lblFilterErrorMouseClicked

    private void lblFilterWarnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblFilterWarnMouseClicked
        hasFilterWarn = !hasFilterWarn;
        resetQuickfilterBorders();
        setLabelBorder(evt.getSource(), hasFilterWarn);
        filter("WARN", false, true);
    }//GEN-LAST:event_lblFilterWarnMouseClicked

    private void lblFilterInfoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblFilterInfoMouseClicked
        hasFilterInfo = !hasFilterInfo;
        resetQuickfilterBorders();
        setLabelBorder(evt.getSource(), hasFilterInfo);
        filter("INFO", false, true);
    }//GEN-LAST:event_lblFilterInfoMouseClicked

    private void lblFilterOwnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblFilterOwnMouseClicked
        hasFilterOwn = !hasFilterOwn;
        resetQuickfilterBorders();
        setLabelBorder(evt.getSource(), hasFilterOwn);
        filter(OWN_PKG, false, true);
    }//GEN-LAST:event_lblFilterOwnMouseClicked

    private void lblFilterAllMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblFilterAllMouseClicked
        hasFilterAll = !hasFilterAll;
        resetQuickfilterBorders();
        setLabelBorder(evt.getSource(), hasFilterAll);
        filter("", false, true);
    }//GEN-LAST:event_lblFilterAllMouseClicked

    private void setLabelBorder(Object src, boolean isEnabled) {
        if (src instanceof JLabel) {
            if (isEnabled) {
                JLabel lbl = (JLabel) src;
                lbl.setBorder(new LineBorder(Color.BLACK, 1));
            }
        } else {
            logger.severe("ERROR: src !instanceof JLabel, but " + src.getClass().getSimpleName());
        }
    }

    private void showFindFrame() {
        findFrame.setVisible(true);
        findFrame.setLocationRelativeTo(this);
    }

    private void showFilterFrame() {
        filterFrame.setVisible(true);
        filterFrame.setLocationRelativeTo(this);
    }

    private void showOptionsFrame() {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                OptionsFrame o = new OptionsFrame();
//                o.setModal(true);
                o.setVisible(true);
                o.setLocationRelativeTo(tabOutput);
            }
        });

    }

    /**
     * Clear output in JTextPane and clear
     * <code>output</code>-StringBuilder
     */
    private void clearTextPane() {
        try {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String args[]) {
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            logger.severe(ex.getMessage());
        }
        //</editor-fold>

        MyLogger.setup(logger);
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                ConsoleFrame cf = new ConsoleFrame();
                cf.test();
                cf.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnOptions;
    private javax.swing.JToggleButton btnStart;
    private javax.swing.JToggleButton btnStop;
    private javax.swing.JMenuItem itmClear;
    private javax.swing.JMenuItem itmFilter;
    private javax.swing.JMenuItem itmFind;
    private javax.swing.JLabel lblFilterAll;
    private javax.swing.JLabel lblFilterError;
    private javax.swing.JLabel lblFilterInfo;
    private javax.swing.JLabel lblFilterOwn;
    private javax.swing.JLabel lblFilterWarn;
    private javax.swing.JLabel lblQuickfilter;
    private javax.swing.JPanel pnlLeft;
    private javax.swing.JPanel pnlQuickfilter;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JScrollPane scp;
    private javax.swing.JTabbedPane tabOutput;
    private javax.swing.JTextPane txtPane;
    // End of variables declaration//GEN-END:variables

    private void checkPopupMenu(MouseEvent e) {
        if (e.isPopupTrigger()) {
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        checkPopupMenu(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        checkPopupMenu(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        checkPopupMenu(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.isControlDown()) {
            switch (e.getKeyCode()) {
                case 'F':
                    showFindFrame();
                    break;
                case 'G':
                    showFilterFrame();
                    break;
                case 'L':
                    clearTextPane();
                    output = new StringBuilder();
                    break;
                default:
                    logger.warning("CTRL+" + e.getKeyCode() + " ist undefiniert");
            }
        } else if (e.getKeyCode() == KeyEvent.VK_F3) {
            if (findModel == null || findModel.getTerm().isEmpty()) {
                showFindFrame();
            } else {
                int newFromIdx = findIdx + findModel.getTerm().length();
                find(findModel.getTerm(), findModel.isRegex(), findModel.isMatchCase(), newFromIdx);
            }
        }
    }

    /**
     * Select text in the JTextPane which matches the search term
     *
     * @return The index of the last place of finding. Is used for 'Find Next'
     */
    public int find(String term, boolean regex, boolean matchCase, int from) {
        try {
            String content = doc.getText(0, doc.getLength());
            logger.info("find " + term + ", regex/matchCase/from=" + regex + "/" + matchCase + "/" + from
                    + ", contentLen=" + content.length());
            findModel = new FindModel(term, regex, matchCase);
            if (!matchCase) {
                content = content.toLowerCase();
                term = term.toLowerCase();
            }
            if (!regex) {
                int idx = content.indexOf(term, from);
                if (idx > -1) {
                    txtPane.select(idx, idx + term.length());
                    findIdx = idx;
                }
                return idx;
            } else {
                Pattern p = Pattern.compile(term);
                Matcher matcher = p.matcher(content);
                int start = -1;
                while (matcher.find()) {
                    start = matcher.start();
                    int end = matcher.end();
                    txtPane.select(start, end);
                    findIdx = start;
                }
                return start;
            }
        } catch (BadLocationException ex) {
            logger.severe(ex.getMessage());
        } catch (IllegalStateException ise) {
            logger.severe(ise.getMessage());
        }
        return -1;
    }

    /**
     * Displays only lines in the JTextPane which are matching the filter terms
     */
    public void filter(String term, boolean regex, boolean matchCase) {
        long t1 = System.currentTimeMillis();
        try {
            String content = output.toString();
            clearTextPane();
            logger.info("filter " + term + ", regex/matchCase=" + regex + "/" + matchCase
                    + ", contentLen=" + content.length());
            if (!matchCase) {
//                content = content.toLowerCase();
                term = term.toLowerCase();
            }
            if (regex) {
                term = ".*" + term + ".*";
            }

            StringTokenizer stok = new StringTokenizer(content, "\n");
            while (stok.hasMoreTokens()) {
                String line = stok.nextToken();
                if (term.isEmpty()) {
                    line += "\n";
                    doc.insertString(doc.getLength(), line, getStyle(doc, line));
                } else if (!regex) {
                    if (!matchCase) {
                        if (line.toLowerCase().indexOf(term) > -1) {
                            line += "\n";
                            doc.insertString(doc.getLength(), line, getStyle(doc, line));
                        }
                    } else {
                        if (line.indexOf(term) > -1) {
                            line += "\n";
                            doc.insertString(doc.getLength(), line, getStyle(doc, line));
                        }
                    }
                } else if (regex) {
                    if (Pattern.matches(term, matchCase ? line : line.toLowerCase())) {
                        line += "\n";
                        doc.insertString(doc.getLength(), line, getStyle(doc, line));
                    }
                }
            }
        } catch (BadLocationException ex) {
            logger.severe(ex.getMessage());
        } catch (IllegalStateException ise) {
            logger.severe(ise.getMessage());
        }
        logger.info("Duration Filter: " + (System.currentTimeMillis() - t1) + " ms");
    }
}