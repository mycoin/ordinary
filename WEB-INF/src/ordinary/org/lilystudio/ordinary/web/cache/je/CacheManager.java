package org.lilystudio.ordinary.web.cache.je;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lilystudio.ordinary.module.AbstractModule;
import org.lilystudio.ordinary.web.Controller;
import org.lilystudio.ordinary.web.cache.je.data.IMetaData;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.JoinConfig;
import com.sleepycat.je.JoinCursor;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryCursor;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;
import com.sleepycat.je.Transaction;

/**
 * 缓存管理器, 支持对需要缓存的数据进行多关键字控制, 在查询缓存数据时,
 * 需要所有的关键字内容相等, 但是更新缓存时, 仅需要指定部分关键字. 只能用于单机的缓存,
 * 对于并行的服务器, 考虑使用RMI来进行数据同步控制
 * 
 * <b>属性</b>
 * 
 * <pre>
 * root--数据库文件保存的路径, 使用相对路径保存
 * </pre>
 * 
 * @version 0.1.4, 2009/01/10
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class CacheManager extends AbstractModule {

  /** 类存储表名称 */
  private static final String DATABASE_CLASS_NAME = "__class__";

  /** 主存储表名称 */
  private static final String DATABASE_MAIN_NAME = "__main__";

  /** 关键字信息比较器, 用于确定两个关键字的先后顺序 */
  final Comparator<Key> comparator = new Comparator<Key>() {

    public int compare(Key o1, Key o2) {
      return o1.getName().compareTo(o2.getName());
    }
  };

  /** Berkeley DB的环境对象 */
  private Environment env;

  /** 二级数据库配置对象 */
  private SecondaryConfig dbConfig;

  /** 联合游标配置对象 */
  private JoinConfig joinConfig;

  /** 类信息缓存, 用于类的序列化与反序列化 */
  private StoredClassCatalog classCatalog;

  /** 主表的键(关键字列表)序列化器 */
  private EntryBinding<IMetaData[]> keyBinding;

  /** 主表的值(缓存对象)序列化器 */
  private EntryBinding<ICache> dataBinding;

  /** 主表, 键是关键字列表的序列化数组, 值是缓存对象的序列化数组 */
  private Database dbMain;

  /** 单个关键字索引表集合, 键是关键字值的索引化数组, 键是dbIndex中的ID号 */
  private Map<String, SecondaryDatabase> dbKeys = new HashMap<String, SecondaryDatabase>();

  /** 数据库容器文件根路径 */
  private String root;

  /** 磁盘路径 */
  private String diskRoot;

  /**
   * 初始化对象
   * 
   * @throws Exception
   *           如果初始化失败
   */
  public void init() throws Exception {
    if (root != null) {
      if (!root.endsWith("/")) {
        root += "/";
      }
    } else {
      root = "";
    }
    diskRoot = Controller.getContextPath() + root;
    EnvironmentConfig envConfig = new EnvironmentConfig();
    envConfig.setAllowCreate(true);
    envConfig.setTransactional(true);
    envConfig.setTxnNoSync(true);
    envConfig.setTxnWriteNoSync(true);
    envConfig.setLockTimeout(5000000);
    File dataDir = new File(diskRoot);
    dataDir.mkdirs();
    try {
      env = new Environment(dataDir, envConfig);
    } catch (Exception e) {
      // 打开失败, 删除原来的内容再重新打开一次
      deleteAll(dataDir);
      env = new Environment(dataDir, envConfig);
    }
    DatabaseConfig dbConfig = new DatabaseConfig();
    dbConfig.setAllowCreate(true);
    dbConfig.setTransactional(true);
    classCatalog = new StoredClassCatalog(env.openDatabase(null,
        DATABASE_CLASS_NAME, dbConfig));
    keyBinding = new SerialBinding<IMetaData[]>(classCatalog, IMetaData[].class);
    dataBinding = new SerialBinding<ICache>(classCatalog, ICache.class);
    dbMain = env.openDatabase(null, DATABASE_MAIN_NAME, dbConfig);

    // 单个关键字索引表允许重复的记录
    SecondaryConfig config = new SecondaryConfig();
    config.setAllowCreate(true);
    config.setTransactional(true);
    config.setSortedDuplicates(true);
    config.setKeyCreator(new SecondaryKeyCreator() {

      public boolean createSecondaryKey(SecondaryDatabase secondary,
          DatabaseEntry key, DatabaseEntry data, DatabaseEntry result)
          throws DatabaseException {
        String name = secondary.getDatabaseName();
        IMetaData[] keys = keyBinding.entryToObject(key);
        for (IMetaData o : keys) {
          if (name.equals(o.getName())) {
            result.setData(o.getBytes());
            return true;
          }
        }
        return false;
      }
    });

    for (String name : env.getDatabaseNames()) {
      if (!name.startsWith("__") || !name.endsWith("__")) {
        dbKeys.put(name, env.openSecondaryDatabase(null, name, dbMain, config));
      }
    }
    this.dbConfig = config;

    joinConfig = new JoinConfig();
    joinConfig.setNoSort(true);
  }

  /**
   * 获取容器根目录
   * 
   * @return 缓存管理器根目录
   */
  public String getRoot() {
    return root;
  }

  /**
   * 获取磁盘根目录
   * 
   * @return 缓存管理器根目录
   */
  public String getDiskRoot() {
    return diskRoot;
  }

  /**
   * 关闭缓存管理器
   * 
   * @throws Exception
   *           关闭缓存管理器异常
   */
  public void close() throws Exception {
    for (Database db : dbKeys.values()) {
      try {
        db.close();
      } catch (Exception e) {
      }
    }
    try {
      dbMain.close();
    } catch (Exception e) {
    }
    try {
      classCatalog.close();
    } catch (Exception e) {
    }
    try {
      env.close();
    } catch (Exception e) {
    }
  }

  /**
   * 取出关键字列表对应的缓存, 如果不存在需要创建缓存,
   * 相关初始化等操作在IHandle接口中定义
   * 
   * @param keys
   *          缓存关键字序列
   * @param cache
   *          缓存操作接口
   * @throws Exception
   *           数据操作异常
   */
  public void get(IMetaData[] keys, ICache cache) throws Exception {
    // 检查是否有还未初始化的单项键值索引表
    for (IMetaData o : keys) {
      String name = o.getName();
      SecondaryDatabase db = dbKeys.get(name);
      if (db == null) {
        synchronized (this) {
          db = dbKeys.get(name);
          if (db == null) {
            dbKeys.put(name, env.openSecondaryDatabase(null, name, dbMain,
                dbConfig));
          }
        }
      }
    }

    DatabaseEntry key = new DatabaseEntry();
    DatabaseEntry data = new DatabaseEntry();

    // 生成总的索引数据
    keyBinding.objectToEntry(keys, key);
    if (dbMain.get(null, key, data, null) != OperationStatus.SUCCESS) {
      synchronized (this) {
        if (dbMain.get(null, key, data, null) != OperationStatus.SUCCESS) {
          Transaction tran = env.beginTransaction(null, null);
          try {
            cache.create();
          } catch (Exception e) {
            throw new InvocationTargetException(e);
          }
          try {
            dataBinding.objectToEntry(cache, data);
            dbMain.put(tran, key, data);
            tran.commit();
          } catch (Exception e) {
            tran.abort();
            throw e;
          }
        }
      }
    }
    cache.setData(dataBinding.entryToObject(data).getData());
  }

  /**
   * 清除指定的关键字对应的缓存
   * 
   * @param name
   *          关键字名称
   * @throws Exception
   *           数据操作异常
   */
  public void clear(String name) throws Exception {
    SecondaryDatabase db = dbKeys.get(name);
    if (db != null) {
      Transaction tran = env.beginTransaction(null, null);
      DatabaseEntry key = new DatabaseEntry();
      DatabaseEntry pKey = new DatabaseEntry();
      DatabaseEntry data = new DatabaseEntry();
      try {
        SecondaryCursor cursor = db.openSecondaryCursor(tran, null);
        try {
          while (cursor.getNext(key, pKey, data, null) == OperationStatus.SUCCESS) {
            cursor.delete();
            dataBinding.entryToObject(data).remove();
          }
        } finally {
          cursor.close();
        }
        tran.commit();
      } catch (Exception e) {
        tran.abort();
        throw e;
      }
    }
  }

  /**
   * 清除指定的关键字列表对应的缓存
   * 
   * @param keys
   *          缓存关键字序列
   * @throws Exception
   *           数据操作异常
   */
  public void clear(IMetaData[] keys) throws Exception {
    int size = keys.length;
    if (size == 1) {
      // 仅指定了一个关键字, 调用快速删除的操作
      clear(keys[0]);
    } else {
      SecondaryCursor[] cursors = new SecondaryCursor[keys.length];
      DatabaseEntry key = new DatabaseEntry();
      DatabaseEntry data = new DatabaseEntry();
      List<DatabaseEntry> entries = new ArrayList<DatabaseEntry>();
      try {
        for (int i = 0; i < size; i++) {
          IMetaData o = keys[i];
          SecondaryDatabase db = dbKeys.get(o.getName());
          if (db == null) {
            return;
          }
          SecondaryCursor cursor = db.openSecondaryCursor(null, null);
          key.setData(o.getBytes());
          cursor.getSearchKey(key, data, null);
          cursors[i] = cursor;
        }
        JoinCursor joinCursor = dbMain.join(cursors, null);
        try {
          while (joinCursor.getNext(key, null) == OperationStatus.SUCCESS) {
            entries.add(key);
            key = new DatabaseEntry();
          }
        } finally {
          joinCursor.close();
        }
      } finally {
        for (SecondaryCursor cursor : cursors) {
          if (cursor == null) {
            break;
          }
          cursor.close();
        }
      }
      Transaction tran = env.beginTransaction(null, null);
      try {
        Cursor cursor = dbMain.openCursor(tran, null);
        try {
          for (DatabaseEntry entry : entries) {
            cursor.getSearchKey(entry, data, null);
            cursor.delete();
            dataBinding.entryToObject(data).remove();
          }
        } finally {
          cursor.close();
        }
        tran.commit();
      } catch (Exception e) {
        tran.abort();
        throw e;
      }
    }
  }

  /**
   * 清除指定的单个关键字对应的缓存
   * 
   * @param data
   *          缓存关键字
   * @throws Exception
   *           数据操作异常
   */
  private void clear(IMetaData data) throws Exception {
    SecondaryDatabase db = dbKeys.get(data.getName());
    if (db != null) {
      Transaction tran = env.beginTransaction(null, null);
      DatabaseEntry key = new DatabaseEntry(data.getBytes());
      DatabaseEntry pKey = new DatabaseEntry();
      DatabaseEntry pData = new DatabaseEntry();
      try {
        SecondaryCursor cursor = db.openSecondaryCursor(tran, null);
        try {
          OperationStatus ret = cursor.getSearchKey(key, pKey, pData, null);
          while (ret == OperationStatus.SUCCESS) {
            cursor.delete();
            dataBinding.entryToObject(pData).remove();
            ret = cursor.getNextDup(pKey, pData, null);
          }
        } finally {
          cursor.close();
        }
        tran.commit();
      } catch (Exception e) {
        tran.abort();
        throw e;
      }
    }
  }

  /**
   * 删除目录下的所有文件
   * 
   * @param dir
   *          目录的文件描述对象
   */
  private void deleteAll(File dir) {
    for (File file : dir.listFiles()) {
      if (file.isFile()) {
        file.delete();
      } else if (file.isDirectory()) {
        deleteAll(file);
        file.delete();
      }
    }
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }
}