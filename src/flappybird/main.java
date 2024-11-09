
package flappybird;


import java.io.IOException;
import javax.swing.*;

public class main{

    public static void main(String[] args) throws IOException {
        int boardWidth = 360;
        int boardHeight = 640;
        
        JFrame frame = new JFrame("Pajarraco Alocado");        
        //frame.setVisible(true);
        frame.setSize(boardWidth,boardHeight);        
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(new ImageIcon(FlappyBird.class.getResource("flappybird.png")).getImage());

        FlappyBird fb = new FlappyBird();        
        frame.add(fb);
        frame.pack();
        frame.requestFocus();
        frame.setVisible(true);
    }
    
}
