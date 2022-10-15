package client;

public class Position {
    private int x;
    private int y;

    Position(int x, int y){
        this.x = x;
        this.y = y;
    }

    Position(Position position){
        this.x = position.x;
        this.y = position.y;
    }

    public int getX(){
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x){
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setXY(int x, int y){
        this.x = x;
        this.y = y;
    }

    public void updateXY(Position position){
        this.x = position.getX();
        this.y = position.getY();
    }

    public void boundaryCheckPos(){
        if (x<0) x=0;
        else if (x>800) x=800;

        if (y<0) y=0;
        else if (y>650) y=650;
    }

    public double distance(Position position){
        double temp = Math.pow((x-position.getX()), 2) + Math.pow((y-position.getY()), 2);
        return Math.pow(temp, 0.5);
    }
}
