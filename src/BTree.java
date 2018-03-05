import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BTree implements Serializable{



    RandomAccessFile file;
    BTreeNode root;
    int nodeSize = 4056;

    final static int k = 8;

    int height;


    public BTree(RandomAccessFile file) throws IOException{
        root = null;
        height = 0;
        this.file = file;
        createTree(this);
    }

    //BOOK
    private void createTree(BTree tree) throws IOException{
        BTreeNode x = allocateNode();
        x.leaf = 1;
        x.count = 0;
        diskwrite(x);
        tree.root = x;
    }

    public void insert(BTree t, int key) throws IOException
    {
        BTreeNode r = t.root;
        //checks if root needs to split
        if(r.count == 2*k - 1) {
            //allocate new node
            BTreeNode s = allocateNode();

            t.root = s;

            s.leaf = 0;
            s.count = 0;

            s.children[0] = r.id;
            //split root
            split(s,0,r);

            //call insert method
            nonfullInsert(s, key);
        }
        else {
            //if its not full just insert it
            nonfullInsert(r, key);
        }
    }

    public void nonfullInsert(BTreeNode x, int key) throws IOException {
        int i = x.count;

        if(x.leaf == 1) {
            while(i >= 1 && key < x.keys[i-1]) {
                x.keys[i] = x.keys[i-1];

                i--;
            }

            x.keys[i] = key;
            x.count ++;
            diskwrite(x);
        }

        else {
            int j = 0;
            while(j < x.count  && key > x.keys[j]) {
                j++;
            }

            BTreeNode child = diskread(x.children[j]);
            if(child.count == k*2 - 1) {
                split(x,j,child);

                if(key > x.keys[j])
                {
                    j++;
                }
            }
            child = diskread(x.children[j]);
            nonfullInsert(child,key);
        }
    }


    //x = parent    y = child    z = new child from y
    public void split(BTreeNode x, int index, BTreeNode y) throws IOException {
        //new child node
        BTreeNode z = allocateNode();

        z.leaf = y.leaf;
        z.count = k - 1;

        //copy end of y into front of z
        for(int j = 0; j < k - 1; j++) {
            z.keys[j] = y.keys[j+k];

        }
        if(y.leaf == 0){
            for(int j = 0; j < k; j++)
            {
                z.children[j] = y.children[j+k];
            }
        }

        y.count = k - 1;

        for(int j = x.count ; j> index ; j--) {
            x.children[j+1] = x.children[j];
        }
        x.children[index+1] = z.id;

        for(int j = x.count; j > index; j--) {
            x.keys[j] = x.keys[j-1];
        }

        x.keys[index] = y.keys[k-1];

        y.keys[k-1 ] = 0;
        x.leaf = 0;
        for(int j = 0; j < k - 1; j++) {
            y.keys[j + k] = 0;
        }



        x.count ++;

        diskwrite(x);
        diskwrite(z);
        diskwrite(y);
    }


    public BTreeNode diskread(long pos) throws IOException{
        file.seek(pos);
        FileChannel f = file.getChannel();
        ByteBuffer b = ByteBuffer.allocate(nodeSize);
        f.read(b);
        b.flip();
        BTreeNode temp = new BTreeNode(-1);
        temp.leaf = b.getInt();
        temp.id = b.getLong();

        temp.count = b.getInt();

        for(int i = 0; i < (2* k)-1; i++){
            temp.keys[i] = b.getLong();
        }
        for(int i = 0; i < 2*k; i++){
            temp.children[i] = b.getLong();
        }
        b.clear();
        return temp;
    }

    public void diskwrite(BTreeNode node) throws IOException{
        file.seek(node.id);
        FileChannel f = file.getChannel();

        ByteBuffer b = ByteBuffer.allocate(nodeSize);
        b.putInt(node.leaf);
        b.putLong(node.id);
        b.putInt(node.count);

        for(long k: node.keys){
            b.putLong(k);
        }
        for(long c: node.children){
            b.putLong(c);
        }
        b.flip();
        f.write(b);
        b.clear();
    }



    public BTreeNode allocateNode() throws IOException{
        file.seek(file.length());
        BTreeNode temp = new BTreeNode(file.getFilePointer());
        diskwrite(temp);
        return temp;
    }

    public boolean contains(BTreeNode x, long key, String url) throws IOException{
        int i = 0;
        while(i < x.count && key > x.keys[i]){
            i++;
        }
        if(i < x.count && key == x.keys[i]){
            try {
                FileInputStream fi = new FileInputStream("C:\\Users\\James\\IdeaProjects\\CSC365-2-2\\src\\files\\" + x.keys[i]);
                ObjectInputStream os = new ObjectInputStream(fi);
                FreqTable ft = (FreqTable) os.readObject();
                if(ft.websiteURL.equalsIgnoreCase(url)){
                    return true;
                }
                return false;
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else if(x.leaf == 1){
            System.out.println(x.leaf + "   " + i + "    " + x.count);
            return false;
        }
        else{
            BTreeNode temp = diskread(x.children[i]);
            return contains(temp, key, url);
        }
        return false;
    }
}
