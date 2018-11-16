package me.qtill.commons.introspector;

import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class BeanUtilsSample {


    public static class User {
        String name;
        String age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAge() {
            return age;
        }

        public void setAge(String age) {
            this.age = age;
        }
    }

    public static void main(String[] args) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        User user = new User();
        user.setAge("12");
        user.setName("aaa");

        String age = BeanUtils.getProperty(user, "age");
    }
}
