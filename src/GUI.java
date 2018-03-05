import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class GUI {


    //define the JavaFX objects
    public TextField input;
    public Label result;
    public Label closest;
    public Label mediod;
    public Label mediodres;

    //String for the url inputted by the user
    private String inputURL;

    //creating the webscraper to go through the hardcoded URL's, and the inputURL
    WebScraper ws = new WebScraper();

    //variables to keep track of the best result, and the best results index
    double compareResult = 0;
    int compareIndex = 0;
    FileInputStream fin;
    ObjectInputStream oin;
    Document d;
    String bestWebsite = "";
    long[] mediods = new long[5];

    BTree tree;

    public void createTree() throws IOException{
        try {
            File tmp = new File("C:\\Users\\James\\IdeaProjects\\CSC365-2-2\\src\\files\\btree");
            if(tmp.exists()){
                tmp.delete();
            }else{
                System.out.println("Delete operation is failed.");
            }
            RandomAccessFile treeFile = new RandomAccessFile(tmp, "rw");
            tree = new BTree(treeFile);

            String[] list = new File("C:\\Users\\James\\IdeaProjects\\CSC365-2-2\\src\\files\\").list();
            for(int i = 0; i < 1000; i++){
                if(!list[i].equalsIgnoreCase("btree")) {
                    tree.insert(tree, Integer.parseInt(list[i]));
                }
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //method called when pressing the compare button, compares the user input to
    public void compare(ActionEvent actionEvent) throws IOException{
        //try catch in order to catch illegal url input
        try{
            //get the user input from the input box
            inputURL = input.getText();
            //create two frequency tables
            FreqTable f1, f2;
            f1 = new FreqTable("", "");
            //create another string object that stores the parsed url from the user input
            String compare = ws.parseCompare(input.getText());
            File[] files = new File("C:\\Users\\James\\IdeaProjects\\CSC365-2-2\\src\\files\\").listFiles();
            f1.mapPage1(ws.parseCompare(inputURL));
            Document d = Jsoup.connect(inputURL).get();
            String title = d.title();
            if(tree.contains(tree.root, title.hashCode(), title)){
                closest.setText(inputURL + " is contained within the tree");
                if(compareMediods(f1).equalsIgnoreCase(title)){
                    mediod.setText("This website is one of the mediods!");
                    result.setText(" ");
                    mediodres.setText(" ");
                }
            }
            else {
                //iterate through the hardcoded parsed urls to find the best match
                for (int i = 0; i < 1000; i++) {
                    //instantiate the two tables created above
                    f2 = new FreqTable("", "");
                    try {
                        fin = new FileInputStream(files[i]);
                        oin = new ObjectInputStream(fin);
                        f2 = (FreqTable) oin.readObject();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (fin != null) {
                            try {
                                fin.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        if (oin != null) {
                            try {
                                oin.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //create a new cosineCalc object to do the cosine similarity calculations
                    CosineCalcs cc = new CosineCalcs();
                    //store the results of the comparison in a variable to compare against previous results
                    double tempResults = cc.compare(f1, f2);
                    //if the most recently compared data is better then previously calculated data
                    if (tempResults > compareResult) {
                        //the most recent comparison becomes the compareResult
                        compareResult = tempResults;
                        bestWebsite = f2.websiteURL;
                        //stores the index of the best fit
                        compareIndex = i;
                    }
                }

                //sets the result text box to the similarity value
                result.setText("" + compareResult);
                //sets the other text box to the website url
                closest.setText(bestWebsite);
                //just to be safe, reset the value of compare result
                compareResult = 0;
                compareMediods(f1);
            }
        }
        catch(IllegalArgumentException e){
            //sets one of the boxes to the following output to show that an improper url was input
            result.setText("Invalid input");
        }
    }

    //hacked together way of getting the url
    String getFileName(long name)throws IOException{
        try {
            File f = new File("C:\\Users\\James\\IdeaProjects\\CSC365-2-2\\src\\files\\" + name);
            FileInputStream fi = new FileInputStream(f);
            ObjectInputStream oi = new ObjectInputStream(fi);
            FreqTable t = (FreqTable) oi.readObject();
            fi.close();
            oi.close();
            return t.websiteURL;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }


    public String compareMediods(FreqTable f1) throws IOException{
        FileInputStream fin;
        ObjectInputStream oin;
        FreqTable test;
        CosineCalcs cc = new CosineCalcs();
        ArrayList<Long> mediods = null;
        try{
            fin = new FileInputStream("C:\\Users\\James\\IdeaProjects\\CSC365-2-2\\src\\files\\mediods");
            oin = new ObjectInputStream(fin);
            mediods = (ArrayList<Long>) oin.readObject();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        double best = 0;
        double temp;
        String bestFile = "";
        for(int i = 0; i < 5; i ++){
            try {
                long fileName = mediods.get(i);
                fin = new FileInputStream(new File("C:\\Users\\James\\IdeaProjects\\CSC365-2-2\\src\\files\\" + fileName));
                oin = new ObjectInputStream(fin);
                test = (FreqTable) oin.readObject();
                if(test.websiteURL.equalsIgnoreCase(f1.websiteURL)){
                    return test.websiteURL;
                }
                else {
                    temp = cc.compare(f1, test);
                    if (temp > best) {
                        best = temp;
                        bestFile = test.websiteURL;
                    }
                    System.out.println(test.websiteURL);
                }
                fin.close();
                oin.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        mediod.setText("Most Similar Center: " + bestFile);
        mediodres.setText("Cluster Similarity: " + best);
        return bestFile;
    }
}