package hre.bila;

/************************************************************************************************
 * Class  HBReportHandler extends BusinessLayer
 * Processes data for recalcukation of relationship code pairs
 * As created by CoPilot and replaced by ChatGPT to Create relationship codes
 * Sends requests to database over Database Layer API
 ************************************************************************************************
 * v0.05.0033 2026-04-10 First draft (D Ferguson)
 * 			  2026-04-12 Second draft (D Ferguson)
 * 			  2026-04-13 Getting cousins almost right (D Ferguson)
 * 			  2026-04-14 Remove invalid codes for no blood relationships (D Ferguson)
 * 			  2026-04-16 Handle aunts/cousins greater than 4 generations (D Ferguson)
 * 			  2026-04-18 Used ChatGPT code to fix all problems (D Ferguson)
 * 			  2026-05-01 Used ChatGPT to extend to 2 relationships (D Ferguson)
 * v0.05.0034 2026-08-03 Converted by ChatGPT to also build PIDS into path lists (D Ferguson)
 * 			  2026-08-04 Split off from HBReportHandler into this handler (D Ferguson)
 * 			  2026-08-06 Updated by ChatGPT to store relation paths of PIDs
 * 						 Also built a relationship diagram from teh PID path lists
 * 			  2026-08-13 Make ascendancies/descendencies direct line
 * 			  2026-08-17 Add ability to return PID of clicked area of display (ChatGPT)
 * 			  2026-08-20 Moved in all Relation handling methods from HG0506ManagePerson (D Ferguson)
 ************************************************************************************************/

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.LongConsumer;

import javax.swing.JPanel;
import javax.swing.UIManager;

import hre.gui.HGlobal;


public class HBRelationHandler extends HBBusinessLayer {

	protected ResultSet pointT401_PERSONS;
	HBProjectOpenData pointOpenProject;
	static HBPersonHandler pointPersonHandler;
	HBRelationHandler pointRelationHandler;
	static HashMap<Long,Object[]> parentMap = new HashMap<Long,Object[]>();

	public HBRelationHandler(HBProjectOpenData pointOpenProject)  {
		this.pointOpenProject = pointOpenProject;
		long personTablePID;
		pointDBlayer = pointOpenProject.getPointDBlayer();
		pointPersonHandler = pointOpenProject.getPersonHandler();
		pointT401_PERSONS =  pointOpenProject.getT401Persons();
		try {
			pointT401_PERSONS.beforeFirst();
			while (pointT401_PERSONS.next()) {
				Object[] parents = new Object[2];
				parents[0] = pointT401_PERSONS.getLong("SPERM_PROVIDER_RPID");
				parents[1] = pointT401_PERSONS.getLong("EGG_PROVIDER_RPID");
				personTablePID = pointT401_PERSONS.getLong("PID");
				parentMap.put(personTablePID, parents);
			}
		} catch (SQLException sqle) {
			System.out.println(" ERROR in  RelationCalculation: " + sqle.getMessage());
			sqle.printStackTrace();
		}
	}

/**
 * Recalculate Relationships and save in all T401 records
 * public void recalcRelationsT401(long focusPersonPID)
 * @param focusPersonPID
 * @throws HBException
 */
	  public void recalcRelationsT401(long focusPersonPID) throws HBException {
		  long personTablePID;
		  int T401_RELATE1, T401_RELATE2, T401_RELATE3, T401_RELATE4;
		  boolean updateT401 = true; // Controls update of T401
		  pointT401_PERSONS =  pointOpenProject.getT401Persons();
		  int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		  // Start transaction
		  updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
		  try {
			  pointT401_PERSONS.beforeFirst();
			  while (pointT401_PERSONS.next()) {
				  personTablePID = pointT401_PERSONS.getLong("PID");
				  Relationship relations = findRelationships(focusPersonPID, personTablePID );
			 // following updates relations
				  T401_RELATE1 = relations.primary.x;
				  T401_RELATE2 = relations.primary.y;
				  T401_RELATE3 = relations.secondary.x;
						  T401_RELATE4 = relations.secondary.y;

			 // Debugging code
/*				  int numberOfPerson = 0, errorIndex = 0;
				  int RELATE1 = pointT401_PERSONS.getInt("RELATE1");
				  int RELATE2 = pointT401_PERSONS.getInt("RELATE2");
				  if (RELATE1 != T401_RELATE1 || RELATE2 != T401_RELATE2) {
					  errorIndex++;
					  System.out.println(" " + errorIndex + " - " + pointPersonHandler.getPersonName(personTablePID)
					  + " - PID: "+ personTablePID + " TMG: (" + RELATE1 + "," + RELATE2 + ") HRE: "
					  + "(" + T401_RELATE1 + "," + T401_RELATE2 + ")");
				  }
				  if ((T401_RELATE3 != 0) || (T401_RELATE4 != 0)) {
					  System.out.println(pointPersonHandler.getPersonName(personTablePID)
							  + " - PID: "+ personTablePID
							  +" primar: " + "(" + T401_RELATE1 + "," + T401_RELATE2 + ")"
							  +" second: " + "(" + T401_RELATE3 + "," + T401_RELATE4 + ")");
					  numberOfPerson++;
				  } */

			 // T401 update code
				  if (updateT401) {
					  pointT401_PERSONS.updateInt("RELATE1", T401_RELATE1);
					  pointT401_PERSONS.updateInt("RELATE2", T401_RELATE2);
					  pointT401_PERSONS.updateInt("RELATE3", T401_RELATE3);
					  pointT401_PERSONS.updateInt("RELATE4", T401_RELATE4);
					  pointT401_PERSONS.updateRow();
				  }
			  }
			  // Debugging output
			  //System.out.println( " Deviations: " + errorIndex + " Total tested: " + numberOfPerson);
			  //System.out.println( " Total with REL-3 or REL-4 != 0: " + numberOfPerson);

		// End transaction
			  updateTableData("COMMIT", dataBaseIndex);
		  } catch (SQLException sqle) {
		 // Roll back transaction
			  updateTableData("ROLLBACK", dataBaseIndex);
			  if (HGlobal.writeLogs) {
				  HB0711Logging.logWrite("ERROR: in recalcRelationsT401 " + sqle.getMessage());	//$NON-NLS-1$
				  HB0711Logging.printStackTraceToFile(sqle);
			  }
		  }
	  }

