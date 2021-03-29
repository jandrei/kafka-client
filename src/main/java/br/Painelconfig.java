package br;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Painelconfig extends JPanel {

    JPanel painelNorte;
    JTextArea textAreaConfig;
    JButton btnAbrirArquivoConfig;

    public Painelconfig() {
        setLayout(new BorderLayout());

        painelNorte = new JPanel();
        painelNorte.setLayout(new MigLayout());
        this.add(painelNorte, BorderLayout.NORTH);

        btnAbrirArquivoConfig = new JButton("Abrir arquivo de Configurações");
        btnAbrirArquivoConfig.setPreferredSize(new Dimension(5, 10));
        this.painelNorte.add(this.btnAbrirArquivoConfig);
        btnAbrirArquivoConfig.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(Config.arquivoProperties.getCanonicalFile());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        this.textAreaConfig = new JTextArea(15, 10);
        textAreaConfig.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(this.textAreaConfig);
        this.add(scrollPane, BorderLayout.CENTER);
    }
}
