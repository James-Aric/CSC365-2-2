import java.util.ArrayList;

public class CosineCalcs {

    //takes two freqtables and begins the comparison calculations
    double compare(FreqTable page1, FreqTable page2) {
        //creates a new table in order to merge the other two
        FreqTable mergedTable = new FreqTable("","");
        //if either page is null, kicks out
        if (page1 == null || page2 == null){
            return 0;
        }

        //add the words to mergedTable from the first table
        mergedTable.addWordsFromArrayList(page1.getWords());
        //merge the second table into mergedTable
        mergedTable.mergeTables(page2);

        //create an array list from the newly merged table
        ArrayList<FreqTable.Word> tableList = mergedTable.getWords();

        //create two double arrays to act as vectors
        double[] p1 = new double[tableList.size()];
        double[] p2 = new double[tableList.size()];

        //iterate through each index in tableList
        for(int i = 0; i < p1.length; i++){
            //set each index of p1 and p2 equal to the respective words freq/that pages wordcount
            p1[i] = (double) tableList.get(i).getFreq1() / page1.getWordCount(); //<- to normalize the words in the page
            p2[i] = (double) tableList.get(i).getFreq2() / page2.getWordCount();
        }
        //call the cosineSimilarity method with the two vectors as inputs
        return cosineSimilarity(p1, p2);
    }

    //actual calculations
    private double cosineSimilarity(double[] p1, double[] p2) {
        //variable to keep track of the dot product
        double dotProduct = 0.0;
        //same as below
        double magp1 = 0.0;
        double magp2 = 0.0;
        //to be honest i looked this up
        for (int i = 0; i < p1.length; i++) {
            dotProduct += p1[i] * p2[i];
            magp1 += Math.pow(p1[i], 2);
            magp2 += Math.pow(p2[i], 2);
        }
        return dotProduct / (Math.sqrt(magp1) * Math.sqrt(magp2));
    }
}
