package br;

import com.google.gson.Gson;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ComboboxModelEnvironments extends DefaultComboBoxModel<ComboboxModelEnvironments.ComboItem> {


    public ComboboxModelEnvironments() {
        super(getItens());
    }

    private static ComboItem[] getItens() {
        Object objectEnvs = Config.get("envs");
        if ("".equals(objectEnvs) || objectEnvs == null) {
            String envsLocal = "[ { \"name\":\"LOCAL\", \"brokers\":\"localhost:9092\"} ]";
            Config.put("envs", new Gson().fromJson(envsLocal, ArrayList.class));
            Config.save();
        }

        List<Map<String, String>> envs = (List<Map<String, String>>) Config.get("envs");
        List<ComboItem> retorno = envs.stream()
                .map(map -> new ComboItem(map.get("name"), map.get("brokers")))
                .collect(Collectors.toList());

        retorno.add(0, new ComboItem("Selecione", ""));

        return retorno.toArray(new ComboItem[envs.size()]);
    }

    static class ComboItem {

        public ComboItem(String name, String brokers) {
            this.name = name;
            this.brokers = brokers;
        }

        String name;
        String brokers;

        @Override
        public String toString() {
            return name;
        }
    }
}
