package com.tinkerpop.mutant;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class EngineHolder {

    private final String languageName;
    private final String languageVersion;
    private final String engineName;
    private final String engineVersion;
    private final ScriptEngine engine;

    public EngineHolder(ScriptEngineFactory factory) {
        this.languageName = factory.getLanguageName();
        this.languageVersion = factory.getLanguageVersion();
        this.engineName = factory.getEngineName();
        this.engineVersion = factory.getEngineVersion();
        this.engine = factory.getScriptEngine();

    }

    public String getEngineName() {
        return engineName;
    }

    public String getEngineVersion() {
        return engineVersion;
    }

    public String getLanguageName() {
        return languageName;
    }

    public ScriptEngine getEngine() {
        return this.engine;
    }


}
