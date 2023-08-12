package au.edu.unimelb.processmining.accuracy.abstraction;

import static au.edu.unimelb.processmining.accuracy.abstraction.subtrace.Subtrace.INIT;

import au.edu.unimelb.processmining.accuracy.abstraction.intermediate.AAEdge;
import au.edu.unimelb.processmining.accuracy.abstraction.intermediate.AANode;
import au.edu.unimelb.processmining.accuracy.abstraction.intermediate.AutomatonAbstraction;
import au.edu.unimelb.processmining.accuracy.abstraction.set.SetAbstraction;
import au.edu.unimelb.processmining.accuracy.abstraction.set.SetLabel;
import au.edu.unimelb.processmining.accuracy.abstraction.subtrace.Subtrace;
import au.edu.unimelb.processmining.accuracy.abstraction.subtrace.SubtraceAbstraction;
import java.util.*;

/**
 * Created by Adriano on 23/01/18.
 */
public class ProcessAbstraction {

    private static final int MAXL = 1000000;
    private static final int MAXE = 2000000;
    private final AutomatonAbstraction automatonAbstraction;

    public ProcessAbstraction(AutomatonAbstraction automatonAbstraction) {
        this.automatonAbstraction = automatonAbstraction;
    }

    public SubtraceAbstraction subtrace(int order) {
        SubtraceAbstraction abstraction = new SubtraceAbstraction(order);

//        System.out.println("DEBUG - converting automaton to subtrace abstraction...");

        AANode source = automatonAbstraction.getSource();
        ArrayList<AANode> toVisit = new ArrayList<>();
        Map<AANode, Set<Subtrace>> explored = new HashMap<>();
        Set<Subtrace> tmpExplored;
        int i = 0;
        Subtrace tmpSubtrace = new Subtrace(order);

//        System.out.print("DEBUG - generating labels...");
        toVisit.add(source);
        source.addSubtraceIA(tmpSubtrace);

        while (!toVisit.isEmpty() && i < MAXL) {
            source = toVisit.remove(0);

            if (!explored.containsKey(source)) explored.put(source, new HashSet<>());
            tmpExplored = explored.get(source);

            for (Subtrace subtrace : new HashSet<>(source.getSubtraces())) {
//                System.out.println("debug - label " + source.getID() + " - " + label);
                if (tmpExplored.contains(subtrace)) continue;
                else tmpExplored.add(subtrace);

                if (automatonAbstraction.getOutgoings().get(source.getID()).isEmpty()) {
                    tmpSubtrace = new Subtrace(subtrace, INIT);
                    abstraction.addSubtrace(tmpSubtrace);
                } else {
                    for (AAEdge e : automatonAbstraction.getOutgoings().get(source.getID())) {
                        if (e.getEID() != AutomatonAbstraction.TAU) {
                            tmpSubtrace = new Subtrace(subtrace, e.getEID());
                        } else tmpSubtrace = new Subtrace(subtrace);

//                    we are adding the labels straight to the abstraction, using the automaton to handle the exploration
                        abstraction.addSubtrace(tmpSubtrace);
                        if (!e.getTGT().addSubtraceIA(tmpSubtrace)) continue;
                        i++;
                        toVisit.add(e.getTGT());
                    }
                }
            }
        }

        if ((double) i / (double) MAXL > 1)
            System.out.println("WARNING - done at " + (double) i / (double) MAXL);

        return abstraction;
    }

    public SetAbstraction set(int order) {
        SetAbstraction abstraction = new SetAbstraction();
        Map<Integer, Set<AAEdge>> outgoings = automatonAbstraction.getOutgoings();
        String tl;

        generateSetLabels(order);

        for (AANode src : automatonAbstraction.getNodes())
            for (String sl : src.getLabels()) {
                abstraction.addNode(sl);
                for (AAEdge e : outgoings.get(src.getID())) {
                    abstraction.addNode(sl);
                    tl = SetLabel.reverseAdd(sl, e.getEID());
                    abstraction.addEdge(sl, tl, e.getEID());
                }
            }

        return abstraction;
    }


//    METHODS for SET Abstraction

    private void generateSetLabels(int size) {
        SetLabel label = new SetLabel(size);
        explore(automatonAbstraction.getSource(), label);
    }

    private void explore(AANode node, SetLabel label) {
        if (!node.addLabel(label.print()))
            return; //this node already has this label, so we do not need to explore it further

        for (AAEdge e : automatonAbstraction.getOutgoings().get(node.getID())) {
            explore(e.getTGT(), new SetLabel(label, e.getEID()));
        }
    }
}
