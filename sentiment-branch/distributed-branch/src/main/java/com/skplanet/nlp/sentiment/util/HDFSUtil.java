package com.skplanet.nlp.sentiment.util;

import com.skplanet.nlp.sentiment.Prop;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HDFS Utilities
 *
 * @author Donghun Shin / donghun.shin@sk.com
 * @date 3/11/16
 */
public class HDFSUtil {
    private static final Logger LOGGER = Logger.getLogger(HDFSUtil.class.getName());

    private static HDFSUtil instance = null;

    // file system
    private Configuration conf;

    /**
     * Get HDFSUtil Instance
     * @return {@link HDFSUtil} instance
     */
    public static HDFSUtil getInstance() {
        if (instance == null) {
            synchronized (HDFSUtil.class) {
                instance = new HDFSUtil();
                LOGGER.debug("[ATTENTION] HDFSUtil instance is created, you must call init() to load collocation dictionary before using it");
            }
        }
        return instance;
    }

    /**
     * Constructor
     * it sets the environment for hdfs
     */
    private HDFSUtil() {
        // get hadoop schema
        conf = new Configuration();
        conf.setBoolean("fs.hdfs.impl.disable.cache", true);
        conf.addResource(new Path(Prop.HADOOP_CONF_PATH + "/core-site.xml"));
        conf.addResource(new Path(Prop.HADOOP_CONF_PATH + "/hdfs-site.xml"));
        conf.addResource(new Path(Prop.HADOOP_CONF_PATH + "/mapred-site.xml"));
        conf.addResource(new Path(Prop.HADOOP_CONF_PATH + "/yarn-site.xml"));
    }

    /**
     * Check if source file exists in the file system
     * @param file file to be looked up
     * @return true if exists
     */
    public boolean exists(String file) {
        boolean isExists = false;
        FileSystem fileSystem = null;
        try {
            fileSystem = FileSystem.get(conf);

            Path path = new Path(file);
            isExists = fileSystem.exists(path);
            fileSystem.close();
        } catch (IOException e) {
            LOGGER.info("failed check existence of target: " + file, e);
        }
        return isExists;
    }

    /**
     * List files and folders
     * @param pathStr path to display
     * @return list of paths and files
     */
    public List<String> ls(String pathStr) throws IOException {
        FileSystem fileSystem = FileSystem.get(conf);

        Path path = new Path(pathStr);
        List<String> resultPaths = new ArrayList<String>();
        try {
            FileStatus[] status = fileSystem.listStatus(path);
            for (FileStatus stat : status) {
                resultPaths.add(stat.getPath().toString());
            }
        } catch (IOException e) {
            LOGGER.error("The path doesn't exist: " + pathStr);
        }
        fileSystem.close();
        return resultPaths;
    }

    /**
     * Get All the datanodes
     */
    public void getHostNames() throws IOException {
        FileSystem fileSystem = FileSystem.get(conf);
        DistributedFileSystem hdfs = (DistributedFileSystem) fileSystem;
        DatanodeInfo[] dataNodeStats = new DatanodeInfo[0];
        try {
            dataNodeStats = hdfs.getDataNodeStats();
        } catch (IOException e) {
            LOGGER.error("failed to get datanode stat", e);
        }

        String[] names = new String[dataNodeStats.length];
        for (int i = 0; i < dataNodeStats.length; i++) {
            names[i] = dataNodeStats[i].getHostName();
            System.out.println((dataNodeStats[i].getHostName()));
        }
        fileSystem.close();
    }


    /**
     * Copy local file to HDFS
     * @param source local file path
     * @param dest hdfs file path
     */
    public void copyFromLocal(String source, String dest) throws IOException {
        FileSystem fileSystem = FileSystem.get(conf);

        Path srcPath = new Path(source);
        Path dstPath = new Path(dest);

        if (!this.exists(source)) {
            LOGGER.debug("destination doesn't exist: " + dstPath);
            return;
        }

        String filename = source.substring(source.lastIndexOf('/') + 1, source.length());

        try {
            fileSystem.copyFromLocalFile(srcPath, dstPath);
            System.out.println("File " + filename + "copied to " + dest);
            fileSystem.close();
        } catch (IOException e) {
            LOGGER.error("failed to copy local file: " + filename, e);
        }

        fileSystem.close();
    }

