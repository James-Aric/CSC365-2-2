import java.io.Serializable;
import java.util.ArrayList;

public class FreqTable implements Serializable{
    //variable to keep track of filled spots
    int filled = 0;

    //place holder variables so that you dont have to constantly call .length methods
    int p1Length = 0;
    int p2Length = 0;

    String websiteURL;
    String updateDate;

    //inner node class, used for storing data in hash table
    static final class Word implements Serializable{
        //each node contains a key (word) freq (frequency it occurs) and a reference to the next node (hopefully no collisions)
        String key;
        int freq1, freq2;
        Word next;

        //Constructor that takes a key, and a frequency for the first page and second page
        Word(String k, int f1, int f2) {
            this.key = k;
            freq1 = f1;
            freq2 = f2;
        }

        //sets the words next reference to the input
        void setNext(Word w) {
            next = w;
        }

        //returns frequency for page 1
        int getFreq1() {
            return freq1;
        }

        //returns frequency for page 2
        int getFreq2() {
            return freq2;
        }

        //used for merging tables
        void add(int p1, int p2) {
            freq1 += p1;
            freq2 += p2;
        }
    }

    //create a new array of words with base size 100
    Word[] table = new Word[100];

    public FreqTable(String website, String lastUpdated){
        this.websiteURL = website;
        this.updateDate = lastUpdated;
    }

    //mostly used for testing
    int getFreq1(String k) {
        int h = k.hashCode();
        int i = Math.abs(h % (table.length));
        Word e = table[i];
        for (;;) {
            if (e == null) {
                return -1;
            } else if (e.key.equals(k)) {
                return e.getFreq1();
            } else {
                e = e.next;
            }
        }
    }

    int getFreq2(String k) {
        int h = k.hashCode();
        int i = Math.abs(h % (table.length));
        Word e = table[i];
        for (;;) {
            if (e == null) {
                return -1;
            } else if (e.key.equals(k)) {
                return e.getFreq2();
            } else {
                e = e.next;
            }
        }
    }


    //method to put the inputted word into the table
    void put(Word word) {
        //hashes the word
        int h = word.key.hashCode();
        //takes the absolute value of the hash % table length -1
        int i = Math.abs(h % (table.length));
        //creates a local variable for the hashed index
        Word current = table[i];
        for (;;) {
            //if the hashed index is null, it is now the inputted word
            if (current == null) {
                table[i] = new Word(word.key, word.getFreq1(), word.getFreq2());
                //sets next equal to current (mostly used if something initially was there)
                table[i].setNext(current);
                //increments the filled counter which is used for resizing
                filled++;
                //checks if filled is at the point where the table needs to be resized
                if(filled >= table.length * .75){
                    //calls resize table method
                    resizeTable();
                }
                break;
            }
            //if the key of the hashed index is equal to the inputted words key, it adds the inputted words freq1 and freq2 to the indexs word
            else if (word.key.equalsIgnoreCase(current.key)) {
                table[i].add(word.getFreq1(), word.getFreq2());
                break;
            }
            //if the current index has a next reference
            else if(current.next!=null){
                //sets current equal to the next reference
                current = current.next;
            }
            //sets word equal to the next reference for that index
            else{
                table[i].setNext(word);
                break;
            }
        }
    }


    //takes an input of FreqTable, and merges it into the current table. The input uses freq2, the orinal uses freq1
    void mergeTables(FreqTable t) {
        for (Word w : t.table) {
            while (w != null) {
                this.put(new Word(w.key, 0, w.getFreq1()));
                w = w.next;
            }
        }
    }

    //uses functional for loop to add arrays to the hashtable from an arraylist
    void addWordsFromArrayList(ArrayList<Word> w) {
        w.forEach(this::put);
    }


    //method to print out the tables key, freq1, and freq2. used for testing and debugging
    void display(){
        for(Word w : table){
            while (w != null){
                System.out.printf("%1$-45s %2$-45s %3$-45s\n",w.key, w.getFreq1(), w.getFreq2());
                w = w.next;
            }
        }
    }

    //map the first page to a table (sloppy but it works)
    void mapPage1(String text) {
        String[] temp = text.split("\\s+");
        p1Length = temp.length;
        //System.out.println(length);
        for (int i = 0; i < p1Length; i++) {
            Word w = new Word(temp[i], 1, 0);
            put(w);
        }
    }

    //same as above but for the second table
    void mapPage2(String text) {
        String[] temp = text.split("\\s+");
        p2Length = temp.length;
        //System.out.println(length);
        for (int i = 0; i < p2Length; i++) {
            Word w = new Word(temp[i], 0, 1);
            put(w);
        }
    }

    //return an array list of the words in the table
    public ArrayList<Word> getWords(){
        ArrayList<Word> words = new ArrayList<>();
        for(Word w: table){
            while(w != null){
                words.add(w);
                w = w.next;
            }
        }
        return words;
    }

    //iterates through the table and returns the total number of filled slots
    int getWordCount(){
        int count = 0;
        for(Word w: table){
            if (w != null){
                count++;
                w = w.next;
            }
        }
        return count;
    }

    //resizes the table ones the filled counter reaches 3/4 of the table length
    void resizeTable(){
        filled = 0;
        Word[] temp = table;
        table = new Word[table.length*2];
        for(int i = 0; i < temp.length; i++){
            if(temp[i] != null){
                put(temp[i]);
                for(;;){
                    if (temp[i].next == null) {
                        break;
                    } else {
                        temp[i] = temp[i].next;
                        put(temp[i]);
                    }
                }
            }
        }
    }

}
