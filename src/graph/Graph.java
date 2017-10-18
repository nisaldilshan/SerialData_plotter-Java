package graph;

import com.fazecast.jSerialComm.SerialPort;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import java.lang.*;

/**
 *
 * @author Nisal_Dilshan
 */
public class Graph {

    static SerialPort chosenPort;
    static int x = 0;
    
    public static void main(String[] args) {
        
        // Create and configure te window
        JFrame window = new JFrame();
        window.setTitle("Data Plot GUI");
        window.setSize(600,400);
        window.setLayout(new BorderLayout());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //create a drop downbox and connect button
        JComboBox<String> portlist = new JComboBox<String>();
        JButton connectButton = new JButton("Connect");
        JPanel topPanel = new JPanel();
        topPanel.add(portlist);
        topPanel.add(connectButton);
        window.add(topPanel,BorderLayout.NORTH);
        
        //populate the dropdown box
        SerialPort[] portNames = SerialPort.getCommPorts();
        for (int i = 0; i < portNames.length; i++) {
            portlist.addItem(portNames[i].getSystemPortName());
        }
        
        //create the line graph
        XYSeries series = new XYSeries("Data Readings");
        
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart("Data Readings", "Time (seconds)", "Voltage (volts)", dataset);
        ChartPanel a= new ChartPanel(chart);
        chart.setAntiAlias(false);
        chart.setTextAntiAlias(false);
        
        a.setDomainZoomable(false);
        a.setRangeZoomable(false);
        window.add(a,BorderLayout.CENTER);
        
        //configure the connect button and use another thread to listen for data
        connectButton.addActionListener(new ActionListener(){
            @Override public void actionPerformed(ActionEvent arg0){
                if(connectButton.getText().equals("Connect")){
                    //attempt to conect to the serial port
                    chosenPort = SerialPort.getCommPort(portlist.getSelectedItem().toString());
                    chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
                    chosenPort.setBaudRate(115200);
                    if(chosenPort.openPort()){
                        connectButton.setText("Disconnect");
                        portlist.setEnabled(false);
                    }
                    
                    //create a tread that listens to incoming messages
                    Thread thread = new Thread(){
                        @Override public void run(){
                            
                            System.out.println("Thread started");
                            
                            Scanner scanner = new Scanner(chosenPort.getInputStream());

                                while(true){
                                    
                                    try{
                                        String line = scanner.nextLine();
                                        int number = Integer.parseInt(line);
                                        //series.add(x++, (int) Math.floor(Math.random() * 101));
                                        series.addOrUpdate(x++, number + (int) Math.floor(Math.random() * 501));
                                        //Thread.sleep(1);
                                    }catch(Exception ex){}                              
                                
                                if(x==500){
                                    try{
                                    Thread.sleep(10);
                                    series.clear();
                                    window.repaint();
                                    x=0;
                                    }catch (Exception e){}
                                }                               
                                
                                }
                        }
                    };
                    thread.start();
                }
                else{
                    //disconnect from port
                    chosenPort.closePort();
                    portlist.setEnabled(true);
                    connectButton.setText("Connect");
                    series.clear();
                    x = 0;
                }
            }
        });
        
        //show window
        window.setVisible(true);
    }
    
}
