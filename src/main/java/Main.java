import jssc.SerialPort;
import jssc.SerialPortException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by vitaly on 09.06.16.
 */
public class Main {
    private JPanel mainPanel;
    private JButton scanBtn;
    private JList portList;
    private JButton onBtn;
    private JButton offBtn;
    public  JTextArea dataList;
    private JPanel pitchPanel;
    private JPanel rollPanel;
    private JButton fileBtn;
    public JLabel pitchLabel;
    public JLabel rollLabel;
    private JButton saveBtn;
    private JFreeChart pitchChart;
    private JFreeChart rollChart;
    private DefaultCategoryDataset data;
    private static SerialPort serialPort;
    private Thread inputHandler;
    public static boolean isReceavingMode = GlobalSettings.STOP;
    private DefaultListModel listModel;


    public TimeSeries pitchSeries;
    public TimeSeries rollSeries;


    public Main() {
        data = new DefaultCategoryDataset();
        data.setValue(100, "Amount", "TP");
        data.setValue(90, "Amount", "TS");
        data.setValue(41, "Amount", "TT");

//        this.pitchSeries = new TimeSeries("Random Data", Millisecond.class);
//        final TimeSeriesCollection dataset = new TimeSeriesCollection(this.series);

        pitchChart = ChartFactory.createAreaChart("Тангаж", "Такты", "Градусы", data, PlotOrientation.VERTICAL, false, false, false);
        rollChart = ChartFactory.createAreaChart("Крен", "Такты", "Градусы", data, PlotOrientation.VERTICAL, false, false, false);

        CategoryPlot plotPitch = pitchChart.getCategoryPlot();
        plotPitch.setDomainGridlinePaint(Color.GREEN);
        ChartPanel pitchChartPanel = new ChartPanel(pitchChart);
        pitchPanel.removeAll();
        pitchPanel.add(pitchChartPanel, BorderLayout.CENTER);
        pitchPanel.validate();

        CategoryPlot plotRoll = rollChart.getCategoryPlot();
        plotRoll.setDomainGridlinePaint(Color.GREEN);
        ChartPanel rollChartPanel = new ChartPanel(rollChart);
        rollPanel.removeAll();
        rollPanel.add(rollChartPanel, BorderLayout.CENTER);
        rollPanel.validate();

        scanBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                portList.removeAll();

                new Thread(new ListScanner(portList, listModel)).start();
            }
        });
        onBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                serialPort = new SerialPort((String) portList.getSelectedValue());
                isReceavingMode = GlobalSettings.RECEIVE;


                try {
                    serialPort.openPort();//Open serial port
                    serialPort.setParams(SerialPort.BAUDRATE_38400,
                            SerialPort.DATABITS_8,
                            SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);//Set params. Also you can set params by this string: serialPort.setParams(9600, 8, 1, 0);

                    serialPort.writeString("b");
                }
                catch (SerialPortException ex) {
                    System.out.println(ex);
                }

                System.out.println("----------");

                try {
                    System.out.println("CTS : " + new Boolean(serialPort.isCTS()).toString());
                    System.out.println("DSR : " + new Boolean(serialPort.isDSR()).toString());
                    System.out.println("RING : " + new Boolean(serialPort.isRING()).toString());
                    System.out.println("RLSD : " + new Boolean(serialPort.isRLSD()).toString());
                    System.out.println("Opend : " + new Boolean(serialPort.isOpened()).toString());
                } catch (SerialPortException escept) {
                    escept.printStackTrace();
                }

                inputHandler = new Thread(new InputHandler(serialPort, Main.this));
                inputHandler.start();
            }
        });
        offBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if(!serialPort.isOpened()){
                        serialPort.openPort();//Open serial port
                        serialPort.setParams(SerialPort.BAUDRATE_38400,
                                SerialPort.DATABITS_8,
                                SerialPort.STOPBITS_1,
                                SerialPort.PARITY_NONE);//Set params. Also you can set params by this string: serialPort.setParams(9600, 8, 1, 0);
                    }
                    isReceavingMode = GlobalSettings.STOP;
                    serialPort.writeString("e");
                    System.out.print("stop");
                    serialPort.closePort();//Close serial port
                    inputHandler = null;
                    Date date = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("mm:ss.SSS");
                    String formattedDate = sdf.format(date);
                    dataList.append(formattedDate + "\n");
                }
                catch (SerialPortException ex) {
                    System.out.println(ex);
                }
            }
        });
    }

    public static void main(String[] args){
        JFrame mainFrame = new JFrame("БИСО");
        mainFrame.setContentPane(new Main().mainPanel);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        listModel = new DefaultListModel();
        portList = new JList(listModel);

        dataList = new JTextArea();
        dataList.setRows(10);
//        dataList.setLineWrap (true);

    }
}
