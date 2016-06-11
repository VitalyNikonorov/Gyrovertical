import jssc.SerialPort;
import jssc.SerialPortException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vitaly on 11.06.16.
 */
public class InputHandler implements Runnable {
    SerialPort serialPort;
    OrientationOperator operator;
    Main main;

    public InputHandler(SerialPort serialPort, Main main){
        this.serialPort = serialPort;
        operator = new OrientationOperator(main);
        this.main = main;
    }
    public static boolean isFirst = true;

    public void run() {
        String str = null;
        String buffer = null;
        try {
            while (Main.isReceavingMode) {

                str = serialPort.readString();
                if(str != null) {
                    //System.out.println("///////\n"+str); //needlog
                    //System.out.println("///////"); //needlog
                    String[] splitted = str.split("\n");

                    for (int i = 0; i < splitted.length; ++i){
                        //System.out.println("splitted: "+splitted[i]); //needlog
                        if(splitted[i].length() > 0) {
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
                            if(splitted[i].length()>2) {
                                if ((splitted[i].charAt(0) == '{') && (splitted[i].charAt(splitted[i].length() - 2) == '}')) {
//                                    System.out.println("json: " + splitted[i]); //туувдщп
                                    main.dataList.append(splitted[i] + "\n");
                                    try {
                                        JSONObject jsonObject = new JSONObject(splitted[i]);
                                        operator.addData(jsonObject);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    //System.out.println("gone"); //needlog
                                    buffer = "";
                                } else {
                                    buffer = splitted[i];
                                }
                            }else {
                                buffer = splitted[i];
                            }
                        }

                    }
                }
//                MainFrame.dataList.append(str);
            }
        }
        catch (SerialPortException ex) {
            System.out.println(ex);
        }
    }

}

