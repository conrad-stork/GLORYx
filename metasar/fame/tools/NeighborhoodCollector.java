package fame.tools;

import org.openscience.cdk.interfaces.IAtom;

import java.util.Set;

public interface NeighborhoodCollector {

    void collect(IAtom atm, Set<IAtom> neighbors, int current_depth);
}
