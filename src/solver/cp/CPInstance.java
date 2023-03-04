package solver.cp;

import ilog.cp.*;

import ilog.concert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import org.apache.commons.lang3.ArrayUtils;

public class CPInstance
{
  // BUSINESS parameters
  int numWeeks;
  int numDays;  
  int numEmployees;
  int numShifts;
  int numIntervalsInDay;
  int[][] minDemandDayShift;
  int minDailyOperation;
  
  // EMPLOYEE parameters
  int minConsecutiveWork;
  int maxDailyWork;
  int minWeeklyWork;
  int maxWeeklyWork;
  int maxConsecutiveNightShift;
  int maxTotalNightShift;

  // ILOG CP Solver
  IloCP cp;
    
  public CPInstance(String fileName)
  {
    try
    {
      Scanner read = new Scanner(new File(fileName));
      
      while (read.hasNextLine())
      {
        String line = read.nextLine();
        String[] values = line.split(" ");
        if(values[0].equals("Business_numWeeks:"))
        {
          numWeeks = Integer.parseInt(values[1]);
        }
        else if(values[0].equals("Business_numDays:"))
        {
          numDays = Integer.parseInt(values[1]);
        }
        else if(values[0].equals("Business_numEmployees:"))
        {
          numEmployees = Integer.parseInt(values[1]);
        }
        else if(values[0].equals("Business_numShifts:"))
        {
          numShifts = Integer.parseInt(values[1]);
        }
        else if(values[0].equals("Business_numIntervalsInDay:"))
        {
          numIntervalsInDay = Integer.parseInt(values[1]);
        }
        else if(values[0].equals("Business_minDemandDayShift:"))
        {
          int index = 1;
          minDemandDayShift = new int[numDays][numShifts];
          for(int d=0; d<numDays; d++)
            for(int s=0; s<numShifts; s++)
              minDemandDayShift[d][s] = Integer.parseInt(values[index++]);
        }
        else if(values[0].equals("Business_minDailyOperation:"))
        {
          minDailyOperation = Integer.parseInt(values[1]);
        }
        else if(values[0].equals("Employee_minConsecutiveWork:"))
        {
          minConsecutiveWork = Integer.parseInt(values[1]);
        }
        else if(values[0].equals("Employee_maxDailyWork:"))
        {
          maxDailyWork = Integer.parseInt(values[1]);
        }
        else if(values[0].equals("Employee_minWeeklyWork:"))
        {
          minWeeklyWork = Integer.parseInt(values[1]);
        }
        else if(values[0].equals("Employee_maxWeeklyWork:"))
        {
          maxWeeklyWork = Integer.parseInt(values[1]);
        }
        else if(values[0].equals("Employee_maxConsecutiveNigthShift:"))
        {
          maxConsecutiveNightShift = Integer.parseInt(values[1]);
        }
        else if(values[0].equals("Employee_maxTotalNigthShift:"))
        {
          maxTotalNightShift = Integer.parseInt(values[1]);
        }
      }
    }
    catch (FileNotFoundException e)
    {
      System.out.println("Error: file not found " + fileName);
    }
  }

