package com.skplanet.nlp.sentiment.util;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * HDFS Utility
 * it supports following,
 * add
 * read
 * delete
 * mkdir
 * copy file from local to HDFS
 * copy file from HDFS to local
 *
 * @author Donghun Shin / donghun.shin@sk.com
 * @date 4/16/15
 */
public class HDFSUtil {
    private static final Logger LOGGER = Logger.getLogger(HDFSUtil.class.getName());

    // file system
    private FileSystem fileSystem;

    /**
     * Constructor
     * it sets the environment for hdfs
     */
    public HDFSUtil(String hadoop_config) {
        // get hadoop env from application's configuration
        String HADOOP_CONF_PATH = hadoop_config;

        // get hadoop schema
        Configuration schema = new Configuration();
        schema.addResource(new Path(HADOOP_CONF_PATH + "/core-site.xml"));
        schema.addResource(new Path(HADOOP_CONF_PATH + "/hdfs-site.xml"));
        schema.addResource(new Path(HADOOP_CONF_PATH + "/mapred-site.xml"));

        // load file system
        try {
            this.fileSystem = FileSystem.get(schema);
        } catch (IOException e) {
            LOGGER.error("Hadoop Schema Not found", e);
        }
    }

    /**
     * Check if source file exists in the file system
     * @param file file to be looked up
     * @return true if exists
     */
    public boolean exists(String file) {
        boolean isExists = false;
        try {
            Path path = new Path(file);
            isExists = this.fileSystem.exists(path);
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
    public List<String> ls(String pathStr) {
        Path path = new Path(pathStr);
        List<String> resultPaths = new ArrayList<String>();
        try {
            FileStatus[] status = this.fileSystem.listStatus(path);
            for (FileStatus stat : status) {
                resultPaths.add(stat.getPath().toString());
            }
        } catch (IOException e) {
            LOGGER.error("The path doesn't exist: " + pathStr);
        }
        return resultPaths;
    }

    /**
     * Get All the datanodes
     */
    public void getHostNames() {
        DistributedFileSystem hdfs = (DistributedFileSystem) this.fileSystem;
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
    }


    /**
     * Copy local file to HDFS
     * @param source local file path
     * @param dest hdfs file path
     */
    public void copyFromLocal(String source, String dest) {

        Path srcPath = new Path(source);
        Path dstPath = new Path(dest);

        if (!this.exists(source)) {
            LOGGER.debug("destination doesn't exist: " + dstPath);
            return;
        }

        String filename = source.substring(source.lastIndexOf('/') + 1, source.length());

        try {
            this.fileSystem.copyFromLocalFile(srcPath, dstPath);
            System.out.println("File " + filename + "copied to " + dest);
            this.fileSystem.close();
        } catch (IOException e) {
            LOGGER.error("failed to copy local file: " + filename, e);
        }
    }

    /**
     * Copy HDFS file to local
     * @param source file in hdfs
     * @param dest local path
     */
    public void copyToLocal(String source, String dest) {

        Path srcPath = new Path(source);
        Path dstPath = new Path(dest);

        if (!this.exists(source)) {
            LOGGER.debug("destination doesn't exist " + srcPath);
            return;
        }

        String filename = source.substring(source.lastIndexOf('/') + 1, source.length());

        try {
            this.fileSystem.copyToLocalFile(srcPath, dstPath);
            LOGGER.info("copy " + filename + " done!: " + dest);
            this.fileSystem.close();
        } catch (Exception e) {
            LOGGER.error("failed to copy: " + srcPath, e);
        }
    }

    /**
     * Rename file
     * @param source source file name
     * @param target target file name
     */
    public void rename(String source, String target) {
        Path fromPath = new Path(source);
        Path toPath = new Path(target);

        if (!(this.exists(source))) {
            System.out.println("No such destination " + fromPath);
            return;
        }

        if (this.exists(source)) {
            System.out.println("Already exists! " + toPath);
            return;
        }

        try {
            boolean isRenamed = this.fileSystem.rename(fromPath, toPath);
            if (isRenamed) {
                LOGGER.info("rename from \"" + source + "\" to \"" + target + "\"");
            }
            this.fileSystem.close();
        } catch (Exception e) {
            LOGGER.error("failed to rename", e);
        }
    }


    /**
     * Read file
     * @param file file path to read
     */
    public String read(String file) {
        StringBuffer sb = new StringBuffer();

        Path path = new Path(file);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(this.fileSystem.open(path)));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            LOGGER.error("failed to open input stream", e);
            return null;
        }

        return sb.toString();
    }

    /**
     * Delete a file/directory
     * @param file target file/directory to delete
     */
    public void delete(String file) {

        if (!this.exists(file)) {
            LOGGER.info(file + " does not exists");
            return;
        }


        try {
            Path path = new Path(file);
            this.fileSystem.delete(path, true);
        } catch (IOException e) {
            LOGGER.error("failed to delete : " + file, e);
        }

        try {
            this.fileSystem.close();
        } catch (IOException e) {
            LOGGER.error("failed to close : " + file, e);
        }


    }

    /**
     * Make a Directory
     * @param directory directory to make
     * @throws IOException
     */
    public void mkdir(String directory) {

        if (this.exists(directory)) {
            LOGGER.info(directory + " already exists!");
            return;
        }

        try {
            Path path = new Path(directory);
            this.fileSystem.mkdirs(path);
            this.fileSystem.close();
        } catch (IOException e) {
            LOGGER.error("failed to make a directory: " + directory);
        }
    }


    /**
     * Create a File to HDFS
     * @param file file to be created
     */
    public void createFile(String file) {
        Path path = new Path(file);
        try {
            this.fileSystem.createNewFile(path);
        } catch (IOException e) {
            LOGGER.error("failed create a file: " + file, e);
        }
    }

    /**
     * Writing Data to a HDFS file
     * @param file file path
     * @param contents contents to be written
     */
    public void write(String file, String contents) {
        if (!exists(file)) {
            createFile(file);
        }
        StringBuilder sb = new StringBuilder(contents);
        byte[] data = sb.toString().getBytes();
        Path path = new Path(file);
        try {
            FSDataOutputStream fsOutStream = this.fileSystem.create(path);
            fsOutStream.write(data);
            fsOutStream.close();
        } catch (IOException e) {
            LOGGER.error("failed to write content: " + file, e);
        }

    }

    /**
     * Load Properties from HDFS
     * @param propFilePath path to properties file in hdfs
     * @return {@link Properties}
     * @throws IOException failed to locate the properties file in hdfs
     */
    public Properties loadProperties(String propFilePath) throws IOException {
        String propContents = this.read(propFilePath);

        Properties properties = new Properties();
        InputStream propIS = new ByteArrayInputStream(propContents.getBytes());
        properties.load(propIS);

        return properties;
    }

    /**
     * Sample Program
     * @param args options and inputs
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        HDFSUtil client = new HDFSUtil("/usr/local/Cellar/hadoop/2.7.1/libexec/etc/hadoop");

        client.createFile("/meta/dc/an/test");
        client.write("/meta/dc/an/test", "test\n");

        client.write("/meta/dc/an/test", "test2\n");
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
