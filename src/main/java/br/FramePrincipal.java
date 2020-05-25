package br;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class FramePrincipal extends JFrame {

    PainelConsumidor painelConsumidor1;
    PainelConsumidor painelConsumidor2;

    public FramePrincipal() throws HeadlessException {
        super("Kafka Client");
        setMinimumSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        
        
        painelConsumidor1 = new PainelConsumidor();
        painelConsumidor1.setMinimumSize(new Dimension(50, 50));

        painelConsumidor2 = new PainelConsumidor();
        painelConsumidor2.setMinimumSize(new Dimension(50, 50));

        JSplitPane splitPane = new JSplitPane(SwingConstants.VERTICAL, painelConsumidor1, painelConsumidor2);
        splitPane.setDividerLocation(getWidth()/2);
        
        tabbedPane.addTab("Send/Subscribes", splitPane);
        tabbedPane.addTab("Configs", new JPanel());
        add(tabbedPane, BorderLayout.CENTER);
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> new FramePrincipal().setVisible(true));
    }
}
