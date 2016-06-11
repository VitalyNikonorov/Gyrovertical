import jssc.SerialPortException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

/**
 * Created by vitaly on 11.06.16.
 */
public class FileHandler implements Runnable {

    FileDataOperator operator;
    Main main;
    File file;

    public FileHandler(Main main, File file){
        operator = new FileDataOperator(main);
        this.main = main;
        this.file = file;
    }

    public void run() {
        String buffer = null;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while (( line = br.readLine()) != null) {

                if (line != null) {
                    String[] splitted = line.split("\n");

                    for (int i = 0; i < splitted.length; ++i) {
                        if (splitted[i].length() > 0) {
                            if (splitted[i].charAt(0) != '{') {
                                if (buffer != null) {
                                    splitted[i] = buffer + splitted[i];
                                } else {
                                    continue;
                                }
                            }
                        } else {
                            continue;
                        }

                        if (splitted[i].lastIndexOf('{') == 0) {
                            if (splitted[i].length() > 2) {
                                if ((splitted[i].charAt(0) == '{') && (splitted[i].charAt(splitted[i].length() - 1) == '}')) {
                                    main.dataList.append(splitted[i] + "\n");
                                    try {
                                        JSONObject jsonObject = new JSONObject(splitted[i]);
                                        operator.addData(jsonObject);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    buffer = "";
                                } else {
                                    buffer = splitted[i];
                                }
                            } else {
                                buffer = splitted[i];
                            }
                        }

                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