  /**
   * Clear the relation variables in T401 and clear the focusPerson in T126
   * @throws HBException
   */
  	  public void clearRelationDataInT401() throws HBException {
  		  pointT401_PERSONS =  pointOpenProject.getT401Persons();
  		  int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
  	// Start transaction
  		  updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
  	// Set focus person in T126 to null_RPID
  		  pointOpenProject.setFocusPersonPID(null_RPID);
  		  try {
  			  pointT401_PERSONS.beforeFirst();
  			  while (pointT401_PERSONS.next()) {
  				  pointT401_PERSONS.updateInt("RELATE1", 0);
  				  pointT401_PERSONS.updateInt("RELATE2", 0);
  				  pointT401_PERSONS.updateInt("RELATE3", 0);
  				  pointT401_PERSONS.updateInt("RELATE4", 0);
  				  pointT401_PERSONS.updateRow();
  			  }

  		// End transaction
  			  updateTableData("COMMIT", dataBaseIndex);
  		  } catch (SQLException sqle) {
  		// Roll back transaction
  				updateTableData("ROLLBACK", dataBaseIndex);
  				if (HGlobal.writeLogs) {
  					HB0711Logging.logWrite("ERROR: in clearRelationsInT401 " + sqle.getMessage());	//$NON-NLS-1$
  					HB0711Logging.printStackTraceToFile(sqle);
  				}
  		  }
  	  }

/**
 * Return parents of a given PID
 * public static Object[] getParentsPID(long personTablePID)
 * @param personTablePID
 * @return
 */
	    public static Object[] getParentsPID(long personTablePID) {
	    	if (parentMap.containsKey(personTablePID))
	    		return parentMap.get(personTablePID);
			return null;
	    }

/**
 * Construct a Relationship (x,y) pair
 */
	    public static class Relationship {
	        public CodePair primary;
	        public CodePair secondary;
	    	public RelationshipPath primaryPath;
	    	public RelationshipPath secondaryPath;
	        public Relationship(CodePair p1, CodePair p2) {
	            this.primary = p1;
	            this.secondary = p2;
	        }
	    }
	    public static class CodePair {
	        public int x, y;
	        public CodePair(int x, int y) {
	            this.x = x;
	            this.y = y;
	        }
	    }

/**
 * One occurrence of an ancestor.
 * Stores both the depth and the path used to reach it.
 */
	    static class AncestorOccurrence {
	        int depth;
	        List<Long> path;
	        AncestorOccurrence(int depth, List<Long> path) {
	            this.depth = depth;
	            this.path = path;
	        }
	    }

/**
 * Queue item used during breadth-first search.
 */
	    static class QueueItem {
	        long pid;
	        int depth;
	        List<Long> path;
	        QueueItem(long pid, int depth, List<Long> path) {
	            this.pid = pid;
	            this.depth = depth;
	            this.path = path;
	        }
	    }

/**
 * Build ancestor map: PID generations up from startPID
 *           depth 0 = self, 1 = parent, 2 = grandparent, ...
 * public static Map<Long, Integer> buildAncestorMap(long startPID)
 * @param startPID
 * @return
 */
	    private static Map<Long, List<AncestorOccurrence>> buildAncestorMap(
	            long startPID, long NULL_RPID, int maxDepth) {

	        Map<Long, List<AncestorOccurrence>> map = new HashMap<>();
	        Queue<QueueItem> q = new LinkedList<>();

	        List<Long> startPath = new ArrayList<>();
	        startPath.add(startPID);
	        q.add(new QueueItem(startPID, 0, startPath));

	        while (!q.isEmpty()) {
	            QueueItem cur = q.poll();
	            long id = cur.pid;
	            int depth = cur.depth;
	            if (depth > maxDepth)
	                continue;
	            if (id == NULL_RPID)
	                continue;

	            map.computeIfAbsent(id, k -> new ArrayList<>())
	               .add(new AncestorOccurrence(depth, new ArrayList<>(cur.path)));
	            Object[] parents = getParentsPID(id);
	            if (parents == null)
	                continue;
	            for (Object p : parents) {
	                if (p == null)
	                    continue;
	                long parent = (Long)p;
	                if (parent == NULL_RPID)
	                    continue;
	                List<Long> nextPath = new ArrayList<>(cur.path);
	                nextPath.add(parent);
	                q.add(new QueueItem(parent, depth + 1, nextPath));
	            }
	        }
	        return map;
	    }
/***
 * Further Helper methods for findRelationships
 */
	    static class LcaCandidate {
	        long lcaPID;
	        int df;
	        int dt;
	        CodePair code;
	        RelationshipPath graph;
	        int score;
	        LcaCandidate(long lcaPID, int df, int dt,  CodePair code, RelationshipPath graph) {
	            this.lcaPID = lcaPID;
	            this.df = df;
	            this.dt = dt;
	            this.code = code;
	            this.graph = graph;
	            this.score = score(code);
	        }
	    }
	    static int score(CodePair c) {
	        if (c.x==0 && c.y==0)
	            return 9999;
	        if (c.x==0 || c.y==0)
	            return Math.abs(c.x)+Math.abs(c.y);
	        return
	           100 * Math.min(Math.abs(c.x),Math.abs(c.y))
	          +10 * Math.abs(Math.abs(c.x)-Math.abs(c.y));
	    }
	    static String canonicalBand(CodePair c) {
	        int a=Math.abs(c.x);
	        int b=Math.abs(c.y);
	        if (a>b) {
	            int t=a;
	            a=b;
	            b=t;
	        }
	        return a + ":" + b;
	    }
	    static boolean dominates(LcaCandidate a, LcaCandidate b) {
	        return
	          a.df <= b.df &&
	          a.dt <= b.dt &&
	          (a.df < b.df || a.dt < b.dt);
	    }

/**
 * Find Relationship - up to 2 relation pairs between focus and target
 * @param focusPID
 * @param targetPID
 * @return
 */
	    public Relationship findRelationships(long focusPID, long targetPID) {
	    	final long NULL_RPID = 1999999999999999L;
	    	// Assumed that no project will be deeper than 60 generations (about 2000 years)
	    	final int MAX_DEPTH = 60;

	    	// List<Relationship> results = new ArrayList<>();
	    	Relationship results;
	    	// Focus person is not related to itself
	    	if (focusPID == targetPID) {
	    		results =  new Relationship(new CodePair(0,0), new CodePair(0,0));
	    		return results;
	    	}

	    	Map<Long, List<AncestorOccurrence>> focusMap =
	    		    buildAncestorMap(focusPID, NULL_RPID, MAX_DEPTH);
	    	Map<Long, List<AncestorOccurrence>> targetMap =
	    		    buildAncestorMap(targetPID, NULL_RPID, MAX_DEPTH);

	    	Set<Long> allLCAs = new HashSet<>(focusMap.keySet());
	    	allLCAs.retainAll(targetMap.keySet());

// Step 1
	    	Map<String, LcaCandidate> bestPerBand = new HashMap<>();

	    	for (Long lcaId : focusMap.keySet()) {
	    		if (!targetMap.containsKey(lcaId))
	    			continue;

	    		List<AncestorOccurrence> fOcc = focusMap.get(lcaId);
	    		List<AncestorOccurrence> tOcc = targetMap.get(lcaId);
	    		int bestDF = Integer.MAX_VALUE;
	    		int bestDT = Integer.MAX_VALUE;
	    		int bestScore = Integer.MAX_VALUE;

	    		// Remember WHICH paths produced the winning depths
	    		List<Long> bestFocusPath = null;
	    		List<Long> bestTargetPath = null;

	    		for (AncestorOccurrence f : fOcc) {
	    		    for (AncestorOccurrence t : tOcc) {
	    		        int score = f.depth + t.depth;
	    		        if (score < bestScore) {
	    		            bestScore = score;
	    		            bestDF = f.depth;
	    		            bestDT = t.depth;
	    		            bestFocusPath = f.path;
	    		            bestTargetPath = t.path;
	    		        }
	    		    }
	    		}

	    		RelationshipPath graph = new RelationshipPath();
	    		graph.lcaPID = lcaId;
	    		graph.focusPath.addAll(bestFocusPath);
	    		graph.targetPath.addAll(bestTargetPath);

	    		if (bestDF == Integer.MAX_VALUE)
	    			continue;

	    		// derive relationship
	    		CodePair primary;
	    		if (bestDF == 0 && bestDT == 0)
	    			primary = new CodePair(0, 0);
	    		else if (bestDT == 0)
	    			primary = new CodePair(0, bestDF);
	    		else if (bestDF == 0)
	    			primary = new CodePair(-bestDT, 0);
	    		else if (bestDF == 1 && bestDT == 1)
	    			primary = new CodePair(-1, -1);
	    		else if ((bestDF == 1 && bestDT == 2) || (bestDF == 2 && bestDT == 1)) {
	    			if (bestDF < bestDT)
	    				primary = new CodePair(-2, -1);
	    			else
	    				primary = new CodePair(-1, -2);
	    		}
	    		else {
	    			int min = Math.min(bestDF, bestDT);
	    			int max = Math.max(bestDF, bestDT);
	    			int cousin = min - 1;
	    			int removed = max - min;
	    			primary = new CodePair(-(cousin + 1), -(cousin + 1 + removed));
	    		}

	    		String band = canonicalBand(primary);

	    		LcaCandidate existing = bestPerBand.get(band);
	    		if (existing == null || score(primary) < score(existing.code)) {
	    			bestPerBand.put(
	    					band,
	    					new LcaCandidate(lcaId, bestDF, bestDT, primary,graph)
	    					);
	    		}
	    	}
	    	// convert map to list
	    	List<LcaCandidate> candidates = new ArrayList<>(bestPerBand.values());

// Step 2 - filter out higher candidate noise
	    	int minSum = Integer.MAX_VALUE;
	    	// find minimum depth
	    	for (LcaCandidate c : candidates) {
	    		int s = c.df + c.dt;
	    		if (s < minSum) minSum = s;
	    	}
	    	// allow one extra level above
	    	int maxAllowed = minSum + 1;
	    	List<LcaCandidate> filtered = new ArrayList<>();
	    	for (LcaCandidate c : candidates) {
	    		int sum = c.df + c.dt;
	    		if (sum <= maxAllowed)
	    			filtered.add(c);
	    	}
	    	candidates = filtered;

// Step 3 - sort
	    	candidates.sort(Comparator.comparingInt(c -> c.score));

// Step 4 - Pick 2 independent bands
	    	LcaCandidate first = null;
	    	LcaCandidate second = null;
	    	Set<String> usedBands = new HashSet<>();
	    	for (LcaCandidate c : candidates) {
	    		String band = canonicalBand(c.code);
	    		if (first == null) {
	    			first = c;
	    			usedBands.add(band);
	    			continue;
	    		}
	    		if (usedBands.contains(band))
	    			continue;
	    		second = c;
	    		break;
	    	}

// Step 5 Suppress bogus secondary if primary is parent/child
	    	if (first != null && second != null) {
	    	    boolean isParentChild =
	    	        (first.code.x == 0 && Math.abs(first.code.y) == 1) ||
	    	        (first.code.y == 0 && Math.abs(first.code.x) == 1);
	    	    if (isParentChild)
	    	    	second = null;
	    	}

// Step 6 - return Results
	    	// Results consists of the code pair and the path lists of PIDs
	    	// that connect focus to LCA and target to LCA
	    	if (first == null) {
	    	    results = new Relationship(
	    	        new CodePair(0, 0),
	    	        new CodePair(0, 0));
	    	}
	    	else if (second == null) {
	    	    results = new Relationship(
	    	        first.code,
	    	        new CodePair(0, 0));
	    	    results.primaryPath = first.graph;
	    	}
	    	else {
	    	    results = new Relationship(
	    	        first.code,
	    	        second.code);
	    	    results.primaryPath = first.graph;
	    	    results.secondaryPath = second.graph;
	    	}
	    	return results;
	    }		// findRelationships

/****************************************************
 * Code from here onsupports creation of
 * graphical view of results from findRelationships
 ***************************************************/

/**
 * Stores the two proof paths for one relationship.
 */
	    public static class RelationshipPath {
	    	public long lcaPID;
	    	public final List<Long> focusPath = new ArrayList<>();
	    	public final List<Long> targetPath = new ArrayList<>();
	    	public RelationshipPath() {
	    	}

