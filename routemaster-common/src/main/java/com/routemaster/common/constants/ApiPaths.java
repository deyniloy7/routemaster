package com.routemaster.common.constants;

public class ApiPaths {
    private ApiPaths(){}

    public static final class Auth {
        private Auth(){}

        public static final String BASE = "/api/v1/auth";
        public static final String REGISTER = BASE + "/register";
        public static final String LOGIN = BASE + "/login";
        public static final String REFRESH = BASE + "/refresh";
    }
}
