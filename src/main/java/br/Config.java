package br;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class Config {

    private static Config INSTANCE;
    private static String userHome = System.getProperty("user.home");
    public static final String CONFIGNAME = ".custom-kafka-client.json";
    public static final File arquivoProperties = new File(userHome.concat(File.separator).concat(CONFIGNAME));
    private Map props = new HashMap<>();

    private Config() {
        if (!arquivoProperties.exists()) {
            try {
                arquivoProperties.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        loadProps();

    }

    public static void save() {
        synchronized (INSTANCE) {
            try {
                String content = new GsonBuilder().setPrettyPrinting().create().toJson(getInstance().props);
                Files.write(getInstance().arquivoProperties.toPath(), content.getBytes(), StandardOpenOption.WRITE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Object get(String name) {
        return get(name, null);
    }

    public static Object get(String name, Object def) {
        Object value = getInstance().getProps(name);
        if (value == null && def != null) {
            getInstance().put(name, def);
            getInstance().save();
            return def;
        } else {
            return value;
        }
    }

    private void loadProps() {
        try {
            String content = new String(Files.readAllBytes(arquivoProperties.toPath()));
            if (!content.trim().isEmpty()) {
                Gson gson = new Gson();
                Type empMapType = new TypeToken<Map>() {
                }.getType();
                props.putAll(gson.fromJson(content, empMapType));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object getProps(String name) {
        return this.props.get(name);
    }

    public synchronized static void put(String name, Object value) {
        getInstance().props.put(name, value);
    }

    public static void main(String[] args) {
        System.out.println(Config.get("author"));
        System.out.println(Config.get("nome", "Jandrei Carlos Masiero"));
    }


    private static synchronized Config getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Config();
        }

        return INSTANCE;
    }

}
