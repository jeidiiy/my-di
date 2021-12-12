package com.wscrg;

@Component
public class ConstructorService {

    private AppRepository appRepository;
    private AppService appService;

    public ConstructorService() {
    }

    @Injectable
    public ConstructorService(AppRepository appRepository, AppService appService) {
        this.appRepository = appRepository;
        this.appService = appService;
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

    public AppService getAppService() {
        return appService;
    }

    public void setAppService(AppService appService) {
        this.appService = appService;
    }
}
