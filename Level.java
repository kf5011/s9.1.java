
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.io.*;
import java.net.*;

public class Level extends JFrame implements Runnable {
    public static final long serialVersionUID = 2L;

    protected static DatagramSocket socket = null;

    public static void main ( String[] args ) throws UnknownHostException,
                                                     SocketException {
        socket = new DatagramSocket(
            65201,
            InetAddress.getLocalHost()/*<-- gets the PC IP address*/
        );
        /* confirm socket port and ip */
        System.err.println("Listening on port:"+socket.getLocalPort()+
        " at ip:"+socket.getLocalAddress());

        /* Create and build UI */
        SwingUtilities.invokeLater( new Runnable() {
            public void run() { new Level(); }
        } );
    }


    public Level() {
        super("Digital Level");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel content = new JPanel( );
        content.setLayout( new BoxLayout( content, BoxLayout.Y_AXIS) );

        JPanel addr = new JPanel(new FlowLayout());
        JPanel level = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        rollgauge = new JSlider(-90,90,roll);
        rollgauge.setEnabled(false);
        rollgauge.setLabelTable( rollgauge.createStandardLabels(30) );
        rollgauge.setPaintLabels(true);
        rollgauge.setMajorTickSpacing(10);
        rollgauge.setPaintTicks(true);
        c.gridx=1;
        c.gridy=0;
        level.add(rollgauge,c);
        pitchgauge = new JSlider(SwingConstants.VERTICAL,-90,90,pitch);
        pitchgauge.setEnabled(false);
        pitchgauge.setLabelTable( pitchgauge.createStandardLabels(30) );
        pitchgauge.setPaintLabels(true);
        pitchgauge.setMajorTickSpacing(10);
        pitchgauge.setPaintTicks(true);
        c.gridx=0;
        c.gridy=1;
        level.add(pitchgauge,c);
        Bubble plot = new Bubble();
        c.gridx=1;
        c.gridy=1;
        c.fill = GridBagConstraints.BOTH;
        level.add(plot,c);


        content.add(level);
        this.setContentPane(content);
        this.pack();
        this.setVisible(true);

        /* start thread that handles comminications */
        (new Thread(this)).start();
    }


    public void run() {
        while(true) {
            try{
                /* start with fresh datagram packet */
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive( packet );
                /* extract message and pick appart into lines */
                String message = new String(packet.getData());
                String[] lines = message.trim().split("\n");
                handle_message(lines);
            }catch(IOException e){
                System.err.println(e.getMessage());
            }
        }
    }

    int pitch=-30;
    int roll = 30;
    JSlider rollgauge, pitchgauge;
    int setPitch(int p){
        pitch=p;
        pitchgauge.setValue(pitch);
        return p;
    }
    int setRoll(int r){
        roll=r;
        rollgauge.setValue(roll);
        return r;
    }

    void handle_message(String[] lines){
        for(String l : lines){
            String[] pair = l.split(":");
            switch(pair[0]){
                case "roll": setRoll((int)Float.parseFloat(pair[1]));
                break;
                case "pitch": setPitch((int)Float.parseFloat(pair[1]));
                break;
            }
            repaint();
        }
    }

    class Bubble extends JPanel {
        public static final long serialVersionUID = 2L;
        public Bubble(){
            this.setBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        }
        public void paintComponent(Graphics g){
            Dimension size = this.getSize();
            g.setColor(Color.green.darker().darker());
            g.drawLine(size.width/2+roll,0,size.width/2+roll,size.height);
            g.setColor(Color.red.darker().darker());
            g.drawLine(0,size.height/2-pitch,size.width,size.height/2-pitch);
        }
    }
}
