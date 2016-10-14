# ZzSQLHelper

A powerful sqlite framework that supports ORM and Cursor mode.
(一个简单却强大的数据库封装工具。)

· 支持ORM和Cursor两种操作模式；

Gradle:

```
compile 'me.zhouzhuo.zzsqlhelper:zz-sql-helper:1.0.0'

```

Maven:

```
<dependency>
  <groupId>me.zhouzhuo.zzsqlhelper</groupId>
  <artifactId>zz-sql-helper</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```


#### 推荐用法：

定义一个单例模式工具类(完成数据库配置、建表和数据库升级操作)：

```java
public class DbUtils {

    private static ZzSqlHelper helper;

    private DbUtils() {
    }

    public static ZzSqlHelper getInstance(Context context) {
        if (helper == null) {
            synchronized (DbUtils.class) {
                if (helper == null) {
                    helper = new ZzSqlHelper.Builder()
                            .setDbName("hello")
                            .setVersion(13)
//                            .addTableSql("create table if not exists student (id integer primary key , name varchar, phone varchar)")
                            .addTableEntity(TestEntity.StudentEntity.class)
                            .setUpgradeListener(new ZzSqlHelper.Builder.UpgradeListener() {
                                @Override
                                public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                                    if (oldVersion != 0 && db != null && db.isOpen()) {
                                        db.execSQL("drop table studententity");
                                    }
                                }
                            })
                            .create(context);
                }
            }
        }
        return helper;
    }


}

```


#### 日志打印：

```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Logger.enable(true);
    }
}

```



## ORM模式：

1.定义实体类，表名就是实体类名， 字段名就是属性名，
  使用@Column()注解可以控制字段是否保存和获取


```java
    public static class StudentEntity {
		
		//id是必须加的字段
        private int id;
        //save=false时，不保存和读取该字段
        @Column(save = false)
        private String name;
        //默认save=true
        @Column()
        private String phone;

		//... getter and setter
	}
```

### 增：

```java
		//实体类
        TestEntity.StudentEntity studentEntity = new TestEntity.StudentEntity();
        studentEntity.setName("hello");
        studentEntity.setPhone("123");
        studentEntity.setMan(true);
        //保存实体类到数据库
        DbUtils.getInstance(this).save(studentEntity);
        //多个实体类可以用saveAll(List<Entity> list)方法。
       
```


### 删

```java
		//条件删除
        DbUtils.getInstance(this).delete(TestEntity.StudentEntity.class,
                new ZzSqlHelper.WhereBuilder("name","==","hello"));
        //根据id删除某行
        DbUtils.getInstance(this).deleteById(TestEntity.StudentEntity.class, 2);
        //删除整个表
        DbUtils.getInstance(this).deleteAll(TestEntity.StudentEntity.class);
```

### 查

		

```java
		//条件查询
        String sql2 = new SQLBuilder()
                .from(TestEntity.StudentEntity.class)
                .select()
                .build();
        List<TestEntity.StudentEntity> students = DbUtils.getInstance(this)
                .findAll(TestEntity.StudentEntity.class, sql2);
		//根据id查询
		TestEntity.StudentEntity entity = DbUtils.getInstance(this)
		         .findById(TestEntity.StudentEntity.class, 1);
		//查询表中所有数据
		List<TestEntity.StudentEntity> datas = DbUtils.getInstance(this)
		         .findAll(TestEntity.StudentEntity.class);
```

### 改

```java
	//传入实体类对象和要更新的字段名，传null更新全部字段。
   entity.setMan(true);
   entity.setPhone("555");
   DbUtils.getInstance(this)
          .update(entity, "phone", "isMan");
   
```

## Cursor模式：

### 增

```java
        SQLBuilder b = new SQLBuilder();
        String sql = b.from(TestEntity.StudentEntity.class)
                .insert(new String[]{"name", "phone"}, new String[]{"tt", "yy"})
                .build();
        DbUtils.getInstance(this).execSQL(sql);
```

### 删

```
        String sql = new SQLBuilder()
                .from(TestEntity.StudentEntity.class)
                .delete()
                .where("name", "=", "tt")
                .build();
        DbUtils.getInstance(this).execSQL(sql);
```

### 查

```
        String sql = new SQLBuilder()
                .from(TestEntity.StudentEntity.class)
                .select()
                .build();
        Cursor cursor = DbUtils.getInstance(MainActivity.this).findAll(sql);
        while (cursor.moveToNext()) {
            Log.e("xxx", "id=" + cursor.getString(cursor.getColumnIndex("id")));
            Log.e("xxx", "name=" + cursor.getString(cursor.getColumnIndex("name")));
            Log.e("xxx", "phone=" + cursor.getString(cursor.getColumnIndex("phone")));
            Log.e("xxx", "isMan=" + cursor.getInt(cursor.getColumnIndex("isMan")));
        }
        cursor.close();
```

### 改
```
        SQLBuilder b = new SQLBuilder();
        String sql = b.from(TestEntity.StudentEntity.class)
                .update(new String[]{"phone"}, new String[]{"4343"})
                .where("id", "=", 1)
                .build();
        DbUtils.getInstance(this).execSQL(sql);
```


## Contact Me

Email ： admin@zhouzhuo.me
QQ群： 154107392 欢迎提意见

