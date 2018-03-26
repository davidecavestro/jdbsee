package com.github.davidecavestro.jdbsee.jdbcli;

import org.jline.reader.*;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.Levenshtein;

import java.io.IOError;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToIntFunction;

/**
 * Adapts LineReaderImpl to provide sorted candidates on completion
 */
public class LineaReaderAdapter extends LineReaderImpl {
    public LineaReaderAdapter (final Terminal terminal) throws IOException {
        super (terminal);
    }

    public LineaReaderAdapter (final Terminal terminal, final String appName) throws IOException {
        super (terminal, appName);
    }

    public LineaReaderAdapter (final Terminal terminal, final String appName, final Map<String, Object> variables) {
        super (terminal, appName, variables);
    }

    @Override
    protected Comparator<Candidate> getCandidateComparator(boolean caseInsensitive, String word) {
        String wdi = caseInsensitive ? word.toLowerCase() : word;
        ToIntFunction<String> wordDistance = w -> distance(wdi, caseInsensitive ? w.toLowerCase() : w);
        return Comparator
                .comparing(Candidate::value, Comparator.naturalOrder());
//                .comparing(Candidate::value, Comparator.comparingInt(wordDistance))
//                .thenComparing(Candidate::value, Comparator.comparingInt(String::length))
//                .thenComparing(Comparator.naturalOrder());
    }

    protected int distance(String word, String cand) {
        if (word.length() < cand.length()) {
            int d1 = Levenshtein.distance(word, cand.substring(0, Math.min(cand.length(), word.length())));
            int d2 = Levenshtein.distance(word, cand);
            return Math.min(d1, d2);
        } else {
            return Levenshtein.distance(word, cand);
        }
    }

    public static class Builder {

        public static Builder builder() {
            return new Builder();
        }

        Terminal terminal;
        String appName;
        Map<String, Object> variables = new HashMap<> ();
        Map<LineReader.Option, Boolean> options = new HashMap<>();
        History history;
        Completer completer;
        History memoryHistory;
        Highlighter highlighter;
        Parser parser;
        Expander expander;

        private Builder() {
        }

        public Builder terminal(Terminal terminal) {
            this.terminal = terminal;
            return this;
        }

        public Builder appName(String appName) {
            this.appName = appName;
            return this;
        }

        public Builder variables(Map<String, Object> variables) {
            Map<String, Object> old = this.variables;
            this.variables = Objects.requireNonNull(variables);
            this.variables.putAll(old);
            return this;
        }

        public Builder variable(String name, Object value) {
            this.variables.put(name, value);
            return this;
        }

        public Builder option(LineReader.Option option, boolean value) {
            this.options.put(option, value);
            return this;
        }

        public Builder history(History history) {
            this.history = history;
            return this;
        }

        public Builder completer(Completer completer) {
            this.completer = completer;
            return this;
        }

        public Builder highlighter(Highlighter highlighter) {
            this.highlighter = highlighter;
            return this;
        }

        public Builder parser(Parser parser) {
            this.parser = parser;
            return this;
        }

        public Builder expander(Expander expander) {
            this.expander = expander;
            return this;
        }

        public LineReader build() {
            Terminal terminal = this.terminal;
            if (terminal == null) {
                try {
                    terminal = TerminalBuilder.terminal();
                } catch (IOException e) {
                    throw new IOError (e);
                }
            }
            LineReaderImpl reader = new LineaReaderAdapter(terminal, appName, variables);
            if (history != null) {
                reader.setHistory(history);
            } else {
                if (memoryHistory == null) {
                    memoryHistory = new DefaultHistory ();
                }
                reader.setHistory(memoryHistory);
            }
            if (completer != null) {
                reader.setCompleter(completer);
            }
            if (highlighter != null) {
                reader.setHighlighter(highlighter);
            }
            if (parser != null) {
                reader.setParser(parser);
            }
            if (expander != null) {
                reader.setExpander(expander);
            }
            for (Map.Entry<LineReader.Option, Boolean> e : options.entrySet()) {
                reader.option(e.getKey(), e.getValue());
            }
            return reader;
        }
    }
}
