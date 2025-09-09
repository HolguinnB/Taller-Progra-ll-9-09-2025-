package com.edu.uptc.handlingParking.persistence;

import com.edu.uptc.handlingParking.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class UserSerialization {
    private String path;

    public UserSerialization(String path) {
        this.path = path;
    }

    @SuppressWarnings("unchecked")
    public List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        File file = new File(path);

        if (!file.exists()) {

            users.add(new User("brayan", "3355"));
            saveUsers(users);
            return users;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            users = (List<User>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (users.isEmpty()) {
            users.add(new User("brayan", "3355"));
            
            saveUsers(users);
        }

        return users;
    }

    public void saveUsers(List<User> users) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
