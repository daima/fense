package com.cxy7.data.fense;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @Author: XiaoYu
 * @Date: 2021/10/6 18:41
 */
public class PatchClassLoader extends ClassLoader{
    private static Logger log = LoggerFactory.getLogger(PatchClassLoader.class);
    /**
     * lib:表示加载的文件在jar包中
     * 类似tomcat就是{PROJECT}/WEB-INF/lib/
     */
    private String libPath;
    /**
     * classes:表示加载的文件是单纯的class文件
     * 类似tomcat就是{PROJECT}/WEB-INF/classes/
     */
    private String classpath;
    /**
     * 采取将所有的jar包中的class读取到内存中
     * 然后如果需要读取的时候，再从map中查找
     */
    private Map<String, byte[]> map;

    /**
     * 只需要指定项目路径就好
     * 默认jar加载路径是目录下{PROJECT}/WEB-INF/lib/
     * 默认class加载路径是目录下{PROJECT}/WEB-INF/classes/
     * @param libPath
     * @param classpath
     * @throws MalformedURLException
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    public PatchClassLoader(String libPath, String classpath) throws NoSuchMethodException, SecurityException, MalformedURLException {
        this.libPath = libPath;
        this.classpath = classpath;
        log.info("load classes from {} and {}", libPath, classpath);
        map = new HashMap<>(64);

        preReadJarFile();
    }

    /**
     * 按照父类的机制，如果在父类中没有找到的类
     * 才会调用这个findClass来加载
     * 这样只会加载放在自己目录下的文件
     * 而系统自带需要的class并不是由这个加载
     */
    @Override
    protected Class<?> findClass(String name){
        try {
            byte[] result = getClassFromFileOrMap(name);
            if(result == null){
                throw new FileNotFoundException();
            }else{
                Class<?> clazz = defineClass(name, result, 0, result.length);
                resolveClass(clazz);
                return clazz;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从指定的classes文件夹下找到文件
     * @param name
     * @return
     */
    private byte[] getClassFromFileOrMap(String name){
        String classPath = this.classpath + name.replace('.', File.separatorChar) + ".class";
        File file = new File(classPath);
        if(file.exists()){
            InputStream input = null;
            try {
                input = new FileInputStream(file);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int bufferSize = 4096;
                byte[] buffer = new byte[bufferSize];
                int bytesNumRead = 0;
                while ((bytesNumRead = input.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesNumRead);
                }
                return baos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            } finally{
                if(input != null){
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }else{
            if(map.containsKey(name)) {
                //去除map中的引用，避免GC无法回收无用的class文件
                return map.remove(name);
            }
        }
        return null;
    }

    /**
     * 预读lib下面的包
     */
    private void preReadJarFile(){
        List<File> list = scanDir();
        for(File f : list){
            JarFile jar;
            try {
                jar = new JarFile(f);
                readJAR(jar);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取一个jar包内的class文件，并存在当前加载器的map中
     * @param jar
     * @throws IOException
     */
    private void readJAR(JarFile jar) throws IOException{
        Enumeration<JarEntry> en = jar.entries();
        while (en.hasMoreElements()){
            JarEntry je = en.nextElement();
            String name = je.getName();
            if (name.endsWith(".class")){
                String clss = name.replace(".class", "").replaceAll("/", ".");
                if(this.findLoadedClass(clss) != null) continue;

                InputStream input = jar.getInputStream(je);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int bufferSize = 4096;
                byte[] buffer = new byte[bufferSize];
                int bytesNumRead = 0;
                while ((bytesNumRead = input.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesNumRead);
                }
                byte[] cc = baos.toByteArray();
                input.close();
                map.put(clss, cc);//暂时保存下来
            }
        }
    }

    /**
     * 扫描lib下面的所有jar包
     * @return
     */
    private List<File> scanDir() {
        List<File> list = new ArrayList<File>();
        File[] files = new File(libPath).listFiles();
        for (File f : files) {
            if (f.isFile() && f.getName().endsWith(".jar"))
                list.add(f);
        }
        return list;
    }

    /**
     * 添加一个jar包到加载器中去。
     * @param jarPath
     * @throws IOException
     */
    public void addJar(String jarPath) throws IOException{
        File file = new File(jarPath);
        if(file.exists()){
            JarFile jar = new JarFile(file);
            readJAR(jar);
        }
    }
}
