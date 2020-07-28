package br;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.regex.Pattern;

public class FramePrincipal extends JFrame {

    PainelComponentes painelComponentes1;
    PainelComponentes painelComponentes2;

    public FramePrincipal() throws HeadlessException {
        super("Kafka Client");
        setMinimumSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();


        painelComponentes1 = new PainelComponentes();
        painelComponentes1.setMinimumSize(new Dimension(50, 50));

        painelComponentes2 = new PainelComponentes();
        painelComponentes2.setMinimumSize(new Dimension(50, 50));

        JSplitPane splitPane = new JSplitPane(SwingConstants.VERTICAL, painelComponentes1, painelComponentes2);
        splitPane.setDividerLocation(getWidth() / 2);
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                splitPane.setDividerLocation(getWidth() / 2);
            }
        });

        tabbedPane.addTab("Send/Subscribes", splitPane);
        tabbedPane.addTab("Configs", new Painelconfig());
        add(tabbedPane, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FramePrincipal().setVisible(true));
    }
}
