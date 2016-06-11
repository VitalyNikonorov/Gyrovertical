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

    public void run() {
        String str = null;
        String buffer = null;
        try {
            while (Main.isReceavingMode) {

                str = serialPort.readString();
                if(str != null) {
                    String[] splitted = str.split("\n");

                    for (int i = 0; i < splitted.length; ++i){
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
                            }else {
                                buffer = splitted[i];
                            }
                        }

                    }
                }
            }
        }
        catch (SerialPortException ex) {
            System.out.println(ex);
        }
    }

}

