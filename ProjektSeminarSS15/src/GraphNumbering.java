import java.util.ArrayList;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;

public class GraphNumbering {
	private static int startEnd = -1;
	private static int depth = -1;
	private static int port = 0;
	private static ArrayList<Vertex> numberedNodes = new ArrayList<Vertex>();
	
	public static ArrayList<Vertex> getNumberedNodes(Vertex root) {
		// Setze Wurzel auf den Port 0
		root.setProperty("port", 0);
		
		// Nummeriere alle Knoten ausgehend von der Wurzel
		numbering(root);
		
		// Entferne alle Selektoren (werden nicht weiter benötigt)
		removeSelektorsFromNumberedNodes();
		
		return numberedNodes;
	}
	
	private static void removeSelektorsFromNumberedNodes() {
		ArrayList<Vertex> removableNodes = new ArrayList<Vertex>();
		for (Vertex node : numberedNodes) {
			if (node.getProperty("operationType").equals("SEL")) {
				removableNodes.add(node);
			}
		}
		for (Vertex removableNode : removableNodes) {
			numberedNodes.remove(removableNode);
		}
	}

	private static void numbering(Vertex node) {
		Iterable<Vertex> children = getChildren(node);

		port = port + 1;
		
		if (node.getProperty("operationType").equals("SEL")) {
		    for (Vertex child : children) {
		    	child.setProperty("port", node.getProperty("port"));
			    numbering(child);
		    }
		} else {
			depth = depth + 1;
			startEnd = startEnd + 1;
			node.setProperty("start", startEnd);
			node.setProperty("depth", depth);
			for (Vertex child : children) {
				child.setProperty("port", port);
				numbering(child);
			}
			startEnd = startEnd + 1;
			node.setProperty("end", startEnd);
			depth = depth - 1;
		}

		numberedNodes.add(node);
	}
	
	public static Iterable<Vertex> getChildren(Vertex node) {
		return node.getVertices(Direction.IN);
	}
	
}