	    	public RelationshipPath(List<Long> focusPath, List<Long> targetPath) {
	    		if (focusPath != null)
	    			this.focusPath.addAll(focusPath);
	    		if (targetPath != null)
	    			this.targetPath.addAll(targetPath);
	    	}

	    	public Long getLcaPID() {
	    		Long focusLca = lastOrNull(focusPath);
	    		Long targetLca = lastOrNull(targetPath);
	    		//IF lca =null, return null
	    		if (focusLca == null || targetLca == null) return null;
	    		//if both paths identical, return null lca
	    		return focusLca.equals(targetLca) ? focusLca : null;
	    	}

	    	private static Long lastOrNull(List<Long> path) {
	    		if (path == null || path.isEmpty()) return null;
	    		// return the last PID in the path
	    		return path.get(path.size() - 1);
	    	}
	    }

/**
 * A laid-out person node
 */
	    private static class NodeLayout {
	        final long pid;
	        final Rectangle bounds;
	        final Side side;
	        NodeLayout(long pid, Rectangle bounds, Side side) {
	            this.pid = pid;
	            this.bounds = bounds;
	            this.side = side;
	        }
	    }
	    private enum Side {
	        FOCUS,
	        TARGET
	    }

/**
 * Panel that lays out and paints one relationship diagram
 */
	    public static class RelationshipPanel extends JPanel {
	      	private static final long serialVersionUID = 001L;
	        private static final int BOX_WIDTH = 250;
	        private static final int BOX_HEIGHT = 34;
	        private static final int ROW_GAP = 24;
	        private static final int COLUMN_GAP = 100;
	        private static final int MARGIN = 50;
	        private static final int MIN_PANEL_WIDTH = 420;
	        private static final int MIN_PANEL_HEIGHT = 180;
	        private final RelationshipPath path;
	        private final List<NodeLayout> focusNodes = new ArrayList<>();
	        private final List<NodeLayout> targetNodes = new ArrayList<>();
	        private Rectangle lcaBounds;
	        private Dimension calculatedSize = new Dimension(MIN_PANEL_WIDTH, MIN_PANEL_HEIGHT);

