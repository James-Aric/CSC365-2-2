import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


public class Runner extends Application{
    static FreqTable[] allTables = new FreqTable[1000];
    public static void main(String[]args) throws IOException{
        BTree tree;
        createFiles();
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 500, 500));
        primaryStage.show();
    }

    public static void createFiles() throws IOException{

        FileOutputStream fo = null;
        ObjectOutputStream os = null;
        URL url = new URL("https://en.wikipedia.org/wiki/Special:Random");
        URLConnection uc = null;

        WebScraper ws = new WebScraper();
        while(new File("C:\\Users\\James\\IdeaProjects\\CSC365-2-2\\src\\files\\").list().length < 1000){
            Document d = Jsoup.connect("https://en.wikipedia.org/wiki/Special:Random").get();
            String title = d.title();
            String update = ws.getModifyDate(d);
            File fi = new File("C:\\Users\\James\\IdeaProjects\\CSC365-2-2\\src\\files\\" + title.hashCode());
            if(fi.exists()){
                fi.delete();
            }
            FreqTable f = new FreqTable(title, update);
            f.mapPage1(ws.parseDoc(d));

            fo = new FileOutputStream("C:\\Users\\James\\IdeaProjects\\CSC365-2-2\\src\\files\\"+title.hashCode());
            os = new ObjectOutputStream(fo);

            os.writeObject(f);
            allTables[new File("C:\\Users\\James\\IdeaProjects\\CSC365-2-2\\src\\files\\").list().length -1] = f;
            os.close();
            fo.close();
        }
        System.out.println("Finished creating files.");
        File[] files = new File("C:\\Users\\James\\IdeaProjects\\CSC365-2-2\\src\\files\\").listFiles();
        String site, date;
        FileInputStream fin = null;
        ObjectInputStream oin = null;
        FreqTable current = null;
        Document d;
        boolean update = false;
        for(int i = 0; i < 1000; i++){
            try{
                fin = new FileInputStream(files[i]);
                oin = new ObjectInputStream(fin);
                current = (FreqTable) oin.readObject();
                d = Jsoup.connect(current.websiteURL).get();
                site = current.websiteURL;
                date = ws.getModifyDate(d);
                if(!current.updateDate.equalsIgnoreCase(date)){
                    System.out.println("updating!");
                    updateFile(d);
                    update = true;
                }
            }
            catch (Exception e){

            }finally {
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
        }
        if(update){
            calcMediods(5);
        }
    }

    public static void updateFile(Document d) throws IOException{
        WebScraper ws = new WebScraper();
        String title = d.title();
        String update = ws.getModifyDate(d);

        FreqTable f = new FreqTable(title, update);
        f.mapPage1(ws.parseDoc(d));

        FileOutputStream fo = new FileOutputStream("C:\\Users\\James\\IdeaProjects\\CSC365-2-2\\src\\files\\"+title.hashCode());
        ObjectOutputStream os = new ObjectOutputStream(fo);

        os.writeObject(f);
        os.close();
        fo.close();
    }

    public static ArrayList<Long> calcMediods(int k) throws IOException {
        ArrayList<Long> mediodNames = new ArrayList<Long>();
        long bestMediod = 0;
        CosineCalcs cs = new CosineCalcs();
        boolean finished = false;
        int runs = 0;
        FreqTable current, test;
        while (!finished) {
            int start = (1000 / k) * runs;
            int finish = (1000 / k) * (runs + 1);
            double bestTotal = 0;
            double currentTotal = 0;
            for (int i = start; i < finish; i++) {
                current = allTables[i];
                for (int j = start; j < finish; j++) {
                    try {
                        test = allTables[j];
                        currentTotal += cs.compare(current, test);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        test = null;
                    }
                }
                if (currentTotal > bestTotal) {
                    bestTotal = currentTotal;
                    bestMediod = current.websiteURL.hashCode();
                }
            }
            mediodNames.add(bestMediod);
            runs++;
            if (runs == k) {
                finished = true;
            }
        }
        for(int i = 0; i < k; i++){
            System.out.println(mediodNames.get(i));
        }
        FileOutputStream fo = new FileOutputStream("C:\\Users\\James\\IdeaProjects\\CSC365-2-2\\src\\files\\mediods");
        ObjectOutputStream os  = new ObjectOutputStream(fo);
        File f = new File("C:\\Users\\James\\IdeaProjects\\CSC365-2-2\\src\\files\\mediods");
        if(f.exists()){
            f.delete();
        }
        os.writeObject(mediodNames);
        fo.close();
        os.close();
        return mediodNames;

    }
}
