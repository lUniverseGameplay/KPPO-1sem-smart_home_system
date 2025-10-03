package com.example.SmartHomeSystem.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.SmartHomeSystem.model.User;
import com.example.SmartHomeSystem.service.UserService;

import jakarta.validation.Valid;



@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    // private List<User> users = new ArrayList<>(Arrays.asList(
    //     new User(1l, "smth", 100),
    //     new User(2l, "smth2", 200)
    // ));

    @GetMapping("/users")
    public List<User> getUsers() {
        return userService.getAll();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        // for (User user : users) {
        //     if (user.getId().equals(id)) {
        //         return ResponseEntity.ok(user);
        //     }
        // }
        return ResponseEntity.ok().body(userService.getById(id));
    }
    
    @PutMapping("/users/{id}")
    public ResponseEntity<User> edit(@PathVariable Long id, @RequestBody User user) {
        User updated = userService.update(id, user);
        if(updated != null){
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        if(userService.deleteById(id)){
            return ResponseEntity.noContent().build();
        }
         return ResponseEntity.ok().build();
    }

    @PostMapping("/users")
    public ResponseEntity<User> addUser(@RequestBody @Valid User user) {
        // user.setId((long)users.size() + 1);
        // users.add(user);
        User newUser = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }
}
