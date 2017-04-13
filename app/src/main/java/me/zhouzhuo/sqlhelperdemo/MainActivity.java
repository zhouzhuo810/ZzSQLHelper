package me.zhouzhuo.sqlhelperdemo;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.zhouzhuo.zzsqlhelper.SQLBuilder;

public class MainActivity extends FragmentActivity {

    private List<TestEntity.StudentEntity> datas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        datas = new ArrayList<>();
        ListView lv = (ListView) findViewById(R.id.lv);
        BaseAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return datas == null ? 0 : datas.size();
            }

            @Override
            public Object getItem(int position) {
                return datas == null ? null : datas.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.list_item, parent, false);
                TextView tvId = (TextView) convertView.findViewById(R.id.m_id);
                TextView tvName = (TextView) convertView.findViewById(R.id.tv_name);
                TextView tvPhone = (TextView) convertView.findViewById(R.id.tv_phone);
                TextView tvMan = (TextView) convertView.findViewById(R.id.tv_man);
                tvId.setText(datas.get(position).getId() + "");
                tvName.setText(datas.get(position).getName());
                tvPhone.setText(datas.get(position).getPhone());
                tvMan.setText(datas.get(position).isMan() + "");
                return convertView;
            }
        };
        lv.setAdapter(adapter);


/****************************ORM***********************************/

        //增
        TestEntity.StudentEntity studentEntity = new TestEntity.StudentEntity();
        studentEntity.setName("hello");
        studentEntity.setPhone("123");
        studentEntity.setMan(true);
        DbUtils.getInstance(this).save(studentEntity);

        TestEntity.StudentEntity studentEntity1 = new TestEntity.StudentEntity();
        studentEntity1.setName("zz");
        studentEntity1.setPhone("321");
        studentEntity1.setMan(false);
        DbUtils.getInstance(this).save(studentEntity1);

        //删
/*        DbUtils.getInstance(this).delete(TestEntity.StudentEntity.class,
                new ZzSqlHelper.WhereBuilder("name","==","hello"));*/
        DbUtils.getInstance(this).deleteById(TestEntity.StudentEntity.class, 2);
//        DbUtils.getInstance(this).deleteAll(TestEntity.StudentEntity.class);

        //查
        String sql2 = new SQLBuilder()
                .from(TestEntity.StudentEntity.class)
                .select()
                .build();
        List<TestEntity.StudentEntity> students = DbUtils.getInstance(this)
                .findAll(TestEntity.StudentEntity.class, sql2);
        if (students != null) {
            datas = students;
            adapter.notifyDataSetChanged();
        }

        //查
        TestEntity.StudentEntity entity = DbUtils.getInstance(this).findById(TestEntity.StudentEntity.class, 1);
        if (entity != null) {
            //改
            entity.setMan(true);
            entity.setPhone("555");
//            DbUtils.getInstance(this).update(entity, "phone", "isMan");
            DbUtils.getInstance(this).update(entity);
        }

        //查
        TestEntity.StudentEntity entity1 = DbUtils.getInstance(this).findById(TestEntity.StudentEntity.class, 1);


/****************************ORM***********************************/


/****************************Cursor***********************************/

        //增
        SQLBuilder b1 = new SQLBuilder();

        String sql3 = b1.from(TestEntity.StudentEntity.class)
//                .insert("name,phone", "'tt', 'yy'")
                .insert(new String[]{"isMan", "phone"}, new String[]{"0", "yy"})
                .build();
        DbUtils.getInstance(this).execSQL(sql3);


        //删
        String sql34 = new SQLBuilder()
                .from(TestEntity.StudentEntity.class)
                .delete()
                .where("phone", "=", "yy")
                .build();
        DbUtils.getInstance(this).execSQL(sql34);


        //改
        SQLBuilder b = new SQLBuilder();
        String sql = b.from(TestEntity.StudentEntity.class)
                .update(new String[]{"phone"}, new String[]{"4343"})
                .where("id", "=", 1)
                .build();
        DbUtils.getInstance(this).execSQL(sql);


        datas = DbUtils.getInstance(this).findAll(TestEntity.StudentEntity.class);
        adapter.notifyDataSetChanged();


        //查
        String sql1 = new SQLBuilder()
                .from(TestEntity.StudentEntity.class)
                .select()
                .build();
        Cursor cursor = DbUtils.getInstance(MainActivity.this).findAll(sql1);
        while (cursor.moveToNext()) {
            Log.e("xxx", "id=" + cursor.getString(cursor.getColumnIndex("id")));
            Log.e("xxx", "phone=" + cursor.getString(cursor.getColumnIndex("phone")));
            Log.e("xxx", "isMan=" + cursor.getInt(cursor.getColumnIndex("isMan")));
        }
        cursor.close();


/****************************Cursor***********************************/

    }
}
