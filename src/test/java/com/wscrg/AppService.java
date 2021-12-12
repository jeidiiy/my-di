package com.wscrg;

@Component
public class AppService {

    @Injectable
    private AppRepository appRepository;

    public AppService() {
    }

    public User findUser(Long id) {
        return appRepository.findById(id);
    }

    public AppRepository getAppRepository() {
        return appRepository;
    }

    public void setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
    }
}
