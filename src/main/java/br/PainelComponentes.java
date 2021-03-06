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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PainelComponentes extends JPanel implements KafkaConfiguration {

    JTextArea textAreaMensagens;
    JTextArea textAreaProdutor;
    JTextArea textAreaLogEnviadas;
    JTextField textFieldFiltro;
    JTextField textFieldConsumerName;
    JCheckBox jCheckBoxAutocomit;
    JLabel jlabelMessagesFounded;

    JScrollPane scrollPaneLogEnviadas;
    JScrollPane scrollPaneProdutor;
    JScrollPane scrollPaneTextArea;

    Integer posicaoMaximaScrol = 0;
    Integer posicaoAtualScrol = 0;

    JButton buttonSubscribe;
    JButton buttonUnsubscribe;
    JButton buttonProducerMessage;

    JComboBox jComboBoxEnvs;
    JComboBox jComboBoxTopicos;
    JComboBox jComboBoxLatestEarliest;

    Map<String, List<ItemComboboxTopics>> mapTopicosLocal = new HashMap<>();

    KafkaService kafkaService;
    AtomicInteger msgTotal = new AtomicInteger();
    AtomicInteger msgFiltradas = new AtomicInteger();

    public PainelComponentes() {
        super();
        setLayout(new MigLayout());
        setBorder(BorderFactory.createEtchedBorder());

        painelConsumer();
        painelProducer();

        kafkaService = new KafkaService(this);

        //actions
        actionsProducer();
        actionsAndEventsConsumer();

    }

    private void painelConsumer() {
        add(new JLabel("Ambiente:"));
        jComboBoxEnvs = new JComboBox(new ComboboxModelEnvironments());
        jComboBoxEnvs.setPreferredSize(new Dimension(100, 15));
        this.add(jComboBoxEnvs, "left");

        this.add(new JLabel("Topicos:"), "right");
        jComboBoxTopicos = new JComboBox(new String[]{});
        jComboBoxTopicos.setPreferredSize(new Dimension(300, 15));
        this.add(jComboBoxTopicos, "left, span 4, wrap");

        this.add(new JLabel("Consumer name: "), "right");
        textFieldConsumerName = new JTextField("randomico");
        textFieldConsumerName.setMinimumSize(new Dimension(150, 10));
        this.add(this.textFieldConsumerName, "left");

        this.add(new JLabel("Autocommit: "), "right");
        jCheckBoxAutocomit = new JCheckBox();
        this.add(this.jCheckBoxAutocomit, "left");

        this.add(new JLabel("Desde: "), "right");
        jComboBoxLatestEarliest = new JComboBox(new String[]{"latest", "earliest"});
        this.add(jComboBoxLatestEarliest, "left, wrap");

        buttonSubscribe = new JButton("SUBSCRIBE");
        this.add(buttonSubscribe);
        buttonUnsubscribe = new JButton("UNSUBSCRIBE");
        this.add(buttonUnsubscribe,"span 2, wrap");

        this.add(new JLabel("Filtro: "), "left");
        textFieldFiltro = new JTextField();
        textFieldFiltro.setMinimumSize(new Dimension(150, 10));
        this.add(this.textFieldFiltro, "left,span 6,  wrap");


        textAreaMensagens = new JTextArea(15, 200);
        textAreaMensagens.setEditable(false);
        scrollPaneTextArea = new JScrollPane(textAreaMensagens);
        this.add(scrollPaneTextArea, "span 7, wrap");

        jlabelMessagesFounded = new JLabel();
        jlabelMessagesFounded.setMinimumSize(new Dimension(300, 10));
        this.add(this.jlabelMessagesFounded, "span 7, wrap");
    }

    private void painelProducer() {
        buttonProducerMessage = new JButton("SEND");
        this.add(buttonProducerMessage);

        this.add(new JSeparator(), "wrap");

        textAreaProdutor = new JTextArea(15, 200);
        scrollPaneProdutor = new JScrollPane(textAreaProdutor);
        this.add(scrollPaneProdutor, "span 7, wrap");

        textAreaLogEnviadas = new JTextArea(10, 200);
        textAreaLogEnviadas.setEditable(false);
        scrollPaneLogEnviadas = new JScrollPane(textAreaLogEnviadas);
        this.add(scrollPaneLogEnviadas, "span 7");
    }

    private void actionsAndEventsConsumer() {
        jComboBoxEnvs.addActionListener(e -> {
            if (jComboBoxEnvs.getSelectedIndex() > 0) {
                SwingUtilities.invokeLater(() -> {
                    textAreaMensagens.setText("");
                    kafkaService.createConsumer(true);
                });
            }
        });

        jComboBoxLatestEarliest.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                kafkaService.unsubcribe();
                kafkaService.createConsumer(false);
            });
        });

        //actions
        buttonSubscribe.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            textAreaMensagens.setText("");
            msgTotal.set(0);
            msgFiltradas.set(0);
            jlabelMessagesFounded.setText("total= 0, filtradas= 0");


            kafkaService.subscribe();
        }));
        buttonUnsubscribe.addActionListener(e -> SwingUtilities.invokeLater(() -> kafkaService.unsubcribe()));

        scrollPaneTextArea.getVerticalScrollBar().addAdjustmentListener(e -> {
            posicaoAtualScrol = e.getValue();
            posicaoMaximaScrol = Math.max(posicaoAtualScrol, posicaoMaximaScrol);
        });
    }


    private void actionsProducer() {
        buttonProducerMessage.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            try {
                kafkaService.createProducer();
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
            if (msg.contains("#ZONEDDATETIMENOW")) {
                msg = StringUtils.replace(msg, "#ZONEDDATETIMENOW", ZonedDateTime.now().toString());
            }
            if (msg.contains("#LOCALDATETIMENOW")) {
                msg = StringUtils.replace(msg, "#LOCALDATETIMENOW", LocalDateTime.now().toString());
            }
            mensagensEnviadas += msg + "\n";

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            kafkaService.send(this.getTopics().stream().findFirst().get(), msg);
        }

        textAreaLogEnviadas.append(mensagensEnviadas);
    }


    @Override
    public void handleRow(KafkaConsumerRecord<String, String> row) {
        jlabelMessagesFounded.setText("total= " + msgTotal.incrementAndGet() + ", filtradas=" + msgFiltradas.get());

        LocalDateTime triggerTime =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(row.timestamp()),
                        TimeZone.getDefault().toZoneId());
        String info = " Ambiente:" + jComboBoxEnvs.getSelectedItem().toString() +
                ", Partição: " + row.partition() +
                ", offset: " + row.offset() +
                ", timestamp: " + triggerTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
                "\n";
        String mensagem = "   " + row.value();

        //se tem filtro
        if (!textFieldFiltro.getText().trim().isEmpty()) {
            //se filtro contains na mensagem
            if (!mensagem.toLowerCase().contains(textFieldFiltro.getText().toLowerCase())) {
                return;
            }

            jlabelMessagesFounded.setText("total= " + msgTotal.get() + ", filtradas=" + msgFiltradas.incrementAndGet());
        }
