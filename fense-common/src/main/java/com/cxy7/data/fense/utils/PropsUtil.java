package com.cxy7.data.fense.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * @author: XiaoYu
 * @date: 2021/3/13 11:58 上午
 */
public class PropsUtil {
    private static final Logger logger = LoggerFactory.getLogger(PropsUtil.class);
    private static final String DEFALUT_PROPERTY_FILE = "default.properties";
    private static final String SECONDARY_PROPERTY_FILE = "application.properties";

    /*---------------------------------------------------------------------------*/
    static String DELIM_START = "${";
    static char DELIM_STOP = '}';
    static int DELIM_START_LEN = 2;
    static int DELIM_STOP_LEN = 1;
    /*---------------------------------------------------------------------------*/

    private static Properties overrideProperties = new Properties();
    private static Properties systemProperties = System.getProperties();
    private static Properties serverProperties = new Properties();
    private static Properties defaultProperties = new Properties();

    private static Properties props = new HierarchicalProperties(overrideProperties,
            new HierarchicalProperties(systemProperties,
                    new HierarchicalProperties(serverProperties, defaultProperties)));

    private static File appRoot;

    public static File getAppRoot() {
        return appRoot;
    }

    static {
        try {
            InputStream is = ClassLoader.getSystemResourceAsStream(DEFALUT_PROPERTY_FILE);
            if (is != null) {
                defaultProperties.load(new InputStreamReader(is, Charset.forName("UTF-8")));
                logger.info("Loaded {}.", DEFALUT_PROPERTY_FILE);
            }
        } catch (IOException e) {
            logger.error("Can't load {}, please ensure it in classpath", DEFALUT_PROPERTY_FILE, e);
            System.exit(1);
        }

        String propertyFileName = System.getProperty("server.config", SECONDARY_PROPERTY_FILE);
        try {
            InputStream is = ClassLoader.getSystemResourceAsStream(propertyFileName);
            if (is != null) {
                serverProperties.load(new InputStreamReader(is, Charset.forName("UTF-8")));
                logger.info("Loaded {}.", propertyFileName);
            }
        } catch (IOException e) {
            logger.error("Can't load %s, please ensure it in classpath", propertyFileName, e);
            System.exit(1);
        }

        if (appRoot == null) {
            String appHome = System.getenv("APP_HOME");
            if (StringUtils.isNotEmpty(appHome)) {
                File file = new File(appHome);
                if (file.exists() && file.isDirectory()) {
                    appRoot = file;
                }
            }
        }

        if (appRoot == null) {
            String appHome = PropsUtil.getString("app.root");
            if (StringUtils.isNotEmpty(appHome)) {
                File file = new File(appHome);
                if (file.exists() && file.isDirectory()) {
                    appRoot = file;
                }
            }
        }

        if (appRoot == null) {
            URL location = PropsUtil.class.getProtectionDomain().getCodeSource().getLocation();
            File file = new File(location.getFile());
            while (file != null) {
                File bin = new File(file, "bin");
                File citycode = new File(bin, "env.sh");
                if (bin.exists() && citycode.exists()) {
                    break;
                }
                file = file.getParentFile();
            }
            if (file != null) {
                appRoot = file;
            }
        }
    }

    /**
     * 加载ClassPath下的属性文件到内存中
     *
     * @param fileName
     */
    public static void loadClassPathProperty(String fileName) {
        try {
            InputStream is = ClassLoader.getSystemResourceAsStream(fileName);
            if (is != null) {
                System.getProperties().load(is);
                logger.info("Loaded {}.", fileName);
            }
        } catch (IOException e) {
            logger.error(String.format("Can't load %s, please ensure it in classpath", fileName), e);
        }
    }

    /**
     * 加载ClassPath下的属性文件到内存中
     *
     * @param fileName
     */
    public static void loadWebClassPathProperty(String fileName) {
        try {
            InputStream is = PropsUtil.class.getClassLoader().getResourceAsStream(fileName);
            if (is != null) {
                System.getProperties().load(is);
                logger.info("Loaded {}.", fileName);
            }
        } catch (IOException e) {
            logger.error(String.format("Can't load %s, please ensure it in classpath", fileName), e);
        }
    }

