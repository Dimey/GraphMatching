import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Stack;

//import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;

public class GraphMatchingAlgorithm {
	static ArrayList<Vertex> patternNodesWithListAndStack = new ArrayList<Vertex>();

	public static ArrayList<Vertex> getSolutions(ArrayList<Vertex> dataNodes, ArrayList<Vertex> patternNodes) {
		// Initialisiere Listen und Stacks
		initiateListsAndStacks(dataNodes, patternNodes);
		
		// Sortiere die Elemente der T-Listen nach dem Startwert
		sortTListsByStartValue(patternNodes);
		
		// Wende rekursiven Algorithmus auf PatternNodes an
		Vertex root = getRoot(patternNodes);
		treeMatch(root);
		
		// Extrahiere EINE Lösung aus dem Stack der Wurzel
		return extractSolution(root);
	}

	private static ArrayList<Vertex> extractSolution(Vertex node) {
		Stack<ArrayList<Vertex>> solutions = node.getProperty("solutionStack");
		for (ArrayList<Vertex> aSolution : solutions) {
			// Letztes nicht zur Lösung gehörendes Root-Objekt löschen
			aSolution.remove(aSolution.size()-1);
			if (!hasPortDuplicates(aSolution))
				return aSolution;
		}
		return null;
	}

	private static boolean hasPortDuplicates(ArrayList<Vertex> aSolution) {
		ArrayList<Integer> listOfUsedPortNumbers = new ArrayList<Integer>();
		for (Vertex node : aSolution) {
			int portNumber = node.getProperty("port");
			if (!listOfUsedPortNumbers.contains(portNumber)) {
				listOfUsedPortNumbers.add(portNumber);
			} else {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private static void treeMatch(Vertex root) {
		Iterator<Vertex> targetListIterator = ((ArrayList<Vertex>) root.getProperty("targetList")).iterator();
		while (targetListIterator.hasNext()) {
			Vertex currentTarget = targetListIterator.next();
			// Speichere aktuelles Element der TargetListe im Knoten
			root.setProperty("currentTarget", currentTarget);
			if(find(root)) {
				ArrayList<Vertex> potentialSolution = new ArrayList<Vertex>(Arrays.asList(currentTarget, root));
				((Stack<ArrayList<Vertex>>) root.getProperty("solutionStack")).push(potentialSolution);
			}
		}	
		generateSolution(root);
	}

	@SuppressWarnings("unchecked")
	private static void generateSolution(Vertex node) {
		ArrayList<Vertex> children = makeArrayList(GraphNumbering.getChildren(node));
		int numberOfChildren = numberOfChildren(node); 
		int counter = 0; 
		while (counter < numberOfChildren) {
			generateSolution(children.get(counter)); 
			Stack<ArrayList<Vertex>> solution = join((Stack<ArrayList<Vertex>>) node.getProperty("solutionStack"), (Stack<ArrayList<Vertex>>) children.get(counter).getProperty("solutionStack"));
			node.setProperty("solutionStack", solution);
			counter++;
		}
	}

	private static Stack<ArrayList<Vertex>> join(Stack<ArrayList<Vertex>> solutionStackFromParent, Stack<ArrayList<Vertex>> solutionStackFromChild) {
		Stack<ArrayList<Vertex>> solution = new Stack<ArrayList<Vertex>>();
		for (ArrayList<Vertex> solutionFromChild : solutionStackFromChild) {
			for (ArrayList<Vertex> solutionFromParent : solutionStackFromParent) {
				int indexOfLastObject = solutionFromChild.size()-1;
				if (solutionFromParent.contains(solutionFromChild.get(indexOfLastObject))) {
					ArrayList<Vertex> tempChild = new ArrayList<Vertex>(solutionFromChild);
					ArrayList<Vertex> tempParent = new ArrayList<Vertex>(solutionFromParent);
					tempChild.removeAll(tempParent);
					if (tempChild.size() != 0) {
						tempParent.addAll(0, tempChild);
						solution.push(tempParent);
					}
				}
			}
		}
		return solution;
	}

	@SuppressWarnings("unchecked")
	private static boolean find(Vertex patternNode) {
		int numberOfChildren = numberOfChildren(patternNode);
		if (numberOfChildren == 0) 
			return true; /* q is leaf node*/ 
		boolean partialSolution = false; 
		int childIndex = 0; 
		ArrayList<Vertex> children = makeArrayList(GraphNumbering.getChildren(patternNode));
		Iterator<Vertex> targetListIterator = ((ArrayList<Vertex>) children.get(childIndex).getProperty("targetList")).iterator();
		/* Mit childIndex werden verschiedene Kinder des PatternNode addressiert*/
		while (partialSolution || targetListIterator.hasNext()) {
			Vertex currentChild = children.get(childIndex);
			Vertex currentTarget = null;
			if (targetListIterator.hasNext()) {
				currentTarget = targetListIterator.next();
				// Speichere aktuelles Element der TargetListe im Knoten
				currentChild.setProperty("currentTarget", currentTarget);
			}
			if ((currentTarget == null) || (Integer) currentTarget.getProperty("start") > (Integer) ((Vertex) patternNode.getProperty("currentTarget")).getProperty("end")) {
				if (partialSolution) {
					childIndex++;
					// Initialisiere neuen Iterator für neues Kind, aber nur wenn der Index valide ist
					if (childIndex < numberOfChildren)
						targetListIterator = ((ArrayList<Vertex>) children.get(childIndex).getProperty("targetList")).iterator();
					partialSolution = false;
				} else {
					int j = 0;
					while ( j < childIndex) {
						cleanStack(children.get(j)); 
						j++;
					}
					return false;
				}
				if (childIndex == numberOfChildren) 
					return true;
			} else if ((Integer) currentTarget.getProperty("start") < (Integer) ((Vertex) patternNode.getProperty("currentTarget")).getProperty("start")) {
				// Zurück zur while-Schleife
			} else if ((Integer)currentTarget.getProperty("depth") == (Integer)((Vertex) patternNode.getProperty("currentTarget")).getProperty("depth")+1 && find(currentChild)) {
				       // Hier müssen noch jeweils das Feld für jede Teillösung hinzugefügt werden,
				       // in dem gespeichert wird, für welchen Patternknoten er der jeweilige Representant ist
				       Vertex currentTargetFromParent = patternNode.getProperty("currentTarget");
				       currentTargetFromParent.setProperty("solutionRepresentant", patternNode);
				       currentTarget.setProperty("solutionRepresentant", currentChild);
					   ArrayList<Vertex> potentialSolution = new ArrayList<Vertex>(Arrays.asList(currentTarget, currentTargetFromParent));
				       ((Stack<ArrayList<Vertex>>) currentChild.getProperty("solutionStack")).push(potentialSolution); 
				       partialSolution=true;
			}
		}
		return false;
	}

	private static ArrayList<Vertex> makeArrayList(Iterable<Vertex> iter) {
		ArrayList<Vertex> list = new ArrayList<Vertex>();
		for (Vertex item : iter)
			list.add(item);
		return list;
	}

	@SuppressWarnings("unchecked")
	private static void cleanStack(Vertex node) {
		int numberOfChildren = numberOfChildren(node);
		int counter = 0;
		ArrayList<Vertex> children = makeArrayList(GraphNumbering.getChildren(node));
		while (counter < numberOfChildren) {
			cleanStack(children.get(counter)); 
			counter++;
		}
		Vertex parentNode = ((ArrayList<Vertex>) ((Stack<ArrayList<Vertex>>) node.getProperty("solutionStack")).peek()).get(1);
		while(!((Stack<ArrayList<Vertex>>) node.getProperty("solutionStack")).empty() && ((ArrayList<Vertex>) ((Stack<ArrayList<Vertex>>) node.getProperty("solutionStack")).peek()).get(1) == parentNode)
			((Stack<ArrayList<Vertex>>) node.getProperty("solutionStack")).pop();
	}

	@SuppressWarnings("unused")
	private static int numberOfChildren(Vertex patternNode) {
		Iterable<Vertex> children = GraphNumbering.getChildren(patternNode);
		int size = 0;
		for(Vertex value : children) {
		   size++;
		}
		return size;
	}

	private static Vertex getRoot(ArrayList<Vertex> patternNodes) {
		for (Vertex patternNode : patternNodes) {
			if (patternNode.getProperty("operationType").equals("OUT")) {
				return patternNode;
			}
		}
		System.out.println("FEHLER: Wurzel ist verloren gegangen!");
		return null;
	}

	@SuppressWarnings("unchecked")
	private static void sortTListsByStartValue(ArrayList<Vertex> patternNodes) {
		for (Vertex patternNode : patternNodes) {
			Collections.sort((ArrayList<Vertex>)patternNode.getProperty("targetList"), new Comparator<Vertex>() {
		        public int compare(Vertex target1, Vertex target2) {
		            return (Integer)target1.getProperty("start") - (Integer)target2.getProperty("start"); // Ascending
		        }
		    });
	    }
	}

	private static void initiateListsAndStacks(ArrayList<Vertex> dataNodes, ArrayList<Vertex> patternNodes) {
		patternNodesWithListAndStack.clear();
		for (Vertex patternNode : patternNodes) {
			String patternNodeOperationType = patternNode.getProperty("operationType");
			ArrayList<Vertex> targetList = new ArrayList<Vertex>();
			// Fülle T-Liste
			for (Vertex dataNode : dataNodes) {
				if (dataNode.getProperty("operationType").equals(patternNodeOperationType)) {
					targetList.add(dataNode);
				}
			}
			// Füge gefüllte T-Listen dem betreffenden Knoten zu
			patternNode.setProperty("targetList", targetList);
			// Füge einen Stack dem betreffenden Knoten zu
			patternNode.setProperty("solutionStack", new Stack<ArrayList<Vertex>>());
		}
	}

}
