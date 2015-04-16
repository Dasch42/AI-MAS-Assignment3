package client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import client.Heuristic.Proximity;
import client.heuristic.ClearHeuristic;
import client.SearchAgent.Status;
import client.Strategy.StrategyBestFirst;
import client.node.Node;
import client.node.storage.Base;
import client.node.storage.Box;
import client.node.storage.LogicalAgent;
import client.node.storage.SearchResult;
import client.node.storage.SearchResult.Result;
import client.parser.RouteParser;
import client.node.GoalState;
import client.node.GoalState.*;
// Searches

public class Conflict{

	// Helping Agent -> Agent Receiving Help
	private static HashMap<SearchAgent, SearchAgent> helping = new HashMap<>();

	public static void doneHelping(SearchAgent helpingAgent){
		if(	helping.get(helpingAgent) != null ){
			helping.get(helpingAgent).status = Status.PLAN;
		}
	}


	public static ArrayList< LinkedList< Node > > solve(Node node, ArrayList< LinkedList< Node > > solutions, List< SearchAgent > agents) throws IOException{
		
		for (SearchAgent agent : agents) {
			if(agent.status == SearchAgent.Status.STUCK){
				/*
				 * find reason
				 */

				ArrayList<Base> route = RouteParser.parse(solutions, agent.id);

				ArrayList<LogicalAgent> agentsInTheWay = new ArrayList<>();
				ArrayList<Box> boxesInTheWay = new ArrayList<>();

				int dirty_test_count = 0;
				boolean dirty_test_flag = true;

				// Parse the route, storing what might be in the way
				// Sanity check on route!
				if( route.size() < 1 ){
					System.err.println("Conflict :: Route is empty.");
				}

				for( Base b : route ){
					Object o = node.objectAt(b);
					if(dirty_test_flag)dirty_test_count++;
					if( o instanceof LogicalAgent ){
						dirty_test_flag = false;
						System.err.println("Conflict :: Agent found in route for agent " + agent.id + "!");
						agentsInTheWay.add( (LogicalAgent)o );
					}else if( o instanceof Box ){
						dirty_test_flag = false;
						System.err.println("Conflict :: Box found in route for agent " + agent.id + "!");
						System.err.println("            Color of box: " + ((Box)o).color + ".");
						if( agent.color != ((Box)o).color )
							boxesInTheWay.add( (Box)o );
					}
				}

				/*
				 * find some one to solve the problem
				 */
				
				// Call an agent that can move the box, and MOVE the fucking box.
				for( Box b : boxesInTheWay ){
					SearchAgent helpingAgent = agents.get( Node.colorMap.get(b.color).get(0) );

					System.err.println("Conflict:: Found agent to help. Asking Agent " + helpingAgent.id);

					Heuristic proximityHeuristic 	= new Proximity(agent, b);
					GoalState proxGoal 				= new ProximityGoalState(agent.id, b);
					Strategy helpingStrategy 		= new StrategyBestFirst(proximityHeuristic);
					helpingAgent.setState(node);
					SearchResult result = helpingAgent.CustomSearch(helpingStrategy, proxGoal);








					// Clear the Helping Agents plan
					if( result.reason != Result.STUCK ){
						solutions.get(helpingAgent.id).clear();
						solutions.get(helpingAgent.id).addAll(result.solution);
						helpingAgent.status = Status.HELPING;
						helping.put(helpingAgent, agent);


						// Figure out what to do
						Node moveStart = null;
						if( !result.solution.isEmpty() ){
							moveStart = result.solution.get(result.solution.size()-1) ;
						}else{
							moveStart = node;
						}

						Heuristic clearHeuristic = new ClearHeuristic(agent, agentsInTheWay.size()+boxesInTheWay.size(), route);
						Strategy clearStrategy = new StrategyBestFirst(clearHeuristic);
						helpingAgent.setState(moveStart);
						SearchResult result2 = helpingAgent.ClearRouteSearch(clearStrategy, agentsInTheWay.size()+boxesInTheWay.size(), route);

						if( result2.reason != Result.STUCK ){
							solutions.get(helpingAgent.id).clear();
							solutions.get(helpingAgent.id).addAll(result2.solution);

							// Remove stuck flag from invoking agent. Makes it move instantly. 
							agent.status = SearchAgent.Status.PLAN;

							// Inject dirty NoOpts into helping agent.
							Node noOptParent = null;
							if( !result2.solution.isEmpty() ){
								noOptParent = result2.solution.get(result2.solution.size()-1) ;
							}else{
								noOptParent = node;
							}

							for( int i = 0 ; i < dirty_test_count ; i++ ){
								Node noOpt = noOptParent.ChildNode();
								noOpt.action = new Command();
								solutions.get(helpingAgent.id).addLast(noOpt);
								noOptParent = noOpt;
							}

						}else{
							// Impossible to help?!
						}

						System.err.println("Conflict :: Agent " + helpingAgent.id + " moving to help.");
					}else{
						System.err.println("Conflict :: No path to help was found.");
						System.err.println(node);
					}
				}

				// Assumption: The agent can move out of the way.
				// Make any agents in route move out of the way.
				for( LogicalAgent a : agentsInTheWay ){

					if( agents.get(a.id).status == Status.HELPING )
						continue;

					System.err.println("Agent " + a.id + " was found to be in the way. Attempting to move.");

					SearchAgent sa = agents.get(a.id);

					sa.status = SearchAgent.Status.HELPING;
					ArrayList< Node > moves = node.getExpandedNodes(sa.id);
					if( moves.size() == 0 ){
						// If there's no possible moves available, report stuck and skip
						agents.get(a.id).status = Status.STUCK;
						continue;
					}

					Node getOut		= moves.get(0);
					Node noOpt		= getOut.ChildNode();
					noOpt.action 	= new Command(); // NoOP command
					Node getBack	= noOpt.ChildNode();
					getBack.action 	= getOut.action.reverseCommand( getOut.action );
					getBack.excecuteCommand(getBack.action, sa.id);

					if( !solutions.get(sa.id).isEmpty() ) {
						solutions.get(sa.id).peek().parent = getBack;
					}

					solutions.get(sa.id).addFirst(getBack);
					solutions.get(sa.id).addFirst(noOpt);
					solutions.get(sa.id).addFirst(getOut);
				}


			}
		}
		return solutions;
	}


}