//        row.headers().stream().forEach(kafkaHeader -> {
//            System.out.println(kafkaHeader.key() + "=" + kafkaHeader.value());
//        });
//        System.out.println("------------------------");

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
    public void callbackFechTopics(Map<String, List<PartitionInfo>> map) {
        SwingUtilities.invokeLater(() -> {
            jComboBoxTopicos.removeAllItems();
            mapTopicosLocal.put(jComboBoxEnvs.getSelectedItem().toString(),
                    map.entrySet()
                            .stream()
                            .map(item -> new ItemComboboxTopics(item.getKey(), item.getValue()))
                            .sorted()
                            .collect(Collectors.toList()));

            for (ItemComboboxTopics key : mapTopicosLocal.get(jComboBoxEnvs.getSelectedItem().toString())) {
                jComboBoxTopicos.addItem(key);
            }
        });

    }

    @Override
    public String getBrokers() {
        if (jComboBoxEnvs.getSelectedIndex() < 0)
            return "";

        return ((ComboboxModelEnvironments.ComboItem) jComboBoxEnvs.getSelectedItem()).brokers;
    }

    @Override
    public Set<String> getTopics() {
        if (jComboBoxTopicos.getSelectedIndex() < 0) {
            return new HashSet<>();
        }
        Set set = new HashSet();
        set.add(jComboBoxTopicos.getSelectedItem().toString());
        return set;
    }

    @Override
    public String fetchSince() {
        return jComboBoxLatestEarliest.getSelectedItem().toString();
    }

    @Override
    public String getConsumerName() {
        if (textFieldConsumerName.getText().equals("randomico")){
            return UUID.randomUUID().toString();
        }
        return StringUtils.defaultIfBlank(textFieldConsumerName.getText(), UUID.randomUUID().toString());
    }

    @Override
    public String isAutocommit() {
        return String.valueOf(jCheckBoxAutocomit.isSelected());
    }

    class ItemComboboxTopics implements Comparable<ItemComboboxTopics> {
        String topic;
        List<PartitionInfo> partitionInfos;

        public ItemComboboxTopics(String topic, List<PartitionInfo> partitionInfos) {
            this.topic = topic;
            this.partitionInfos = partitionInfos;
        }

        @Override
        public int compareTo(ItemComboboxTopics o) {
            return this.topic.compareTo(o.getTopic());
        }

        public String getTopic() {
            return topic;
        }

        public List<PartitionInfo> getPartitionInfos() {
            return partitionInfos;
        }

        @Override
        public String toString() {
            return topic;
        }
    }
}



