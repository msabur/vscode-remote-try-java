public class A {
    public static void main(String[] args) {
        int x = 3;
        //@ ensures false;  // statement spec, but no warning about false assertion
        int y = 4;
    }
}
