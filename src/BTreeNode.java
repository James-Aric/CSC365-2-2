import java.io.IOException;

public class BTreeNode implements java.io.Serializable{
    static final int k = 8;
    long[] keys = new long[(2*k)-1];
    long[] children = new long[2*k];
    int leaf;
    long id;
    int count;

    public BTreeNode(long id) throws IOException{
        this.id = id;
        leaf = 1;
        count = 0;
        for(int i = 0; i < keys.length; i++){
            keys[i] = Long.MIN_VALUE;
        }
        for(int i = 0; i < children.length; i++){
            children[i] = Long.MIN_VALUE;
        }
    }
}
