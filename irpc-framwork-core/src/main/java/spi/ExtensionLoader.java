package spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实现SPI机制
 * */
public class ExtensionLoader {

    public static String EXTENSION_LOADER_DIR_PREFIX = "META-INF/irpc/";

    /**
     * 存储spi从资源目录下加载到内存的类信息
     * */
    public static Map<String, LinkedHashMap<String, Class>> EXTENSION_LOADER_CLASS_CACHE = new ConcurrentHashMap<>();

    /**
     * 触发spi的实现类进行类加载，但不会进行实例化<br>
     * spi文件应该存放的路径：resource/META-INF/irpc/目录下<br>
     * spi文件命名格式：以spi接口名称作为文件名称<br>
     * spi文件内容格式：key value格式 可以多个key value<br>
     * key代表的含义为可以在外界对irpc.properties配置文件中所指定的关键字<br>
     * value则是对应的接口实现类名称<br>
     * */
    public void loadExtension(Class clazz) throws IOException,ClassNotFoundException {
        if (clazz == null){
            throw new IllegalArgumentException("class is null");
        }
        // 自定义spi 文件名称中包含实现的spi接口名称
        String spiFilePath = EXTENSION_LOADER_DIR_PREFIX + clazz.getName();
        // 类加载器的作用是什么？
        ClassLoader classLoader = this.getClass().getClassLoader();
        Enumeration<URL> enumeration = null;
        // 获取路径下的资源文件集合
        enumeration = classLoader.getResources(spiFilePath);

        while (enumeration.hasMoreElements()) {
            URL url = enumeration.nextElement();
            InputStreamReader inputStreamReader =  null;
            inputStreamReader = new InputStreamReader(url.openStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            LinkedHashMap<String,Class> classMap = new LinkedHashMap<>();
            // 循环的作用是：将实现类名称
            while ((line = bufferedReader.readLine()) != null) {
                //如果配置中加入了#开头则表示忽略该类无需进行加载
                if (line.startsWith("#")) {
                    continue;
                }
                String[] lineArr = line.split("=");
                // key 可以在外界对irpc.properties配置文件中所指定的关键字
                String implClassSimpleName = lineArr[0];
                // value则是对应的接口实现类名称
                String interfaceImplClassName = lineArr[1];
                classMap.put(implClassSimpleName, Class.forName(interfaceImplClassName));
            }
            //只会触发class文件的加载，而不会触发对象的实例化
            if(EXTENSION_LOADER_CLASS_CACHE.containsKey(clazz.getName())){
                //支持开发者自定义配置
                EXTENSION_LOADER_CLASS_CACHE.get(clazz.getName()).putAll(classMap);
            } else {
                EXTENSION_LOADER_CLASS_CACHE.put(clazz.getName(), classMap);
            }
        }
    }

}
