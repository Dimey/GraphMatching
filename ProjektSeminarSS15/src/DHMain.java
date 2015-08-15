import java.io.InputStream;
import java.util.ArrayList;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

//import com.tinkerpop.blueprints.Direction;
//import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;
//import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;

public class DHMain {
 
  private static final String XML_DATA = "src/GraphFiles/DataGraphWithoutCircle.xml";
  private static final String XML_PATTERN = "src/GraphFiles/PatternGraphWithoutCircle.xml";
 
  public static void main(String[] args) throws Exception {
    Graph masterGraph = new TinkerGraph();
    Graph patternGraph = new TinkerGraph();
    
    //Einlesen vom Datengraphen und Patterngrapen
    GraphMLReader masterReader = new GraphMLReader(masterGraph);
    GraphMLReader patternReader = new GraphMLReader(patternGraph);

    InputStream masterStream = new BufferedInputStream(new FileInputStream(XML_DATA));
    masterReader.inputGraph(masterStream);
    
    InputStream patternStream = new BufferedInputStream(new FileInputStream(XML_PATTERN));
    patternReader.inputGraph(patternStream);
    
    // Nummeriere die Knoten des Grapen mit StartEnd-, Depth- und Portattributen und gebe sie als Array zurück
    ArrayList<Vertex> dataNodes = GraphNumbering.getNumberedNodes(getRoot(masterGraph));
    
    // Extrahiere Knoten aus dem Pattern
    ArrayList<Vertex> patternNodes = (ArrayList<Vertex>) patternGraph.getVertices();
    
    // Übergebe nummerierte Knoten und die Knoten des gesuchten Pattern dem Algorithmus
    ArrayList<Vertex> solution = GraphMatchingAlgorithm.getSolutions(dataNodes, patternNodes);
    
    if (solution == null) {
    	System.out.println("Keine Übereinstimmung gefunden!");
    } else {
    	System.out.println("Der Datengraph muss folgende Knoten einsetzen, um den Datenfluss des Kunden abzubilden:");
    	for (Vertex solutionNode  : solution) {
    		System.out.println(solutionNode.getId() + "_" + solutionNode.getProperty("operationType"));
    	}
    }
    
//    //------------------
//    Iterable<Vertex> vertices = patternGraph.getVertices();
// 
//    for (Vertex vertex : vertices) {
//      Iterable<Edge> edges = vertex.getEdges(Direction.IN);
// 
//      for (Edge edge : edges) {
//    	  Vertex outVertex = edge.getVertex(Direction.OUT);
//          Vertex inVertex = edge.getVertex(Direction.IN);
//          
//          String input = outVertex.getId() + "_" + outVertex.getProperty("operationType");
//          String output = inVertex.getId() + "_" + inVertex.getProperty("operationType");
//          
//          String sentence = input + " -> " + output;
//          System.out.println(sentence);
//      }
//    }
//    
//    for (Vertex dataNode : dataNodes) {
//    	System.out.println(dataNode.getId() + "_" + dataNode.getProperty("operationType") + "<" + dataNode.getProperty("start") + ","
//    			+ dataNode.getProperty("end") + ","
//    			+ dataNode.getProperty("depth") + ","
//    			+ dataNode.getProperty("port") + ">");
//    }
//    
//    for (Vertex patternNode : patternNodes) {
//    	System.out.println(patternNode.getId() + "_" + patternNode.getProperty("operationType") + "<" + patternNode.getProperty("targetList").toString() + ">");
//    }
//    
//    //Ausgeben
//    GraphMLWriter writer = new GraphMLWriter(masterGraph);
//    writer.setNormalize(true);
//    writer.outputGraph("src/exampleGraphOut.xml");
//    //-----------------
  }

  protected static Vertex getRoot(Graph graph) {
	Iterable<Vertex> roots = graph.getVertices("operationType", "OUT");
	for (Vertex node : roots) {
		return node;
	}
	System.out.println("FEHLER: Kein Ausgangsknoten im Graphen vorhanden!");
	return null;
  }
}