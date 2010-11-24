package com.tinkerpop.mutant;

import com.tinkerpop.gremlin.compiler.context.VariableLibrary;
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
    Bindings bindings = new VariableLibrary();
    int currentEngine = -1;
    PrintStream output = System.out;

    public Console() throws Exception {
        ScriptEngineManager manager = new ScriptEngineManager();
        for (ScriptEngineFactory factory : manager.getEngineFactories()) {
            this.output.println("Enabling " + factory.getEngineName() + "[" + factory.getLanguageName() + "]");
            this.scriptEngines.add(factory.getScriptEngine());
            this.scriptNames.add(factory.getLanguageName());
        }
        if (this.scriptEngines.size() == 0) {
            throw new Exception("No script engines to load");
        } else {
            this.currentEngine = 0;
        }

        this.primaryLoop();
    }

    public void primaryLoop() throws Exception {


        final ConsoleReader reader = new ConsoleReader();
        reader.setBellEnabled(false);
        reader.setUseHistory(true);

        try {
            History history = new History();
            history.setHistoryFile(new File(".mutant-history"));
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
                    if (line.equals("?quit"))
                        return;
                    else if (line.equals("?next"))
                        this.currentEngine = (this.currentEngine + 1) % this.scriptEngines.size();
                    else if (line.equals("?binds"))
                        this.output.println(this.getBindingsString());
                } else {
                    this.output.println(getScriptEngine().eval(line, this.bindings));
                }
            } catch (Exception e) {
                this.output.println(e.getMessage());
            }
        }
    }

    public String getBindingsString() {
        String string = new String();
        for(String key : this.bindings.keySet()) {
            string = string + key + "=" + this.bindings.get(key) + "\n";
        }
        return string.trim();
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

    public static void main(String[] args) throws Exception {
        new Console();
    }
}
