package client;

public class ManagerFlag {
    private boolean iAmManager = false;
    public ManagerFlag(){

    }
    public boolean getFlag(){
        return iAmManager;
    }
    public void setFlag(boolean flag){
        iAmManager = flag;
    }
}
