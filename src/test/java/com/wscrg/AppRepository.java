package com.wscrg;

import java.util.HashMap;
import java.util.Map;

@Component
public class AppRepository {

    private Map<Long, User> fakeRepository = new HashMap<>();

    {
        fakeRepository.put(1L, new User("John", "1234"));
        fakeRepository.put(2L, new User("Edgar", "1234"));
        fakeRepository.put(3L, new User("Sam", "1234"));
    }

    public AppRepository() {
    }

    public User findById(Long id) {
        return fakeRepository.get(id);
    }

}