    public static void addLibraryPath(String path) {
        String javaLibraryPath = System.getProperty("java.library.path");
        String[] paths = new String[0];
        if (StringUtils.isNotEmpty(javaLibraryPath)) {
            paths = javaLibraryPath.split(File.pathSeparator, -1);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(path);
        sb.append(File.pathSeparator);
        for (String p : paths) {
            if (StringUtils.isEmpty(p)) {
                continue;
            }
            if (p.equals(path)) {
                continue;
            }
            sb.append(p);
            sb.append(File.pathSeparator);
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        javaLibraryPath = sb.toString();
        System.setProperty("java.library.path", javaLibraryPath);
    }

    public static void addClassPath(String path) {
        String javaClassPath = System.getProperty("java.class.path");
        String[] paths = new String[0];
        if (StringUtils.isNotEmpty(javaClassPath)) {
            paths = javaClassPath.split(File.pathSeparator, -1);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(path);
        sb.append(File.pathSeparator);
        for (String p : paths) {
            if (StringUtils.isEmpty(p)) {
                continue;
            }
            if (p.equals(path)) {
                continue;
            }
            sb.append(p);
            sb.append(File.pathSeparator);
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        javaClassPath = sb.toString();
        System.setProperty("java.class.path", javaClassPath);
    }
    /*---------------------------------------------------------------------------*/

    /**
     * Perform variable substitution in string <code>val</code> from the values
     * of keys found in the system propeties.
     *
     * <p>
     * The variable substitution delimeters are <b>${</b> and <b>}</b>.
     *
     * <p>
     * For example, if the System properties contains "key=value", then the call
     *
     * <pre>
     * String s = OptionConverter.substituteVars(&quot;Value of key is ${key}.&quot;);
     * </pre>
     * <p>
     * will set the variable <code>s</code> to "Value of key is value.".
     *
     * <p>
     * If no value could be found for the specified key, then the
     * <code>props</code> parameter is searched, if the value could not be found
     * there, then substitution defaults to the empty string.
     *
     * <p>
     * For example, if system propeties contains no value for the key
     * "inexistentKey", then the call
     *
     * <pre>
     * String s = OptionConverter
     * 		.subsVars(&quot;Value of inexistentKey is [${inexistentKey}]&quot;);
     * </pre>
     * <p>
     * will set <code>s</code> to "Value of inexistentKey is []"
     *
     * <p>
     * An {@link IllegalArgumentException} is thrown if
     * <code>val</code> contains a start delimeter "${" which is not balanced by
     * a stop delimeter "}".
     * </p>
     *
     * <p>
     * <b>Author</b> Avy Sharell</a>
     * </p>
     *
     * @param val The string on which variable substitution is performed.
     * @throws IllegalArgumentException if <code>val</code> is malformed.
     */
    public static String substVars(String val, Properties props)
            throws IllegalArgumentException {

        StringBuffer sbuf = new StringBuffer();

        int i = 0;
        int j, k;

        while (true) {
            j = val.indexOf(DELIM_START, i);
            if (j == -1) {
                // no more variables
                if (i == 0) { // this is a simple string
                    return val;
                } else { // add the tail string which contails no variables and
                    // return the result.
                    sbuf.append(val.substring(i));
                    return sbuf.toString();
                }
            } else {
                sbuf.append(val, i, j);
                k = val.indexOf(DELIM_STOP, j);
                if (k == -1) {
                    throw new IllegalArgumentException(
                            '"'
                                    + val
                                    + "\" has no closing brace. Opening brace at position "
                                    + j + '.');
                } else {
                    j += DELIM_START_LEN;
                    String key = val.substring(j, k);
                    // first try in System properties
                    String replacement = props.getProperty(key);
                    if (replacement != null) {
                        // Do variable substitution on the replacement string
                        // such that we can solve "Hello ${x2}" as "Hello p1"
                        // the where the properties are
                        // x1=p1
                        // x2=${x1}
                        String recursiveReplacement = substVars(replacement,
                                props);
                        sbuf.append(recursiveReplacement);
                    }
                    i = k + DELIM_STOP_LEN;
                }
            }
        }
    }

    public static String substVars(String val) {
        return substVars(val, getProperties());
    }

    public static Properties getProperties() {
        return props;
    }

    public static void setProperty(String key, String value) {
        overrideProperties.setProperty(key, value);
    }

    public static String getProperty(String key) {
        String value = props.getProperty(key);
        if (value == null) {
            return null;
        }
        value = substVars(value, props);
        return value.trim();
    }

    public static String getProperty(String[] keys) {
        String value;
        for (String key : keys) {
            value = props.getProperty(key);
            if (value != null) {
                value = substVars(value, props);
                return value.trim();
            }
        }
        return null;
    }

    public static String getProperty(String key, String def) {
        String value = getProperty(key);
        if (value == null) {
            value = def;
        }
        if (value != null) {
            value = substVars(value, props);
            return value.trim();
        }
        return null;
    }

    public static String getProperty(String[] keys, String def) {
        String value = getProperty(keys);
        if (value == null) {
            value = def;
        }
        if (value != null) {
            value = substVars(value, props);
            return value.trim();
        }
        return null;
    }

    //
    // String
    //
    public static String getString(String key, String def) {
        String value = getProperty(key);
        if (value != null) {
            return value;
        }
        if (def != null) {
            value = substVars(def, props);
            return value.trim();
        }
        return null;
    }

    public static String getString(String[] keys, String def) {
        String value = getProperty(keys);
        if (value != null) {
            return value;
        }
        if (def != null) {
            value = substVars(def, props);
            return value.trim();
        }
        return null;
    }

    public static String getString(String key) {
        String value = getProperty(key);
        if (value != null) {
            return value;
        }
        return null;
    }

    public static String getString(String[] keys) {
        String value = getProperty(keys);
        if (value != null) {
            return value;
        }
        return null;
    }

    //
    // Long
    //

    public static long getLong(String key, long def) {
        String value = getProperty(key);
        if (value == null) {
            return def;
        }
        return Long.valueOf(value);
    }

    public static long getLong(String[] keys, long def) {
        String value = getProperty(keys);
        if (value == null) {
            return def;
        }
        return Long.valueOf(value);
    }

    public static long getLong(String key) {
        String value = getProperty(key);
        if (value == null) {
            throw new NullPointerException();
        }
        return Long.valueOf(value);
    }

    public static long getLong(String[] keys) {
        String value = getProperty(keys);
        if (value == null) {
            throw new NullPointerException();
        }
        return Long.valueOf(value);
    }

    //
    // Integer
    //
    public static int getInteger(String key, int def) {
        String value = getProperty(key);
        if (value == null) {
            return def;
        }
        return Integer.valueOf(value);
    }

    public static int getInteger(String key) {
        String value = getProperty(key);
        if (value == null) {
            throw new NullPointerException();
        }
        return Integer.valueOf(value);
    }

    public static int getInteger(String[] keys, int def) {
        String value = getProperty(keys);
        if (value == null) {
            return def;
        }
        return Integer.valueOf(value);
    }

    public static int getInteger(String[] keys) {
        String value = getProperty(keys);
        if (value == null) {
            throw new NullPointerException();
        }
        return Integer.valueOf(value);
    }

    //
    // Boolean
    //
    public static boolean getBoolean(String key, boolean def) {
        String value = getProperty(key);
        if (value == null) {
            return def;
        }
        return str2bool(value);
    }

    public static boolean getBoolean(String key) {
        String value = getProperty(key);
        if (value == null) {
            throw new NullPointerException();
        }
        return str2bool(value);
    }

    public static boolean getBoolean(String[] keys, boolean def) {
        String value = getProperty(keys);
        if (value == null) {
            return def;
        }
        return str2bool(value);
    }

    public static boolean getBoolean(String[] keys) {
        String value = getProperty(keys);
        if (value == null) {
            throw new NullPointerException();
        }
        return str2bool(value);
    }

    private static boolean str2bool(String value) {
        if (value == null) {
            return false;
        }
        return value.equalsIgnoreCase("true")
                || value.equalsIgnoreCase("1")
                || value.equalsIgnoreCase("yes")
                || value.equalsIgnoreCase("on");
    }
}
