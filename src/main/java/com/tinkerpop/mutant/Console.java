package com.tinkerpop.mutant;

import jline.ConsoleReader;
import jline.History;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Console {

    ScriptEngineManager manager = new ScriptEngineManager();
    List<ScriptEngine> scriptEngines = new ArrayList<ScriptEngine>();
    List<String> scriptNames = new ArrayList<String>();
    int currentEngine = -1;

    PrintStream output = System.out;

    private static final String MUTANT_HISTORY = ".mutant_history";

    public Console() throws Exception {
        System.setProperty("org.jruby.embed.localvariable.behavior", "persistent");
        for (ScriptEngineFactory factory : manager.getEngineFactories()) {
            this.output.println("Initializing " + factory.getEngineName() + " " + factory.getEngineVersion() + " [" + factory.getLanguageName() + "]");
            this.scriptEngines.add(factory.getScriptEngine());
            this.scriptNames.add(factory.getLanguageName());
        }
        if (this.scriptEngines.size() == 0) {
            throw new Exception("No script engines to load");
        } else {
            this.currentEngine = 0;
        }

        this.output.println("\nMuTanT v0.1 [" + Tokens.HELP + "= help]");
        this.primaryLoop();
    }

    public void primaryLoop() throws Exception {

        final ConsoleReader reader = new ConsoleReader();
        reader.setBellEnabled(false);
        reader.setUseHistory(true);

        try {
            History history = new History();
            history.setHistoryFile(new File(MUTANT_HISTORY));
            reader.setHistory(history);
        } catch (IOException e) {
            System.err.println("Could not find history file");
        }

        String line = "";
        this.output.println();

        /*reader.addTriggeredAction((char) 14, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                System.out.println(event);
                output.println("marko");
            }
        });*/

        while (line != null) {

            try {

                line = new String();
                boolean submit = false;
                boolean newline = false;
                while (!submit) {
                    if (newline)
                        line = line + "\n" + reader.readLine(Console.makeSpace(this.getPrompt().length() + 2));
                    else
                        line = line + "\n" + reader.readLine(this.getPrompt());
                    if (line.endsWith(" .")) {
                        newline = true;
                        line = line.substring(0, line.length() - 2);
                    } else {
                        line = line.trim();
                        submit = true;
                    }
                }
                if (line.isEmpty())
                    continue;
                else if (line.startsWith("?")) {
                    if (line.equals(Tokens.QUIT))
                        return;
                    else if (line.equals(Tokens.DROP))
                        this.dropCurrentEngine();
                    else if (line.equals(Tokens.NEXT))
                        this.moveCurrentEngine(1);
                    else if (line.equals(Tokens.PREVIOUS))
                        this.moveCurrentEngine(-1);
                    else if (line.equals(Tokens.BINDINGS))
                        this.printBindings(this.manager.getBindings());
                    else if (line.equals(Tokens.HELP))
                        this.printHelp();
                    else if (line.equals(Tokens.ENGINES))
                        this.printEngines();
                } else {
                    this.output.println(getScriptEngine().eval(line, this.manager.getBindings()));
                    //this.printBindings(getScriptEngine().getBindings(ScriptContext.ENGINE_SCOPE));
                    //this.printBindings(getScriptEngine().getBindings(ScriptContext.GLOBAL_SCOPE));
                }
            } catch (Exception e) {
                this.output.println(e.getMessage());
            }
        }
    }

    public void printHelp() {
        this.output.println(Tokens.PREVIOUS + ": previous engine");
        this.output.println(Tokens.NEXT + ": next engine");
        this.output.println(Tokens.BINDINGS + ": show bindings");
        this.output.println(Tokens.ENGINES + ": show engines");
        this.output.println(Tokens.DROP + ": drop engine");
        this.output.println(Tokens.QUIT + ": quit");

    }

    public void printEngines() {
        String string = new String();
        for (String name : this.scriptNames) {
            string = string + name + "\n";
        }
        this.output.println(string.trim());
    }

    public void printBindings(Bindings bindings) {
        String string = new String();
        for (String key : bindings.keySet()) {
            string = string + key + "=" + bindings.get(key) + "\n";
        }
        this.output.println(string.trim());
    }

    public String getPrompt() {
        return "mutant[" + this.getScriptName() + "]> ";
    }

    public String getScriptName() {
        return this.scriptNames.get(this.currentEngine);
    }

    public ScriptEngine getScriptEngine() {
        return this.scriptEngines.get(this.currentEngine);
    }

    public void moveCurrentEngine(int direction) {
        this.currentEngine = this.currentEngine + direction;
        if (this.currentEngine == -1)
            this.currentEngine = this.scriptEngines.size() - 1;
        else
            this.currentEngine = this.currentEngine % this.scriptEngines.size();

    }

    public void dropCurrentEngine() {
        this.scriptEngines.remove(this.currentEngine);
        this.scriptNames.remove(this.currentEngine);
    }

    public static String makeSpace(int number) {
        String space = new String();
        for (int i = 0; i < number; i++) {
            space = space + " ";
        }
        return space;
    }

    public static void main(String[] args) throws Exception {
        new Console();
    }
}
