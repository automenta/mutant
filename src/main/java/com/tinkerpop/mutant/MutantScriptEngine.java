package com.tinkerpop.mutant;

import javax.script.*;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class MutantScriptEngine extends AbstractScriptEngine {

    private final ScriptEngineManager manager = new ScriptEngineManager();
    private final List<EngineHolder> engines = new ArrayList<EngineHolder>();
    private final int currentEngine = 0;

    public MutantScriptEngine() throws RuntimeException {
        // for ruby
        System.setProperty("org.jruby.embed.localvariable.behavior", "persistent");
        for (ScriptEngineFactory factory : manager.getEngineFactories()) {
            this.engines.add(new EngineHolder(factory));
        }
        if (this.engines.size() == 0) {
            throw new RuntimeException("No script engines to load");
        }
    }

    public ScriptEngineFactory getFactory() {
        return new MutantScriptEngineFactory();
    }

    public Bindings createBindings() {
        return new SimpleBindings();
    }

    public Object eval(String script, ScriptContext context) {
        return this.eval(new StringReader(script), context);
    }

    public Object eval(Reader reader, ScriptContext context) {
        return null;
    }


}
