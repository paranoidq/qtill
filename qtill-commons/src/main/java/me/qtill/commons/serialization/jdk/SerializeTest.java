package me.qtill.commons.serialization.jdk;

import java.io.*;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class SerializeTest {

    public static class Foo implements Externalizable {
        private transient String name;
        private           String age;
        private transient String email;

        public Foo() {
        }

        public Foo(String name, String age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public String getAge() {
            return age;
        }

        public String getEmail() {
            return email;
        }


        private void writeObject(ObjectOutputStream oos) throws IOException {
            oos.defaultWriteObject();
            oos.writeUTF(email);
        }

        private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
            ois.defaultReadObject();
            this.email = ois.readUTF();
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {

        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

        }
    }

    public static void main(String[] args) throws Exception {

        Foo foo = new Foo("qianwei", "29", "paranoid_qian@163.com");
        JdkSerializer jdkSerializer = new JdkSerializer();
        byte[] bytes = jdkSerializer.serialize(foo);

        Foo test = jdkSerializer.deserialize(bytes, Foo.class);

        System.out.println(test.getName());
        System.out.println(test.getAge());
        System.out.println(test.getEmail());

    }

    /**
     * 添加了Externalizable接口，writeObject方法被忽略，必须覆写writeExternal方法
     * null
     * null
     * null
     */
}
