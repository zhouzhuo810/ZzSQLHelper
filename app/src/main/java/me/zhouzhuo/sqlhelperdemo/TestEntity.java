package me.zhouzhuo.sqlhelperdemo;


import me.zhouzhuo.zzsqlhelper.anotation.Column;

/**
 * Created by zz on 2016/10/13.
 */

public class TestEntity {
    private String name;

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public static class StudentEntity {

        private int id;
        @Column(save = false)
        private String name;
        private String phone;
        private boolean isMan;

        public boolean isMan() {
            return isMan;
        }

        public void setMan(boolean man) {
            isMan = man;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        @Override
        public String toString() {
            return "StudentEntity{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", phone='" + phone + '\'' +
                    ", isMan=" + isMan +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "TestEntity{" +
                "name='" + name + '\'' +
                '}';
    }
}
