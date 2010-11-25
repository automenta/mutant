package com.tinkerpop.mutant;

import jline.ConsoleReader;
import jline.History;

import javax.script.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Console {

    List<ScriptEngine> scriptEngines = new ArrayList<ScriptEngine>();
    List<String> scriptNames = new ArrayList<String>();
    Bindings bindings = new SimpleBindings();
    int currentEngine = -1;

    PrintStream output = System.out;

    private static final String MUTANT_HISTORY = ".mutant_history";

    public Console() throws Exception {
        System.setProperty("org.jruby.embed.localvariable.behavior", "persistent");
        ScriptEngineManager manager = new ScriptEngineManager();
        for (ScriptEngineFactory factory : manager.getEngineFactories()) {
            this.output.println("Initializing " + factory.getEngineName() + "[" + factory.getLanguageName() + "]");
            this.scriptEngines.add(factory.getScriptEngine());
            this.scriptNames.add(factory.getLanguageName());
        }
        if (this.scriptEngines.size() == 0) {
            throw new Exception("No script engines to load");
        } else {
            this.currentEngine = 0;
        }

        this.output.println("\nMuTanT v0.1 [?h = help]");
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
            System.err.println("Could not find history file.");
        }

        String line = "";
        this.output.println();


        while (line != null) {
            try {    // read console line
                line = reader.readLine(this.getPrompt()).trim();
                if (line.isEmpty())
                    continue;
                else if (line.startsWith("?")) {
                    if (line.equals("?q"))
                        return;
                    else if (line.equals("?x"))
                        this.moveCurrentEngine(1);
                    else if (line.equals("?z"))
                        this.moveCurrentEngine(-1);
                    else if (line.equals("?b"))
                        this.printBindings();
                    else if (line.equals("?h"))
                        this.printHelp();
                    else if (line.equals("?e"))
                        this.printEngines();
                } else {
                    //getScriptEngine().setBindings(this.bindings, ScriptContext.ENGINE_SCOPE);
                    //getScriptEngine().setBindings(this.bindings, ScriptContext.GLOBAL_SCOPE);
                    this.output.println(getScriptEngine().eval(line, this.bindings));
                    //this.bindings = getScriptEngine().getBindings(ScriptContext.GLOBAL_SCOPE);
                }
            } catch (Exception e) {
                this.output.println(e.getMessage());
            }
        }
    }

    public void printHelp() {
        this.output.println("?z: previous engine");
        this.output.println("?x: next engine");
        this.output.println("?b: show bindings");
        this.output.println("?e: show engines");
        this.output.println("?q: quit");

    }

    public void printEngines() {
        String string = new String();
        for (String name : this.scriptNames) {
            string = string + name + "\n";
        }
        this.output.println(string.trim());
    }

    public void printBindings() {
        String string = new String();
        for (String key : this.bindings.keySet()) {
            string = string + key + "=" + this.bindings.get(key) + "\n";
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

    public static void main(String[] args) throws Exception {
        new Console();
    }
}
