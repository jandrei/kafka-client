package br;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;

public class PainelProdutor extends JPanel {

    JTextArea textAreaMensagens;
    JScrollPane scrollPaneTextArea;
    
    JTextArea textAreaMensagensEnviadas;
    JScrollPane scrollPaneTextAreaEnviadas;
    JButton buttonSend;
    ProdutorKafka produtorKafka;


    public PainelProdutor(Consumidor painelConsumidor) {
        super();
        setLayout(new MigLayout());
        setBorder(BorderFactory.createEtchedBorder());

        buttonSend = new JButton("SEND");
        this.add(buttonSend);

        this.add(new JSeparator(), "wrap");

        textAreaMensagens = new JTextArea(15, 200);
        scrollPaneTextArea = new JScrollPane(textAreaMensagens);
        this.add(scrollPaneTextArea, "span 4, wrap");

        textAreaMensagensEnviadas = new JTextArea(10, 200);
        textAreaMensagensEnviadas.setEditable(false);
        scrollPaneTextAreaEnviadas = new JScrollPane(textAreaMensagensEnviadas);
        this.add(scrollPaneTextAreaEnviadas, "span 4");

        produtorKafka = new ProdutorKafka(painelConsumidor);
        //actions
        buttonSend.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            produtorKafka.start();
            String mensagem = textAreaMensagens.getText();
            if (mensagem.contains("#UUID")) {
                mensagem = StringUtils.replace(mensagem, "#UUID", UUID.randomUUID().toString());
            }
            textAreaMensagensEnviadas.setText(mensagem);
            
            produtorKafka.send(painelConsumidor.topics().stream().findFirst().get(), mensagem);
        }));
    }


}
