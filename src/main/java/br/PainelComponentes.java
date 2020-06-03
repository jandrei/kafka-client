package br;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.vertx.kafka.client.common.PartitionInfo;
import io.vertx.kafka.client.consumer.KafkaConsumerRecord;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class PainelComponentes extends JPanel implements IConsumidor {

    JTextArea textAreaMensagens;
    JTextArea textAreaProdutor;
    JTextArea textAreaLogEnviadas;

    JScrollPane scrollPaneLogEnviadas;
    JScrollPane scrollPaneProdutor;
    JScrollPane scrollPaneTextArea;

    Integer posicaoMaximaScrol = 0;
    Integer posicaoAtualScrol = 0;

    JButton buttonSubscribe;
    JButton buttonUnsubscribe;
    JButton buttonProducerMessage;

    JComboBox jComboBoxEnv;
    JComboBox jComboBoxTopicos;
    JComboBox jComboBoxDesde;

    Map<String, List<String>> mapTopicosLocal = new HashMap<>();

    ConsumidorKafka consumidorKafka;


    public PainelComponentes() {
        super();
        setLayout(new MigLayout());
        setBorder(BorderFactory.createEtchedBorder());

        painelConsumer();
        painelProducer();

        consumidorKafka = new ConsumidorKafka(this);

        //actions
        actionsProducer();
        actionsAndEventsConsumer();

    }

    private void painelConsumer() {
        add(new JLabel("Ambiente:"));
        jComboBoxEnv = new JComboBox(new ComboboxModelEnvironments());
        jComboBoxEnv.setPreferredSize(new Dimension(100, 15));
        this.add(jComboBoxEnv, "left");

        this.add(new JLabel("Topicos:"), "right");
        jComboBoxTopicos = new JComboBox(new String[]{});
        jComboBoxTopicos.setPreferredSize(new Dimension(300, 15));
        this.add(jComboBoxTopicos, "left, wrap");


        buttonSubscribe = new JButton("SUBSCRIBE");
        this.add(buttonSubscribe);
        buttonUnsubscribe = new JButton("UNSUBSCRIBE");
        this.add(buttonUnsubscribe);

        this.add(new JLabel("Desde: "), "right");
        jComboBoxDesde = new JComboBox(new String[]{"latest", "earliest"});
        this.add(jComboBoxDesde, "left, wrap");

        textAreaMensagens = new JTextArea(15, 200);
        textAreaMensagens.setEditable(false);
        scrollPaneTextArea = new JScrollPane(textAreaMensagens);
        this.add(scrollPaneTextArea, "span 4, wrap");
    }

    private void painelProducer() {
        buttonProducerMessage = new JButton("SEND");
        this.add(buttonProducerMessage);

        this.add(new JSeparator(), "wrap");

        textAreaProdutor = new JTextArea(15, 200);
        scrollPaneProdutor = new JScrollPane(textAreaProdutor);
        this.add(scrollPaneProdutor, "span 4, wrap");

        textAreaLogEnviadas = new JTextArea(10, 200);
        textAreaLogEnviadas.setEditable(false);
        scrollPaneLogEnviadas = new JScrollPane(textAreaLogEnviadas);
        this.add(scrollPaneLogEnviadas, "span 4");
    }

    private void actionsAndEventsConsumer() {
        jComboBoxEnv.addActionListener(e -> {
            if (jComboBoxEnv.getSelectedIndex() > 0) {
                SwingUtilities.invokeLater(() -> {
                    textAreaMensagens.setText("");
                    consumidorKafka.createConsumer(true);
                });
            }
        });

        jComboBoxDesde.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                consumidorKafka.unsubcribe();
                consumidorKafka.createConsumer(false);
            });
        });

        //actions
        buttonSubscribe.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            textAreaMensagens.setText("");
            consumidorKafka.subscribe();
        }));
        buttonUnsubscribe.addActionListener(e -> SwingUtilities.invokeLater(() -> consumidorKafka.unsubcribe()));

        scrollPaneTextArea.getVerticalScrollBar().addAdjustmentListener(e -> {
            posicaoAtualScrol = e.getValue();
            posicaoMaximaScrol = Math.max(posicaoAtualScrol, posicaoMaximaScrol);
        });
    }

    private void actionsProducer() {
        buttonProducerMessage.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            try {
                consumidorKafka.createProducer();
                enviaMensagem();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }));
    }

    private void enviaMensagem() {
        String mensagem = textAreaProdutor.getText();
        if (!mensagem.trim().startsWith("[")) {
            mensagem = "[" + mensagem + "]";
        }
        Gson gson = new Gson();
        Type empMapType = new TypeToken<List<Map>>() {
        }.getType();

        List<Map> mensagens = gson.fromJson(mensagem, empMapType);

        String mensagensEnviadas = "";

        for (Map mapMsg : mensagens) {
            String msg = new Gson().toJson(mapMsg);
            if (StringUtils.isBlank(msg)) {
                continue;
            }
            if (msg.contains("#UUID")) {
                msg = StringUtils.replace(msg, "#UUID", UUID.randomUUID().toString());
            }
            mensagensEnviadas += msg + "\n";

            consumidorKafka.send(this.topics().stream().findFirst().get(), msg);
        }

        textAreaLogEnviadas.append(mensagensEnviadas);
    }


    @Override
    public void handler(KafkaConsumerRecord<String, String> row) {
        LocalDateTime triggerTime =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(row.timestamp()),
                        TimeZone.getDefault().toZoneId());
        String info = "   " + jComboBoxEnv.getSelectedItem().toString() + " - " + row.topic() + " - " + row.partition() + " - " + triggerTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n";
        String mensagem = "   " + row.value();
        textAreaMensagens.append(info + mensagem + "\n\n");

        if (posicaoAtualScrol >= posicaoMaximaScrol) {
            textAreaMensagens.setCaretPosition(textAreaMensagens.getText().length() - 1);
        }
        if (textAreaMensagens.getLineCount() > 100) {
            String text = textAreaMensagens.getText();


            textAreaMensagens.setText(text);
            textAreaMensagens.setCaretPosition(textAreaMensagens.getText().length() - 1);
        }
    }

    @Override
    public void registerPartitions(Map<String, List<PartitionInfo>> map) {
        SwingUtilities.invokeLater(() -> {
            jComboBoxTopicos.removeAllItems();
            if (!mapTopicosLocal.containsKey(jComboBoxEnv.getSelectedItem().toString())) {
                mapTopicosLocal.put(jComboBoxEnv.getSelectedItem().toString(),
                        map.entrySet()
                                .stream()
                                .map(Map.Entry::getKey)
                                .sorted()
                                .filter(s -> s.toLowerCase().contains("v1"))
                                .collect(Collectors.toList()));
            }
            for (String key : mapTopicosLocal.get(jComboBoxEnv.getSelectedItem().toString())) {
                jComboBoxTopicos.addItem(key);
            }
        });

    }

    @Override
    public String brokers() {
        if (jComboBoxEnv.getSelectedIndex() < 0)
            return "";

        return ((ComboboxModelEnvironments.ComboItem) jComboBoxEnv.getSelectedItem()).brokers;
    }

    @Override
    public Set<String> topics() {
        if (jComboBoxTopicos.getSelectedIndex() < 0) {
            return new HashSet<>();
        }
        Set set = new HashSet();
        set.add(jComboBoxTopicos.getSelectedItem().toString());
        return set;
    }

    @Override
    public String desde() {
        return jComboBoxDesde.getSelectedItem().toString();
    }
}
