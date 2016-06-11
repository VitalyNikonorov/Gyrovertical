import jssc.SerialPortList;

import javax.swing.*;

/**
 * Created by vitaly on 11.06.16.
 */
public class ListScanner implements Runnable {

    private JList portList;
    private DefaultListModel listModel;

    public ListScanner(JList portList, DefaultListModel listModel) {
        this.portList = portList;
        this.listModel = listModel;
    }

    public void run() {
        portList.removeAll();
        String[] portNames = SerialPortList.getPortNames();
        for(int i = 0; i < portNames.length; i++){
            listModel.addElement(portNames[i]);
        }
    }
}
