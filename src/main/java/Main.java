import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import core.App;
import jssc.SerialPort;
import jssc.SerialPortException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
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
    private JButton threeDBtn;
    private JFreeChart pitchChart;
    private JFreeChart rollChart;
    private static SerialPort serialPort;
    private Thread inputHandler;
    public static boolean isReceavingMode = GlobalSettings.STOP;
    private DefaultListModel listModel;


    public TimeSeries pitchSeries;
    public TimeSeries rollSeries;

    public TimeSeriesCollection dataPitch;
    public TimeSeriesCollection dataRoll;


    public Main() {

        this.pitchSeries = new TimeSeries("Тангаж", Millisecond.class);
        dataPitch = new TimeSeriesCollection(this.pitchSeries);

        this.rollSeries = new TimeSeries("Крен", Millisecond.class);
        dataRoll = new TimeSeriesCollection(this.rollSeries);

        pitchChart = createChart(dataPitch, "Тангаж");
        rollChart = createChart(dataRoll, "Крен");


        ChartPanel pitchChartPanel = new ChartPanel(pitchChart);
        pitchPanel.removeAll();
        pitchPanel.add(pitchChartPanel, BorderLayout.CENTER);
        pitchPanel.validate();

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

        saveBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String directory = System.getProperty("user.dir");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd G 'в' HH:mm:ss z");
                String dir = dateFormat.format( new Date() );

                new File(String.format("%s/%s", directory, dir)).mkdir();

                File outputRaw = new File(String.format("%s/%s/%s", directory, dir, "raw.txt"));

                try(FileOutputStream os = new FileOutputStream(outputRaw)) {
                    os.write(dataList.getText().getBytes());
                    os.flush();

                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                File outputData = new File(String.format("%s/%s/%s", directory, dir, "data.txt"));

                try(FileOutputStream os = new FileOutputStream(outputData)) {
                    for (int i = 0; i < Math.min(pitchSeries.getItemCount(), rollSeries.getItemCount()); i++) {
                        os.write(String.format("тангаж:\t%f\tкрен:\t%f\n", pitchSeries.getDataItem(i).getValue(), rollSeries.getDataItem(i).getValue()).getBytes());
                    }
                    os.flush();

                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        fileBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {

                JFileChooser fileopen = new JFileChooser();
                int ret = fileopen.showOpenDialog(null);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File file = fileopen.getSelectedFile();
                    new Thread(new FileHandler(Main.this, file)).start();
                }

            }
        });
        threeDBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
                new LwjglApplication(new App(), config);
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

    private JFreeChart createChart(final XYDataset dataset, String title) {
        final JFreeChart result = ChartFactory.createTimeSeriesChart(
                title,
                "время",
                "градусы",
                dataset,
                true,
                true,
                false
        );
        final XYPlot plot = result.getXYPlot();
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(300000.0);  // 60 seconds
        axis = plot.getRangeAxis();
        axis.setRange(-180.0, 180.0);
        return result;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        listModel = new DefaultListModel();
        portList = new JList(listModel);

        dataList = new JTextArea();
        dataList.setRows(10);
    }
}
