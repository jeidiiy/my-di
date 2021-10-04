package com.wscrg;

@Component
public class AppService {

    @Injectable
    AppRepository appRepository;

    public AppService() {
    }

}