    /**
     * Copy HDFS file to local
     * @param source file in hdfs
     * @param dest local path
     */
    public void copyToLocal(String source, String dest) throws IOException {
        FileSystem fileSystem = FileSystem.get(conf);

        Path srcPath = new Path(source);
        Path dstPath = new Path(dest);

        if (!this.exists(source)) {
            LOGGER.debug("destination doesn't exist " + srcPath);
            return;
        }

        String filename = source.substring(source.lastIndexOf('/') + 1, source.length());

        try {
            fileSystem.copyToLocalFile(srcPath, dstPath);
            LOGGER.info("copy " + filename + " done!: " + dest);
            fileSystem.close();
        } catch (Exception e) {
            LOGGER.error("failed to copy: " + srcPath, e);
        }
        fileSystem.close();
    }

    /**
     * Rename file
     * @param source source file name
     * @param target target file name
     */
    public void rename(String source, String target) throws IOException {
        FileSystem fileSystem = FileSystem.get(conf);
        Path fromPath = new Path(source);
        Path toPath = new Path(target);

        if (!(fileSystem.exists(fromPath))) {
            System.out.println("No such destination " + fromPath);
            return;
        }

        if (fileSystem.exists(toPath)) {
            System.out.println("Already exists! " + toPath);
            return;
        }

        try {
            boolean isRenamed = fileSystem.rename(fromPath, toPath);
            if (isRenamed) {
                LOGGER.info("rename from \"" + source + "\" to \"" + target + "\"");
            }
        } catch (Exception e) {
            LOGGER.error("failed to rename", e);
        } finally {
            fileSystem.close();
        }
    }


    /**
     * Read file
     * @param file file path to read
     */
    public String read(String file) {
        FileSystem fileSystem;
        try {
            fileSystem = FileSystem.get(conf);
        } catch (IOException e) {
            LOGGER.error("failed to get Filesystem", e);
            return null;
        }

        StringBuffer sb = new StringBuffer();

        Path path = new Path(file);
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(fileSystem.open(path));

            BufferedReader reader = new BufferedReader(inputStreamReader);

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            inputStreamReader.close();
            reader.close();
            fileSystem.close();
        } catch (IOException e) {
            LOGGER.error("failed to read file : " + file, e);
        }

