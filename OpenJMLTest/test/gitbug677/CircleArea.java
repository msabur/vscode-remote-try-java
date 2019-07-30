public class CircleArea{
    //@ requires r > 0; 
    //@ requires 3*r*r <= Integer.MAX_VALUE;
    void calculateArea(int r)
    {
        //@ show r, 3*r, 3*r*r;
        int area = 3*r*r;
        //@ show area;
    }

    //@ requires r > 0; 
    //@ requires 3*r*r <= Integer.MAX_VALUE;
    //@ @org.jmlspecs.annotation.Options("-code-math=java")
    void calculateAreaJava(int r)
    {
        //@ show r, 3*r, 3*r*r;
        int area = 3*r*r;
        //@ show area;
    }

    //@ requires r > 0; 
    //@ requires 3*r*r <= Integer.MAX_VALUE;
    //@ @org.jmlspecs.annotation.Options("-code-math=math")
    void calculateAreaMath(int r)
    {
        //@ show r, 3*r, 3*r*r;
        int area = 3*r*r;
        //@ show area;
    }
}
