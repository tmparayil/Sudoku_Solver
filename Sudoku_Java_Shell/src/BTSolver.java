import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BTSolver
{

	// =================================================================
	// Properties
	// =================================================================

	private ConstraintNetwork network;
	private SudokuBoard sudokuGrid;
	private Trail trail;

	private boolean hasSolution = false;

	public String varHeuristics;
	public String valHeuristics;
	public String cChecks;

	// =================================================================
	// Constructors
	// =================================================================

	public BTSolver ( SudokuBoard sboard, Trail trail, String val_sh, String var_sh, String cc )
	{
		this.network    = new ConstraintNetwork( sboard );
		this.sudokuGrid = sboard;
		this.trail      = trail;

		varHeuristics = var_sh;
		valHeuristics = val_sh;
		cChecks       = cc;
	}

	// =================================================================
	// Consistency Checks
	// =================================================================

	// Basic consistency check, no propagation done
	private boolean assignmentsCheck ( )
	{
		for ( Constraint c : network.getConstraints() )
			if ( ! c.isConsistent() )
				return false;

		return true;
	}

	// =================================================================
	// Arc Consistency
	// =================================================================
	public boolean arcConsistency ( )
    {
        List<Variable> toAssign = new ArrayList<Variable>();
        List<Constraint> RMC = network.getModifiedConstraints();
        for(int i = 0; i < RMC.size(); ++i)
        {
            List<Variable> LV = RMC.get(i).vars;
            for(int j = 0; j < LV.size(); ++j)
            {
                if(LV.get(j).isAssigned())
                {
                    List<Variable> Neighbors = network.getNeighborsOfVariable(LV.get(j));
                    int assignedValue = LV.get(j).getAssignment();
                    for(int k = 0; k < Neighbors.size(); ++k)
                    {
                        Domain D = Neighbors.get(k).getDomain();
                        if(D.contains(assignedValue))
                        {
                            if(D.size() == 1)
                                return false;
                            if(D.size() == 2)
                                toAssign.add(Neighbors.get(k));
                            trail.push(Neighbors.get(k));
                            Neighbors.get(k).removeValueFromDomain(assignedValue);
                        }
                    }
                }
            }
        }
        if(!toAssign.isEmpty())
        {
            for(int i = 0; i < toAssign.size(); ++i)
            {
                Domain D = toAssign.get(i).getDomain();
                ArrayList<Integer> assign = D.getValues();
                trail.push(toAssign.get(i));
                toAssign.get(i).assignValue(assign.get(0));
            }
            return arcConsistency();
        }
        return network.isConsistent();
    }


	/**
	 * Part 1 TODO: Implement the Forward Checking Heuristic
	 *
	 * This function will do both Constraint Propagation and check
	 * the consistency of the network
	 *
	 * (1) If a variable is assigned then eliminate that value from
	 *     the square's neighbors.
	 *
	 * Note: remember to trail.push variables before you change their domain
	 *
	 * Return: a pair of a HashMap and a Boolean. The map contains the pointers to all MODIFIED variables, mapped to their MODIFIED domain. 
	 *         The Boolean is true if assignment is consistent, false otherwise.
	 */
	public Map.Entry<HashMap<Variable,Domain>, Boolean> forwardChecking ( )
	{
		HashMap<Variable,Domain> map = new HashMap<>();
		List<Constraint> RMC = network.getModifiedConstraints();
		for(int i=0;i<RMC.size();i++)
		{
			List<Variable> LV = RMC.get(i).vars;
			for(int j=0;j< LV.size();j++)
			{
				if(LV.get(j).isAssigned())
				{
					List<Variable> neighbors = network.getNeighborsOfVariable(LV.get(j));
					int assignedValue = LV.get(j).getAssignment();
					for(int k=0;k<neighbors.size();k++)
					{
						if(neighbors.get(k).isAssigned() && neighbors.get(k).getAssignment() == assignedValue)
						{
							return Pair.of(null,false);
						}
						Domain d = neighbors.get(k).getDomain();
						if(d.contains(assignedValue))
						{
							trail.push(neighbors.get(k));
							neighbors.get(k).removeValueFromDomain(assignedValue);
							map.put(neighbors.get(k),neighbors.get(k).getDomain());

							if(neighbors.get(k).getDomain().size() == 1)
							{
								neighbors.get(k).assignValue(neighbors.get(k).getValues().get(0));
							}
						}
					}
				}
			}
		}
		
		return Pair.of(map,true);	
	}

	/**
	 * Part 2 TODO: Implement both of Norvig's Heuristics
	 *
	 * This function will do both Constraint Propagation and check
	 * the consistency of the network
	 *
	 * (1) If a variable is assigned then eliminate that value from
	 *     the square's neighbors.
	 *
	 * (2) If a constraint has only one possible place for a value
	 *     then put the value there.
	 *
	 * Note: remember to trail.push variables before you change their domain
	 * Return: a pair of a map and a Boolean. The map contains the pointers to all variables that were assigned during the whole 
	 *         NorvigCheck propagation, and mapped to the values that they were assigned. 
	 *         The Boolean is true if assignment is consistent, false otherwise.
	 */
	public Map.Entry<HashMap<Variable,Integer>,Boolean> norvigCheck ( )
	{
		
       		HashMap<Variable,Integer> map = new HashMap<Variable,Integer>();
                List<Constraint> RMC = network.getModifiedConstraints();
                for(int i=0;i<RMC.size();i++)
                {
                        List<Variable> LV = RMC.get(i).vars;
                        for(int j=0;j< LV.size();j++)
                        {
                                if(LV.get(j).isAssigned())
                                {
                                        List<Variable> neighbors = network.getNeighborsOfVariable(LV.get(j));
                                        int assignedValue = LV.get(j).getAssignment();
                                        for(int k=0;k<neighbors.size();k++)
                                        {
                                                if(neighbors.get(k).isAssigned() && neighbors.get(k).getAssignment() == assignedValue)
                                                {
                                                        return Pair.of(null,false);
                                                }
                                                Domain d = neighbors.get(k).getDomain();
                                                if(d.contains(assignedValue))
                                                {
                                                        trail.push(neighbors.get(k));
                                                        neighbors.get(k).removeValueFromDomain(assignedValue);

                                                        if(neighbors.get(k).getDomain().size() == 1)
                                                        {
                                                                neighbors.get(k).assignValue(neighbors.get(k).getValues().get(0));
																map.put(neighbors.get(k) , neighbors.get(k).getAssignment());
                                                        }
                                                }
                                        }
                                }
                        }
                }

                return Pair.of(map,true);
	}

	/**
	 * Optional TODO: Implement your own advanced Constraint Propagation
	 *
	 * Completing the three tourn heuristic will automatically enter
	 * your program into a tournament.
	 */
	private boolean getTournCC ( )
	{
		return false;
	}

	// =================================================================
	// Variable Selectors
	// =================================================================

	// Basic variable selector, returns first unassigned variable
	private Variable getfirstUnassignedVariable()
	{
		for ( Variable v : network.getVariables() )
			if ( ! v.isAssigned() )
				return v;

		// Everything is assigned
		return null;
	}

	/**
	 * Part 1 TODO: Implement the Minimum Remaining Value Heuristic
	 *
	 * Return: The unassigned variable with the smallest domain
	 */
	public Variable getMRV ( )
	{
		Variable min_domain = null;
		for(Variable v : network.getVariables())
			if( !(v.isAssigned())) {
				if(min_domain == null) min_domain = v;
				else if(v.getDomain().size() < min_domain.getDomain().size())
					min_domain = v;
			}
        	return min_domain;
	}

	/**
	 * Part 2 TODO: Implement the Minimum Remaining Value Heuristic
	 *                with Degree Heuristic as a Tie Breaker
	 *
	 * Return: The unassigned variable with the smallest domain and affecting the most unassigned neighbors.
	 *         If there are multiple variables that have the same smallest domain with the same number 
	 *         of unassigned neighbors, add them to the list of Variables.
	 *         If there is only one variable, return the list of size 1 containing that variable.
	 */
	public List<Variable> MRVwithTieBreaker ( )
	{	
		Variable min = getMRV();
		Variable nullVar = null;
		List<Variable> ans = new ArrayList<>();
		if( min == null)
			{
				ans.add(nullVar);
				return ans;
			}
		
		for(Variable v : network.getVariables())
			if(!(v.isAssigned())){
				if(v.getDomain().size() == min.getDomain().size())
					ans.add(v);
			}

		HashMap<Variable, Integer> countMap = new HashMap<Variable,Integer>();
		int count = 0; int max = 0;
		for(int i=0;i<ans.size();i++)
		{
			count = 0;
			Variable v = ans.get(i);
			for(Variable neighbor : network.getNeighborsOfVariable(v))
			{
				if(!neighbor.isAssigned())
					count++;
			}
			max = Math.max(max,count);
			countMap.put(v,count);
		}
		
		// We avoid the sort, since we select the first variable in the list -> just add the highest value to index 0.
		// Save time here..
		for(Variable key: countMap.keySet())
		{
			if(countMap.get(key) == max)
			{
				ans.remove(key);
				ans.add(0,key);
			}
		}
			
        	return ans;
   	}

	/**
	 * Optional TODO: Implement your own advanced Variable Heuristic
	 *
	 * Completing the three tourn heuristic will automatically enter
	 * your program into a tournament.
	 */
	private Variable getTournVar ( )
	{
		return null;
	}

	// =================================================================
	// Value Selectors
	// =================================================================

	// Default Value Ordering
	public List<Integer> getValuesInOrder ( Variable v )
	{
		List<Integer> values = v.getDomain().getValues();

		Comparator<Integer> valueComparator = new Comparator<Integer>(){

			@Override
			public int compare(Integer i1, Integer i2) {
				return i1.compareTo(i2);
			}
		};
		Collections.sort(values, valueComparator);
		return values;
	}

	/**
	 * Part 1 TODO: Implement the Least Constraining Value Heuristic
	 *
	 * The Least constraining value is the one that will knock the least
	 * values out of it's neighbors domain.
	 *
	 * Return: A list of v's domain sorted by the LCV heuristic
	 *         The LCV is first and the MCV is last
	 */
	public List<Integer> getValuesLCVOrder ( Variable v )
	{
        	List<Integer> ans = new ArrayList<>();
		HashMap<Integer,Integer> map = new HashMap<>();
		
		// Get the list of neighbors for the current variable
		List<Variable> neighbors = network.getNeighborsOfVariable(v);
		
		/*
 		 * Iterate through the list of values for the current variable.
		 * We need to check if this value is present in the neighboring domains.
		 * If present, then update the map with increased frequency.
 		 */ 		
		for(int i=0;i < v.getValues().size();i++)
		{
			int value = v.getValues().get(i);
			map.put(value,0);
			for(int j =0;j<neighbors.size();j++)
			{
				if(neighbors.get(j).getValues().contains(value))
				{
					if(map.containsKey(value))
					{
						map.put(value , map.get(value) + 1);
					}
					else
					{
						map.put(value,1);
					}
				}
			}
		}
		
		/*
 		 * Here we transfer the map values to a 2-D array for sorting.
 		 * We sort the array based on frequency in ascending order.
 		 */ 		
		int[][] temp = new int[map.size()][2];
		int count = 0;
		for(int key: map.keySet())
		{
			temp[count][0] = key;
			temp[count][1] = map.get(key);
			count++;
		}
		
		java.util.Arrays.sort(temp, new Comparator<int[]>(){
		public int compare(int[] a,int[] b)
		{
			if(a[1] > b[1])
				return 1;
			else if(a[1] == b[1])
				return a[0] > b[0] ? 1 : -1;
			return -1;
		}
		});
		
		// Returning the sorted array as list with least LCV at the start and max at the end.
		for(int i=0;i < temp.length;i++)
		{
			ans.add(temp[i][0]);
		}

		return ans;
	}

	/**
	 * Optional TODO: Implement your own advanced Value Heuristic
	 *
	 * Completing the three tourn heuristic will automatically enter
	 * your program into a tournament.
	 */
	public List<Integer> getTournVal ( Variable v )
	{
		return null;
	}

	//==================================================================
	// Engine Functions
	//==================================================================

	public void solve ( )
	{
		if ( hasSolution )
			return;

		// Variable Selection
		Variable v = selectNextVariable();

		if ( v == null )
		{
			for ( Variable var : network.getVariables() )
			{
				// If all variables haven't been assigned
				if ( ! var.isAssigned() )
				{
					System.out.println( "Error" );
					return;
				}
			}

			// Success
			hasSolution = true;
			return;
		}

		// Attempt to assign a value
		for ( Integer i : getNextValues( v ) )
		{
			// Store place in trail and push variable's state on trail
			trail.placeTrailMarker();
			trail.push( v );

			// Assign the value
			v.assignValue( i );

			// Propagate constraints, check consistency, recurse
			if ( checkConsistency() )
				solve();

			// If this assignment succeeded, return
			if ( hasSolution )
				return;

			// Otherwise backtrack
			trail.undo();
		}
	}

	public boolean checkConsistency ( )
	{
		switch ( cChecks )
		{
			case "forwardChecking":
				return forwardChecking().getValue();

			case "norvigCheck":
				return norvigCheck().getValue();

			case "tournCC":
				return getTournCC();

			default:
				return assignmentsCheck();
		}
	}

	public Variable selectNextVariable ( )
	{
		switch ( varHeuristics )
		{
			case "MinimumRemainingValue":
				return getMRV();

			case "MRVwithTieBreaker":
				return MRVwithTieBreaker().get(0);

			case "tournVar":
				return getTournVar();

			default:
				return getfirstUnassignedVariable();
		}
	}

	public List<Integer> getNextValues ( Variable v )
	{
		switch ( valHeuristics )
		{
			case "LeastConstrainingValue":
				return getValuesLCVOrder( v );

			case "tournVal":
				return getTournVal( v );

			default:
				return getValuesInOrder( v );
		}
	}

	public boolean hasSolution ( )
	{
		return hasSolution;
	}

	public SudokuBoard getSolution ( )
	{
		return network.toSudokuBoard ( sudokuGrid.getP(), sudokuGrid.getQ() );
	}

	public ConstraintNetwork getNetwork ( )
	{
		return network;
	}
}
