package com.tinkerpop.mutant.swing;

import com.tinkerpop.mutant.Tokens;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 *
 * @author seh
 */
public class MutantPanel extends JPanel {

    float fontScale = 1.5f;
    
    ScriptEngineManager manager = new ScriptEngineManager();
    List<ScriptEngine> scriptEngines = new ArrayList<ScriptEngine>();
    List<String> scriptNames = new ArrayList<String>();
    int currentEngine = -1;
    private static final String MUTANT_HISTORY = ".mutant_history";
    JTextArea output = new JTextArea();
    JTextArea input = new JTextArea();
    JComboBox codeSelect;

    public MutantPanel() {
        super(new BorderLayout());

        recreate();

        append("MuTanT v0.1 [" + Tokens.HELP + "= help]\n\n");
        printHelp();
    }

    protected void recreate() {
        removeAll();

        JMenuBar menu = new JMenuBar();
        {
            JMenu fileMenu = new JMenu("File");
            menu.add(fileMenu);

            JMenu viewMenu = new JMenu("View");
            //clear
            //increase font size
            //decrease font size
            menu.add(viewMenu);

            JMenuItem helpMenu = new JMenuItem("Help");
            helpMenu.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    printHelp();
                }
            });
            menu.add(helpMenu);
        }
        
        add(menu, BorderLayout.NORTH);

        codeSelect = new JComboBox();

        {
            System.setProperty("org.jruby.embed.localvariable.behavior", "persistent");
            for (ScriptEngineFactory factory : manager.getEngineFactories()) {
                append("Initializing " + factory.getEngineName() + " " + factory.getEngineVersion() + " [" + factory.getLanguageName() + "]\n");
                this.scriptEngines.add(factory.getScriptEngine());
                this.scriptNames.add(factory.getLanguageName());
                codeSelect.addItem(factory.getLanguageName());
            }
            if (this.scriptEngines.size() == 0) {
                //throw new Exception("No script engines to load");
                append("ERROR: No script engines to load");
            } else {
                this.currentEngine = 0;
            }
        }

        append("\n");

        JButton enterButton = new JButton(">");
        enterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enter();
            }
        });

        float displayFontSize = getFont().getSize() * fontScale;
        output.setFont(output.getFont().deriveFont(displayFontSize));
        input.setFont(input.getFont().deriveFont(displayFontSize));

        JPanel inputPane = new JPanel(new BorderLayout());

        inputPane.add(codeSelect, BorderLayout.WEST);
        inputPane.add(input, BorderLayout.CENTER);
        inputPane.add(enterButton, BorderLayout.EAST);

        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                enter();
                            }
                        });
                    }
                    else if (e.getKeyCode() == KeyEvent.VK_UP) {
                        nextEngine(-1);
                    }
                    else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        nextEngine(1);
                    }
                }
            }
        });

        output.setBackground(Color.DARK_GRAY);
        output.setForeground(Color.WHITE);
        output.setEditable(false);

        JSplitPane outerPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(output), inputPane);
        outerPane.setResizeWeight(0.95);

        add(outerPane, BorderLayout.CENTER);
        updateUI();
    }

    protected void nextEngine(int delta) {
        currentEngine = codeSelect.getSelectedIndex();
        int next = currentEngine + delta;
        if (next < 0) {
            next = scriptEngines.size()-1;
        }
        if (next > scriptEngines.size()-1) {
            next = 0;
        }
        codeSelect.setSelectedIndex(next);
        currentEngine = next;
    }

    protected void append(String s) {
        output.setText(output.getText() + s);
    }

    protected void quit() {
        System.exit(0);
    }

    protected void enter() {
        String line = input.getText();
        line = line.trim();

        if (line.isEmpty())
            return;

        if (line.startsWith("?")) {
            if (line.equals(Tokens.QUIT)) {
                quit();
                return;            
//            } else if (line.equals(Tokens.DROP)) {
//                this.dropCurrentEngine();
//            } else if (line.equals(Tokens.NEXT)) {
//                this.moveCurrentEngine(1);
//            } else if (line.equals(Tokens.PREVIOUS)) {
//                this.moveCurrentEngine(-1);
            } else if (line.equals(Tokens.BINDINGS)) {
                this.printBindings(this.manager.getBindings());
            } else if (line.equals(Tokens.HELP)) {
                this.printHelp();
            } else if (line.equals(Tokens.ENGINES)) {
                this.printEngines();
            }
        } else {
            currentEngine = codeSelect.getSelectedIndex();
            append(getScriptName() + ": " + line + "\n");
            try {
                append("  " + getScriptEngine().eval(line, this.manager.getBindings()) + "\n");
            }
            catch (Exception e) {
                append("ERROR: " + e.toString() + "\n");
                append(" " + Arrays.asList(e.getStackTrace()) + "\n");
            }
            append("\n");
            //this.printBindings(getScriptEngine().getBindings(ScriptContext.ENGINE_SCOPE));
            //this.printBindings(getScriptEngine().getBindings(ScriptContext.GLOBAL_SCOPE));
        }

        input.setText("");
    }
