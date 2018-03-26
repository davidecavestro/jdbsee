package org.jline.builtins;

import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.*;

/**
 * Adapted from jline TreeCompleter in order to provide sorted completers.
 */
public class SortedTreeCompleter implements org.jline.reader.Completer {

    final Map<String, org.jline.reader.Completer> completers = new LinkedHashMap<> ();
    final Completers.RegexCompleter completer;

    public SortedTreeCompleter(Completers.TreeCompleter.Node... nodes) {
        this(Arrays.asList(nodes));
    }

    public SortedTreeCompleter(List<Completers.TreeCompleter.Node> nodes) {
        StringBuilder sb = new StringBuilder();
        addRoots(sb, nodes);
        completer = new Completers.RegexCompleter (sb.toString(), completers::get);
    }

    public static Completers.TreeCompleter.Node node(Object... objs) {
        org.jline.reader.Completer comp = null;
        List<Candidate> cands = new ArrayList<> ();
        List<Completers.TreeCompleter.Node> nodes = new ArrayList<>();
        for (Object obj : objs) {
            if (obj instanceof String) {
                cands.add(new Candidate((String) obj));
            } else if (obj instanceof Candidate) {
                cands.add((Candidate) obj);
            } else if (obj instanceof Completers.TreeCompleter.Node) {
                nodes.add((Completers.TreeCompleter.Node) obj);
            } else if (obj instanceof org.jline.reader.Completer) {
                comp = (org.jline.reader.Completer) obj;
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (comp != null) {
            if (!cands.isEmpty()) {
                throw new IllegalArgumentException();
            }
            return new Completers.TreeCompleter.Node (comp, nodes);
        } else if (!cands.isEmpty()) {
            return new Completers.TreeCompleter.Node ((r, l, c) -> c.addAll(cands), nodes);
        } else {
            throw new IllegalArgumentException();
        }
    }

    void addRoots(StringBuilder sb, List<Completers.TreeCompleter.Node> nodes) {
        if (!nodes.isEmpty()) {
            sb.append(" ( ");
            boolean first = true;
            for (Completers.TreeCompleter.Node n : nodes) {
                if (first) {
                    first = false;
                } else {
                    sb.append(" | ");
                }
                String name = "c" + completers.size();
                completers.put(name, n.completer);
                sb.append(name);
                addRoots(sb, n.nodes);
            }
            sb.append(" ) ");
        }
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        completer.complete(reader, line, candidates);
    }
}