	        public RelationshipPanel(RelationshipPath path) {
	            this.path = path != null ? path : new RelationshipPath();
	            Font textFieldFont = UIManager.getFont("TextField.font");
	            if (textFieldFont != null) setFont(textFieldFont);
	            setBackground(UIManager.getColor("TableHeader.background"));		// diagram background colour
	            setOpaque(true);
	            computeLayout();
	            // Add mouse listener for click action in panel
	            addMouseListener(new MouseAdapter() {
	                @Override
	                public void mouseClicked(MouseEvent e) {
	                    handleMouseClick(e);
	                }
	            });
	            // Add mouse listener to change cursor to show clickable area
		        addMouseMotionListener(new MouseMotionAdapter() {
		            public void mouseMoved(MouseEvent e) {
		                long pid = findPIDat(e.getPoint());
		                if (pid != 0L) setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		                else setCursor(Cursor.getDefaultCursor());
		            }
		        });
	        }
	        // routine to get PID of clicked person area
	        private void handleMouseClick(MouseEvent e) {
	            long pid = findPIDat(e.getPoint());
	            if (pid == 0L) return;
	            if (personClickHandler != null)
	                personClickHandler.accept(pid);
	        }
	        // routine to lookup PID at the clicked point of the disaplay
	        private long findPIDat(Point point) {
	            // Check LCA first
	            if (lcaBounds != null && lcaBounds.contains(point)) {
	                Long lcaPID = path.getLcaPID();
	                if (lcaPID != null) return lcaPID;
	            }
	            // Focus branch
	            for (NodeLayout node : focusNodes)
	                if (node.bounds.contains(point)) return node.pid;
	            // Target branch
	            for (NodeLayout node : targetNodes)
	                if (node.bounds.contains(point)) return node.pid;
	            return 0L;
	        }

	        private LongConsumer personClickHandler;
	        public void setPersonClickHandler(
	                LongConsumer handler) {
	            this.personClickHandler = handler;
	        }



	        // Recomputes the node positions and required panel size
	        private void computeLayout() {
	            focusNodes.clear();
	            targetNodes.clear();
	            int leftX = MARGIN;
	            int rightX = leftX + BOX_WIDTH + COLUMN_GAP;
	            int lcaX = leftX + ((BOX_WIDTH + COLUMN_GAP) / 2);
	            int lcaY = MARGIN;

	            lcaBounds = new Rectangle(lcaX, lcaY,  BOX_WIDTH, BOX_HEIGHT);

	            List<Long> focusWithoutLca = withoutFinalLca(path.focusPath);
	            List<Long> targetWithoutLca = withoutFinalLca(path.targetPath);

	            int firstRowY = lcaY + BOX_HEIGHT + ROW_GAP;

	            // Direct ancestor/descendant relationship:
	            // one side consists only of the LCA.
	            boolean directLine = path.focusPath.size() == 1 || path.targetPath.size() == 1;

	            if (directLine) {
	                // Put the actual descent/ascent chain directly below the LCA.
	                int directX = lcaX;
	                if (!focusWithoutLca.isEmpty())
	                    layoutBranch(focusWithoutLca, directX, firstRowY, Side.FOCUS, focusNodes);
	                if (!targetWithoutLca.isEmpty())
	                    layoutBranch(targetWithoutLca, directX, firstRowY, Side.TARGET, targetNodes);
	            }
	            else {
	                // Normal collateral relationship:
	                // focus branch left, target branch right.
	                layoutBranch(focusWithoutLca, leftX, firstRowY, Side.FOCUS, focusNodes);
	                layoutBranch(targetWithoutLca, rightX, firstRowY, Side.TARGET, targetNodes);
	            }

	            int longestBranch = Math.max(focusWithoutLca.size(), targetWithoutLca.size());
	            int requiredWidth = MARGIN + BOX_WIDTH + COLUMN_GAP + BOX_WIDTH + MARGIN;
	            int requiredHeight = MARGIN + BOX_HEIGHT + (longestBranch*(BOX_HEIGHT + ROW_GAP))  + MARGIN;

	            calculatedSize = new Dimension(
	                    Math.max(MIN_PANEL_WIDTH, requiredWidth),
	                    Math.max(MIN_PANEL_HEIGHT, requiredHeight)
	            );
	            revalidate();
	        }

	        private void layoutBranch(List<Long> pathWithoutLca, int x, int firstRowY,
	                Side side, List<NodeLayout> destination) {
	            int y = firstRowY;
	             // The source path is stored as:
	             // person -> parent -> ... -> LCA
	             // To draw from the LCA downward, iterate backwards.
	            for (int i = pathWithoutLca.size() - 1; i >= 0; i--) {
	                long pid = pathWithoutLca.get(i);
	                destination.add(new NodeLayout(pid, new Rectangle(x, y, BOX_WIDTH, BOX_HEIGHT), side));
	                y += BOX_HEIGHT + ROW_GAP;
	            }
	        }

	        private List<Long> withoutFinalLca(List<Long> source) {
	            List<Long> copy = new ArrayList<>();
	            if (source == null || source.isEmpty()) return copy;

	            int lastIndex = source.size() - 1;
	            for (int i = 0; i < lastIndex; i++) {
	                copy.add(source.get(i));
	            }
	            return copy;
	        }
	        @Override
	        public Dimension getPreferredSize() {
	            return new Dimension(calculatedSize);
	        }
	        @Override
	        protected void paintComponent(Graphics g) {
	            super.paintComponent(g);
	            Graphics2D g2 = (Graphics2D) g.create();
	            try {
	                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	                        RenderingHints.VALUE_ANTIALIAS_ON);
	                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
	                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	                drawConnections(g2);
	                drawLca(g2);
	                drawNodes(g2, focusNodes);
	                drawNodes(g2, targetNodes);
	            } finally {
	                g2.dispose();
	            }
	        }
	        private void drawConnections(Graphics2D g2) {
	            Stroke oldStroke = g2.getStroke();
	            g2.setStroke(new BasicStroke(1.5f));
	            g2.setColor(new Color(105, 105, 105));
	            boolean directLine =
	                    focusNodes.isEmpty() || targetNodes.isEmpty();
	            drawBranchConnections(g2, focusNodes, directLine);
	            drawBranchConnections(g2, targetNodes, directLine);
	            g2.setStroke(oldStroke);
	        }
	        private void drawBranchConnections(
	                Graphics2D g2,
	                List<NodeLayout> nodes,
	                boolean directLine) {
	            if (nodes.isEmpty()) return;
	            Point previous = bottomCentre(lcaBounds);
	            boolean firstNode = true;
	            for (NodeLayout node : nodes) {
	                Point next = topCentre(node.bounds);
	                if (directLine && firstNode) {
	                    // Direct ancestor/descendant:
	                    // straight from LCA to first person.
	                    g2.drawLine(previous.x, previous.y, next.x, next.y);
	                }
	                else {
	                    int middleY = previous.y + Math.max(10, (next.y - previous.y) / 2);
	                    g2.drawLine(previous.x, previous.y, previous.x, middleY);
	                    g2.drawLine(previous.x, middleY, next.x, middleY);
	                    g2.drawLine(next.x, middleY, next.x, next.y);
	                }
	                previous = bottomCentre(node.bounds);
	                firstNode = false;
	            }
	        }
	        private void drawLca(Graphics2D g2) {
	            Long lcaPID = path.getLcaPID();
	            String text = lcaPID == null  ? "No common LCA" : getDisplayName(lcaPID);
	            drawPersonBox(g2, lcaBounds, text,
	            		new Color(255, 255, 255),		// LCA box color Mauve = 225, 210, 250
	                    new Color(105, 70, 140));		// darker
	        }
	        private void drawNodes(Graphics2D g2, List<NodeLayout> nodes) {
	            for (NodeLayout node : nodes) {
	                Color fill;
	                Color border;
	                if (node.side == Side.FOCUS) {
	                    fill = new Color(255, 255, 255);		// 215, 235, 250 = pale blue
	                    border = new Color(55, 105, 145);		// darker
	                } else {
	                    fill = new Color(255, 255, 255);		// 250, 225, 225 = pale pink
	                    border = new Color(145, 75, 75);		// darker
	                }
	                drawPersonBox(g2, node.bounds, getDisplayName(node.pid), fill,border);
	            }
	        }
	        private void drawPersonBox(Graphics2D g2, Rectangle bounds,String text,
	                					Color fill, Color border) {
	            g2.setColor(fill);
	            g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 14, 14);
	            g2.setColor(border);
	            g2.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 14, 14);
	            Font diagramFont = getFont();
	            if (diagramFont == null) diagramFont = UIManager.getFont("TextField.font");
	            if (diagramFont != null) g2.setFont(diagramFont);
	            FontMetrics fm = g2.getFontMetrics();
	            String clipped = clipText(fm, text, bounds.width - 16);
	            int textX = bounds.x + 8;
	            int textY = bounds.y + ((bounds.height - fm.getHeight()) / 2) + fm.getAscent();
	            g2.setColor(Color.BLACK);			// for Names
	            g2.drawString(clipped, textX, textY);
	        }
	        private String clipText(FontMetrics fm, String text, int maxWidth) {
	            if (text == null)  return "";

	            if (fm.stringWidth(text) <= maxWidth) return text;

	            String ellipsis = "...";
	            int ellipsisWidth = fm.stringWidth(ellipsis);
	            StringBuilder result = new StringBuilder(text);
	            while (result.length() > 0
	                    && fm.stringWidth(result.toString())
	                    + ellipsisWidth > maxWidth) {
	                result.deleteCharAt(result.length() - 1);
	            }
	            return result + ellipsis;
	        }
	        private Point topCentre(Rectangle r) {
	            return new Point(r.x + (r.width / 2), r.y);
	        }
	        private Point bottomCentre(Rectangle r) {
	        	return new Point(r.x + (r.width / 2),r.y + r.height);
	        }

	       // Return Person name from PID lookup
	        private String getDisplayName(long pid) {
	        	String persName = "";
	        	try {
					persName = pointPersonHandler.getPersonName(pid);
				} catch (HBException sqle) {
					  if (HGlobal.writeLogs) {
						  HB0711Logging.logWrite("ERROR: in getDisplayName " + sqle.getMessage());	//$NON-NLS-1$
						  HB0711Logging.printStackTraceToFile(sqle);
					  }
				}
	        	return persName;
	        }
	    }		// End RelationshipPanel

