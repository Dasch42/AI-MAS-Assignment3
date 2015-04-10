package client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import client.node.Node;
import client.node.storage.LogicalAgent;
import client.node.storage.Box;
import client.node.storage.Goal;

public abstract class Heuristic implements Comparator< Node > {

	public Node initialState;
	public SearchAgent agent;
	
	public HashMap<Node, Integer> hs =new HashMap<>();

	private static HashMap<Goal, Integer> agent_goal_bookkeeping = new HashMap<>();
	
	public Heuristic(Node initialState, SearchAgent agent) {
		this.initialState = initialState;
		this.agent = agent;

	}

	public int compare( Node n1, Node n2 ) {
		return f( n1 ) - f( n2 );
	}

//	public int h ( Node n ) {
//		//euclid distance from mover to box and from box to goal
//		Integer tmpH=hs.get(n);
//		if(tmpH==null){
//
//			int h=0;			
//			for (Box box : n.getBoxes()) {				
//				if(agent.subgoals.size() > 0){
//					for (Goal goal : agent.subgoals){
//						if(goal.getType() == box.getType()){
//							if (n.distance(box, goal) > 0){						
//								h+=n.distance(box, goal) +1 ;
//							}
//							h+=n.distance(n.agents[agent.id], goal);
//						}					
//					}
//				}else{
//					Goal goal = n.getGoals(box.getType()).get(0);
//					if(goal.getType() == box.getType()){
//						if (n.distance(box, goal) > 0){						
//							h+=n.distance(box, goal) +1 ;
//						}
//						h+=n.distance(n.agents[agent.id], goal);
//					}
//				}
//								
//			}
//			hs.put(n, h);
//			return h;
//		}else{
//			return tmpH;
//		}
//		
//	}
	
	public int h ( Node n ) {
		//euclid distance from mover to box and from box to goal
		Integer tmpH=hs.get(n);
		if(tmpH==null){

			int h=0;
			for (Goal subgoal : agent.subgoals) {
				int tmp=Integer.MAX_VALUE;
				for (Box box : n.getBoxes(subgoal.getType())) {
					if(!box.isAt(subgoal.row, subgoal.col)){
						tmp=Math.min(tmp, n.distance(n.agents[agent.id], box)+n.distance(box, subgoal));
					}else{
						tmp=0;
						break;
					}
				}
				h+=tmp;
			}
			hs.put(n, h);
			return h;
		}else{
			return tmpH;
		}
		
	}

	public abstract int f( Node n);

	public static class AStar extends Heuristic {
		public AStar(Node initialState, SearchAgent agent) {
			super( initialState, agent);
		}

		public int f( Node n) {
			return n.g() + h( n );
		}

		public String toString() {
			return "A* evaluation";
		}
	}

	public static class WeightedAStar extends Heuristic {
		private int W;

		public WeightedAStar(Node initialState, SearchAgent agent) {
			super( initialState, agent);
			W = 5; // You're welcome to test this out with different values, but for the reporting part you must at least indicate benchmarks for W = 5
		}

		public int f( Node n ) {
			return n.g() + W * h( n );
		}

		public String toString() {
			return String.format( "WA*(%d) evaluation", W );
		}
	}

	public static class Greedy extends Heuristic {

		public Greedy(Node initialState, SearchAgent agent) {
			super( initialState, agent);
		}
		

		public int f( Node n ) {
			return h( n );
		}

		public String toString() {
			return "Greedy evaluation";
		}
	}
	

	public void finishedWithGoal(Goal g){
		Heuristic.agent_goal_bookkeeping.remove(g);
	}


	public Goal selectGoal(){
		Goal g = selectGoal_boxGoalDist();
		if(g!=null){
			Heuristic.agent_goal_bookkeeping.put(g, this.agent.id);
		}
		return g;
	}

	private boolean goalInUse(Goal g){
		return ( Heuristic.agent_goal_bookkeeping.get(g) != null /*|| Heuristic.agent_goal_bookkeeping.get(g).intValue != this.agentID */);
	}

	@SuppressWarnings("unused")
	private Goal selectGoal_goalDist(){
		LogicalAgent agent = this.initialState.agents[this.agent.id];
		ArrayList<Goal> goals = this.initialState.getCluster(agent);
		Goal selectedGoal = goals.get(0);
		
		for( Goal goal : goals ){
			if( goalInUse(goal) )
				continue;

			if( this.initialState.distance(agent, goal) < this.initialState.distance(agent, selectedGoal) )
				selectedGoal = goal;
		}
		return selectedGoal;
	}

	private Goal selectGoal_boxGoalDist(){
		LogicalAgent agent = this.initialState.agents[this.agent.id];
		ArrayList<Goal> goals = this.initialState.getCluster(agent);

		// Selected Values
		Goal selectedGoal = null;
		@SuppressWarnings("unused")
		Box selectedBox = null;
		int dist = Integer.MAX_VALUE;

		for( Goal goal : goals ){
			if( goalInUse(goal) )
				continue;

			for( Box box : initialState.getBoxes(goal.getType()) ){
				if( ( this.initialState.distance(agent, box) + this.initialState.distance(box, goal) ) < dist ){
					dist = this.initialState.distance(agent, box) + this.initialState.distance(box, goal);
					// Set the selects
					selectedGoal = goal;
					selectedBox = box;
				}
			}
		}
		/*
		if( selectedGoal == null )
			throw new NullPointerException("No more goals.");
		if( selectedBox == null )
			throw new NullPointerException("No available boxes for selected goal.");
		*/
		return selectedGoal;
	}
}