//    public void primaryLoop() throws Exception {
//
//        final ConsoleReader reader = new ConsoleReader();
//        reader.setBellEnabled(false);
//        reader.setUseHistory(true);
//
//        try {
//            History history = new History();
//            history.setHistoryFile(new File(MUTANT_HISTORY));
//            reader.setHistory(history);
//        } catch (IOException e) {
//            System.err.println("Could not find history file");
//        }
//
//        String line = "";
//        this.output.println();
//
//        /*reader.addTriggeredAction((char) 14, new ActionListener() {
//            public void actionPerformed(ActionEvent event) {
//                System.out.println(event);
//                output.println("marko");
//            }
//        });*/
//
//        while (line != null) {
//
//            try {
//
//                line = new String();
//                boolean submit = false;
//                boolean newline = false;
//                while (!submit) {
//                    if (newline)
//                        line = line + "\n" + reader.readLine(Console.makeSpace(this.getPrompt().length() + 2));
//                    else
//                        line = line + "\n" + reader.readLine(this.getPrompt());
//                    if (line.endsWith(" .")) {
//                        newline = true;
//                        line = line.substring(0, line.length() - 2);
//                    } else {
//                        line = line.trim();
//                        submit = true;
//                    }
//                }
//                if (line.isEmpty())
//                    continue;
//                else if (line.startsWith("?")) {
//                    if (line.equals(Tokens.QUIT))
//                        return;
//                    else if (line.equals(Tokens.DROP))
//                        this.dropCurrentEngine();
//                    else if (line.equals(Tokens.NEXT))
//                        this.moveCurrentEngine(1);
//                    else if (line.equals(Tokens.PREVIOUS))
//                        this.moveCurrentEngine(-1);
//                    else if (line.equals(Tokens.BINDINGS))
//                        this.printBindings(this.manager.getBindings());
//                    else if (line.equals(Tokens.HELP))
//                        this.printHelp();
//                    else if (line.equals(Tokens.ENGINES))
//                        this.printEngines();
//                } else {
//                    this.output.println(getScriptEngine().eval(line, this.manager.getBindings()));
//                    //this.printBindings(getScriptEngine().getBindings(ScriptContext.ENGINE_SCOPE));
//                    //this.printBindings(getScriptEngine().getBindings(ScriptContext.GLOBAL_SCOPE));
//                }
//            } catch (Exception e) {
//                this.output.println(e.getMessage());
//            }
//        }
//    }
//
    public void printHelp() {
//        this.output.println(Tokens.PREVIOUS + ": previous engine");
//        this.output.println(Tokens.NEXT + ": next engine");
        append("ctrl-Enter to execute\n");
        append("ctrl-Up to select previous engine\n");
        append("ctrl-Down to select next engine\n");
        append(Tokens.BINDINGS + ": show bindings\n");
        append(Tokens.ENGINES + ": show engines\n");
//        this.output.println(Tokens.DROP + ": drop engine");
        append(Tokens.QUIT + ": quit\n");
        append("\n");

    }

    public void printEngines() {
        String string = new String();
        for (String name : this.scriptNames) {
            string = string + name + "\n";
        }
        append(string.trim() + "\n");
        append("\n");
    }
    

    public void printBindings(Bindings bindings) {
        if (bindings.size() == 0) {
            append("No bindings exist yet.\n");
            return;
        }
        
        String string = new String();
        for (String key : bindings.keySet()) {
            string = string + key + "=" + bindings.get(key) + "\n";
        }
        append(string.trim() + "\n");
        append("\n");
    }

//    public String getPrompt() {
//        return "mutant[" + this.getScriptName() + "]> ";
//    }

    public String getScriptName() {
        return this.scriptNames.get(this.currentEngine);
    }

    public ScriptEngine getScriptEngine() {
        return this.scriptEngines.get(this.currentEngine);
    }

    public void moveCurrentEngine(int direction) {
        this.currentEngine = this.currentEngine + direction;
        if (this.currentEngine == -1) {
            this.currentEngine = this.scriptEngines.size() - 1;
        } else {
            this.currentEngine = this.currentEngine % this.scriptEngines.size();
        }

    }

    public void dropCurrentEngine() {
        this.scriptEngines.remove(this.currentEngine);
        this.scriptNames.remove(this.currentEngine);
    }

//    public static String makeSpace(int number) {
//        String space = new String();
//        for (int i = 0; i < number; i++) {
//            space = space + " ";
//        }
//        return space;
//    }

    public static void main(String[] args) {
        JFrame jf = new JFrame("Tinkerpop MuTanT");
        jf.getContentPane().add(new MutantPanel());
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setSize(800, 600);
        jf.setVisible(true);
    }
}