/*******************************************************************************
 * Following are all Relation analysis methods originally in HG0506ManagePerson.
 *  RelationshipDescriptor - clasifies type
 *  RelationshipClassifier - classifies a relationship (x,y) pair to a type
 *  RelationshipNamerFactory - invokes appropriate Namer based on language
 *  RelationshipNamer - converts (x,y) pair + sex to readable text
 *******************************************************************************/
 // ======================================
 // Universal Relationship Descriptors
 // ======================================
 	public static class RelationshipDescriptor {
 		    enum Type {SELF, ANCESTOR, DESCENDANT, SIBLING, NIBLING, PIBLING, COUSIN }

 		    public final Type type;
 		    public final int generationsUp;
 		    public final int generationsDown;
 		    public final int cousinDegree;
 		    public final int cousinRemoval;
 	        public final String sexCode;       // "M" or "F"

 		    public RelationshipDescriptor(Type type, int generationsUp, int generationsDown,
 		                                  int cousinDegree, int cousinRemoval, String sexCode)
 		    	{
 			        this.type = type;
 			        this.generationsUp = generationsUp;
 			        this.generationsDown = generationsDown;
 			        this.cousinDegree = cousinDegree;
 			        this.cousinRemoval = cousinRemoval;
 			        this.sexCode = sexCode;
 		    	}
 		}	// End RelationshipDescriptor

 // ===================================================
 // Conversion of (x,y) pair to RelationshipDescriptor
 // ===================================================
 	public static class RelationshipClassifier {
 	    public static RelationshipDescriptor classify(int x, int y, String sexCode) {
 	        // SELF
 	        if (x == 0 && y == 0)
 	            return new RelationshipDescriptor(
 	                    RelationshipDescriptor.Type.SELF, 0, 0, 0, 0, sexCode );

 	        // DIRECT ANCESTOR  (root is ABOVE target)  (0, +k)
 	        if (x == 0 && y > 0)
 	            return new RelationshipDescriptor(
 	                    RelationshipDescriptor.Type.ANCESTOR, y, 0, 0, 0, sexCode );

 	        // DIRECT DESCENDANT (root is BELOW target) (-k, 0)
 	        if (y == 0 && x < 0)
 	            return new RelationshipDescriptor(
 	                    RelationshipDescriptor.Type.DESCENDANT, 0, -x, 0, 0, sexCode );

 	        // SIBLING  (-1, -1)
 	        if (x == -1 && y == -1)
 	            return new RelationshipDescriptor(
 	                    RelationshipDescriptor.Type.SIBLING, 0, 0, 0, 0, sexCode );

 	        // NIBLING (niece/nephew)  (-k, -1)  where k > 1
 	        if (y == -1 && x < -1)
 	            return new RelationshipDescriptor(
 	                    RelationshipDescriptor.Type.NIBLING, 0, -(x + 1), 0, 0, sexCode );

 	        // PIBLING (aunt/uncle) (-1, -k)  where k > 1
 	        if (x == -1 && y < -1)
 	            return new RelationshipDescriptor(
 	                    RelationshipDescriptor.Type.PIBLING, -(y + 1), 0, 0, 0, sexCode );

 	        // COUSINS
 	        // (-k, -k)  ? degree = k - 1
 	        // (-k1, -k2) ? removal = |k1 - k2|
 	        if (x < -1 && y < -1) {
 	            int kRoot   = -y;   // steps root ? LCA
 	            int kTarget = -x;   // steps target ? LCA
 	            int k = Math.min(kRoot, kTarget);   // 2?1st, 3?2nd, 4?3rd
 	            int degree = k - 1;                 // 1,2,3,...
 	            int removal = Math.abs(kRoot - kTarget);
 	            return new RelationshipDescriptor(
 	                    RelationshipDescriptor.Type.COUSIN, 0, 0, degree, removal, sexCode );
 	        }
 	        // FALLBACK (should never happen)
 	        return new RelationshipDescriptor(
 	                RelationshipDescriptor.Type.SELF, 0, 0, 0, 0, sexCode  );
 	    }
 	}		// End RelationshipClassifier

     public interface RelationshipNamer {
         String name(RelationshipDescriptor d);
     }

     public static class RelationshipNamerFactory {
         public static RelationshipNamer forLanguage(String lang) {
             return switch (lang) {
                 case "en-US" -> new EnglishNamer();		//$NON-NLS-1$
                 case "en-GB" -> new EnglishNamer();		//$NON-NLS-1$
                 case "fr-FR" -> new FrenchNamer();		//$NON-NLS-1$
                 case "de-DE" -> new GermanNamer();		//$NON-NLS-1$
                 case "es-ES" -> new SpanishNamer();		//$NON-NLS-1$
                 case "nl-NL" -> new DutchNamer();		//$NON-NLS-1$
                 case "it-IT" -> new ItalianNamer();		//$NON-NLS-1$
                 case "no-NB" -> new NorwegianNamer();	//$NON-NLS-1$
 			default -> new EnglishNamer();
             };
         }
     }		// End RelationshipNamerFactory

 // ================================================================================
 // LANGUAGE IMPLEMENTATIONS
 // NB: SELF case should never be returned as (0,0) pairs should never
 // be passed into these routines.
 // NB: no NLS done after this point as ALL strings are already language-specific
 // ===============================================================================
 // ---------------- ENGLISH ----------------
     public static class EnglishNamer implements RelationshipNamer {
         @Override
         public String name(RelationshipDescriptor d) {
             return switch (d.type) {
                 case SELF -> "self";
                 case ANCESTOR -> ancestor(d.generationsUp, d.sexCode);
                 case DESCENDANT -> descendant(d.generationsDown, d.sexCode);
                 case SIBLING -> d.sexCode == "M" ? "brother" : "sister";
                 case NIBLING -> nibling(d.generationsDown, d.sexCode);
                 case PIBLING -> pibling(d.generationsUp, d.sexCode);
                 case COUSIN -> cousin(d.cousinDegree, d.cousinRemoval);
             };
         }
         private String ancestor(int up, String sex) {
             String base = sex.equals("M") ? "grandfather" : "grandmother";
             if (up == 1) return sex.equals("M") ? "father" : "mother";
             if (up == 2) return base;
             int greats = up - 2;
             // Use full "great-great-" only for 1–2 greats
             if (greats <= 2)
                 return "great-".repeat(greats) + base;
             // Compact form: "4th-gt grandfather"
             return ordinal(greats) + "-gt " + base;
         }
         private String descendant(int down, String sex) {
             String base = sex.equals("M") ? "grandson" : "granddaughter";
             if (down == 1) return sex.equals("M") ? "son" : "daughter";
             if (down == 2) return base;
             int greats = down - 2;
             if (greats <= 2)
                 return "great-".repeat(greats) + base;
             return ordinal(greats) + "-gt " + base;
         }
         private String nibling(int down, String sex) {
             String base = sex.equals("M") ? "nephew" : "niece";
             if (down == 1) return base;
             int greats = down - 1;
             if (greats <= 2)
                 return "great-".repeat(greats) + base;
             return ordinal(greats) + "-gt " + base;
         }
         private String pibling(int up, String sex) {
             String base = sex.equals("M") ? "uncle" : "aunt";
             if (up == 1) return base;
             int greats = up - 1;
             if (greats <= 2)
                 return "great-".repeat(greats) + base;
             return ordinal(greats) + "-gt " + base;
         }

         private String cousin(int degree, int removal) {
             int n = degree; // 1?1st, 2?2nd, 3?3rd
             String base = ordinal(n) + " cousin";
             if (removal == 0) return base;
             return base + " " + removal + " time" + (removal > 1 ? "s" : "") + " removed";
         }
         private String ordinal(int n) {
             return switch (n) {
                 case 1 -> "1st";
                 case 2 -> "2nd";
                 case 3 -> "3rd";
                 default -> n + "th";
             };
         }
     }		// End EnglishNamer
  // ---------------- FRENCH ----------------
     public static class FrenchNamer implements RelationshipNamer {
         @Override
         public String name(RelationshipDescriptor d) {
             return switch (d.type) {
                 case SELF -> "soi-même";
                 case ANCESTOR -> ancestor(d.generationsUp, d.sexCode);
                 case DESCENDANT -> descendant(d.generationsDown, d.sexCode);
                 case SIBLING -> d.sexCode == "M" ? "frère" : "soeur";
                 case NIBLING -> nibling(d.generationsDown, d.sexCode);
                 case PIBLING -> pibling(d.generationsUp, d.sexCode);
                 case COUSIN -> cousin(d.cousinDegree, d.cousinRemoval, d.sexCode);
             };
         }
         private String ancestor(int up, String sex) {
             if (up == 1) return sex == "M" ? "père" : "mère";
             if (up == 2) return sex == "M" ? "grand-père" : "grand-mère";
             return "arrière-".repeat(up - 2) + (sex == "M" ? "grand-père" : "grand-mère");
         }
         private String descendant(int down, String sex) {
             if (down == 1) return sex == "M" ? "fils" : "fille";
             if (down == 2) return sex == "M" ? "petit-fils" : "petite-fille";
             return "arrière-".repeat(down - 2) + (sex == "M" ? "petit-fils" : "petite-fille");
         }
         private String nibling(int down, String sex) {
             if (down == 1) return sex == "M" ? "neveu" : "nièce";
             return "petit-".repeat(down - 1) + (sex == "M" ? "neveu" : "nièce");
         }
         private String pibling(int up, String sex) {
             if (up == 1) return sex == "M" ? "oncle" : "tante";
             return "grand-".repeat(up - 1) + (sex == "M" ? "oncle" : "tante");
         }
         private String cousin(int degree, int removal, String sex) {
             String base = (sex == "M" ? "cousin" : "cousine") + " " + degree + "? degré";
             if (removal == 0) return base;
             return base + " éloigné(e) de " + removal + " génération(s)";
         }
     }	// End FrenchNamer
  // ---------------- GERMAN ----------------
     public static class GermanNamer implements RelationshipNamer {
         @Override
         public String name(RelationshipDescriptor d) {
             return switch (d.type) {
                 case SELF -> "selbst";
                 case ANCESTOR -> ancestor(d.generationsUp, d.sexCode);
                 case DESCENDANT -> descendant(d.generationsDown, d.sexCode);
                 case SIBLING -> d.sexCode == "M" ? "Bruder" : "Schwester";
                 case NIBLING -> nibling(d.generationsDown, d.sexCode);
                 case PIBLING -> pibling(d.generationsUp, d.sexCode);
                 case COUSIN -> cousin(d.cousinDegree, d.cousinRemoval, d.sexCode);
             };
         }
         private String ancestor(int up, String sex) {
             if (up == 1) return sex == "M" ? "Vater" : "Mutter";
             if (up == 2) return sex == "M" ? "Großvater" : "Großmutter";
             return "Ur-".repeat(up - 2) + (sex == "M" ? "Großvater" : "Großmutter");
         }
         private String descendant(int down, String sex) {
             if (down == 1) return sex == "M" ? "Sohn" : "Tochter";
             if (down == 2) return sex == "M" ? "Enkel" : "Enkelin";
             return "Ur-".repeat(down - 2) + (sex == "M" ? "Enkel" : "Enkelin");
         }
         private String nibling(int down, String sex) {
             if (down == 1) return sex == "M" ? "Neffe" : "Nichte";
             return "Groß-".repeat(down - 1) + (sex == "M" ? "Neffe" : "Nichte");
         }
         private String pibling(int up, String sex) {
             if (up == 1) return sex == "M" ? "Onkel" : "Tante";
             return "Groß-".repeat(up - 1) + (sex == "M" ? "Onkel" : "Tante");
         }
         private String cousin(int degree, int removal, String sex) {
             String base = degree + ". Grades " + (sex == "M" ? "Cousin" : "Cousine");
             if (removal == 0) return base;
             return base + ", " + removal + " mal entfernt";
         }
     }		// End GermanNamer
  // ---------------- SPANISH ----------------
     public static class SpanishNamer implements RelationshipNamer {
         @Override
         public String name(RelationshipDescriptor d) {
             return switch (d.type) {
                 case SELF -> "yo mismo";
                 case ANCESTOR -> ancestor(d.generationsUp, d.sexCode);
                 case DESCENDANT -> descendant(d.generationsDown, d.sexCode);
                 case SIBLING -> d.sexCode == "M" ? "hermano" : "hermana";
                 case NIBLING -> nibling(d.generationsDown, d.sexCode);
                 case PIBLING -> pibling(d.generationsUp, d.sexCode);
                 case COUSIN -> cousin(d.cousinDegree, d.cousinRemoval, d.sexCode);
             };
         }
         private String ancestor(int up, String sex) {
             if (up == 1) return sex == "M" ? "padre" : "madre";
             if (up == 2) return sex == "M" ? "abuelo" : "abuela";
             return "bis-".repeat(up - 2) + (sex == "M" ? "abuelo" : "abuela");
         }
         private String descendant(int down, String sex) {
             if (down == 1) return sex == "M" ? "hijo" : "hija";
             if (down == 2) return sex == "M" ? "nieto" : "nieta";
             return "bis-".repeat(down - 2) + (sex == "M" ? "nieto" : "nieta");
         }
         private String nibling(int down, String sex) {
             if (down == 1) return sex == "M" ? "sobrino" : "sobrina";
             return "sobrino/sobrina de " + (down - 1) + "º grado";
         }
         private String pibling(int up, String sex) {
             if (up == 1) return sex == "M" ? "tío" : "tía";
             return "tío/tía de " + (up - 1) + "º grado";
         }
         private String cousin(int degree, int removal, String sex) {
             String base = (sex == "M" ? "primo" : "prima") + " de " + degree + "º grado";
             if (removal == 0) return base;
             return base + ", " + removal + " vez" + (removal > 1 ? "es" : "") + " removido";
         }
     }		// End SpanishNamer
  // ---------------- DUTCH ----------------
     public static class DutchNamer implements RelationshipNamer {
         @Override
         public String name(RelationshipDescriptor d) {
         	String[] dutchGreats = {"", "",			// for up = 0, 1
         							"groot",		// for up or down = 2, etc
         							"overgroot",
         							"betovergroot",
         							"oud",
         							"oudgroot",
         							"oudovergroot",
         							"oudbetovergroot",
         							"stam",
         							"stamgroot",			// 10
         							"stamovergroot",
         							"stambetovergroot",
         							"stamoud",
         							"stamoudgroot",
         							"stamoudovergroot",
         							"stamoudbetovergroot",
         							"edel",
         							"edelgroot",
         							"edelovergroot",
         							"edelbetovergroot",			// 20
         							"edeloud",
         							"edeloudgroot",
         							"edeloudovergroot",
         							"edeloudbetovergroot",
         							"edelstam",
         							"edelstamgroot",
         							"edelstamovergroot",
         							"edelstambeovergroot",
         							"edelstamoud",
         							"edelstamoudgroot",			// 30
         							"edelstamoudovergroot",
         							"edelstamoudbetovergroot",
         							"voor",
            							"voorgroot",
         							"voorovergroot",
         							"voorbetovergroot",
            							"vooroudgroot",
         							"vooroudovergroot",
         							"vooroudbetovergroot",
         							"voorstamgroot"  };			// 40
             return switch (d.type) {
                 case SELF -> "zelf";
                 case ANCESTOR -> ancestor(d.generationsUp, d.sexCode, dutchGreats);
                 case DESCENDANT -> descendant(d.generationsDown, d.sexCode);
                 case SIBLING -> d.sexCode == "M" ? "broer" : "zus";
                 case NIBLING -> nibling(d.generationsDown, d.sexCode, dutchGreats);
                 case PIBLING -> pibling(d.generationsUp, d.sexCode, dutchGreats);
                 case COUSIN -> cousin(d.cousinDegree, d.cousinRemoval, d.sexCode);
             };
         }
         private String ancestor(int up, String sex, String[] greats) {
             if (up == 1) return sex == "M" ? "vader" : "moeder";
             if (up < 41) return greats[up] + (sex == "M" ? "vader" : "moeder");
             // Hopefully never get as far as this!
             return String.valueOf(up - 2) + (sex == "M" ? "-overgrootvader" : "-overgrootmoeder");
         }
         private String descendant(int down, String sex) {
             if (down == 1) return sex == "M" ? "zoon" : "dochter";
             if (down == 2) return sex == "M" ? "kleinzoon" : "kleindochter";
             if (down == 3) return sex == "M" ? "achterkleinzoon" : "achterkleindochter";
             if (down == 4) return sex == "M" ? "achterachterkleinzoon" : "achterachterkleindochter";
             return String.valueOf(down - 2) + (sex == "M" ? "-achterkleinzoon" : "-achterkleindochter");
         }
         private String nibling(int down, String sex, String[] greats) {
             if (down == 1) return sex == "M" ? "neef" : "nicht";
             if (down < 41) return greats[down] + (sex == "M" ? "neef" : "nicht");
             // Hopefully never get as far as this!
             return String.valueOf(down - 2) + (sex == "M" ? "-overgrootneef" : "-overgrootnicht");
         }
         private String pibling(int up, String sex, String[] greats) {
             if (up == 1) return sex == "M" ? "oom" : "tante";
             if (up < 41) return greats[up] + (sex == "M" ? "oom" : "tante");
             // Hopefully never get as far as this!
             return String.valueOf(up - 2) + (sex == "M" ? "-overgrootoom" : "-overgroottante");
         }
         private String cousin(int degree, int removal, String sex) {
         	String cousinNum ="", cousinRem ="";
         	if (degree == 1 || degree > 20) cousinNum = degree + "st ";
         	else cousinNum = degree + "de";
         	if (removal == 1 || removal > 20) cousinRem = " " + removal + "st graad";
         	else cousinRem = " " + removal + "de graad";
             String base = cousinNum + (sex == "M" ? "neven" : "nichten");
             if (removal == 0) return base;
             return base + cousinRem;
         }
     }		// End DutchNamer
  // ---------------- ITALIAN ----------------
     public static class ItalianNamer implements RelationshipNamer {
         @Override
         public String name(RelationshipDescriptor d) {
             return switch (d.type) {
                 case SELF -> "sé stesso";
                 case ANCESTOR -> ancestor(d.generationsUp, d.sexCode);
                 case DESCENDANT -> descendant(d.generationsDown, d.sexCode);
                 case SIBLING -> d.sexCode == "M" ? "fratello" : "sorella";
                 case NIBLING -> nibling(d.generationsDown, d.sexCode);
                 case PIBLING -> pibling(d.generationsUp, d.sexCode);
                 case COUSIN -> cousin(d.cousinDegree, d.cousinRemoval, d.sexCode);
             };
         }
         private String ancestor(int up, String sex) {
             if (up == 1) return sex == "M" ? "padre" : "madre";
             if (up == 2) return sex == "M" ? "nonno" : "nonna";
             return "bis-".repeat(up - 2) + (sex == "M" ? "nonno" : "nonna");
         }
         private String descendant(int down, String sex) {
             if (down == 1) return sex == "M" ? "figlio" : "figlia";
             if (down == 2) return sex == "M" ? "nipote" : "nipote"; // gender-neutral in Italian
             return "bis-".repeat(down - 2) + "nipote";
         }
         private String nibling(int down, String sex) {
             if (down == 1) return sex == "M" ? "nipote" : "nipote"; // nephew/niece both "nipote"
             return "pro-".repeat(down - 1) + "nipote";
         }
         private String pibling(int up, String sex) {
             if (up == 1) return sex == "M" ? "zio" : "zia";
             return "pro-".repeat(up - 1) + (sex == "M" ? "zio" : "zia");
         }
         private String cousin(int degree, int removal, String sex) {
             String base = (sex == "M" ? "cugino" : "cugina") + " di " + degree + "º grado";
             if (removal == 0) return base;
             return base + ", " + removal + " volta" + (removal > 1 ? "e" : "") + " rimosso";
         }
     }		// End ItalianNamer
  // ---------------- NORWEGIAN ----------------
     public static class NorwegianNamer implements RelationshipNamer {
         @Override
         public String name(RelationshipDescriptor d) {
             return switch (d.type) {
                 case SELF -> "selv";
                 case ANCESTOR -> ancestor(d.generationsUp, d.sexCode);
                 case DESCENDANT -> descendant(d.generationsDown, d.sexCode);
                 case SIBLING -> d.sexCode == "M" ? "bror" : "søster";
                 case NIBLING -> nibling(d.generationsDown, d.sexCode);
                 case PIBLING -> pibling(d.generationsUp, d.sexCode);
                 case COUSIN -> cousin(d.cousinDegree, d.cousinRemoval, d.sexCode);
             };
         }
         private String ancestor(int up, String sex) {				// father/mother and upwards
             if (up == 1) return sex == "M" ? "far" : "mor";
             if (up == 2) return sex == "M" ? "bestefar" : "bestemor";
             if (up == 3) return sex == "M" ? "oldefar" : "oldemor";
             if (up == 4) return sex == "M" ? "tippoldefar" : "tippoldemor";
             if (up == 5) return sex == "M" ? "tipptippoldefar" : "tipptippoldemor";
             return (up - 3) + (sex == "M" ? ". tippoldefar" : ". tippoldemor");
         }
         private String descendant(int down, String sex) {			// son/daughter and downwards
             if (down == 1) return sex == "M" ? "sønn" : "datter";
             if (down == 2) return sex == "M" ? "barnebarn" : "barnebarn"; // gender-neutral
             if (down == 3) return sex == "M" ? "oldebarn" : "oldebarn";
             if (down == 4) return sex == "M" ? "tippoldebarn" : "tippoldebarn";
             if (down == 5) return sex == "M" ? "tipptippoldebarn" : "tipptippoldebarn";
             return (down - 3) + ". tippoldeebarn";
         }
         private String nibling(int down, String sex) {
             if (down == 1) return sex == "M" ? "nevø" : "niese";		// nephew/niece etc
             if (down == 2) return sex == "M" ? "grandnevø" : "grandniese";
             if (down == 3) return sex == "M" ? "oldebarn av søsken" : "oldebarn av søsken";
             if (down == 4) return sex == "M" ? "tippoldebarn av søsken" : "tippoldebarn av søsken";
             if (down == 5) return sex == "M" ? "tipptippoldebarn av søsken" : "tipptippoldebarn av søsken";
             return (down - 3) + sex == "M"  ? ". tippoldebarn av søsken" : ". tippoldebarn av søsken";
         }
         private String pibling(int up, String sex) {
             if (up == 1) return sex == "M" ? "onkel" : "tante";			// uncle/aunt, etc
             if (up == 2) return sex == "M" ? "grandonkel" : "grandtante";
             if (up == 3) return sex == "M" ? "bror av oldeforeldre" : "søster av oldeforeldre";
             if (up == 4) return sex == "M" ? "bror av tippoldeforeldre" : "søster av tippoldeforeldre";
             if (up == 5) return sex == "M" ? "bror av tipptippoldeforeldre" : "søster av tipptippoldeforeldre";
             return  (sex == "M" ? "bror av " + (up - 3) + ". tippoldeforeldre" : "søste av " + (up - 3) + ". tippoldeforeldre");
         }
         private String cousin(int degree, int removal, String sex) {
         	// Original ChatGPT code for cousins
//	             String base = degree + ". grad " + (sex == "M" ? "fetter" : "kusine");		// cousins
//	             if (removal == 0) return base;
  //           return base + ", " + removal + " gang fjernet";
         	// If degree > 30 or remoaval > 29 we can't handle it, so return nothing
         	if (degree > 30 || removal > 29) return "";
         	String[] norseCousins = {"", " ",			// for degree = 0, 1
         			"tremenning",
         			"firmenning",
         			"femmenning",
         			"seksmenning",
         			"sjumenning",
         			"åttemenning",
         			"nimenning",
         			"timenning",
         			"ellevemenning",		// 10th
         			"tolvmenning",
         			"trettenmenning",
         			"fjortenmenning",
         			"femtenmenning",
         			"sekstenmenning",
         			"syttenmenning",
         			"attenmenning",
         			"nittenmenning",
         			"tjuemenning",
         			"tjueenmenning",
         			"tjuetomenning",
         			"tjuetremenning",		// 20th
         			"tjuefiremenning",
         			"tjuefemmenning",
         			"tjueseksmenning",
         			"tjuesjumenning",
         			"tjueåttemenning",
         			"tjuenimenning",
         			"trettimenning",
         			"trettienmenning"};		// 30th
         	if (removal == 0) {
         		if (degree == 1) return sex == "M" ? "fetter" : "kusine";
         		if (degree > 1) return norseCousins[degree];
         	}
         	if (removal == 1) {
            		if (degree == 1) return sex == "M" ? "sønn av søskenbarn" : "datter av søskenbarn";
         		if (degree > 1) return sex == "M" ? "sønn av " + norseCousins[degree] : "datter av " + norseCousins[degree];
         	}
         	if (removal == 2) {
            		if (degree == 1) return "barnebarn av søskenbarn";
         		if (degree > 1) return "barnebarn av " + norseCousins[degree];
         	}
            	if (removal == 3) {
            		if (degree == 1) return "oldebarn av søskenbarnmoved";
         		if (degree > 1) return "oldebarn av " + norseCousins[degree];
         	}
            	if (removal == 4) {
            		if (degree == 1) return "tippoldebarn av søskenbarn";
         		if (degree > 1) return "tippoldebarn av " + norseCousins[degree];
         	}
           	if (removal == 5) {
            		if (degree == 1) return "tipptippoldebarn av søskenbarn";
         		if (degree > 1) return "tipptippoldebarn av " + norseCousins[degree];
         	}
          	if (removal > 5) {
          		if (degree == 1) return (removal - 3) + ". tippoldebarn av søskenbarn";
         		if (degree > 1) return (removal - 3) + ". tippoldebarn av " + norseCousins[degree];
         	}
          	return "";	// fallback option
         }
     }		// End NorwegianNamer

} // End class RelationHandler
