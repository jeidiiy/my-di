package com.wscrg;

@Component
public class SetterService {

    private AppRepository appRepository;
    private AppService appService;

    public SetterService() {
    }

    public User findUser(Long id) {
        return appRepository.findById(id);
    }

    public AppRepository getAppRepository() {
        return appRepository;
    }

    @Injectable
    public void setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
    }

    public AppService getAppService() {
        return appService;
    }

    @Injectable
    public void setAppService(AppService appService) {
        this.appService = appService;
    }
}
