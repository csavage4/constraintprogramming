package solver.cp;

public class CPResult {
    private int numEmployees;
    private int numDays;
    private int[][] beginED;
    private int[][] endED;

    public CPResult(int a, int b, int[][] c, int[][]d){
        this.numEmployees = a;
        this.numDays = b;
        this.beginED = c;
        this.endED = d;
    }

    public int getNumE(){
        return numEmployees;
    }

    public int getNumD(){
        return numDays;
    }

    public int[][] getBegin(){
        return beginED;
    }

    public int[][] getEnd(){
        return endED;
    }

}
