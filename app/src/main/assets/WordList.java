import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yingchen on 2018/6/1.
 */
public class WordList {

    private static Map<String,ArrayList<String>> list = new HashMap<String,ArrayList<String>>();
    private static List<String> nineLetterWords = new ArrayList<>();

    public void readWord() {
        String dir = System.getProperty("user.dir");
        String file = dir + File.separator + "wordlist.txt";

        try (BufferedReader inputFile = new BufferedReader(new InputStreamReader(
                new FileInputStream(file),
                Charset.defaultCharset()));) {
            String line;

            while ((line = inputFile.readLine()) != null) {
                if (line.length() == 9) {
                    nineLetterWords.add(line);
                }
                String key = line.substring(0, 3);
                if (!list.containsKey(key)) {
                    list.put(key, new ArrayList<>());
                    list.get(key).add(line);
                } else {
                    list.get(key).add(line);
                }
            }

        } catch (FileNotFoundException fnfe) {
            System.out.println("*** OOPS! A file was not found : " + fnfe.getMessage());
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            System.out.println("Something went wrong! : " + ioe.getMessage());
            ioe.printStackTrace();
        }
    }

    public void writeMap() {
        try{
            File file = new File("wordlistmap");
            FileOutputStream fos=new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(list);
            oos.flush();
            oos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeNineLetterWords() {
        try{
            File file = new File("allnineletterwords");
            FileOutputStream fos=new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(nineLetterWords);
            oos.flush();
            oos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        WordList wl = new WordList();
        wl.readWord();
        wl.writeMap();
        wl.writeNineLetterWords();

    }
}