  public void solve()
  {
    try
    {
      cp = new IloCP();

      // TODO: Employee Scheduling Model Goes Here
        
      // Important: Do not change! Keep these parameters as is
      cp.setParameter(IloCP.IntParam.Workers, 1);
      cp.setParameter(IloCP.DoubleParam.TimeLimit, 300);
    
      IloIntVar[][] shiftMatrix = new IloIntVar[numEmployees][]; // Which shift each employee does every day
      IloIntVar[][] lengthMatrix = new IloIntVar[numEmployees][];
      // Not sure about this IloTupleSet, but we'll see if its possible to implement this.  Alternatively, we could use flags and sums of 
      // the other arrays to calculate the number if workers at any given time.  This also reduces the number of values too.
    
      for(int i=0; i< shiftMatrix.length; i++){
        shiftMatrix[i] = cp.intVarArray(numDays, 0, 3);
        lengthMatrix[i] = cp.intVarArray(numDays, 0, maxDailyWork);
      }


      for(int i=0; i<numEmployees; i++){
        //  Here we'll use cp.allDiff() to ensure that for the first 4 days, the employees are always on different shifts.
        IloIntVar[] firstFour = ArrayUtils.subarray(shiftMatrix[i], 0, 4);
        cp.allDiff(firstFour);
        for(int j=0; j<numDays; j++){
            cp.add(cp.equiv(cp.eq(shiftMatrix[i][j], 0), cp.eq(lengthMatrix[i][j],0)));
            cp.add(cp.equiv(cp.geq(shiftMatrix[i][j], 1), cp.geq(lengthMatrix[i][j],4)));
            if(j>=maxConsecutiveNightShift){
                //if an employee is working a night shift, they cannot have worked one for the past maxConsecutiveNightShift days.
                IloIntVar[] prevShifts = ArrayUtils.subarray(shiftMatrix[i], j-maxConsecutiveNightShift, j+1);
                cp.add(cp.or(cp.neq(cp.maximum(prevShifts),cp.minimum(prevShifts)),cp.neq(shiftMatrix[i][j],1)));
            }
            //Here we add a cond
            // Checks that start time is  within their required shift// Checks that end time is also eithin required shift (need some rounding done (maybe -0.01))
            // Also not sure whether cp.mod rounds to ints, so might need to cast or something 
        }
        for(int j=0; j<(int)numDays%7; j++){
        // Here we're just handling the condition of workers working between minHours and maxHours a week.
            IloIntVar[] week = ArrayUtils.subarray(lengthMatrix[i], (j*7), (j+1)*7);
            cp.add(cp.ge(cp.sum(week),minWeeklyWork));
            cp.add(cp.ge(maxWeeklyWork, cp.sum(week));
        }
      }

      for(int i=0; i<numDays; i++){
        // Get some statement to ensure that a worker is in set[i][j] if and only they are working shift [j] on day [i].
        // We'll iterate over the tuple for each shift of each day, checking if the ensure that the size of the set is enough
        // We'll sum the length matrices for each thing in the tuple sets for the entire days and check that its greatere than minDailyOperation
        // We won't  get duplicates because wee;ve ensured that each employee only works on4 shift a day.
      }

      
      // cp.setParameter(IloCP.IntParam.SearchType, IloCP.ParameterValues.DepthFirst);   
  
      // Uncomment this: to set the solver output level if you wish
      // cp.setParameter(IloCP.IntParam.LogVerbosity, IloCP.ParameterValues.Quiet);
      if(cp.solve())
      {
        cp.printInformation();
        
        // Uncomment this: for poor man's Gantt Chart to display schedules
        // prettyPrint(numEmployees, numDays, beginED, endED);	
      }
      else
      {
        System.out.println("No Solution found!");
        System.out.println("Number of fails: " + cp.getInfo(IloCP.IntInfo.NumberOfFails));
      }
    }
    catch(IloException e)
    {
      System.out.println("Error: " + e);
    }
  }

  // SK: technically speaking, the model with the global constaints
  // should result in fewer number of fails. In this case, the problem 
  // is so simple that, the solver is able to re-transform the model 
  // and replace inequalities with the global all different constrains.
  // Therefore, the results don't really differ
  void solveAustraliaGlobal()
  {
    String[] Colors = {"red", "green", "blue"};
    try 
    {
      cp = new IloCP();
      IloIntVar WesternAustralia = cp.intVar(0, 3);
      IloIntVar NorthernTerritory = cp.intVar(0, 3);
      IloIntVar SouthAustralia = cp.intVar(0, 3);
      IloIntVar Queensland = cp.intVar(0, 3);
      IloIntVar NewSouthWales = cp.intVar(0, 3);
      IloIntVar Victoria = cp.intVar(0, 3);
      
      IloIntExpr[] clique1 = new IloIntExpr[3];
      clique1[0] = WesternAustralia;
      clique1[1] = NorthernTerritory;
      clique1[2] = SouthAustralia;
      
      IloIntExpr[] clique2 = new IloIntExpr[3];
      clique2[0] = Queensland;
      clique2[1] = NorthernTerritory;
      clique2[2] = SouthAustralia;
      
      IloIntExpr[] clique3 = new IloIntExpr[3];
      clique3[0] = Queensland;
      clique3[1] = NewSouthWales;
      clique3[2] = SouthAustralia;
      
      IloIntExpr[] clique4 = new IloIntExpr[3];
      clique4[0] = Queensland;
      clique4[1] = Victoria;
      clique4[2] = SouthAustralia;
      
      cp.add(cp.allDiff(clique1));
      cp.add(cp.allDiff(clique2));
      cp.add(cp.allDiff(clique3));
      cp.add(cp.allDiff(clique4));
      
	  cp.setParameter(IloCP.IntParam.Workers, 1);
      cp.setParameter(IloCP.DoubleParam.TimeLimit, 300);
	  cp.setParameter(IloCP.IntParam.SearchType, IloCP.ParameterValues.DepthFirst);   
	  
      if (cp.solve())
      {    
         System.out.println();
         System.out.println( "WesternAustralia:    " + Colors[(int)cp.getValue(WesternAustralia)]);
         System.out.println( "NorthernTerritory:   " + Colors[(int)cp.getValue(NorthernTerritory)]);
         System.out.println( "SouthAustralia:      " + Colors[(int)cp.getValue(SouthAustralia)]);
         System.out.println( "Queensland:          " + Colors[(int)cp.getValue(Queensland)]);
         System.out.println( "NewSouthWales:       " + Colors[(int)cp.getValue(NewSouthWales)]);
         System.out.println( "Victoria:            " + Colors[(int)cp.getValue(Victoria)]);
      }
      else
      {
        System.out.println("No Solution found!");
      }
    } catch (IloException e) 
    {
      System.out.println("Error: " + e);
    }
  }
  
  void solveAustraliaBinary()
  {
    String[] Colors = {"red", "green", "blue"};
    try 
    {
      cp = new IloCP();
      IloIntVar WesternAustralia = cp.intVar(0, 3);
      IloIntVar NorthernTerritory = cp.intVar(0, 3);
      IloIntVar SouthAustralia = cp.intVar(0, 3);
      IloIntVar Queensland = cp.intVar(0, 3);
      IloIntVar NewSouthWales = cp.intVar(0, 3);
      IloIntVar Victoria = cp.intVar(0, 3);
      
      cp.add(cp.neq(WesternAustralia , NorthernTerritory)); 
      cp.add(cp.neq(WesternAustralia , SouthAustralia)); 
      cp.add(cp.neq(NorthernTerritory , SouthAustralia));
      cp.add(cp.neq(NorthernTerritory , Queensland));
      cp.add(cp.neq(SouthAustralia , Queensland)); 
      cp.add(cp.neq(SouthAustralia , NewSouthWales)); 
      cp.add(cp.neq(SouthAustralia , Victoria)); 
      cp.add(cp.neq(Queensland , NewSouthWales));
      cp.add(cp.neq(NewSouthWales , Victoria)); 
      
	  cp.setParameter(IloCP.IntParam.Workers, 1);
      cp.setParameter(IloCP.DoubleParam.TimeLimit, 300);
	  cp.setParameter(IloCP.IntParam.SearchType, IloCP.ParameterValues.DepthFirst);   
	  
      if (cp.solve())
      {    
         System.out.println();
         System.out.println( "WesternAustralia:    " + Colors[(int)cp.getValue(WesternAustralia)]);
         System.out.println( "NorthernTerritory:   " + Colors[(int)cp.getValue(NorthernTerritory)]);
         System.out.println( "SouthAustralia:      " + Colors[(int)cp.getValue(SouthAustralia)]);
         System.out.println( "Queensland:          " + Colors[(int)cp.getValue(Queensland)]);
         System.out.println( "NewSouthWales:       " + Colors[(int)cp.getValue(NewSouthWales)]);
         System.out.println( "Victoria:            " + Colors[(int)cp.getValue(Victoria)]);
      }
      else
      {
        System.out.println("No Solution found!");
      }
    } catch (IloException e) 
    {
      System.out.println("Error: " + e);
    }
  }

  void solveSendMoreMoney()
  {
    try 
    {
      // CP Solver
      cp = new IloCP();
	
      // SEND MORE MONEY
      IloIntVar S = cp.intVar(1, 9);
      IloIntVar E = cp.intVar(0, 9);
      IloIntVar N = cp.intVar(0, 9);
      IloIntVar D = cp.intVar(0, 9);
      IloIntVar M = cp.intVar(1, 9);
      IloIntVar O = cp.intVar(0, 9);
      IloIntVar R = cp.intVar(0, 9);
      IloIntVar Y = cp.intVar(0, 9);
      
      IloIntVar[] vars = new IloIntVar[]{S, E, N, D, M, O, R, Y};
      cp.add(cp.allDiff(vars));
      
      //                1000 * S + 100 * E + 10 * N + D 
      //              + 1000 * M + 100 * O + 10 * R + E
      //  = 10000 * M + 1000 * O + 100 * N + 10 * E + Y 
      
      IloIntExpr SEND = cp.sum(cp.prod(1000, S), cp.sum(cp.prod(100, E), cp.sum(cp.prod(10, N), D)));
      IloIntExpr MORE   = cp.sum(cp.prod(1000, M), cp.sum(cp.prod(100, O), cp.sum(cp.prod(10,R), E)));
      IloIntExpr MONEY  = cp.sum(cp.prod(10000, M), cp.sum(cp.prod(1000, O), cp.sum(cp.prod(100, N), cp.sum(cp.prod(10,E), Y))));
      
      cp.add(cp.eq(MONEY, cp.sum(SEND, MORE)));
      
      // Solver parameters
      cp.setParameter(IloCP.IntParam.Workers, 1);
      cp.setParameter(IloCP.IntParam.SearchType, IloCP.ParameterValues.DepthFirst);
      if(cp.solve())
      {
        System.out.println("  " + cp.getValue(S) + " " + cp.getValue(E) + " " + cp.getValue(N) + " " + cp.getValue(D));
        System.out.println("  " + cp.getValue(M) + " " + cp.getValue(O) + " " + cp.getValue(R) + " " + cp.getValue(E));
        System.out.println(cp.getValue(M) + " " + cp.getValue(O) + " " + cp.getValue(N) + " " + cp.getValue(E) + " " + cp.getValue(Y));
      }
      else
      {
        System.out.println("No Solution!");
      }
    } catch (IloException e) 
    {
      System.out.println("Error: " + e);
    }
  }
  
 /**
   * Poor man's Gantt chart.
   * author: skadiogl
   *
   * Displays the employee schedules on the command line. 
   * Each row corresponds to a single employee. 
   * A "+" refers to a working hour and "." means no work
   * The shifts are separated with a "|"
   * The days are separated with "||"
   * 
   * This might help you analyze your solutions. 
   * 
   * @param numEmployees the number of employees
   * @param numDays the number of days
   * @param beginED int[e][d] the hour employee e begins work on day d, -1 if not working
   * @param endED   int[e][d] the hour employee e ends work on day d, -1 if not working
   */
  void prettyPrint(int numEmployees, int numDays, int[][] beginED, int[][] endED)
  {
    for (int e = 0; e < numEmployees; e++)
    {
      System.out.print("E"+(e+1)+": ");
      if(e < 9) System.out.print(" ");
      for (int d = 0; d < numDays; d++)
      {
        for(int i=0; i < numIntervalsInDay; i++)
        {
          if(i%8==0)System.out.print("|");
          if (beginED[e][d] != endED[e][d] && i >= beginED[e][d] && i < endED[e][d]) System.out.print("+");
          else  System.out.print(".");
        }
        System.out.print("|");
      }
      System.out.println(" ");
    }
  }

  /**
   * Generate Visualizer Input
   * author: lmayo1
   *
   * Generates an input solution file for the visualizer. 
   * The file name is numDays_numEmployees_sol.txt
   * The file will be overwritten if it already exists.
   * 
   * @param numEmployees the number of employees
   * @param numDays the number of days
   * @param beginED int[e][d] the hour employee e begins work on day d, -1 if not working
   * @param endED   int[e][d] the hour employee e ends work on day d, -1 if not working
   */
   void generateVisualizerInput(int numEmployees, int numDays, int[][] beginED, int[][] endED){
    String solString = String.format("%d %d %n", numEmployees, numDays);

    for (int d = 0; d <  numDays; d ++){
      for(int e = 0; e < numEmployees; e ++){
            solString += String.format("%d %d %n", (int)beginED[e][d], (int)endED[e][d]);
      }
    }

    String fileName = Integer.toString(numDays) + "_" + Integer.toString(numEmployees) + "_sol.txt";

    try {
      File resultsFile = new File(fileName);
      if (resultsFile.createNewFile()) {
        System.out.println("File created: " + fileName);
      } else {
        System.out.println("Overwritting the existing " + fileName);
      }
      FileWriter writer = new FileWriter(resultsFile, false);
      writer.write(solString);
      writer.close();
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
}

}
