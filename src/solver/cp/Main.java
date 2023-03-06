package solver.cp;

import ilog.concert.IloException;

import ilog.cp.IloCP;

import java.io.FileNotFoundException;

import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main
{  
  public static void main(String[] args) throws FileNotFoundException, IOException, IloException
  {
	if(args.length == 0)
	{
		System.out.println("Usage: java Main <file>");
		return;
	}
		
  String input = args[0];
	Path path = Paths.get(input);
	String filename = path.getFileName().toString();
	System.out.println("Instance: " + input);
     
	Timer watch = new Timer();
	watch.start();
	CPInstance instance = new CPInstance(input);
	CPResult result = instance.solve();
	watch.stop();

	System.out.print("{\"Instance\": \"" + filename +
			"\", \"Time\": " + String.format("%.2f",watch.getTime()) +
			", \"Result\": " + instance.cp.getInfo(IloCP.IntInfo.NumberOfFails) + ", ");

    int[][] begin = result.getBegin();
    int[][] end = result.getEnd();
    String str = "";
    for(int i=0; i<result.getNumE();i++){
        for(int j=0; j<result.getNumD(); j++){
            str = str + " " + begin[i][j] + " " + end[i][j];
        }
    }
    System.out.println("\"Solution\": \""+ str.trim() + "\"}");
					   
    // Timer watch = new Timer();
    // watch.start();
    // CPInstance instance = new CPInstance(input);
    // instance.solveAustraliaBinary();
    // watch.stop();
     
    // // OUTPUT FORMAT
    // System.out.println("Instance: " + "Binary" + 
                       // " Time: " + String.format("%.2f",watch.getTime()) +
                       // " Result: " + instance.cp.getInfo(IloCP.IntInfo.NumberOfFails));
 
    // watch.start();
    // instance.solveAustraliaGlobal();
    // watch.stop();
     
    // // OUTPUT FORMAT
    // System.out.println("Instance: " + "Global" + 
                       // " Time: " + String.format("%.2f",watch.getTime()) +
                       // " Result: " + instance.cp.getInfo(IloCP.IntInfo.NumberOfFails));

    // watch.start();
    // instance.solveSendMoreMoney();
    // watch.stop();
     
    // // OUTPUT FORMAT
    // System.out.println("Instance: " + "SMM" + 
                       // " Time: " + String.format("%.2f",watch.getTime()) +
                       // " Result: " + instance.cp.getInfo(IloCP.IntInfo.NumberOfFails));
  }
}