        return sb.toString();
    }

    /**
     * Delete a file/directory
     * @param file target file/directory to delete
     */
    public void delete(String file) {
        FileSystem fileSystem = null;
        try {
            fileSystem = FileSystem.get(conf);
        } catch (IOException e) {
            LOGGER.error("can't get the fileSystem", e);
        }
        Path path = new Path(file);
        try {
            if (!fileSystem.exists(path)) {
                LOGGER.info(file + " does not exists");
                return;
            }
            fileSystem.delete(path, true);
            fileSystem.close();
        } catch (IOException e) {
            LOGGER.error("failed to processing the fileSystem", e);
        }

    }

    /**
     * Make a Directory
     * @param directory directory to make
     * @throws IOException
     */
    public void mkdir(String directory) throws IOException {
        FileSystem fileSystem = FileSystem.get(conf);
        Path path = new Path(directory);
        if (fileSystem.exists(path)) {
            LOGGER.info(directory + " already exists!");
            return;
        }

        fileSystem.mkdirs(path);
        fileSystem.close();
    }


    /**
     * Create a File to HDFS
     * @param file file to be created
     */
    public void createFile(String file) {
        try {
            FileSystem fileSystem = FileSystem.get(conf);
            Path path = new Path(file);
            fileSystem.createNewFile(path);
            fileSystem.close();
        } catch (IOException e) {
            LOGGER.error("can't create a file: " + file);
        }
    }

    /**
     * Writing Data to a HDFS file
     * @param file file path
     * @param contents contents to be written
     */
    public void write(String file, String contents) {
        FileSystem fileSystem = null;
        FSDataOutputStream fsOutStream = null;
        try {
            fileSystem = FileSystem.get(conf);

            if (!exists(file)) {
                createFile(file);
            }

            StringBuilder sb = new StringBuilder(contents);
            byte[] data = sb.toString().getBytes();
            Path path = new Path(file);
            fsOutStream = fileSystem.create(path);

            fsOutStream.write(data);
            fsOutStream.close();
            fileSystem.close();
        } catch (IOException e) {
            LOGGER.error("can't write the file: " + file);
        }

    }

    /**
     * Sample Program
     * @param args options and inputs
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        String test = "this is test / -/attribute-392.dict/dife";

        String pattern = "(\\d+)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(test);

        m.find();
        System.out.println(m.group());
        /*
        if (m.find( )) {
            System.out.println("Found value: " + m.group(0) );
            System.out.println("Found value: " + m.group(1) );
            System.out.println("Found value: " + m.group(2) );
        } else {
            System.out.println("NO MATCH");
        }
        */



        /*
        if (args.length < 1) {
            System.exit(1);
        }

        HDFSUtil client = new HDFSUtil();

        if (args[0].equals("read")) {
            if (args.length < 2) {
                System.out.println("Usage: HDFSUtil read <hdfs_path>");
                System.exit(1);
            }
            client.read(args[1]);

        } else if (args[0].equals("delete")) {
            if (args.length < 2) {
                System.out.println("Usage: HDFSUtil delete <hdfs_path>");
                System.exit(1);
            }

            client.delete(args[1]);
        } else if (args[0].equals("mkdir")) {
            if (args.length < 2) {
                System.out.println("Usage: HDFSUtil mkdir <hdfs_path>");
                System.exit(1);
            }

            client.mkdir(args[1]);
        } else if (args[0].equals("copyfromlocal")) {
            if (args.length < 3) {
                System.out.println("Usage: HDFSUtil copyfromlocal <from_local_path> <to_hdfs_path>");
                System.exit(1);
            }

            client.copyFromLocal(args[1], args[2]);
        } else if (args[0].equals("rename")) {
            if (args.length < 3) {
                System.out.println("Usage: HDFSUtil rename <old_hdfs_path> <new_hdfs_path>");
                System.exit(1);
            }

            client.rename(args[1], args[2]);
        } else if (args[0].equals("copytolocal")) {
            if (args.length < 3) {
                System.out.println("Usage: HDFSUtil copytolocal <from_hdfs_path> <to_local_path>");
                System.exit(1);
            }

            client.copyToLocal(args[1], args[2]);
        }  else if (args[0].equals("gethostnames")) {
            client.getHostNames();
        } else {
            System.exit(1);
        }

        System.out.println("Done!");
        */
    }

    /*
    public void getBlockLocations(String filePath) throws IOException {
        Path path = new Path(filePath);

        if (!(this.exists(path))) {
            LOGGER.debug("file doesn't exist: " + path);
            return;
        }

        String filename = filePath.substring(filePath.lastIndexOf('/') + 1, filePath.length());

        FileStatus fileStatus = fileSystem.getFileStatus(path);

        BlockLocation[] blkLocations = fileSystem.getFileBlockLocations(fileStatus, 0, fileStatus.getLen());
        int blkCount = blkLocations.length;

        System.out.println("File :" + filename + "stored at:");
        for (int i = 0; i < blkCount; i++) {
            String[] hosts = blkLocations[i].getHosts();
            System.out.format("Host %d: %s %n", i, hosts);
        }

    }
    */

    /*
    public void getModificationTime(String source) throws IOException {

        Path srcPath = new Path(source);

        if (!(fileSystem.exists(srcPath))) {
            System.out.println("No such destination " + srcPath);
            return;
        }

        String filename = source.substring(source.lastIndexOf('/') + 1, source.length());

        FileStatus fileStatus = fileSystem.getFileStatus(srcPath);
        long modificationTime = fileStatus.getModificationTime();

        System.out.format("File %s; Modification time : %0.2f %n", filename, modificationTime);

    }

    public void add(String source, String target) {
        String filename = source.substring(source.lastIndexOf('/') + 1, source.length());

        if (target.charAt(target.length() - 1) != '/') {
            target = target + "/" + filename;
        } else {
            target = target + filename;
        }

        Path path = new Path(target);
        if (this.exists(path)) {
            LOGGER.error(target + " already exists");
            return;
        }

        FSDataOutputStream out;
        try {
            out = this.fileSystem.create(path);
            InputStream in = new BufferedInputStream(new FileInputStream(new File(source)));
            byte[] b = new byte[1024];
            int numBytes = 0;
            while ((numBytes = in.read(b)) > 0) {
                out.write(b, 0, numBytes);
            }
            in.close();
            out.close();
            this.fileSystem.close();
        } catch (IOException e) {
            LOGGER.error("failed to add file to the file system: ", e);
        }

    }
    */

}

